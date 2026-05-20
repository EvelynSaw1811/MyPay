package com.mypay.reporting.service.impl;

import com.mypay.common.util.MoneyUtil;
import com.mypay.reporting.client.CollectionClient;
import com.mypay.reporting.client.TransactionClient;
import com.mypay.reporting.client.WalletClient;
import com.mypay.reporting.dto.*;
import com.mypay.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingServiceImpl implements ReportingService {

    private final WalletClient walletClient;
    private final CollectionClient collectionClient;
    private final TransactionClient transactionClient;

    @Override
    public UserDashboardReport getDashboard(String userId) {
        CompletableFuture<Map<String, Object>> walletFuture =
                CompletableFuture.supplyAsync(() -> safeGet(walletClient.getMyWallet(userId)));
        CompletableFuture<List<Map<String, Object>>> collsFuture =
                CompletableFuture.supplyAsync(() -> safeList(collectionClient.getMyCollections(userId)));
        CompletableFuture<List<Map<String, Object>>> txnsFuture =
                CompletableFuture.supplyAsync(() -> safeList(transactionClient.getHistory(userId)));

        CompletableFuture.allOf(walletFuture, collsFuture, txnsFuture).join();

        Map<String, Object> wallet = walletFuture.join();
        List<Map<String, Object>> collections = collsFuture.join();
        List<Map<String, Object>> transactions = txnsFuture.join();

        BigDecimal totalTransacted = transactions.stream()
                .map(t -> toBigDecimal(t.get("amount")))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);
        PersonalPositionReport position = getPersonalPosition(userId);
        Map<String, Object> walletBalances = extractWalletBalances(wallet);

        return UserDashboardReport.builder()
                .walletSummary(wallet != null ? wallet : Collections.emptyMap())
                .walletBalances(walletBalances)
                .collectionCount(collections.size())
                .totalTransacted(totalTransacted)
                .transactionCount(transactions.size())
                .totalReceivable(position.getTotalReceivable())
                .totalPayable(position.getTotalPayable())
                .netPosition(position.getNetPosition())
                .build();
    }

    @Override
    public SpendingSummaryReport getSpendingSummary(String userId) {
        List<Map<String, Object>> transactions = safeList(transactionClient.getHistory(userId));

        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        Map<String, BigDecimal> byType = new HashMap<>();

        for (Map<String, Object> txn : transactions) {
            BigDecimal amount = toBigDecimal(txn.get("amount"));
            if (amount == null) continue;
            String payerId = (String) txn.get("payerId");
            String type = (String) txn.get("type");

            // TOP_UP always means money flowing into the user's own wallet — treat as received.
            if (isTopUp(type) || !userId.equals(payerId)) {
                totalReceived = MoneyUtil.add(totalReceived, amount);
            } else {
                totalSpent = MoneyUtil.add(totalSpent, amount);
            }
            if (type != null) {
                byType.merge(type, amount, MoneyUtil::add);
            }
        }

        return SpendingSummaryReport.builder()
                .totalSpent(totalSpent)
                .totalReceived(totalReceived)
                .netBalance(MoneyUtil.subtract(totalReceived, totalSpent))
                .byType(byType)
                .build();
    }

    @Override
    public CurrencyLedgerReport getCurrencyLedger(String userId, String currency) {
        List<Map<String, Object>> transactions = safeList(transactionClient.getHistoryByCurrency(userId, currency));

        BigDecimal inflow = BigDecimal.ZERO;
        BigDecimal outflow = BigDecimal.ZERO;

        for (Map<String, Object> txn : transactions) {
            BigDecimal amount = toBigDecimal(txn.get("amount"));
            if (amount == null) continue;
            String payerId = (String) txn.get("payerId");
            String type    = (String) txn.get("type");

            // TOP_UP always flows INTO the user's wallet (they are adding money to themselves).
            // Payer-based direction is only correct for SETTLEMENT / TRANSFER / NETTING.
            if (isTopUp(type) || !userId.equals(payerId)) {
                inflow = MoneyUtil.add(inflow, amount);
            } else {
                outflow = MoneyUtil.add(outflow, amount);
            }
        }

        return CurrencyLedgerReport.builder()
                .currency(currency)
                .totalInflow(inflow)
                .totalOutflow(outflow)
                .net(MoneyUtil.subtract(inflow, outflow))
                .build();
    }

    @Override
    public CollectionStatementReport getCollectionStatement(String userId, String collectionId) {
        CompletableFuture<Map<String, Object>> collectionFuture =
                CompletableFuture.supplyAsync(() -> safeGet(collectionClient.getCollection(collectionId, userId)));
        CompletableFuture<List<Map<String, Object>>> expensesFuture =
                CompletableFuture.supplyAsync(() -> safeList(collectionClient.getExpenses(collectionId, userId)));
        CompletableFuture<List<Map<String, Object>>> balancesFuture =
                CompletableFuture.supplyAsync(() -> safeList(collectionClient.getBalances(collectionId, userId)));

        CompletableFuture.allOf(collectionFuture, expensesFuture, balancesFuture).join();

        Map<String, Object> collection = collectionFuture.join();
        List<Map<String, Object>> expenses = expensesFuture.join();
        BigDecimal totalAmount = expenses.stream()
                .map(e -> toBigDecimal(e.get("amount")))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        return CollectionStatementReport.builder()
                .collectionId(collectionId)
                .collectionName(Objects.toString(collection.get("name"), collectionId))
                .category(Objects.toString(collection.get("category"), null))
                .currency(Objects.toString(collection.get("currency"), "MYR"))
                .status(Objects.toString(collection.get("status"), null))
                .totalAmount(totalAmount)
                .expenses(expenses)
                .memberBalances(balancesFuture.join())
                .build();
    }

    @Override
    public PersonalPositionReport getPersonalPosition(String userId) {
        List<Map<String, Object>> collections = safeList(collectionClient.getMyCollections(userId));

        List<String> collectionIds = collections.stream()
                .map(c -> (String) c.get("collectionId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Parallelise per-collection balance calls (eliminates N+1)
        List<CompletableFuture<List<Map<String, Object>>>> futures = collectionIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return safeList(collectionClient.getBalances(id, userId));
                    } catch (Exception e) {
                        log.warn("Could not get balances for collection {}: {}", id, e.getMessage());
                        return Collections.<Map<String, Object>>emptyList();
                    }
                }))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalOwing = BigDecimal.ZERO;
        Map<String, BigDecimal> byCollection = new HashMap<>();
        List<Map<String, Object>> byCollectionRows = new ArrayList<>();
        List<Map<String, Object>> netDebts = new ArrayList<>();

        for (int i = 0; i < collectionIds.size(); i++) {
            String collectionId = collectionIds.get(i);
            Map<String, Object> collection = collections.get(i);
            List<Map<String, Object>> balances = futures.get(i).join();
            String collectionName = Objects.toString(collection.get("name"), collectionId);
            String currency = Objects.toString(collection.get("currency"), "MYR");
            for (Map<String, Object> b : balances) {
                if (!userId.equals(b.get("userId"))) continue;
                BigDecimal net = toBigDecimal(b.get("netBalance"));
                if (net == null) continue;
                byCollection.put(collectionId, net);
                byCollectionRows.add(Map.of(
                        "collectionId", collectionId,
                        "collectionName", collectionName,
                        "balance", net,
                        "currency", currency
                ));
                if (net.compareTo(BigDecimal.ZERO) > 0) {
                    totalOwed = MoneyUtil.add(totalOwed, net);
                } else {
                    totalOwing = MoneyUtil.add(totalOwing, net.abs());
                    addNetDebts(netDebts, userId, collectionId, collectionName, currency, net.abs(), balances);
                }
            }
        }

        return PersonalPositionReport.builder()
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .totalReceivable(totalOwed)
                .totalPayable(totalOwing)
                .netPosition(MoneyUtil.subtract(totalOwed, totalOwing))
                .positionByCollection(byCollection)
                .positionsByCollection(byCollectionRows)
                .byCollection(byCollectionRows)
                .netDebts(netDebts)
                .build();
    }

    private void addNetDebts(List<Map<String, Object>> netDebts, String userId, String collectionId,
                             String collectionName, String currency, BigDecimal amount,
                             List<Map<String, Object>> balances) {
        BigDecimal remaining = amount;
        for (Map<String, Object> balance : balances) {
            if (userId.equals(balance.get("userId"))) continue;
            BigDecimal creditorNet = toBigDecimal(balance.get("netBalance"));
            if (creditorNet == null || creditorNet.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal debtAmount = remaining.min(creditorNet);
            if (debtAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

            String creditorId   = Objects.toString(balance.get("userId"), "");
            // userNickname comes from BalanceSummaryResponse serialised by the collection service
            String creditorName = Objects.toString(balance.get("userNickname"), creditorId);
            if (creditorName == null || creditorName.isBlank()) creditorName = creditorId;
            netDebts.add(Map.of(
                    "counterpartyId", creditorId,
                    "creditorId",     creditorId,
                    "creditorName",   creditorName,
                    "debtorId",       userId,
                    "amount",         debtAmount,
                    "currency",       currency,
                    "collectionId",   collectionId,
                    "collectionName", collectionName,
                    "collectionIds",  List.of(collectionId)
            ));

            remaining = MoneyUtil.subtract(remaining, debtAmount);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractWalletBalances(Map<String, Object> wallet) {
        if (wallet == null) return Collections.emptyMap();
        Object balances = wallet.get("balances");
        if (balances instanceof Map<?, ?> map) {
            return map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
        }
        Object accounts = wallet.get("wallets");
        if (accounts == null) {
            accounts = wallet.get("accounts");
        }
        if (!(accounts instanceof List<?> accountList)) return Collections.emptyMap();
        Map<String, Object> result = new HashMap<>();
        for (Object item : accountList) {
            if (item instanceof Map<?, ?> account) {
                Object currency = account.get("currency");
                Object balance = account.get("balance");
                if (currency != null) result.put(currency.toString(), balance != null ? balance : BigDecimal.ZERO);
            }
        }
        return result;
    }

    private <T> T safeGet(com.mypay.common.dto.ApiResponse<T> response) {
        return response != null ? response.getData() : null;
    }

    private List<Map<String, Object>> safeList(com.mypay.common.dto.ApiResponse<List<Map<String, Object>>> response) {
        if (response == null || response.getData() == null) return Collections.emptyList();
        return response.getData();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns true when the transaction type is a wallet top-up.
     * TOP_UP means the user added funds to their own wallet, which is always an inflow
     * regardless of who is recorded as the payer.
     */
    private boolean isTopUp(String type) {
        return "TOP_UP".equalsIgnoreCase(type) || "TOPUP".equalsIgnoreCase(type);
    }
}

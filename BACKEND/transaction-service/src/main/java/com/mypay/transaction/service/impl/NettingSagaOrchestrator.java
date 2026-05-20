package com.mypay.transaction.service.impl;

import com.mypay.common.constant.TransactionStatus;
import com.mypay.common.constant.TransactionType;
import com.mypay.common.event.NotificationEvent;
import com.mypay.common.event.RabbitMQConstants;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.util.MoneyUtil;
import com.mypay.transaction.client.CollectionClient;
import com.mypay.transaction.client.WalletClient;
import com.mypay.transaction.dto.NettingRequest;
import com.mypay.transaction.dto.NettingResponse;
import com.mypay.transaction.entity.SagaState;
import com.mypay.transaction.entity.Settlement;
import com.mypay.transaction.entity.Transaction;
import com.mypay.transaction.repository.SagaStateRepository;
import com.mypay.transaction.repository.SettlementRepository;
import com.mypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NettingSagaOrchestrator {

    private final TransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final SagaStateRepository sagaStateRepository;
    private final CollectionClient collectionClient;
    private final WalletClient walletClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public NettingResponse execute(NettingRequest request, String userId) {
        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<Transaction> existing = transactionRepository.findByTransactionIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                Transaction txn = existing.get();
                return buildResponse(txn);
            }
        }

        String counterpartyId = request.getCounterpartyId();
        String currency = request.getCurrency() != null ? request.getCurrency() : "MYR";

        // Step 1: Fetch all expenses and find relevant unsettled shares between user and counterparty
        List<Map<String, Object>> sharesToSettle = new ArrayList<>();
        BigDecimal userOwes = BigDecimal.ZERO;
        BigDecimal counterpartyOwes = BigDecimal.ZERO;

        for (String collectionId : request.getCollectionIds()) {
            try {
                var expensesResp = collectionClient.getExpenses(collectionId, userId);
                if (expensesResp == null || expensesResp.getData() == null) continue;

                for (Map<String, Object> expense : expensesResp.getData()) {
                    String expenseId = (String) expense.get("expenseId");
                    String paidBy = (String) expense.get("paidBy");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> shares = (List<Map<String, Object>>) expense.get("shares");
                    if (shares == null) continue;

                    for (Map<String, Object> share : shares) {
                        Boolean settled = (Boolean) share.get("settled");
                        if (Boolean.TRUE.equals(settled)) continue;

                        String shareUserId = (String) share.get("userId");
                        BigDecimal shareAmount = new BigDecimal(share.get("totalAmount").toString());

                        // User owes counterparty: user has a share in an expense paid by counterparty
                        if (userId.equals(shareUserId) && counterpartyId.equals(paidBy)) {
                            userOwes = MoneyUtil.add(userOwes, shareAmount);
                            Map<String, Object> shareInfo = new HashMap<>(share);
                            shareInfo.put("expenseId", expenseId);
                            shareInfo.put("collectionId", collectionId);
                            shareInfo.put("direction", "USER_OWES");
                            sharesToSettle.add(shareInfo);
                        }
                        // Counterparty owes user: counterparty has a share in an expense paid by user
                        else if (counterpartyId.equals(shareUserId) && userId.equals(paidBy)) {
                            counterpartyOwes = MoneyUtil.add(counterpartyOwes, shareAmount);
                            Map<String, Object> shareInfo = new HashMap<>(share);
                            shareInfo.put("expenseId", expenseId);
                            shareInfo.put("collectionId", collectionId);
                            shareInfo.put("direction", "COUNTERPARTY_OWES");
                            sharesToSettle.add(shareInfo);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch expenses for collection {}: {}", collectionId, e.getMessage());
            }
        }

        // Step 2: Calculate net balance
        BigDecimal netAmount = userOwes.subtract(counterpartyOwes).abs();
        if (netAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("No outstanding balance to net between the users");
        }

        boolean userIsNetPayer = userOwes.compareTo(counterpartyOwes) > 0;
        String netPayerId = userIsNetPayer ? userId : counterpartyId;
        String netPayeeId = userIsNetPayer ? counterpartyId : userId;

        Transaction txn = Transaction.builder()
                .transactionPayerId(netPayerId)
                .transactionPayeeId(netPayeeId)
                .transactionAmount(netAmount)
                .transactionCurrency(currency)
                .transactionType(TransactionType.NETTING)
                .transactionStatus(TransactionStatus.PENDING)
                .transactionIdempotencyKey(request.getIdempotencyKey())
                .build();
        txn = transactionRepository.save(txn);

        SagaState saga = SagaState.builder()
                .sagaStateTransactionId(txn.getTransactionId())
                .sagaStateStep(2)
                .sagaStateStatus("IN_PROGRESS")
                .build();
        sagaStateRepository.save(saga);

        boolean debitDone = false;
        boolean creditDone = false;

        try {
            // Step 3: Debit net payer
            walletClient.debit(netPayerId, Map.of("amount", netAmount, "currency", currency));
            debitDone = true;
            saga.setSagaStateStep(3);
            sagaStateRepository.save(saga);

            // Step 4: Credit net payee
            walletClient.credit(netPayeeId, Map.of("amount", netAmount, "currency", currency));
            creditDone = true;
            saga.setSagaStateStep(4);
            sagaStateRepository.save(saga);

            // Step 5: Mark all relevant shares as settled
            Settlement settlement = Settlement.builder()
                    .settlementTransactionId(txn.getTransactionId())
                    .settlementPayerId(netPayerId)
                    .settlementPayeeId(netPayeeId)
                    .settlementAmount(netAmount)
                    .build();
            settlementRepository.save(settlement);

            for (Map<String, Object> share : sharesToSettle) {
                try {
                    String shareId = (String) share.get("shareId");
                    String expenseId = (String) share.get("expenseId");
                    String collectionId = (String) share.get("collectionId");
                    collectionClient.settleShare(collectionId, expenseId, shareId, userId);
                } catch (Exception e) {
                    log.warn("Failed to settle share during netting: {}", e.getMessage());
                }
            }

            saga.setSagaStateStep(5);
            saga.setSagaStateStatus("COMPLETED");
            sagaStateRepository.save(saga);

            txn.setTransactionStatus(TransactionStatus.COMPLETED);
            txn = transactionRepository.save(txn);

            publishNettingNotifications(netPayerId, netPayeeId, txn.getTransactionId(), netAmount);

            return buildResponse(txn);

        } catch (Exception e) {
            log.error("Netting saga failed at step {}: {}", saga.getSagaStateStep(), e.getMessage());
            compensate(saga, txn, netPayerId, netPayeeId, netAmount, currency, debitDone, creditDone);
            throw new BadRequestException("Netting failed: " + e.getMessage());
        }
    }

    private void compensate(SagaState saga, Transaction txn, String netPayerId, String netPayeeId,
                            BigDecimal netAmount, String currency, boolean debitDone, boolean creditDone) {
        saga.setSagaStateStatus("COMPENSATING");
        try {
            if (creditDone) {
                walletClient.debit(netPayeeId, Map.of("amount", netAmount, "currency", currency));
                saga.setSagaStateCompensationStep(4);
            }
            if (debitDone) {
                walletClient.credit(netPayerId, Map.of("amount", netAmount, "currency", currency));
                saga.setSagaStateCompensationStep(3);
            }
        } catch (Exception ex) {
            log.error("Netting compensation failed: {}", ex.getMessage());
        }
        txn.setTransactionStatus(TransactionStatus.FAILED);
        transactionRepository.save(txn);
        saga.setSagaStateStatus("FAILED");
        sagaStateRepository.save(saga);
    }

    private void publishNettingNotifications(String payerId, String payeeId, String transactionId, BigDecimal amount) {
        try {
            NotificationEvent payerEvent = NotificationEvent.builder()
                    .userId(payerId)
                    .type("NETTING_SENT")
                    .title("Net Payment Sent")
                    .message("You paid net amount " + amount + " in netting settlement")
                    .referenceId(transactionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_SETTLEMENT_CONFIRMED, payerEvent);

            NotificationEvent payeeEvent = NotificationEvent.builder()
                    .userId(payeeId)
                    .type("NETTING_RECEIVED")
                    .title("Net Payment Received")
                    .message("You received net amount " + amount + " in netting settlement")
                    .referenceId(transactionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_SETTLEMENT_RECEIVED, payeeEvent);
        } catch (Exception e) {
            log.warn("Failed to publish netting notifications: {}", e.getMessage());
        }
    }

    private NettingResponse buildResponse(Transaction txn) {
        return NettingResponse.builder()
                .transactionId(txn.getTransactionId())
                .netPayerId(txn.getTransactionPayerId())
                .netPayeeId(txn.getTransactionPayeeId())
                .netAmount(txn.getTransactionAmount())
                .currency(txn.getTransactionCurrency())
                .status(txn.getTransactionStatus())
                .createdAt(txn.getTransactionCreated())
                .build();
    }
}

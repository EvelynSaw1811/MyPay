package com.mypay.transaction.service.impl;

import com.mypay.common.constant.TransactionStatus;
import com.mypay.common.constant.TransactionType;
import com.mypay.common.event.NotificationEvent;
import com.mypay.common.event.RabbitMQConstants;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ConflictException;
import com.mypay.transaction.client.CollectionClient;
import com.mypay.transaction.client.CurrencyClient;
import com.mypay.transaction.client.WalletClient;
import com.mypay.transaction.dto.SettleRequest;
import com.mypay.transaction.dto.SettlementResponse;
import com.mypay.transaction.dto.ShareResponse;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementSagaOrchestrator {

    private final TransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final SagaStateRepository sagaStateRepository;
    private final CollectionClient collectionClient;
    private final CurrencyClient currencyClient;
    private final WalletClient walletClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public SettlementResponse execute(SettleRequest request, String payerId) {
        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<Transaction> existing = transactionRepository.findByTransactionIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                Transaction txn = existing.get();
                Settlement stl = settlementRepository.findBySettlementTransactionId(txn.getTransactionId()).stream().findFirst().orElseThrow();
                return buildResponse(txn, stl);
            }
        }

        // Step 1: Validate share and resolve creditor
        ShareResponse share = validateShare(request, payerId);
        String payeeId = resolvePayeeId(request, payerId);
        BigDecimal amount = share.getTotalAmount();
        String currency = "MYR";

        // Step 2: CurrencyCode conversion
        BigDecimal convertedAmount = null;
        String payeeCurrency = request.getPayeeCurrency();
        if (payeeCurrency != null && !payeeCurrency.equalsIgnoreCase(currency)) {
            convertedAmount = convertCurrency(currency, payeeCurrency, amount);
        }

        // Create transaction record
        Transaction txn = Transaction.builder()
                .transactionPayerId(payerId)
                .transactionPayeeId(payeeId)
                .transactionAmount(amount)
                .transactionCurrency(currency)
                .transactionConvertedAmount(convertedAmount)
                .transactionPayeeCurrency(payeeCurrency)
                .transactionType(TransactionType.SETTLEMENT)
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
            // Step 3: Debit payer
            BigDecimal debitAmount = amount;
            walletClient.debit(payerId, Map.of("amount", debitAmount, "currency", currency));
            debitDone = true;
            saga.setSagaStateStep(3);
            sagaStateRepository.save(saga);

            // Step 4: Credit payee
            BigDecimal creditAmount = convertedAmount != null ? convertedAmount : amount;
            String creditCurrency = payeeCurrency != null ? payeeCurrency : currency;
            walletClient.credit(payeeId, Map.of("amount", creditAmount, "currency", creditCurrency));
            creditDone = true;
            saga.setSagaStateStep(4);
            sagaStateRepository.save(saga);

            // Step 5: Save settlement record
            Settlement settlement = Settlement.builder()
                    .settlementTransactionId(txn.getTransactionId())
                    .settlementExpenseShareId(request.getShareId())
                    .settlementCollectionId(request.getCollectionId())
                    .settlementPayerId(payerId)
                    .settlementPayeeId(payeeId)
                    .settlementAmount(amount)
                    .build();
            settlement = settlementRepository.save(settlement);
            saga.setSagaStateStep(5);
            sagaStateRepository.save(saga);

            // Step 6: Mark share settled
            collectionClient.settleShare(request.getCollectionId(), request.getExpenseId(), request.getShareId(), payerId);
            saga.setSagaStateStep(6);
            sagaStateRepository.save(saga);

            // Step 7: Publish notifications
            publishSettlementNotifications(payerId, payeeId, txn.getTransactionId(), amount);
            saga.setSagaStateStep(7);
            saga.setSagaStateStatus("COMPLETED");
            sagaStateRepository.save(saga);

            txn.setTransactionStatus(TransactionStatus.COMPLETED);
            txn = transactionRepository.save(txn);

            return buildResponse(txn, settlement);

        } catch (Exception e) {
            log.error("Settlement saga failed at step {}: {}", saga.getSagaStateStep(), e.getMessage());
            compensate(saga, txn, payerId, payeeId, request, amount, currency,
                    debitDone, creditDone, convertedAmount, payeeCurrency);
            throw new BadRequestException("Settlement failed: " + e.getMessage());
        }
    }

    private void compensate(SagaState saga, Transaction txn, String payerId, String payeeId,
                            SettleRequest request, BigDecimal amount, String currency,
                            boolean debitDone, boolean creditDone,
                            BigDecimal convertedAmount, String payeeCurrency) {
        int failedStep = saga.getSagaStateStep();
        saga.setSagaStateStatus("COMPENSATING");

        try {
            if (failedStep >= 6) {
                collectionClient.unsettleShare(request.getCollectionId(), request.getExpenseId(), request.getShareId(), payerId);
            }
            if (creditDone) {
                BigDecimal creditAmount = convertedAmount != null ? convertedAmount : amount;
                String creditCurrency = payeeCurrency != null ? payeeCurrency : currency;
                walletClient.debit(payeeId, Map.of("amount", creditAmount, "currency", creditCurrency));
                saga.setSagaStateCompensationStep(4);
            }
            if (debitDone) {
                walletClient.credit(payerId, Map.of("amount", amount, "currency", currency));
                saga.setSagaStateCompensationStep(3);
            }
        } catch (Exception ex) {
            log.error("Compensation step failed: {}", ex.getMessage());
        }

        txn.setTransactionStatus(TransactionStatus.FAILED);
        transactionRepository.save(txn);
        saga.setSagaStateStatus("FAILED");
        sagaStateRepository.save(saga);
    }

    private String resolvePayeeId(SettleRequest request, String payerId) {
        var response = collectionClient.getExpense(request.getCollectionId(), request.getExpenseId(), payerId);
        if (response == null || response.getData() == null) {
            throw new BadRequestException("Expense not found");
        }
        String paidBy = (String) response.getData().get("paidBy");
        if (paidBy == null) {
            throw new BadRequestException("Expense paidBy field is missing");
        }
        return paidBy;
    }

    private ShareResponse validateShare(SettleRequest request, String payerId) {
        var response = collectionClient.getShare(
                request.getCollectionId(), request.getExpenseId(), request.getShareId(), payerId);
        if (response == null || response.getData() == null) {
            throw new BadRequestException("Share not found");
        }
        ShareResponse share = response.getData();
        if (share.isSettled()) {
            throw new ConflictException("Share is already settled");
        }
        return share;
    }

    private BigDecimal convertCurrency(String from, String to, BigDecimal amount) {
        try {
            var response = currencyClient.convert(from, to, amount.toPlainString());
            if (response != null && response.getData() != null) {
                Object converted = response.getData().get("convertedAmount");
                if (converted != null) {
                    return new BigDecimal(converted.toString());
                }
            }
        } catch (Exception e) {
            log.warn("CurrencyCode conversion failed, using original amount: {}", e.getMessage());
        }
        return null;
    }

    private void publishSettlementNotifications(String payerId, String payeeId, String transactionId, BigDecimal amount) {
        try {
            NotificationEvent payerEvent = NotificationEvent.builder()
                    .userId(payerId)
                    .type("SETTLEMENT_SENT")
                    .title("Payment Sent")
                    .message("You paid " + amount + " in settlement")
                    .referenceId(transactionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_SETTLEMENT_CONFIRMED, payerEvent);

            NotificationEvent payeeEvent = NotificationEvent.builder()
                    .userId(payeeId)
                    .type("SETTLEMENT_RECEIVED")
                    .title("Payment Received")
                    .message("You received " + amount + " settlement")
                    .referenceId(transactionId)
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, RabbitMQConstants.ROUTING_SETTLEMENT_RECEIVED, payeeEvent);
        } catch (Exception e) {
            log.warn("Failed to publish settlement notifications: {}", e.getMessage());
        }
    }

    private SettlementResponse buildResponse(Transaction txn, Settlement stl) {
        return SettlementResponse.builder()
                .transactionId(txn.getTransactionId())
                .settlementId(stl.getSettlementId())
                .payerId(txn.getTransactionPayerId())
                .payeeId(txn.getTransactionPayeeId())
                .amount(txn.getTransactionAmount())
                .currency(txn.getTransactionCurrency())
                .convertedAmount(txn.getTransactionConvertedAmount())
                .payeeCurrency(txn.getTransactionPayeeCurrency())
                .status(txn.getTransactionStatus())
                .createdAt(txn.getTransactionCreated())
                .build();
    }
}

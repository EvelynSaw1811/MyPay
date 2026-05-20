package com.mypay.transaction.seed;

import com.mypay.common.constant.SeedDataIds;
import com.mypay.common.constant.SeedUsers;
import com.mypay.common.constant.TransactionStatus;
import com.mypay.common.constant.TransactionType;
import com.mypay.transaction.entity.SagaState;
import com.mypay.transaction.entity.Settlement;
import com.mypay.transaction.entity.Transaction;
import com.mypay.transaction.repository.SagaStateRepository;
import com.mypay.transaction.repository.SettlementRepository;
import com.mypay.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final TransactionRepository txnRepo;
    private final SettlementRepository stlRepo;
    private final SagaStateRepository sagaRepo;
    private final JdbcTemplate jdbc;

    private static final String U1 = SeedUsers.U1;
    private static final String U2 = SeedUsers.U2;
    private static final String U3 = SeedUsers.U3;
    private static final String U4 = SeedUsers.U4;
    private static final String U5 = SeedUsers.U5;
    private static final String U6 = SeedUsers.U6;
    private static final String U9 = SeedUsers.U9;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (txnRepo.existsById(SeedDataIds.TXN_TOPUP_ALICE_MYR)
                || txnRepo.existsByTransactionIdempotencyKey(seedTopUpKey(U1, "MYR"))) {
            log.info("[Seed] Deterministic transaction scenario already exists - skipping transaction seed.");
            return;
        }

        topUp(SeedDataIds.TXN_TOPUP_ALICE_MYR, U1, "MYR", bd(2000));
        topUp(SeedDataIds.TXN_TOPUP_BOB_SGD, U2, "SGD", bd(1500));
        topUp(SeedDataIds.TXN_TOPUP_CAROL_MYR, U3, "MYR", bd(1000));
        topUp(SeedDataIds.TXN_TOPUP_FRANK_SGD, U6, "SGD", bd(500));
        topUp(SeedDataIds.TXN_TOPUP_IVY_MYR, U9, "MYR", bd(300));

        settle(SeedDataIds.TXN_SETTLE_CAROL_ALICE, U3, U1, "MYR", bd(300), "MYR", bd(300),
                SeedDataIds.C1_BALI_TRIP, SeedDataIds.S_E1_CAROL, TransactionStatus.COMPLETED, "COMPLETED", 7, null);
        settle(SeedDataIds.TXN_SETTLE_CAROL_BOB, U3, U2, "MYR", bd(720), "MYR", bd(720),
                SeedDataIds.C1_BALI_TRIP, SeedDataIds.S_E2_CAROL, TransactionStatus.COMPLETED, "COMPLETED", 7, null);
        settle(SeedDataIds.TXN_SETTLE_EMMA_BOB, U5, U2, "MYR", bd(100), "MYR", bd(100),
                SeedDataIds.C2_OFFICE_LUNCH, SeedDataIds.S_E3_EMMA, TransactionStatus.COMPLETED, "COMPLETED", 7, null);

        settle(SeedDataIds.TXN_SETTLE_DAVID_ALICE_PENDING, U4, U1, "MYR", bd(300), "MYR", bd(300),
                SeedDataIds.C1_BALI_TRIP, SeedDataIds.S_E1_DAVID, TransactionStatus.PENDING, "PENDING", 2, null);

        settle(SeedDataIds.TXN_SETTLE_DAVID_FRANK_CROSS, U4, U6, "MYR", bd(170), "SGD", bd(50),
                SeedDataIds.C3_SG_WEEKEND, SeedDataIds.S_E4_DAVID, TransactionStatus.COMPLETED, "COMPLETED", 7, null);

        netting(SeedDataIds.TXN_NETTING_BOB_ALICE, U2, U1, "MYR", bd(150), SeedDataIds.C1_BALI_TRIP);

        failedSettlement(SeedDataIds.TXN_FAILED_FRANK_DAVID, U6, U4, "USD", bd(40),
                SeedDataIds.C5_HOLIDAY_DINNER, SeedDataIds.S_E6_FRANK);

        reversedTransfer(SeedDataIds.TXN_REVERSED_ALICE_BOB, U1, U2, "MYR", bd(150));

        log.info("[Seed] Created 5 top-ups, 6 settlement/netting transactions, 1 failed transaction, 1 reversed transfer, and saga states.");
    }

    private void topUp(String txnId, String userId, String currency, BigDecimal amount) {
        insertTransaction(txnId, userId, userId, currency, amount, amount, currency,
                TransactionType.TOP_UP, TransactionStatus.COMPLETED,
                seedTopUpKey(userId, currency));
    }

    private void settle(String txnId, String payerId, String payeeId,
                        String payerCurr, BigDecimal amount,
                        String payeeCurr, BigDecimal convertedAmt,
                        String collectionId, String shareId,
                        TransactionStatus txnStatus, String sagaStatus,
                        int sagaStep, Integer compensationStep) {
        insertTransaction(txnId, payerId, payeeId, payerCurr, amount, convertedAmt, payeeCurr,
                TransactionType.SETTLEMENT, txnStatus,
                "seed-stl-" + payerId + "-" + payeeId + "-" + collectionId + "-" + shareId);

        stlRepo.save(Settlement.builder()
                .settlementTransactionId(txnId)
                .settlementExpenseShareId(shareId)
                .settlementCollectionId(collectionId)
                .settlementPayerId(payerId)
                .settlementPayeeId(payeeId)
                .settlementAmount(amount)
                .build());

        saveSaga(txnId, sagaStep, sagaStatus, compensationStep);
    }

    private void netting(String txnId, String payerId, String payeeId, String currency,
                         BigDecimal amount, String collectionId) {
        insertTransaction(txnId, payerId, payeeId, currency, amount, amount, currency,
                TransactionType.NETTING, TransactionStatus.COMPLETED,
                "seed-netting-" + payerId + "-" + payeeId + "-" + collectionId);

        stlRepo.save(Settlement.builder()
                .settlementTransactionId(txnId)
                .settlementCollectionId(collectionId)
                .settlementPayerId(payerId)
                .settlementPayeeId(payeeId)
                .settlementAmount(amount)
                .build());

        saveSaga(txnId, 5, "COMPLETED", null);
    }

    private void failedSettlement(String txnId, String payerId, String payeeId, String currency,
                                  BigDecimal amount, String collectionId, String shareId) {
        insertTransaction(txnId, payerId, payeeId, currency, amount, amount, currency,
                TransactionType.SETTLEMENT, TransactionStatus.FAILED,
                "seed-failed-" + payerId + "-" + payeeId + "-" + collectionId);

        saveSaga(txnId, 4, "FAILED", 3);
    }

    private void reversedTransfer(String txnId, String payerId, String payeeId, String currency,
                                  BigDecimal amount) {
        insertTransaction(txnId, payerId, payeeId, currency, amount, amount, currency,
                TransactionType.TRANSFER, TransactionStatus.REVERSED,
                "seed-reversed-" + payerId + "-" + payeeId);

        saveSaga(txnId, 7, "REVERSED", 5);
    }

    private void saveSaga(String transactionId, int step, String status, Integer compensationStep) {
        sagaRepo.save(SagaState.builder()
                .sagaStateTransactionId(transactionId)
                .sagaStateStep(step)
                .sagaStateStatus(status)
                .sagaStateCompensationStep(compensationStep)
                .build());
    }

    private void insertTransaction(String txnId, String payerId, String payeeId,
                                   String currency, BigDecimal amount,
                                   BigDecimal convertedAmount, String payeeCurrency,
                                   TransactionType type, TransactionStatus status,
                                   String idempotencyKey) {
        if (txnRepo.existsById(txnId) || txnRepo.existsByTransactionIdempotencyKey(idempotencyKey)) {
            return;
        }
        jdbc.update("""
                INSERT INTO transaction_t
                  (txn_id, txn_payer_id, txn_payee_id, txn_amount, txn_currency,
                   txn_converted_amt, txn_payee_curr, txn_type, txn_status,
                   txn_idem_key, txn_created, txn_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """,
                txnId, payerId, payeeId, amount, currency, convertedAmount, payeeCurrency,
                type.name(), status.name(), idempotencyKey);
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }

    private static String seedTopUpKey(String userId, String currency) {
        return "seed-topup-" + userId + "-" + currency;
    }
}

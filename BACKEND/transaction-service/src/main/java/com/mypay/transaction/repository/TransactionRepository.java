package com.mypay.transaction.repository;

import com.mypay.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByTransactionIdempotencyKey(String idemKey);
    boolean existsByTransactionIdempotencyKey(String idemKey);
    List<Transaction> findByTransactionPayerIdOrTransactionPayeeIdOrderByTransactionCreatedDesc(String payerId, String payeeId);

    @Query("""
            SELECT t
              FROM Transaction t
             WHERE (t.transactionPayerId = :userId OR t.transactionPayeeId = :userId)
               AND t.transactionCurrency = :currency
             ORDER BY t.transactionCreated DESC
            """)
    List<Transaction> findHistoryByUserIdAndCurrency(
            @Param("userId") String userId,
            @Param("currency") String currency);
}

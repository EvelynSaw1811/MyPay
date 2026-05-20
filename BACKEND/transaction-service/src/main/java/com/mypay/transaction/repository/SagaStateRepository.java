package com.mypay.transaction.repository;

import com.mypay.transaction.entity.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaStateRepository extends JpaRepository<SagaState, String> {
    Optional<SagaState> findBySagaStateTransactionId(String transactionId);
}

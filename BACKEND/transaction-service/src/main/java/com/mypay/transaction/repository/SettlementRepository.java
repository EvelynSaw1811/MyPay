package com.mypay.transaction.repository;

import com.mypay.transaction.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, String> {
    List<Settlement> findBySettlementTransactionId(String transactionId);
}

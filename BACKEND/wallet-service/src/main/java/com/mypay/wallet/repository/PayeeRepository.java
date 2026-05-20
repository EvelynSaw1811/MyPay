package com.mypay.wallet.repository;

import com.mypay.wallet.entity.Payee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayeeRepository extends JpaRepository<Payee, String> {
    List<Payee> findByPayeeAccountId(String accountId);
    boolean existsByPayeeAccountIdAndPayeeUserId(String accountId, String userId);
    Optional<Payee> findByPayeeIdAndPayeeAccountId(String payeeId, String accountId);
}

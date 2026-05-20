package com.mypay.wallet.repository;

import com.mypay.wallet.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountUserId(String accountUserId);
    boolean existsByAccountUserId(String accountUserId);
}

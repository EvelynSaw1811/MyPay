package com.mypay.wallet.repository;

import com.mypay.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByAccount_AccountId(String accountId);
    long countByAccount_AccountId(String accountId);
    Optional<Wallet> findByAccount_AccountUserIdAndWalletCurrency(String userId, String currency);
    boolean existsByAccount_AccountIdAndWalletCurrency(String accountId, String currency);
}

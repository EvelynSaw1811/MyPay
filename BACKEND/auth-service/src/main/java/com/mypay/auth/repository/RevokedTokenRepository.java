package com.mypay.auth.repository;

import com.mypay.auth.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    boolean existsByRevokedTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.revokedTokenExpireDateTime < :now")
    void deleteExpiredTokens(LocalDateTime now);
}

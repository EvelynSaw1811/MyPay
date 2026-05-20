package com.mypay.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "WALLET_T",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wllt_acct_id", "wllt_currency"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wllt_id", columnDefinition = "CHAR(36)", updatable = false)
    private String walletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wllt_acct_id", nullable = false)
    private Account account;

    @Column(name = "wllt_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String walletUserId;

    @Column(name = "wllt_currency", columnDefinition = "CHAR(3)", nullable = false)
    private String walletCurrency;

    @Column(name = "wllt_balance", precision = 19, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(name = "wllt_status", length = 20, nullable = false)
    @Builder.Default
    private String walletStatus = "ACTIVE";

    @Column(name = "wllt_created", updatable = false)
    private LocalDateTime walletCreated;

    @Column(name = "wllt_updated")
    private LocalDateTime walletUpdated;

    @PrePersist
    protected void onCreate() {
        walletCreated = LocalDateTime.now();
        walletUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        walletUpdated = LocalDateTime.now();
    }
}

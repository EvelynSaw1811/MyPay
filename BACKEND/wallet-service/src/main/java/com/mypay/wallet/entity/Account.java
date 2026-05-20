package com.mypay.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ACCOUNT_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "acct_id", columnDefinition = "CHAR(36)", updatable = false)
    private String accountId;

    @Column(name = "acct_user_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String accountUserId;

    @Column(name = "acct_created", updatable = false)
    private LocalDateTime accountCreated;

    @Column(name = "acct_updated")
    private LocalDateTime accountUpdated;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Wallet> wallets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        accountCreated = LocalDateTime.now();
        accountUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        accountUpdated = LocalDateTime.now();
    }
}

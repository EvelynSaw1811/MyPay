package com.mypay.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYEE_T",
        uniqueConstraints = @UniqueConstraint(columnNames = {"paye_acct_id", "paye_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "paye_id", columnDefinition = "CHAR(36)", updatable = false)
    private String payeeId;

    @Column(name = "paye_acct_id", columnDefinition = "CHAR(36)", nullable = false)
    private String payeeAccountId;

    @Column(name = "paye_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String payeeUserId;

    @Column(name = "paye_nickname", length = 100)
    private String payeeNickname;

    @Column(name = "paye_created", updatable = false)
    private LocalDateTime payeeCreated;

    @PrePersist
    protected void onCreate() {
        payeeCreated = LocalDateTime.now();
    }
}

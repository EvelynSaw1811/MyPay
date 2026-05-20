package com.mypay.transaction.entity;

import com.mypay.common.constant.TransactionStatus;
import com.mypay.common.constant.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTION_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "txn_id", columnDefinition = "CHAR(36)", updatable = false)
    private String transactionId;

    @Column(name = "txn_payer_id", columnDefinition = "CHAR(36)", nullable = false)
    private String transactionPayerId;

    @Column(name = "txn_payee_id", columnDefinition = "CHAR(36)", nullable = false)
    private String transactionPayeeId;

    @Column(name = "txn_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal transactionAmount;

    @Column(name = "txn_currency", columnDefinition = "CHAR(3)")
    private String transactionCurrency;

    @Column(name = "txn_converted_amt", precision = 19, scale = 4)
    private BigDecimal transactionConvertedAmount;

    @Column(name = "txn_payee_curr", columnDefinition = "CHAR(3)")
    private String transactionPayeeCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_status", length = 20, nullable = false)
    @Builder.Default
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @Column(name = "txn_idem_key", length = 255, unique = true)
    private String transactionIdempotencyKey;

    @Column(name = "txn_created", updatable = false)
    private LocalDateTime transactionCreated;

    @Column(name = "txn_updated")
    private LocalDateTime transactionUpdated;

    @PrePersist
    protected void onCreate() {
        transactionCreated = LocalDateTime.now();
        transactionUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        transactionUpdated = LocalDateTime.now();
    }
}

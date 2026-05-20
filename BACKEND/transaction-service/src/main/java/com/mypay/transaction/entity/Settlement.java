package com.mypay.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stl_id", columnDefinition = "CHAR(36)", updatable = false)
    private String settlementId;

    @Column(name = "stl_txn_id", columnDefinition = "CHAR(36)", nullable = false)
    private String settlementTransactionId;

    @Column(name = "stl_share_id", columnDefinition = "CHAR(36)")
    private String settlementExpenseShareId;

    @Column(name = "stl_coll_id", columnDefinition = "CHAR(36)")
    private String settlementCollectionId;

    @Column(name = "stl_payer_id", columnDefinition = "CHAR(36)", nullable = false)
    private String settlementPayerId;

    @Column(name = "stl_payee_id", columnDefinition = "CHAR(36)", nullable = false)
    private String settlementPayeeId;

    @Column(name = "stl_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal settlementAmount;

    @Column(name = "stl_created", updatable = false)
    private LocalDateTime settlementCreated;

    @PrePersist
    protected void onCreate() {
        settlementCreated = LocalDateTime.now();
    }
}

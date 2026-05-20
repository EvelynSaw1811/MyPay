package com.mypay.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EXPENSE_SHARE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseShare {

    @Id
    @Column(name = "es_id", columnDefinition = "CHAR(36)", updatable = false)
    private String expenseShareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "es_exp_id", nullable = false)
    private Expense expense;

    @Column(name = "es_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String expenseShareUserId;

    @Column(name = "es_base_amt", precision = 19, scale = 4)
    private BigDecimal expenseShareBaseAmount;

    @Column(name = "es_tax_amt", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal expenseShareTaxAmount = BigDecimal.ZERO;

    @Column(name = "es_total_amt", precision = 19, scale = 4, nullable = false)
    private BigDecimal expenseShareTotalAmount;

    @Column(name = "es_settled")
    @Builder.Default
    private Boolean expenseShareSettled = false;

    @Column(name = "es_settled_at")
    private LocalDateTime expenseShareSettledDateTime;

    @PrePersist
    protected void onCreate() {
        if (expenseShareId == null) {
            expenseShareId = UUID.randomUUID().toString();
        }
    }
}

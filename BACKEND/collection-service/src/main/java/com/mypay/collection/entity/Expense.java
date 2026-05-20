package com.mypay.collection.entity;

import com.mypay.common.constant.SplitType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EXPENSE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @Column(name = "exp_id", columnDefinition = "CHAR(36)", updatable = false)
    private String expenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exp_coll_id", nullable = false)
    private Collection collection;

    @Column(name = "exp_title", length = 30, nullable = false)
    private String expenseTitle;

    @Column(name = "exp_desc", length = 100)
    private String expenseDescription;

    @Column(name = "exp_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal expenseAmount;

    @Column(name = "exp_currency", columnDefinition = "CHAR(3)", nullable = false)
    private String expenseCurrency;

    @Column(name = "exp_paid_by", columnDefinition = "CHAR(36)", nullable = false)
    private String expensePaidBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "exp_split_type", length = 20, nullable = false)
    private SplitType expenseSplitType;

    @Column(name = "exp_tax_rate", precision = 5, scale = 4)
    private BigDecimal expenseTaxRate;

    @Column(name = "exp_tax_type", length = 20)
    private String expenseTaxType;

    @Column(name = "exp_created", updatable = false)
    private LocalDateTime expenseCreated;

    @Column(name = "exp_updated")
    private LocalDateTime expenseUpdated;

    @PrePersist
    protected void onCreate() {
        if (expenseId == null) {
            expenseId = UUID.randomUUID().toString();
        }
        expenseCreated = LocalDateTime.now();
        expenseUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        expenseUpdated = LocalDateTime.now();
    }
}

package com.mypay.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "SPLIT_RULE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitRule {

    @Id
    @Column(name = "sr_id", columnDefinition = "CHAR(36)", updatable = false)
    private String splitRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sr_exp_id", nullable = false)
    private Expense expense;

    @Column(name = "sr_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String splitRuleUserId;

    @Column(name = "sr_percentage", precision = 8, scale = 4)
    private BigDecimal splitRulePercentage;

    @Column(name = "sr_fixed_amt", precision = 19, scale = 4)
    private BigDecimal splitRuleFixedAmount;

    @Column(name = "sr_weight")
    private Integer splitRuleWeight;

    @PrePersist
    protected void onCreate() {
        if (splitRuleId == null) {
            splitRuleId = UUID.randomUUID().toString();
        }
    }
}

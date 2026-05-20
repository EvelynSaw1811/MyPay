package com.mypay.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SAGA_STATE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saga_id", columnDefinition = "CHAR(36)", updatable = false)
    private String sagaStateId;

    @Column(name = "saga_txn_id", columnDefinition = "CHAR(36)", nullable = false)
    private String sagaStateTransactionId;

    @Column(name = "saga_step", nullable = false)
    private int sagaStateStep;

    @Column(name = "saga_status", length = 20)
    private String sagaStateStatus;

    @Column(name = "saga_comp_step")
    private Integer sagaStateCompensationStep;

    @Column(name = "saga_updated")
    private LocalDateTime sagaStateUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        sagaStateUpdated = LocalDateTime.now();
    }
}

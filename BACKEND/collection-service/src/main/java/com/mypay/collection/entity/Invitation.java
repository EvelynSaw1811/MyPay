package com.mypay.collection.entity;

import com.mypay.common.constant.CollectionRole;
import com.mypay.common.constant.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "INVITATION_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @Column(name = "inv_id", columnDefinition = "CHAR(36)", updatable = false)
    private String invitationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inv_coll_id", nullable = false)
    private Collection collection;

    @Column(name = "inv_inviter", columnDefinition = "CHAR(36)", nullable = false)
    private String invitationInviter;

    @Column(name = "inv_invitee", columnDefinition = "CHAR(36)", nullable = false)
    private String invitationInvitee;

    @Enumerated(EnumType.STRING)
    @Column(name = "inv_role", length = 20, nullable = false)
    private CollectionRole invitationRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "inv_status", length = 20)
    @Builder.Default
    private InvitationStatus invitationStatus = InvitationStatus.PENDING;

    @Column(name = "inv_created", updatable = false)
    private LocalDateTime invitationCreated;

    @Column(name = "inv_updated")
    private LocalDateTime invitationUpdated;

    @PrePersist
    protected void onCreate() {
        if (invitationId == null) {
            invitationId = UUID.randomUUID().toString();
        }
        invitationCreated = LocalDateTime.now();
        invitationUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        invitationUpdated = LocalDateTime.now();
    }
}

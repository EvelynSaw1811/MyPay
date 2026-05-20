package com.mypay.collection.entity;

import com.mypay.common.constant.CollectionRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "COLLECTION_MEMBER_T",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cm_coll_id", "cm_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionMember {

    @Id
    @Column(name = "cm_id", columnDefinition = "CHAR(36)", updatable = false)
    private String collectionMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cm_coll_id", nullable = false)
    private Collection collection;

    @Column(name = "cm_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String collectionMemberUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cm_role", length = 20, nullable = false)
    private CollectionRole collectionMemberRole;

    @Column(name = "cm_joined_at", updatable = false)
    private LocalDateTime collectionMemberJoinedDateTime;

    @PrePersist
    protected void onCreate() {
        if (collectionMemberId == null) {
            collectionMemberId = UUID.randomUUID().toString();
        }
        collectionMemberJoinedDateTime = LocalDateTime.now();
    }
}

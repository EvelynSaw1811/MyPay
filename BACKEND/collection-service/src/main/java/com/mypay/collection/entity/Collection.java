package com.mypay.collection.entity;

import com.mypay.common.constant.CollectionCategory;
import com.mypay.common.constant.CollectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "COLLECTION_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {

    @Id
    @Column(name = "coll_id", columnDefinition = "CHAR(36)", updatable = false)
    private String collectionId;

    @Column(name = "coll_name", length = 255, nullable = false)
    private String collectionName;

    @Column(name = "coll_desc", length = 500)
    private String collectionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "coll_category", length = 20)
    private CollectionCategory collectionCategory;

    @Column(name = "coll_type_name", length = 100)
    private String collectionTypeName;

    @Column(name = "coll_currency", columnDefinition = "CHAR(3)", nullable = false)
    private String collectionCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "coll_status", length = 20)
    @Builder.Default
    private CollectionStatus collectionStatus = CollectionStatus.ACTIVE;

    @Column(name = "coll_owner_id", columnDefinition = "CHAR(36)", nullable = false)
    private String collectionOwnerId;

    @Column(name = "coll_created", updatable = false)
    private LocalDateTime collectionCreated;

    @Column(name = "coll_updated")
    private LocalDateTime collectionUpdated;

    @PrePersist
    protected void onCreate() {
        if (collectionId == null) {
            collectionId = UUID.randomUUID().toString();
        }
        collectionCreated = LocalDateTime.now();
        collectionUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        collectionUpdated = LocalDateTime.now();
    }
}

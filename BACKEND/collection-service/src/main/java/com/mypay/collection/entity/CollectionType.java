package com.mypay.collection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "COLLECTION_TYPE_T",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ctyp_user_id", "ctyp_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionType {

    @Id
    @Column(name = "ctyp_id", columnDefinition = "CHAR(36)", updatable = false)
    private String collectionTypeId;

    @Column(name = "ctyp_user_id", columnDefinition = "CHAR(36)")
    private String collectionTypeUserId;

    @Column(name = "ctyp_name", length = 100, nullable = false)
    private String collectionTypeName;

    @Column(name = "ctyp_system")
    @Builder.Default
    private boolean collectionTypeSystem = false;

    @Column(name = "ctyp_created", updatable = false)
    private LocalDateTime collectionTypeCreated;

    @PrePersist
    protected void onCreate() {
        if (collectionTypeId == null) {
            collectionTypeId = UUID.randomUUID().toString();
        }
        collectionTypeCreated = LocalDateTime.now();
    }
}

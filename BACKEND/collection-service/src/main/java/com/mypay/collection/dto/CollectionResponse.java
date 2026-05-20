package com.mypay.collection.dto;

import com.mypay.common.constant.CollectionCategory;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.constant.CollectionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionResponse {
    private String collectionId;
    private String name;
    private String description;
    private CollectionCategory category;
    private String typeName;
    private String currency;
    private CollectionStatus status;
    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSettledAt;
    /** Role of the requesting user in this collection (when listed via /me). */
    private CollectionRole myRole;
    private BigDecimal myNetBalance;
}

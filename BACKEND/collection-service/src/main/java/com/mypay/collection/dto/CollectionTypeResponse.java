package com.mypay.collection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionTypeResponse {
    private String collectionTypeId;
    private String name;
    private boolean system;
}

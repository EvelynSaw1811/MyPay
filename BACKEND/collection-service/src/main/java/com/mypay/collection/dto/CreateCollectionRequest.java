package com.mypay.collection.dto;

import com.mypay.common.constant.CollectionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCollectionRequest {

    @NotBlank(message = "Collection name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private CollectionCategory category;

    private String typeName;

    @NotBlank(message = "CurrencyCode is required")
    private String currency;
}

package com.mypay.collection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollectionTypeRequest {
    @NotBlank(message = "Collection type name is required")
    private String name;
}

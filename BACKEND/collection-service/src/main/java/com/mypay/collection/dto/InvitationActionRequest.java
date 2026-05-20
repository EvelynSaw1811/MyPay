package com.mypay.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InvitationActionRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "ACCEPT|DECLINE", message = "Action must be ACCEPT or DECLINE")
    private String action;
}

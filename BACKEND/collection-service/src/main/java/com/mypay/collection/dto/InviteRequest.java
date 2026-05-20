package com.mypay.collection.dto;

import com.mypay.common.constant.CollectionRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {

    private String inviteeUserId;

    private String identifier;

    @NotNull(message = "Role is required")
    private CollectionRole role;
}

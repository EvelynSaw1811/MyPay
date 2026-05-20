package com.mypay.collection.dto;

import com.mypay.common.constant.CollectionRole;
import com.mypay.common.constant.InvitationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InvitationResponse {
    private String invitationId;
    private String collectionId;
    private String collectionName;
    private String inviterId;
    private String inviteeId;
    private CollectionRole role;
    private InvitationStatus status;
    private LocalDateTime createdAt;
}

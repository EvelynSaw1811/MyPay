package com.mypay.collection.dto;

import com.mypay.common.constant.CollectionRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private String memberId;
    private String userId;
    private CollectionRole role;
    private LocalDateTime joinedAt;
    private String userNickname;
    private String invitationCode;
}

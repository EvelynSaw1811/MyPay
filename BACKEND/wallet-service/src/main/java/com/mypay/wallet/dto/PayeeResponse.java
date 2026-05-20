package com.mypay.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PayeeResponse {
    private String payeeId;
    private String userId;
    private String nickname;
    private LocalDateTime createdAt;
}

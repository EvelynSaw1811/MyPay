package com.mypay.collection.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceSummaryResponse {
    private String userId;
    private BigDecimal netBalance;
    private BigDecimal totalOwed;
    private BigDecimal totalOwing;
    private String userNickname;
    private String invitationCode;
}

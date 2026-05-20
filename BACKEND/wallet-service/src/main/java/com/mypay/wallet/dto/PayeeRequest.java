package com.mypay.wallet.dto;

import lombok.Data;

@Data
public class PayeeRequest {

    private String payeeUserId;

    private String identifier;
    private String nickname;
}

package com.mypay.wallet.service;

import com.mypay.wallet.dto.PayeeRequest;
import com.mypay.wallet.dto.PayeeResponse;

import java.util.List;

public interface PayeeService {
    List<PayeeResponse> getPayees(String userId);
    PayeeResponse addPayee(String userId, PayeeRequest request);
    void removePayee(String userId, String payeeId);
}

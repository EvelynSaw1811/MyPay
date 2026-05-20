package com.mypay.transaction.service;

import com.mypay.transaction.dto.NettingRequest;
import com.mypay.transaction.dto.NettingResponse;
import com.mypay.transaction.dto.SettleRequest;
import com.mypay.transaction.dto.SettlementResponse;
import com.mypay.transaction.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {
    SettlementResponse settle(SettleRequest request, String payerId);
    NettingResponse settleNet(NettingRequest request, String userId);
    List<TransactionResponse> getHistory(String userId);
    List<TransactionResponse> getHistoryByCurrency(String userId, String currency);
}

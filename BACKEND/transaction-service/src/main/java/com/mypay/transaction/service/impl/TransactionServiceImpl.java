package com.mypay.transaction.service.impl;

import com.mypay.transaction.dto.*;
import com.mypay.transaction.entity.Transaction;
import com.mypay.transaction.repository.TransactionRepository;
import com.mypay.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final SettlementSagaOrchestrator settlementSaga;
    private final NettingSagaOrchestrator nettingSaga;
    private final TransactionRepository transactionRepository;

    @Override
    public SettlementResponse settle(SettleRequest request, String payerId) {
        return settlementSaga.execute(request, payerId);
    }

    @Override
    public NettingResponse settleNet(NettingRequest request, String userId) {
        return nettingSaga.execute(request, userId);
    }

    @Override
    public List<TransactionResponse> getHistory(String userId) {
        return transactionRepository
                .findByTransactionPayerIdOrTransactionPayeeIdOrderByTransactionCreatedDesc(userId, userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getHistoryByCurrency(String userId, String currency) {
        return transactionRepository
                .findHistoryByUserIdAndCurrency(userId, currency)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction txn) {
        return TransactionResponse.builder()
                .transactionId(txn.getTransactionId())
                .payerId(txn.getTransactionPayerId())
                .payeeId(txn.getTransactionPayeeId())
                .amount(txn.getTransactionAmount())
                .currency(txn.getTransactionCurrency())
                .type(txn.getTransactionType())
                .status(txn.getTransactionStatus())
                .createdAt(txn.getTransactionCreated())
                .build();
    }
}

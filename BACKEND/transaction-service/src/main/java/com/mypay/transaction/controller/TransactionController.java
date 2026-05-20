package com.mypay.transaction.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.transaction.dto.*;
import com.mypay.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/settle")
    public ResponseEntity<ApiResponse<SettlementResponse>> settle(
            @Valid @RequestBody SettleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Settlement completed", transactionService.settle(request, RequestContextHolder.currentUserId())));
    }

    @PostMapping("/settle-net")
    public ResponseEntity<ApiResponse<NettingResponse>> settleNet(
            @Valid @RequestBody NettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Netting settlement completed", transactionService.settleNet(request, RequestContextHolder.currentUserId())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getHistory(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/history/{currency}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistoryByCurrency(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getHistoryByCurrency(RequestContextHolder.currentUserId(), currency)));
    }
}

package com.mypay.wallet.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.wallet.dto.PayeeRequest;
import com.mypay.wallet.dto.PayeeResponse;
import com.mypay.wallet.service.PayeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets/payees")
@RequiredArgsConstructor
public class PayeeController {

    private final PayeeService payeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayeeResponse>>> getPayees() {
        return ResponseEntity.ok(ApiResponse.success(payeeService.getPayees(RequestContextHolder.currentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PayeeResponse>> addPayee(
            @Valid @RequestBody PayeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payee added", payeeService.addPayee(RequestContextHolder.currentUserId(), request)));
    }

    @DeleteMapping("/{payeeId}")
    public ResponseEntity<ApiResponse<Void>> removePayee(
            @PathVariable String payeeId) {
        payeeService.removePayee(RequestContextHolder.currentUserId(), payeeId);
        return ResponseEntity.ok(ApiResponse.success("Payee removed", null));
    }
}

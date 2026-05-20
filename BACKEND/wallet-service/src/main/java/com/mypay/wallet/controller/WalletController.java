package com.mypay.wallet.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.wallet.dto.*;
import com.mypay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/internal/create")
    public ResponseEntity<ApiResponse<AccountResponse>> createWallet(@RequestHeader("X-User-Id") String userId) {
        AccountResponse response = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Account created", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyWallet() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getMyWallet(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/account/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccount() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getMyAccount(RequestContextHolder.currentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> openWallet(
            @Valid @RequestBody OpenWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet opened", walletService.openWallet(RequestContextHolder.currentUserId(), request)));
    }

    @GetMapping("/registration/{currency}")
    public ResponseEntity<ApiResponse<WalletRegistrationStatusResponse>> getRegistrationStatus(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(
                walletService.getRegistrationStatus(RequestContextHolder.currentUserId(), currency)));
    }

    @GetMapping("/balance/{currency}")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getBalance(RequestContextHolder.currentUserId(), currency)));
    }

    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<WalletResponse>> topUp(
            @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Top-up successful", walletService.topUp(RequestContextHolder.currentUserId(), request)));
    }

    @PostMapping("/internal/debit/{userId}")
    public ResponseEntity<ApiResponse<Void>> debit(
            @PathVariable String userId,
            @Valid @RequestBody WalletOperationRequest request) {
        walletService.debit(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Debit successful", null));
    }

    @PostMapping("/internal/credit/{userId}")
    public ResponseEntity<ApiResponse<Void>> credit(
            @PathVariable String userId,
            @Valid @RequestBody WalletOperationRequest request) {
        walletService.credit(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Credit successful", null));
    }
}

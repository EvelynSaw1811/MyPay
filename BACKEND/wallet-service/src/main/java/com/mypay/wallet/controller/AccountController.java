package com.mypay.wallet.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.wallet.dto.AccountResponse;
import com.mypay.wallet.dto.OpenWalletRequest;
import com.mypay.wallet.dto.WalletResponse;
import com.mypay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccount() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getMyAccount(RequestContextHolder.currentUserId())));
    }

    @PostMapping("/wallets")
    public ResponseEntity<ApiResponse<WalletResponse>> openWallet(
            @Valid @RequestBody OpenWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet opened", walletService.openWallet(RequestContextHolder.currentUserId(), request)));
    }

    @DeleteMapping("/wallets/{currency}")
    public ResponseEntity<ApiResponse<WalletResponse>> closeWallet(@PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success("Wallet closed",
                walletService.closeWallet(RequestContextHolder.currentUserId(), currency)));
    }
}

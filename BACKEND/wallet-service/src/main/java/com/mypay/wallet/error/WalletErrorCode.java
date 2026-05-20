package com.mypay.wallet.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WalletErrorCode implements ErrorCode {
    ACCOUNT_NOT_FOUND("Account not found", HttpStatus.NOT_FOUND),
    WALLET_NOT_FOUND("Wallet not found", HttpStatus.NOT_FOUND),
    WALLET_ALREADY_EXISTS("Wallet already exists", HttpStatus.CONFLICT),
    MAX_WALLETS_REACHED("Account already has the maximum number of wallets", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("Insufficient wallet balance", HttpStatus.UNPROCESSABLE_ENTITY),
    PAYEE_NOT_FOUND("Payee not found", HttpStatus.NOT_FOUND),
    PAYEE_ALREADY_EXISTS("Payee already added", HttpStatus.CONFLICT),
    UNSUPPORTED_CURRENCY("Unsupported wallet currency", HttpStatus.BAD_REQUEST);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "WALLET"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

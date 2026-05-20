package com.mypay.transaction.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {
    SETTLEMENT_FAILED("Settlement failed", HttpStatus.BAD_REQUEST),
    NETTING_FAILED("Netting failed", HttpStatus.BAD_REQUEST),
    SHARE_ALREADY_SETTLED("Share is already settled", HttpStatus.CONFLICT),
    NO_OUTSTANDING_BALANCE("No outstanding balance to net between users", HttpStatus.BAD_REQUEST),
    DEPENDENCY_UNAVAILABLE("Required service is unavailable", HttpStatus.BAD_REQUEST);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "TRANSACTION"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

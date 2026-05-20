package com.mypay.currency.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CurrencyErrorCode implements ErrorCode {
    RATE_NOT_FOUND("Exchange rate is not available", HttpStatus.NOT_FOUND),
    INVALID_AMOUNT("Amount must be positive", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_CURRENCY("Unsupported currency", HttpStatus.BAD_REQUEST);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "CURRENCY"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

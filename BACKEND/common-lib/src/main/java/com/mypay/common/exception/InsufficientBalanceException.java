package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public InsufficientBalanceException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public InsufficientBalanceException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

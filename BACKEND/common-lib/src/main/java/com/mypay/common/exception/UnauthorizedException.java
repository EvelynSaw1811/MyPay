package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

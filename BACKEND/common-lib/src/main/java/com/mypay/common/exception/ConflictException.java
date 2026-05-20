package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ConflictException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

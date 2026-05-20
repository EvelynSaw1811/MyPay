package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

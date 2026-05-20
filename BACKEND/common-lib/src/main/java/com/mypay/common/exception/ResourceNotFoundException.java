package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

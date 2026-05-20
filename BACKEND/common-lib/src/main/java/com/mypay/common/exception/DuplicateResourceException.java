package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

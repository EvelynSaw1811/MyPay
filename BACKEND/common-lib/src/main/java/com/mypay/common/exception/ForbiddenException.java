package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode.defaultMessage(), errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(message, errorCode);
    }
}

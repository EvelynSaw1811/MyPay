package com.mypay.common.exception;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = null;
    }

    protected BaseException(String message, ErrorCode errorCode) {
        super(message);
        this.status = errorCode.status();
        this.errorCode = errorCode;
    }
}

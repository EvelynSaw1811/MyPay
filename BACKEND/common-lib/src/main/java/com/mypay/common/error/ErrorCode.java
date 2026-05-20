package com.mypay.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String code();

    String module();

    String defaultMessage();

    HttpStatus status();
}

package com.mypay.reporting.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportingErrorCode implements ErrorCode {
    REPORT_NOT_AVAILABLE("Report is not available", HttpStatus.NOT_FOUND),
    REPORT_ACCESS_DENIED("You cannot access this report", HttpStatus.FORBIDDEN);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "REPORTING"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

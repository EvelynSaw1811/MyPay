package com.mypay.notification.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND("Notification not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_ACCESS_DENIED("You cannot access this notification", HttpStatus.FORBIDDEN),
    PREFERENCE_NOT_FOUND("Notification preferences not found", HttpStatus.NOT_FOUND);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "NOTIFICATION"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

package com.mypay.auth.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_CREDENTIALS("Invalid credentials", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_REGISTERED("Email is already registered", HttpStatus.CONFLICT),
    PROFILE_ACCESS_DENIED("You can only access your own profile", HttpStatus.FORBIDDEN),
    INVITATION_CODE_GENERATION_FAILED("Could not generate a unique invitation code", HttpStatus.CONFLICT);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "AUTH"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

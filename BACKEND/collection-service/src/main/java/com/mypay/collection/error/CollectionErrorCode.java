package com.mypay.collection.error;

import com.mypay.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CollectionErrorCode implements ErrorCode {
    COLLECTION_NOT_FOUND("Collection not found", HttpStatus.NOT_FOUND),
    COLLECTION_ACCESS_DENIED("You are not a member of this collection", HttpStatus.FORBIDDEN),
    INSUFFICIENT_COLLECTION_ROLE("Insufficient collection role", HttpStatus.FORBIDDEN),
    COLLECTION_CLOSED("Cannot modify a closed collection", HttpStatus.BAD_REQUEST),
    EXPENSE_NOT_FOUND("Expense not found", HttpStatus.NOT_FOUND),
    SHARE_NOT_FOUND("Share not found", HttpStatus.NOT_FOUND),
    INVITATION_NOT_FOUND("Invitation not found", HttpStatus.NOT_FOUND),
    INVITATION_ALREADY_PENDING("Invitation already pending for this user", HttpStatus.CONFLICT),
    INVITATION_ALREADY_RESPONDED("Invitation already responded to", HttpStatus.CONFLICT),
    COLLECTION_TYPE_EXISTS("Collection type already exists", HttpStatus.CONFLICT);

    private final String defaultMessage;
    private final HttpStatus status;

    @Override public String code() { return name(); }
    @Override public String module() { return "COLLECTION"; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public HttpStatus status() { return status; }
}

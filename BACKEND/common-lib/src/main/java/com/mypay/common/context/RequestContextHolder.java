package com.mypay.common.context;

import com.mypay.common.exception.UnauthorizedException;

public final class RequestContextHolder {
    private static final ThreadLocal<RequestContext> CURRENT = new ThreadLocal<>();

    private RequestContextHolder() {}

    public static void set(RequestContext context) {
        CURRENT.set(context);
    }

    public static RequestContext current() {
        return CURRENT.get();
    }

    public static String currentUserId() {
        RequestContext context = current();
        if (context == null || context.getUserId() == null || context.getUserId().isBlank()) {
            throw new UnauthorizedException("Authenticated user context is missing");
        }
        return context.getUserId();
    }

    public static String currentUserIdOr(String fallback) {
        RequestContext context = current();
        return context != null && context.getUserId() != null && !context.getUserId().isBlank()
                ? context.getUserId()
                : fallback;
    }

    public static String traceId() {
        RequestContext context = current();
        return context == null ? null : context.getTraceId();
    }

    public static void clear() {
        CURRENT.remove();
    }
}

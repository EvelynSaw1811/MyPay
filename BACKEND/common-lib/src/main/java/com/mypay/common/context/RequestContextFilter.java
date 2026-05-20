package com.mypay.common.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = headerOrNew(request, RequestHeaderConstants.TRACE_ID);
        String requestId = headerOrNew(request, RequestHeaderConstants.REQUEST_ID);

        RequestContext context = RequestContext.builder()
                .userId(request.getHeader(RequestHeaderConstants.USER_ID))
                .requestId(requestId)
                .traceId(traceId)
                .userStatus(request.getHeader(RequestHeaderConstants.USER_STATUS))
                .verificationStatus(request.getHeader(RequestHeaderConstants.VERIFICATION_STATUS))
                .authSource(request.getHeader(RequestHeaderConstants.AUTH_SOURCE))
                .sourceIp(resolveIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .method(request.getMethod())
                .path(request.getRequestURI())
                .build();

        RequestContextHolder.set(context);
        putMdc(context);
        response.setHeader(RequestHeaderConstants.TRACE_ID, traceId);
        response.setHeader(RequestHeaderConstants.REQUEST_ID, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            RequestContextHolder.clear();
        }
    }

    private String headerOrNew(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void putMdc(RequestContext context) {
        put("traceId", context.getTraceId());
        put("requestId", context.getRequestId());
        put("userId", context.getUserId());
        put("method", context.getMethod());
        put("path", context.getPath());
    }

    private void put(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}

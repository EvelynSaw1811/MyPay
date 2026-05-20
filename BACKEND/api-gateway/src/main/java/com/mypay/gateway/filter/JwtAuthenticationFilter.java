package com.mypay.gateway.filter;

import com.mypay.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private static final String USER_ID = "X-User-Id";
    private static final String REQUEST_ID = "X-Request-Id";
    private static final String TRACE_ID = "X-Trace-Id";
    private static final String AUTH_SOURCE = "X-Auth-Source";

    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String traceId = headerOrNew(exchange, TRACE_ID);
        String requestId = headerOrNew(exchange, REQUEST_ID);

        // Block external access to inter-service internal endpoints
        if (path.contains("/internal/")) {
            return error(exchange, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", traceId, requestId);
        }

        if (isOpenPath(path)) {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> setContextHeaders(headers, null, traceId, requestId))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required", traceId, requestId);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required", traceId, requestId);
        }

        String userId = jwtUtil.extractUserId(token);
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(headers -> setContextHeaders(headers, userId, traceId, requestId))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isOpenPath(String path) {
        return OPEN_PATHS.stream().anyMatch(path::equals);
    }

    private String headerOrNew(ServerWebExchange exchange, String headerName) {
        String value = exchange.getRequest().getHeaders().getFirst(headerName);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value;
    }

    private void setContextHeaders(HttpHeaders headers, String userId, String traceId, String requestId) {
        headers.remove(USER_ID);
        if (userId != null && !userId.isBlank()) {
            headers.set(USER_ID, userId);
        }
        headers.set(TRACE_ID, traceId);
        headers.set(REQUEST_ID, requestId);
        headers.set(AUTH_SOURCE, "JWT");
    }

    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String code, String message,
                             String traceId, String requestId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(TRACE_ID, traceId);
        response.getHeaders().set(REQUEST_ID, requestId);
        String body = """
                {"success":false,"message":"%s","errorCode":"%s","module":"GATEWAY","traceId":"%s"}
                """.formatted(message, code, traceId);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

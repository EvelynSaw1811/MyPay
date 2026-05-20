package com.mypay.common.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestContext {
    private String userId;
    private String requestId;
    private String traceId;
    private String userStatus;
    private String verificationStatus;
    private String authSource;
    private String sourceIp;
    private String userAgent;
    private String method;
    private String path;
}

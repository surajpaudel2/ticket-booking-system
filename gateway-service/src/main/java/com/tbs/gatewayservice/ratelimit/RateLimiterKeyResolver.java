package com.tbs.gatewayservice.ratelimit;

import com.tbs.gatewayservice.constant.GatewayConstants;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimiterKeyResolver {

    public KeyResolver resolvePublicRouteKey() {
        return exchange -> Mono.just(extractClientIpFromRequest(exchange.getRequest()));
    }

    public KeyResolver resolveAuthenticatedRouteKey() {
        return exchange -> Mono.just(extractUserIdFromRequest(exchange.getRequest()));
    }

    private String extractClientIpFromRequest(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst(GatewayConstants.HEADER_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private String extractUserIdFromRequest(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(GatewayConstants.HEADER_USER_ID);
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        return extractClientIpFromRequest(request);
    }
}

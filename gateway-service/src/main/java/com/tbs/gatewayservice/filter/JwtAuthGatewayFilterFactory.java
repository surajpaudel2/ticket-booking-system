package com.tbs.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.gatewayservice.dto.ApiResponse;
import com.tbs.gatewayservice.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.tbs.gatewayservice.constant.GatewayConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("JwtAuthGatewayFilter execution started for URI: {}", exchange.getRequest().getURI());

            Optional<String> bearerToken = extractBearerTokenFromHeader(exchange);
            if (bearerToken.isEmpty()) {
                return handleMissingToken(exchange);
            }
            return validateAndProcessToken(bearerToken.get(), config, exchange, chain);
        };
    }

    private Optional<String> extractBearerTokenFromHeader(ServerWebExchange exchange) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HEADER_AUTHORIZATION);
        if (authorizationHeader == null || !isBearerToken(authorizationHeader)) {
            return Optional.empty();
        }
        return Optional.of(authorizationHeader.substring(BEARER_PREFIX.length()));
    }

    private boolean isAuthorizationHeaderPresent(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HEADER_AUTHORIZATION);
        return header != null && !header.isBlank();
    }

    private boolean isBearerToken(String authorizationHeader) {
        return authorizationHeader.startsWith(BEARER_PREFIX);
    }

    private Mono<Void> validateAndProcessToken(String token, Config config, ServerWebExchange exchange, GatewayFilterChain chain) {

        return jwtTokenProvider.validateAndExtractClaims(token)
                .map(claims -> {
                    String userId = jwtTokenProvider.extractUserIdFromClaims(claims);
                    String role = jwtTokenProvider.extractRoleFromClaims(claims);

                    log.debug("Token validated successfully. Extracted userId: {}, role: {}", userId, role);

                    ServerWebExchange mutatedExchange = buildMutatedRequest(exchange, userId, role);
                    return checkAdminRoleIfRequired(role, config, exchange, chain, mutatedExchange);
                })
                .orElseGet(() -> handleInvalidToken(exchange));
    }

    private Mono<Void> checkAdminRoleIfRequired(String role, Config config, ServerWebExchange exchange, GatewayFilterChain chain, ServerWebExchange mutatedExchange) {
        if (!config.isRequiresAdmin()) {
            return chain.filter(mutatedExchange);
        }
        if (!isAdminRole(role)) {
            log.warn("Access denied: User with role '{}' attempted to access admin resource at URI: {}", role, exchange.getRequest().getURI());
            return handleInsufficientRole(exchange);
        }

        log.debug("Admin role verified successfully for request URI: {}", exchange.getRequest().getURI());
        return chain.filter(mutatedExchange);
    }

    private boolean isAdminRole(String role) {
        return ROLE_ADMIN.equals(role);
    }

    private ServerWebExchange buildMutatedRequest(ServerWebExchange exchange, String userId, String role) {
        return exchange.mutate()
                .request(r -> r
                        .header(HEADER_USER_ID, userId)
                        .header(HEADER_USER_ROLES, role))
                .build();
    }

    private Mono<Void> handleMissingToken(ServerWebExchange exchange) {
        log.warn("Authentication failed: Missing or malformed Bearer token for URI: {}", exchange.getRequest().getURI());
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication token is missing");
    }

    private Mono<Void> handleInvalidToken(ServerWebExchange exchange) {
        log.warn("Authentication failed: Invalid or expired token for URI: {}", exchange.getRequest().getURI());
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication token is invalid or expired");
    }

    private Mono<Void> handleInsufficientRole(ServerWebExchange exchange) {
        return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] errorBytes = objectMapper.writeValueAsBytes(ApiResponse.error(message));
            DataBuffer dataBuffer = response.bufferFactory().wrap(errorBytes);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response to JSON for URI: {}", exchange.getRequest().getURI(), e);
            return response.setComplete();
        }
    }

    public static class Config {

        private boolean requiresAdmin;

        public Config() {}

        public Config(boolean requiresAdmin) {
            this.requiresAdmin = requiresAdmin;
        }

        public boolean isRequiresAdmin() {
            return requiresAdmin;
        }

        public void setRequiresAdmin(boolean requiresAdmin) {
            this.requiresAdmin = requiresAdmin;
        }
    }
}
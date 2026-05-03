package com.tbs.gatewayservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.gatewayservice.dto.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class GatewayExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        if (throwable instanceof ResponseStatusException responseStatusException) {
            if (HttpStatus.TOO_MANY_REQUESTS.equals(responseStatusException.getStatusCode())) {
                return handleRequestRateLimitedException(exchange);
            }
            return handleResponseStatusException(exchange, responseStatusException);
        }
        return handleGenericException(exchange);
    }

    private Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException ex) {
        HttpStatus resolvedStatus = HttpStatus.resolve(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        HttpStatus status = resolvedStatus != null ? resolvedStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        return buildErrorResponse(exchange, status, reason);
    }

    private Mono<Void> handleRequestRateLimitedException(ServerWebExchange exchange) {
        return buildErrorResponse(exchange, HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests. Please slow down and try again.");
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange) {
        return buildErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
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
            return response.setComplete();
        }
    }
}

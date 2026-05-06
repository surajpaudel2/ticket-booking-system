package com.tbs.gatewayservice.filter;

import com.tbs.gatewayservice.constant.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("CorrelationIdFilter start for request URI: {}", exchange.getRequest().getURI());

        String correlationId = resolveCorrelationId(exchange);
        log.info("CorrelationIdFilter resolved correlation ID: {}", correlationId);

        addCorrelationIdToResponse(exchange, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header(GatewayConstants.HEADER_CORRELATION_ID, correlationId))
                .build();

        log.debug("Successfully mutated request with correlation ID header. Proceeding to next filter in chain.");
        return chain.filter(mutatedExchange);
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String existingCorrelationId = exchange.getRequest().getHeaders()
                .getFirst(GatewayConstants.HEADER_CORRELATION_ID);

        if (existingCorrelationId != null && !existingCorrelationId.isBlank()) {
            log.debug("Found existing correlation ID in request headers: {}", existingCorrelationId);
            return existingCorrelationId;
        }

        log.debug("No correlation ID found in request headers. Generating a new UUID.");
        return UUID.randomUUID().toString();
    }

    private void addCorrelationIdToResponse(ServerWebExchange exchange, String correlationId) {
        log.info("Adding correlation ID to response: {}", correlationId);
        exchange.getResponse().getHeaders().add(GatewayConstants.HEADER_CORRELATION_ID, correlationId);
    }
}
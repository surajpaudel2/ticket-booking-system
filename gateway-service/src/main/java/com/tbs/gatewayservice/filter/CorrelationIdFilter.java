package com.tbs.gatewayservice.filter;

import com.tbs.gatewayservice.constant.GatewayConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange);
        addCorrelationIdToResponse(exchange, correlationId);
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header(GatewayConstants.HEADER_CORRELATION_ID, correlationId))
                .build();
        return chain.filter(mutatedExchange);
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String existingCorrelationId = exchange.getRequest().getHeaders()
                .getFirst(GatewayConstants.HEADER_CORRELATION_ID);
        if (existingCorrelationId != null && !existingCorrelationId.isBlank()) {
            return existingCorrelationId;
        }
        return UUID.randomUUID().toString();
    }

    private void addCorrelationIdToResponse(ServerWebExchange exchange, String correlationId) {
        exchange.getResponse().getHeaders().add(GatewayConstants.HEADER_CORRELATION_ID, correlationId);
    }
}

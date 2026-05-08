package com.tbs.authservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    static final String USER_ID_HEADER = "X-User-Id";
    static final String USER_ROLES_HEADER = "X-User-Roles";
    static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("doFilterInternal called");
        String userId = request.getHeader(USER_ID_HEADER);
        String rolesHeader = request.getHeader(USER_ROLES_HEADER);
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        // Set correlation ID in MDC so it appears in every log line automatically
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put("correlationId", correlationId);
            log.debug("Correlation ID set in MDC: {}", correlationId);
        }

        if (userId != null && rolesHeader != null) {
            List<SimpleGrantedAuthority> authorities = buildAuthorities(rolesHeader);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Security context set for userId: {}, roles: {}", userId, rolesHeader);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // always clean up MDC after request finishes
        }
    }

    private List<SimpleGrantedAuthority> buildAuthorities(String rolesHeader) {
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
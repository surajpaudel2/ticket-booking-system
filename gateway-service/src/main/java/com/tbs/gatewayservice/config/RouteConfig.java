package com.tbs.gatewayservice.config;

import com.tbs.gatewayservice.filter.JwtAuthGatewayFilterFactory;
import com.tbs.gatewayservice.ratelimit.RateLimiterKeyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.tbs.gatewayservice.constant.GatewayConstants.*;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final JwtAuthGatewayFilterFactory jwtAuthGatewayFilterFactory;
    private final RateLimiterKeyResolver rateLimiterKeyResolver;

    // ── Spring-managed beans — Redis is properly injected by IoC container ──
    @Qualifier("publicRateLimiter")
    private final RedisRateLimiter publicRateLimiter;

    @Qualifier("authenticatedRateLimiter")
    private final RedisRateLimiter authenticatedRateLimiter;

    @Qualifier("adminRateLimiter")
    private final RedisRateLimiter adminRateLimiter;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        routes = registerPublicRoutes(routes);
        routes = registerProtectedRoutes(routes);
        routes = registerAdminRoutes(routes);
        return routes.build();
    }

    // ─────────────────────────────────────────────
    // ROUTE GROUPS
    // ─────────────────────────────────────────────

    private RouteLocatorBuilder.Builder registerPublicRoutes(RouteLocatorBuilder.Builder routes) {
        return routeToAuthService(routes);
    }

    private RouteLocatorBuilder.Builder registerProtectedRoutes(RouteLocatorBuilder.Builder routes) {
        routes = routeToAuthServiceLogout(routes);
        routes = routeToUserService(routes);
        routes = routeToEventService(routes);
        routes = routeToBookingService(routes);
        routes = routeToPaymentService(routes);
        return routes;
    }

    private RouteLocatorBuilder.Builder registerAdminRoutes(RouteLocatorBuilder.Builder routes) {
        return routeToAdminService(routes);
    }

    // ─────────────────────────────────────────────
    // PUBLIC ROUTES — no JWT required, keyed by IP
    // ─────────────────────────────────────────────

    private RouteLocatorBuilder.Builder routeToAuthService(RouteLocatorBuilder.Builder routes) {
        KeyResolver publicKeyResolver = rateLimiterKeyResolver.resolvePublicRouteKey();
        return routes.route("auth-public", r -> r
                .path("/auth/register", "/auth/login", "/auth/refresh")
                .filters(f -> applyCircuitBreaker(
                        applyPublicRateLimiter(f, publicKeyResolver),
                        CB_AUTH_SERVICE, FALLBACK_AUTH))
                .uri("lb://auth-service"));
    }

    // ─────────────────────────────────────────────
    // PROTECTED ROUTES — JWT required, keyed by userId
    // ─────────────────────────────────────────────

    private RouteLocatorBuilder.Builder routeToAuthServiceLogout(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config authConfig = new JwtAuthGatewayFilterFactory.Config(false);
        return routes.route("auth-logout", r -> r
                .path("/auth/logout")
                .filters(f -> applyCircuitBreaker(
                        applyAuthenticatedRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(authConfig)),
                                authenticatedKeyResolver),
                        CB_AUTH_SERVICE, FALLBACK_AUTH))
                .uri("lb://auth-service"));
    }

    private RouteLocatorBuilder.Builder routeToUserService(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config authConfig = new JwtAuthGatewayFilterFactory.Config(false);
        return routes.route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> applyCircuitBreaker(
                        applyAuthenticatedRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(authConfig)),
                                authenticatedKeyResolver),
                        CB_USER_SERVICE, FALLBACK_USER))
                .uri("lb://user-service"));
    }

    private RouteLocatorBuilder.Builder routeToEventService(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config authConfig = new JwtAuthGatewayFilterFactory.Config(false);
        return routes.route("event-service", r -> r
                .path("/api/events/**")
                .filters(f -> applyCircuitBreaker(
                        applyAuthenticatedRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(authConfig)),
                                authenticatedKeyResolver),
                        CB_EVENT_SERVICE, FALLBACK_EVENT))
                .uri("lb://event-service"));
    }

    private RouteLocatorBuilder.Builder routeToBookingService(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config authConfig = new JwtAuthGatewayFilterFactory.Config(false);
        return routes.route("booking-service", r -> r
                .path("/api/bookings/**")
                .filters(f -> applyCircuitBreaker(
                        applyAuthenticatedRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(authConfig)),
                                authenticatedKeyResolver),
                        CB_BOOKING_SERVICE, FALLBACK_BOOKING))
                .uri("lb://booking-service"));
    }

    private RouteLocatorBuilder.Builder routeToPaymentService(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config authConfig = new JwtAuthGatewayFilterFactory.Config(false);
        return routes.route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> applyCircuitBreaker(
                        applyAuthenticatedRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(authConfig)),
                                authenticatedKeyResolver),
                        CB_PAYMENT_SERVICE, FALLBACK_PAYMENT))
                .uri("lb://payment-service"));
    }

    // ─────────────────────────────────────────────
    // ADMIN ROUTES — JWT + admin role required
    // ─────────────────────────────────────────────

    private RouteLocatorBuilder.Builder routeToAdminService(RouteLocatorBuilder.Builder routes) {
        KeyResolver authenticatedKeyResolver = rateLimiterKeyResolver.resolveAuthenticatedRouteKey();
        JwtAuthGatewayFilterFactory.Config adminConfig = new JwtAuthGatewayFilterFactory.Config(true);
        return routes.route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> applyCircuitBreaker(
                        applyAdminRateLimiter(
                                f.filter(jwtAuthGatewayFilterFactory.apply(adminConfig)),
                                authenticatedKeyResolver),
                        CB_ADMIN_SERVICE, FALLBACK_ADMIN))
                .uri("lb://admin-service"));
    }

    // ─────────────────────────────────────────────
    // FILTER HELPERS
    // ─────────────────────────────────────────────

    private GatewayFilterSpec applyPublicRateLimiter(GatewayFilterSpec filterSpec, KeyResolver keyResolver) {
        return filterSpec.requestRateLimiter(config -> config
                .setRateLimiter(publicRateLimiter)
                .setKeyResolver(keyResolver));
    }

    private GatewayFilterSpec applyAuthenticatedRateLimiter(GatewayFilterSpec filterSpec, KeyResolver keyResolver) {
        return filterSpec.requestRateLimiter(config -> config
                .setRateLimiter(authenticatedRateLimiter)
                .setKeyResolver(keyResolver));
    }

    private GatewayFilterSpec applyAdminRateLimiter(GatewayFilterSpec filterSpec, KeyResolver keyResolver) {
        return filterSpec.requestRateLimiter(config -> config
                .setRateLimiter(adminRateLimiter)
                .setKeyResolver(keyResolver));
    }

    private GatewayFilterSpec applyCircuitBreaker(GatewayFilterSpec filterSpec, String circuitBreakerName, String fallbackUri) {
        return filterSpec.circuitBreaker(config -> config
                .setName(circuitBreakerName)
                .setFallbackUri(fallbackUri));
    }
}
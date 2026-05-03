package com.tbs.gatewayservice.constant;

public final class GatewayConstants {

    private GatewayConstants() {}

    public static final String HEADER_AUTHORIZATION  = "Authorization";
    public static final String HEADER_USER_ID        = "X-User-Id";
    public static final String HEADER_USER_ROLES     = "X-User-Roles";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_FORWARDED_FOR  = "X-Forwarded-For";
    public static final String BEARER_PREFIX         = "Bearer ";

    public static final String ROLE_ADMIN    = "ROLE_ADMIN";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    public static final String CB_AUTH_SERVICE    = "authServiceCB";
    public static final String CB_USER_SERVICE    = "userServiceCB";
    public static final String CB_BOOKING_SERVICE = "bookingServiceCB";
    public static final String CB_PAYMENT_SERVICE = "paymentServiceCB";
    public static final String CB_ADMIN_SERVICE   = "adminServiceCB";
    public static final String CB_EVENT_SERVICE   = "eventServiceCB";

    public static final String FALLBACK_AUTH    = "forward:/fallback/auth";
    public static final String FALLBACK_USER    = "forward:/fallback/user";
    public static final String FALLBACK_BOOKING = "forward:/fallback/booking";
    public static final String FALLBACK_PAYMENT = "forward:/fallback/payment";
    public static final String FALLBACK_ADMIN   = "forward:/fallback/admin";
    public static final String FALLBACK_EVENT   = "forward:/fallback/event";
}

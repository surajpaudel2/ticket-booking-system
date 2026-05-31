package com.tbs.notificationservice.client;

import com.tbs.notificationservice.exception.NotificationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/** Translates Feign HTTP error responses from downstream services into NotificationExceptions. */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        if (status == 404) {
            log.warn("Downstream resource not found methodKey={}", methodKey);
            return new NotificationException("Resource not found: " + methodKey);
        }
        if (status >= 500) {
            log.error("Downstream service error methodKey={} status={}", methodKey, status);
            return new NotificationException("Downstream service error: status=" + status);
        }
        log.warn("Unexpected downstream response methodKey={} status={}", methodKey, status);
        return new NotificationException("Unexpected response from downstream: status=" + status);
    }
}

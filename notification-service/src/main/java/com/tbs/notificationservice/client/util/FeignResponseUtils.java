package com.tbs.notificationservice.client.util;

import com.tbs.notificationservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

// FeignResponseUtils returns Optional so callers decide on empty, never throws
@Slf4j
public final class FeignResponseUtils {

    private FeignResponseUtils() {}

    public static <T> Optional<T> unwrap(ApiResponse<T> response) {
        if (response != null && response.success() && response.data() != null) {
            return Optional.of(response.data());
        }
        log.warn("Downstream response absent or unsuccessful: success={} data={}",
                response != null ? response.success() : "null",
                response != null ? response.data() : "null");
        return Optional.empty();
    }
}

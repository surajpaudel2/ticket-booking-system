package com.tbs.bookingservice.client.util;

import com.tbs.bookingservice.dto.response.ApiResponse;
import com.tbs.bookingservice.exception.BookingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public final class FeignResponseUtils {

    private FeignResponseUtils() {}

    // Extracts data from a successful ApiResponse or throws BookingException with the failure message
    public static <T> T unwrap(ApiResponse<T> response) {
        if (response != null && response.success() && response.data() != null) {
            return response.data();
        }
        String message = response != null ? response.message() : "Empty response from downstream service";
        throw new BookingException(message, HttpStatus.BAD_GATEWAY);
    }
}
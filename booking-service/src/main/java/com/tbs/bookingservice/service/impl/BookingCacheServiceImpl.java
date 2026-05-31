package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.dto.cache.BookingPendingCache;
import com.tbs.bookingservice.mapper.BookingCacheMapper;
import com.tbs.bookingservice.service.BookingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCacheServiceImpl implements BookingCacheService {

    private static final String KEY_PREFIX = "booking:pending:";
    private static final Duration TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> redisTemplate;
    private final BookingCacheMapper bookingCacheMapper;

    @Override
    public void storeBookingPending(BookingPendingCache bookingPendingCache) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + bookingPendingCache.bookingId(), bookingPendingCache, TTL);
            log.info("Stored booking:pending:{} with TTL={}min", bookingPendingCache.bookingId(), TTL.toMinutes());
        } catch (Exception ex) {
            log.warn("Redis store failed for bookingId={}: {}", bookingPendingCache.bookingId(), ex.getMessage());
        }
    }

    @Override
    public void evictBookingPending(Long bookingId) {
        try {
            redisTemplate.delete(KEY_PREFIX + bookingId);
            log.info("Evicted booking:pending:{}", bookingId);
        } catch (Exception ex) {
            log.warn("Redis evict failed for bookingId={}: {}", bookingId, ex.getMessage());
        }
    }

    @Override
    public Optional<BookingPendingCache> getBookingPending(Long bookingId) {
        try {
            Object cached =  redisTemplate.opsForValue().get(KEY_PREFIX + bookingId);
            if(cached == null) {
                log.debug("BookingPending cache miss for bookingId={}", bookingId);
                return Optional.empty();
            }

            log.debug("BookingPending cache found for bookingId={}", bookingId);
            return Optional.of(bookingCacheMapper.toBookingPendingCache(cached));

        } catch (Exception ex) {
            log.warn("Redis BookingPendingCache retrieve failed for bookingId={}: {}", bookingId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isBookingPendingInCache(Long bookingId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + bookingId));
        } catch (Exception ex) {
            log.warn("Redis check failed for bookingId={}: {}", bookingId, ex.getMessage());
            return false;
        }
    }
}
package com.tbs.userservice.service.impl;

import com.tbs.userservice.dto.response.UserResponse;
import com.tbs.userservice.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_ID_PREFIX = "user:profile:id:";
    private static final String CACHE_KEY_EMAIL_PREFIX = "user:profile:email:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    @Override
    public UserResponse getUserById(UUID userId) {
        return getFromCache(CACHE_KEY_ID_PREFIX + userId);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return getFromCache(CACHE_KEY_EMAIL_PREFIX + email);
    }

    @Override
    public void saveUser(UserResponse response) {
        if (response == null) return;

        // Save both lookup paths simultaneously
        saveToCache(CACHE_KEY_ID_PREFIX + response.id(), response);
        saveToCache(CACHE_KEY_EMAIL_PREFIX + response.email(), response);
    }

    @Override
    public void evictUserByEmail(String email) {
        if (email == null) return;

        String key = CACHE_KEY_EMAIL_PREFIX + email;
        try {
            redisTemplate.delete(key);
            log.debug("Evicted Redis cache for key: {}", key);
        } catch (Exception e) {
            log.error("Redis delete failed for key {}: {}", key, e.getMessage());
        }
    }

    // --- Private Helper Methods ---

    private UserResponse getFromCache(String key) {
        try {
            return (UserResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis read failed for key {}: {}", key, e.getMessage());
            return null; // Fail gracefully to database
        }
    }

    private void saveToCache(String key, UserResponse response) {
        try {
            redisTemplate.opsForValue().set(key, response, CACHE_TTL);
        } catch (Exception e) {
            log.error("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }
}
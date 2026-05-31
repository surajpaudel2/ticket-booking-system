package com.tbs.eventservice.mapper;

import com.tbs.eventservice.entity.Fixture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FixtureCacheMapper {

    private final ObjectMapper objectMapper;

    public Fixture toFixture(Object cached) {
        return objectMapper.convertValue(cached, Fixture.class);
    }
}
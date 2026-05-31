package com.tbs.eventservice.service.impl;

import com.tbs.eventservice.dto.request.CreateSeasonRequest;
import com.tbs.eventservice.dto.response.SeasonResponse;
import com.tbs.eventservice.entity.Season;
import com.tbs.eventservice.exception.InvalidSeasonDateRangeException;
import com.tbs.eventservice.mapper.SeasonMapper;
import com.tbs.eventservice.repository.SeasonRepository;
import com.tbs.eventservice.service.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonMapper seasonMapper;

    @Override
    public SeasonResponse createSeason(CreateSeasonRequest request) {
        log.debug("createSeason — seasonName={} start={} end={}",
                request.getSeasonName(),
                request.getExpectedStartDateTime(),
                request.getExpectedEndDateTime());

        validateDateRange(request);

        Season saved = seasonRepository.save(seasonMapper.toEntity(request));

        log.info("Season created — id={} seasonName={}", saved.getId(), saved.getSeasonName());
        return seasonMapper.toResponse(saved);
    }

    private void validateDateRange(CreateSeasonRequest request) {
        if (!request.getExpectedStartDateTime().isBefore(request.getExpectedEndDateTime())) {
            throw new InvalidSeasonDateRangeException(
                    request.getExpectedStartDateTime(),
                    request.getExpectedEndDateTime());
        }
    }
}

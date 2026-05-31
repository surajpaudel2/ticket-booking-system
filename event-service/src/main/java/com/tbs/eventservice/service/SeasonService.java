package com.tbs.eventservice.service;

import com.tbs.eventservice.dto.request.CreateSeasonRequest;
import com.tbs.eventservice.dto.response.SeasonResponse;

/** Defines the contract for Season lifecycle operations. */
public interface SeasonService {

    /**
     * Creates a new Season from the supplied request.
     *
     * @param request the validated Season creation request
     * @return the persisted Season as a response DTO
     */
    SeasonResponse createSeason(CreateSeasonRequest request);
}

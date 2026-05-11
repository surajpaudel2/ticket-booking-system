package com.tbs.eventservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for seat reservation — validated before entering the service layer. */
@Getter
@Setter
@NoArgsConstructor
public class ReserveSeatRequest {

    @NotNull
    @Schema(description = "ID of the fixture", example = "42")
    private Long fixtureId;

    @Min(1)
    @Max(10)
    @Schema(description = "Seats to reserve. Min 1, max 10.", example = "2")
    private int requestedSeats;

    @NotNull
    @Schema(description = "ID of the user requesting seats", example = "1")
    private Long userId;
}

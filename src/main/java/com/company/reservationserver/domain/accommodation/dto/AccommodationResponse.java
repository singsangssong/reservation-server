package com.company.reservationserver.domain.accommodation.dto;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccommodationResponse(
        Long id,
        String name,
        Long price,
        LocalDateTime eventStartTime,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        Integer remainedStock
) {
    public static AccommodationResponse from(Accommodation accommodation) {
        return AccommodationResponse.builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .price(accommodation.getPrice())
                .eventStartTime(accommodation.getEventStartTime())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .remainedStock(accommodation.getRemainedStock())
                .build();
    }
}

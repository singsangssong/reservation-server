package com.company.reservationserver.domain.order.dto;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CheckoutResponse (
        // 사용자
        Long userId,
        Long pointY,

        // 숙소 상품 정보
        Long accommodationId,
        String accommodationName,
        LocalDateTime eventStartTime,
        Long price,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {

    public static CheckoutResponse of(User user, Accommodation accommodation) {
        return CheckoutResponse.builder()
                .userId(user.getId())
                .pointY(user.getPointY())
                .accommodationId(accommodation.getId())
                .accommodationName(accommodation.getName())
                .eventStartTime(accommodation.getEventStartTime())
                .price(accommodation.getPrice())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .build();
    }
}

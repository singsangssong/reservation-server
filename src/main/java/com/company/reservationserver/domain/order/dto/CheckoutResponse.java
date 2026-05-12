package com.company.reservationserver.domain.order.dto;

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
        Long price,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
}

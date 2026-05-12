package com.company.reservationserver.domain.payment.dto;

import java.util.List;

public record BookingRequest(
        Long userId,
        Long accommodationId,
        String idempotencyKey, // 따닥 방지용 고유 키 [cite: 46]
        List<PaymentRequest> payments // 복합 결제 리스트 [cite: 51]
) {
}

package com.company.reservationserver.domain.payment.dto;

import java.util.List;

public record BookingRequest(
        Long userId,
        Long accommodationId,
        String idempotencyKey,
        List<PaymentRequest> payments
) {
    public Long calculateTotalAmount() {
        if (payments == null || payments.isEmpty()) {
            return 0L;
        }
        return payments.stream()
                .mapToLong(PaymentRequest::amount)
                .sum();
    }
}

package com.company.reservationserver.domain.payment.dto;

import com.company.reservationserver.domain.payment.entity.PaymentMethod;

public record PaymentRequest(
        PaymentMethod method,
        Long amount
) {
}

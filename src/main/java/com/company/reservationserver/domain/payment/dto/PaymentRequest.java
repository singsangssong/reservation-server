package com.company.reservationserver.domain.payment.dto;

import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentMethod method,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        Long amount
) {
}

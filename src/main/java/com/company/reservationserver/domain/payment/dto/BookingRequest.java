package com.company.reservationserver.domain.payment.dto;

import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "숙소 ID는 필수입니다.")
        Long accommodationId,

        @NotBlank(message = "멱등성 키는 필수입니다.")
        String idempotencyKey,

        @NotEmpty(message = "최소 1개 이상의 결제 수단이 필요합니다.")
        @Valid
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

    public Long getPointAmount() {
        return payments.stream()
                .filter(p -> p.method() == PaymentMethod.POINT)
                .mapToLong(PaymentRequest::amount)
                .sum();
    }
}

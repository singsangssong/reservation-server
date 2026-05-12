package com.company.reservationserver.domain.payment.dto;

import com.company.reservationserver.domain.order.entity.OrderStatus;

import java.util.List;

public record BookingResponse(
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        List<PaymentInfoResponse> paymentDetails
) {
}

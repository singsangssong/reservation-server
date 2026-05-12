package com.company.reservationserver.domain.payment.dto;

import com.company.reservationserver.domain.order.entity.Order;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public record BookingResponse(
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        List<PaymentInfo> paymentInfos
) {
    public static BookingResponse of(Order order, BookingRequest request) {
        List<PaymentInfo> infos = request.payments().stream()
                .map(p -> new PaymentInfo(p.method(), p.amount()))
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .paymentInfos(infos)
                .build();
    }
}

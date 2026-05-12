package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.client.PgClient;
import com.company.reservationserver.domain.order.entity.Order;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.order.repository.OrderRepository;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final PgClient pgClient;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public BookingResponse postBooking(BookingRequest request) {
        // 1. 주문 총액 계산
        Long totalPrice = request.calculateTotalAmount();

        // 2. 주문 정보 먼저 임시 저장 (PENDING)
        Order order = Order.builder()
                .userId(request.userId())
                .productId(request.accommodationId())
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .idempotencyKey(request.idempotencyKey())
                .build();
        Order savedOrder = orderRepository.save(order);

        // 3. PG사 결제 요청 & 결제 결과에 따른 상태 업데이트
        if (!pgClient.pay(totalPrice, request.payments().get(0).method().name())) {
            savedOrder.markAsFailed();
            throw new RuntimeException("결제 처리에 실패했습니다.");
        }

        savedOrder.markAsSuccess();

        // 4. 응답 생성
        return BookingResponse.of(savedOrder, request);
    }
}

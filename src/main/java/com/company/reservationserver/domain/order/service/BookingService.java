package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.client.PgClient;
import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.exception.AccommodationNotFoundException;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.entity.Order;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.order.repository.OrderRepository;
import com.company.reservationserver.domain.order.validator.BookingValidator;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.domain.payment.entity.Payment;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import com.company.reservationserver.domain.payment.entity.PaymentStatus;
import com.company.reservationserver.domain.payment.exception.PaymentFailedException;
import com.company.reservationserver.domain.payment.repository.PaymentRepository;
import com.company.reservationserver.domain.user.entity.User;
import com.company.reservationserver.domain.user.exception.UserNotFoundException;
import com.company.reservationserver.domain.user.repository.UserRepository;
import com.company.reservationserver.support.annotation.DistributedLock;
import com.company.reservationserver.support.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final PgClient pgClient;
    private final BookingValidator bookingValidator;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;

    @DistributedLock(key = "#request.accommodationId()")
    @Idempotent(key = "#request.idempotencyKey()")
    @Transactional
    public BookingResponse postBooking(BookingRequest request) {
        // 1. 요청 비즈니스 규칙 검증 (시간, 결제 수단 혼용 등)
        bookingValidator.validate(request);

        // 2. 도메인 엔티티 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(UserNotFoundException::new);
        Accommodation accommodation = accommodationRepository.findById(request.accommodationId())
                .orElseThrow(AccommodationNotFoundException::new);

        // 3. 엔티티 상태 변경
        user.deductPoint(request.getPointAmount());
        accommodation.deductStock();

        // 4. 주문 정보 PENDING 상태로 임시 저장
        Order order = Order.createPending(request);
        Order savedOrder = orderRepository.save(order);

        // 5. 외부 PG 결제
        PaymentMethod primaryMethod = extractPrimaryPaymentMethod(request);
        boolean isPaymentSuccess = pgClient.pay(request.calculateTotalAmount(), primaryMethod.name());

        // 6. 결제 내역 엔티티 생성 및 일괄 저장
        List<Payment> payments = request.payments().stream()
                .map(p -> Payment.builder()
                        .orderId(savedOrder.getId())
                        .paymentMethod(p.method())
                        .amount(p.amount())
                        .status(isPaymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                        .build())
                .toList();
        paymentRepository.saveAll(payments); // PaymentRepository 주입 필요

        // 7. 결과 처리
        if (!isPaymentSuccess) {
            savedOrder.markAsFailed();
            throw new PaymentFailedException(); // 커스텀 예외!
        }

        savedOrder.markAsSuccess();
        return BookingResponse.of(savedOrder, request);
    }

    private PaymentMethod extractPrimaryPaymentMethod(BookingRequest request) {
        return request.payments().stream()
                .map(p -> p.method())
                .filter(m -> m != PaymentMethod.POINT)
                .findFirst()
                .orElse(PaymentMethod.POINT);
    }
}

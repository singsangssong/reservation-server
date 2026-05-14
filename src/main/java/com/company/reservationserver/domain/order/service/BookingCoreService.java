package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.client.PgClient;
import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.exception.AccommodationNotFoundException;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.entity.Order;
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
public class BookingCoreService {

    private final PgClient pgClient;
    private final BookingValidator bookingValidator;
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    // 💡 1. 평소에 동작하는 Redis 락 메인 로직
    @DistributedLock(key = "#request.accommodationId()")
    @Idempotent(key = "#request.idempotencyKey()")
    @Transactional
    public BookingResponse postBookingWithRedisLock(BookingRequest request) {
        bookingValidator.validate(request);
        User user = userRepository.findById(request.userId())
                .orElseThrow(UserNotFoundException::new);
        Accommodation accommodation = accommodationRepository.findById(request.accommodationId())
                .orElseThrow(AccommodationNotFoundException::new);

        return processBooking(request, user, accommodation);
    }

    // 💡 2. Redis 장애 시 동작하는 DB 비관적 락 Fallback 로직
    @Transactional
    public BookingResponse postBookingWithPessimisticLock(BookingRequest request) {
        bookingValidator.validate(request);
        User user = userRepository.findById(request.userId())
                .orElseThrow(UserNotFoundException::new);
        // 비관적 락 전용 쿼리 호출
        Accommodation accommodation = accommodationRepository.findByIdForUpdate(request.accommodationId())
                .orElseThrow(AccommodationNotFoundException::new);

        return processBooking(request, user, accommodation);
    }

    // 💡 3. 중복되는 순수 비즈니스 로직을 private으로 빼서 재사용
    private BookingResponse processBooking(BookingRequest request, User user, Accommodation accommodation) {
        user.deductPoint(request.getPointAmount());
        accommodation.deductStock();

        Order order = Order.createPending(request);
        Order savedOrder = orderRepository.save(order);

        PaymentMethod primaryMethod = extractPrimaryPaymentMethod(request);
        boolean isPaymentSuccess = pgClient.pay(request.calculateTotalAmount(), primaryMethod.name());

        List<Payment> payments = request.payments().stream()
                .map(p -> Payment.builder()
                        .orderId(savedOrder.getId())
                        .paymentMethod(p.method())
                        .amount(p.amount())
                        .status(isPaymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                        .build())
                .toList();
        paymentRepository.saveAll(payments);

        if (!isPaymentSuccess) {
            savedOrder.markAsFailed();
            throw new PaymentFailedException();
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

package com.company.reservationserver.domain.order.controller;

import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.support.exception.SystemOverloadedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class OrderController {

    private final BookingService bookingService;

    @PostMapping
    @CircuitBreaker(name = "booking", fallbackMethod = "fastFailFallback")
    public ResponseEntity<BookingResponse> postBooking(
            @Valid @RequestBody BookingRequest request
    ) {
        BookingResponse response = bookingService.postBooking(request);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<BookingResponse> fastFailFallback(BookingRequest request, Throwable t) {
        // 비즈니스 예외(재고 부족 등)는 차단하지 않고 그대로 던짐
        if (t instanceof IllegalStateException || t instanceof IllegalArgumentException) {
            throw (RuntimeException) t;
        }

        // 인프라 장애로 인한 서킷 차단 시
        log.error("[서킷 브레이커 작동] 시스템 과부하로 인해 요청을 차단합니다. 원인: {}", t.getMessage());

        // 503 Service Unavailable 상태코드 반환 (클라이언트에게 잠시 후 다시 시도하라고 알림)
        throw new SystemOverloadedException();
    }
}

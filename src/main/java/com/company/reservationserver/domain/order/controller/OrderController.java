package com.company.reservationserver.domain.order.controller;

import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.support.exception.SystemOverloadedException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
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
        // 1. 서킷 브레이커가 열려서(OPEN) Resilience4j가 접근을 원천 차단한 경우
        if (t instanceof CallNotPermittedException) {
            log.error("[서킷 브레이커 작동] 시스템 과부하로 인해 요청을 차단합니다. (상태: OPEN)");
            throw new SystemOverloadedException();
        }

        // 2. 그 외의 모든 예외(커스텀 예외 400, 404, 409 등)는 건드리지 않고 그대로 다시 던집니다.
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }

        throw new RuntimeException(t);
    }
}

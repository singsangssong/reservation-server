package com.company.reservationserver.domain.order.controller;

import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.order.service.CheckoutService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;
    private final BookingService bookingService;

    @GetMapping("/checkout")
    public ResponseEntity<CheckoutResponse> getCheckoutInfo(
            @RequestParam Long userId,
            @RequestParam Long accommodationId) {

        CheckoutResponse response = checkoutService.getCheckoutInfo(userId, accommodationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking")
    public ResponseEntity<BookingResponse> postBooking(@Valid @RequestBody BookingRequest request) {
        // @Valid 어노테이션이 DTO의 조건들을 1차 검증하고, 실패 시 MethodArgumentNotValidException 발생
        BookingResponse response = bookingService.postBooking(request);
        return ResponseEntity.ok(response);
    }
}

package com.company.reservationserver.domain.accommodation.controller;

import com.company.reservationserver.domain.accommodation.dto.AccommodationResponse;
import com.company.reservationserver.domain.accommodation.service.AccommodationService;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.order.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final CheckoutService checkoutService;

    @GetMapping
    public ResponseEntity<List<AccommodationResponse>> getAccommodations() {
        return ResponseEntity.ok(accommodationService.getAllAccommodations());
    }

    @GetMapping("/{accommodationId}/checkout")
    public ResponseEntity<CheckoutResponse> getCheckoutInfo(
            @PathVariable Long accommodationId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(checkoutService.getCheckoutInfo(accommodationId, userId));
    }
}

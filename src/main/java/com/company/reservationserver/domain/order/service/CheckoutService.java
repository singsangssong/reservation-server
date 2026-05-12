package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.user.entity.User;
import com.company.reservationserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutInfo(Long userId, Long accommodationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("숙소 정보를 찾을 수 없습니다."));

        return CheckoutResponse.builder()
                .userId(user.getId())
                .pointY(user.getPointY())
                .accommodationId(accommodation.getId())
                .accommodationName(accommodation.getName())
                .price(accommodation.getPrice())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .build();
    }
}

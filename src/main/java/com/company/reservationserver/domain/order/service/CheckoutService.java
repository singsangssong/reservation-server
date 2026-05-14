package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.exception.AccommodationNotFoundException;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.user.entity.User;
import com.company.reservationserver.domain.user.exception.UserNotFoundException;
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
                .orElseThrow(UserNotFoundException::new);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(AccommodationNotFoundException::new);

        return CheckoutResponse.of(user, accommodation);
    }
}

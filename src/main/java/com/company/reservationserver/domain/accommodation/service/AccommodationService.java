package com.company.reservationserver.domain.accommodation.service;

import com.company.reservationserver.domain.accommodation.dto.AccommodationResponse;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    public List<AccommodationResponse> getAllAccommodations() {
        return accommodationRepository.findAll().stream()
                .map(AccommodationResponse::from)
                .toList();
    }
}

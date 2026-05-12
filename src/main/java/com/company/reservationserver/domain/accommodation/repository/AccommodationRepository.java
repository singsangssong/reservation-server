package com.company.reservationserver.domain.accommodation.repository;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}

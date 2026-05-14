package com.company.reservationserver.domain.accommodation.repository;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Accommodation a WHERE a.id = :id")
    Optional<Accommodation> findByIdForUpdate(@Param("id") Long id);
}

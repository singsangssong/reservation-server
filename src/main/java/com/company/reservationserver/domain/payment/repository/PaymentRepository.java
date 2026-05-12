package com.company.reservationserver.domain.payment.repository;

import com.company.reservationserver.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

package com.company.reservationserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class ReservationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServerApplication.class, args);
    }
}

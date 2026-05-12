package com.company.reservationserver.client;

import org.springframework.stereotype.Component;

@Component
public class DummyPgClient implements PgClient {

    @Override
    public boolean pay(Long amount, String paymentMethod) {
        if (amount > 1000000L) {
            return false;
        }
        return true;
    }
}

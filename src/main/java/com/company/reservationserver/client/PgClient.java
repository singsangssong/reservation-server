package com.company.reservationserver.client;

public interface PgClient {
    boolean pay(Long amount, String paymentMethod);
}

package com.company.reservationserver.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ExceptionSituation {
    private final String message;
    private final HttpStatus statusCode;

    public static ExceptionSituation of(String message, HttpStatus statusCode) {
        return new ExceptionSituation(message, statusCode);
    }
}

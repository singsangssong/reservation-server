package com.company.reservationserver.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponse(String message) {

    public static ExceptionResponse from(ExceptionSituation exceptionSituation) {
        return new ExceptionResponse(exceptionSituation.getMessage());
    }
}

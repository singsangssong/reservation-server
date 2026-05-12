package com.company.reservationserver.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {}

    public static ExceptionSituation getSituationOf(Exception exception) {
        return mapper.get(exception.getClass());
    }
}

package com.company.reservationserver.exception;

import com.company.reservationserver.domain.accommodation.exception.AccommodationNotFoundException;
import com.company.reservationserver.domain.accommodation.exception.OutOfStockException;
import com.company.reservationserver.domain.order.exception.NotOpenTimeException;
import com.company.reservationserver.domain.payment.exception.PaymentFailedException;
import com.company.reservationserver.domain.payment.exception.PaymentMethodMixedException;
import com.company.reservationserver.domain.user.exception.PointNotEnoughException;
import com.company.reservationserver.domain.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionMapper {

    private static final Map<Class<? extends Exception>, ExceptionSituation> mapper = new LinkedHashMap<>();

    static {
        setUpExceptions();
    }

    public static ExceptionSituation getSituationOf(Exception exception) {
        return mapper.get(exception.getClass());
    }

    private static void setUpExceptions() {
        mapper.put(PaymentFailedException.class,
                ExceptionSituation.of("결제에 실패했습니다.", HttpStatus.BAD_REQUEST));
        mapper.put(UserNotFoundException.class,
                ExceptionSituation.of("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(AccommodationNotFoundException.class,
                ExceptionSituation.of("숙소 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        mapper.put(PointNotEnoughException.class,
                ExceptionSituation.of("포인트가 부족합니다.", HttpStatus.BAD_REQUEST));
        mapper.put(OutOfStockException.class,
                ExceptionSituation.of("재고가 소진되었습니다.", HttpStatus.CONFLICT));
        mapper.put(NotOpenTimeException.class,
                ExceptionSituation.of("아직 예약 오픈 시간이 아닙니다.", HttpStatus.BAD_REQUEST));
        mapper.put(PaymentMethodMixedException.class,
                ExceptionSituation.of("신용카드와 Y페이는 혼용할 수 없습니다.", HttpStatus.BAD_REQUEST));
    }
}

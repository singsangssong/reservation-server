package com.company.reservationserver.domain.order.validator;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.order.exception.NotOpenTimeException;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import com.company.reservationserver.domain.payment.exception.PaymentMethodMixedException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingValidator {

    public void validate(BookingRequest request, Accommodation accommodation) {
        validateOpenTime(accommodation);
        validatePaymentMethods(request);
    }

    private void validateOpenTime(Accommodation accommodation) {
        LocalDateTime now = LocalDateTime.now();
        if (!accommodation.isBookable(now)) {
            throw new NotOpenTimeException();
        }
    }

    private void validatePaymentMethods(BookingRequest request) {
        boolean hasCard = request.payments().stream().anyMatch(p -> p.method() == PaymentMethod.CARD);
        boolean hasYPay = request.payments().stream().anyMatch(p -> p.method() == PaymentMethod.Y_PAY);

        if (hasCard && hasYPay) {
            throw new PaymentMethodMixedException();
        }
    }
}

package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.domain.payment.dto.PaymentInfo;
import com.company.reservationserver.domain.payment.dto.PaymentRequest;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    BookingService bookingService;

    @Mock
    BookingCoreService bookingCoreService;

    @Test
    @DisplayName("성공: 결제 및 예약 성공")
    void postBooking_Success() {
        // given
        BookingRequest request = createRequest(List.of(
                new PaymentRequest(PaymentMethod.CARD, 40000L),
                new PaymentRequest(PaymentMethod.POINT, 10000L)
        ));

        BookingResponse mockResponse = new BookingResponse(
                100L,
                OrderStatus.SUCCESS,
                50000L,
                List.of(
                        new PaymentInfo(PaymentMethod.CARD, 40000L),
                        new PaymentInfo(PaymentMethod.POINT, 10000L)
                )
        );

        given(bookingCoreService.postBookingWithRedisLock(any(BookingRequest.class)))
                .willReturn(mockResponse);

        // when
        BookingResponse response = bookingService.postBooking(request);

        // then
        assertEquals(OrderStatus.SUCCESS, response.status());
        assertEquals(50000L, response.totalPrice());
    }

    // ---------------------------- PRIVATE METHODS ----------------------------

    private BookingRequest createRequest(List<PaymentRequest> payments) {
        return new BookingRequest(1L, 1L, "uuid-1234", payments);
    }
}

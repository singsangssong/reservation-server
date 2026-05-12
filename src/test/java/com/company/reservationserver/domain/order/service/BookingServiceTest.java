package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.order.entity.Order;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.order.repository.OrderRepository;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
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
    OrderRepository orderRepository;

    @Test
    @DisplayName("성공: 결제 및 예약 성공")
    void postBooking_Success() {
        // given
        BookingRequest request = createRequest(List.of(
                new PaymentRequest(PaymentMethod.CARD, 40000L),
                new PaymentRequest(PaymentMethod.POINT, 10000L)
        ));

        Order mockOrder = Order.builder()
                .userId(1L).productId(1L).status(OrderStatus.SUCCESS).totalPrice(50000L).idempotencyKey("uuid-1234")
                .build();
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        // when
         BookingResponse response = bookingService.postBooking(request);

         // then
         assertEquals(response.status(), OrderStatus.SUCCESS);
    }

    // ---------------------------- PRIVATE METHODS ----------------------------
    private BookingRequest createRequest(List<PaymentRequest> payments) {
        return new BookingRequest(1L, 1L, "uuid-1234", payments);
    }
}

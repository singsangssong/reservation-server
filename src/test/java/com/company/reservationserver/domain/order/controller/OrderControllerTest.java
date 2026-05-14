package com.company.reservationserver.domain.order.controller;

import com.company.reservationserver.domain.accommodation.exception.OutOfStockException;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.domain.payment.dto.PaymentInfo;
import com.company.reservationserver.domain.payment.dto.PaymentRequest;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("성공: 올바른 결제 요청 시 200 OK를 반환한다")
    void postBooking_Success() throws Exception {
        // given
        BookingRequest request = new BookingRequest(1L, 1L, "uuid", List.of(new PaymentRequest(PaymentMethod.CARD, 50000L)));
        BookingResponse response = new BookingResponse(100L, OrderStatus.SUCCESS, 50000L, List.of(new PaymentInfo(PaymentMethod.CARD, 50000L)));

        given(bookingService.postBooking(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("실패: @Valid 입력값 검증 실패 시 400 에러를 반환한다")
    void postBooking_Fail_Validation() throws Exception {
        // given
        BookingRequest badRequest = new BookingRequest(1L, 1L, "uuid", Collections.emptyList());

        // when & then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("최소 1개 이상의 결제 수단이 필요합니다."));
    }

    @Test
    @DisplayName("실패: 비즈니스 로직 예외(재고 부족) 발생 시 400 에러를 반환한다")
    void postBooking_Fail_OutOfStock() throws Exception {
        // given
        BookingRequest request = new BookingRequest(1L, 1L, "uuid", List.of(new PaymentRequest(PaymentMethod.CARD, 50000L)));
        given(bookingService.postBooking(any())).willThrow(new OutOfStockException());

        // when & then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("재고가 소진되었습니다."));
    }
}

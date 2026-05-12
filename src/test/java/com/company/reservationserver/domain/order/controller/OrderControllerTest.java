package com.company.reservationserver.domain.order.controller;

import com.company.reservationserver.domain.accommodation.exception.OutOfStockException;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.order.entity.OrderStatus;
import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.order.service.CheckoutService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import com.company.reservationserver.domain.payment.dto.PaymentInfo;
import com.company.reservationserver.domain.payment.dto.PaymentRequest;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import com.company.reservationserver.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
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

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    @DisplayName("성공: 체크아웃 정보 조회 시 200 OK를 반환한다")
    void getCheckoutInfo_Success() throws Exception {
        // given
        CheckoutResponse mockResponse = CheckoutResponse.builder()
                .userId(1L).pointY(50000L).accommodationId(1L)
                .accommodationName("오션뷰 펜션").price(50000L)
                .checkInTime(LocalDateTime.now()).checkOutTime(LocalDateTime.now().plusDays(1))
                .build();
        given(checkoutService.getCheckoutInfo(1L, 1L)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/checkout")
                        .param("userId", "1")
                        .param("accommodationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointY").value(50000L));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 조회 시 404 예외 응답을 반환한다 (ExceptionAdvice 작동)")
    void getCheckoutInfo_Fail_UserNotFound() throws Exception {
        // given
        given(checkoutService.getCheckoutInfo(999L, 1L)).willThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/checkout")
                        .param("userId", "999")
                        .param("accommodationId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("성공: 올바른 결제 요청 시 200 OK를 반환한다")
    void postBooking_Success() throws Exception {
        // given
        BookingRequest request = new BookingRequest(1L, 1L, "uuid", List.of(new PaymentRequest(PaymentMethod.CARD, 50000L)));
        BookingResponse response = new BookingResponse(100L, OrderStatus.SUCCESS, 50000L, List.of(new PaymentInfo(PaymentMethod.CARD, 50000L)));

        given(bookingService.postBooking(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("실패: @Valid 입력값 검증 실패 시 400 에러를 반환한다 (ExceptionAdvice 작동)")
    void postBooking_Fail_Validation() throws Exception {
        // given
        BookingRequest badRequest = new BookingRequest(1L, 1L, "uuid", Collections.emptyList());

        // when & then
        mockMvc.perform(post("/booking")
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
        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("재고가 소진되었습니다."));
    }
}

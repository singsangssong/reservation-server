package com.company.reservationserver.domain.accommodation.controller;

import com.company.reservationserver.domain.accommodation.service.AccommodationService;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.order.service.CheckoutService;
import com.company.reservationserver.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccommodationController.class)
class AccommodationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private AccommodationService accommodationService;

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
        mockMvc.perform(get("/api/v1/accommodations/{accommodationId}/checkout", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointY").value(50000L));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 조회 시 404 예외 응답을 반환한다")
    void getCheckoutInfo_Fail_UserNotFound() throws Exception {
        // given
        given(checkoutService.getCheckoutInfo(anyLong(), anyLong())).willThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/accommodations/{accommodationId}/checkout", 1L)
                        .param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }
}

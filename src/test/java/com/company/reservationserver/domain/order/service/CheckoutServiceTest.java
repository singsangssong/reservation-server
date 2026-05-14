package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.dto.CheckoutResponse;
import com.company.reservationserver.domain.user.entity.User;
import com.company.reservationserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @InjectMocks
    private CheckoutService checkoutService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Test
    @DisplayName("성공: 유저, 숙소 정보 조회")
    void getCheckoutInfo_Success() {
        // given
        Long userId = 1L;
        Long accommodationId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(createMockUser(100000L)));
        given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(createMockAccommodation()));

        // when
        CheckoutResponse response = checkoutService.getCheckoutInfo(userId, accommodationId);

        // then
        assertNotNull(response);
        assertEquals(response.pointY(), 100000L);
        assertEquals(response.accommodationName(), "초특가 오션뷰 펜션");
    }

    // ---------------------------- PRIVATE METHODS ----------------------------

    private User createMockUser(Long point) {
        User user = User.builder()
                .pointY(point)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Accommodation createMockAccommodation() {
        Accommodation accommodation = Accommodation.builder()
                .name("초특가 오션뷰 펜션")
                .price(50000L)
                .eventStartTime(LocalDateTime.of(2026, 5, 1, 0, 0))
                .checkInTime(LocalDateTime.of(2026, 6, 1, 15, 0))
                .checkOutTime(LocalDateTime.of(2026, 6, 2, 11, 0))
                .totalStock(10)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", 1L);
        return accommodation;
    }
}

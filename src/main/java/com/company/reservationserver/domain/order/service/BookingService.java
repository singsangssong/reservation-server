package com.company.reservationserver.domain.order.service;

import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingCoreService bookingCoreService;

    public BookingResponse postBooking(BookingRequest request) {
        try {
            // 1. 평소: Redis 분산 락 로직 시도
            return bookingCoreService.postBookingWithRedisLock(request);

        } catch (RedisException | RedisConnectionFailureException e) {
            // 2. 인프라 장애: Redis 에러 감지 시 Fallback 수행
            log.error("[인프라 장애] Redis 연결 실패. DB 비관적 락으로 우회합니다. cause: {}", e.getMessage());
            return bookingCoreService.postBookingWithPessimisticLock(request);

        } catch (Exception e) {
            // 3. 비즈니스 예외(재고 부족, 존재하지 않는 유저 등)는 그대로 상위로 던짐
            throw e;
        }
    }
}

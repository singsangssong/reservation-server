package com.company.reservationserver.infra;

import com.company.reservationserver.client.PgClient;
import com.company.reservationserver.domain.accommodation.entity.Accommodation;
import com.company.reservationserver.domain.accommodation.repository.AccommodationRepository;
import com.company.reservationserver.domain.order.service.BookingService;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import com.company.reservationserver.domain.payment.dto.PaymentRequest;
import com.company.reservationserver.domain.payment.entity.PaymentMethod;
import com.company.reservationserver.domain.user.entity.User;
import com.company.reservationserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @MockitoBean
    private PgClient pgClient;

    private Long savedAccommodationId;
    private List<Long> userIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 세팅: 재고가 딱 10개인 숙소 생성
        Accommodation accommodation = Accommodation.builder()
                .name("선착순 10명 호캉스")
                .price(50000L)
                .totalStock(10)
                .checkInTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0)) // 내일 15시
                .checkOutTime(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0)) // 모레 11시
                .build();
        savedAccommodationId = accommodationRepository.save(accommodation).getId();

        // 100명의 유저 생성 (포인트 빵빵하게)
        userIds.clear();
        for (int i = 0; i < 100; i++) {
            User savedUser = userRepository.save(new User(100000L));
            userIds.add(savedUser.getId());
        }

        // PG 결제는 항상 성공한다고 가정
        given(pgClient.pay(any(Long.class), anyString())).willReturn(true);
    }

    @AfterEach
    void tearDown() {
        accommodationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시성 제어: 100명이 동시에 예약을 요청해도 재고(10개)만큼만 성공해야 한다")
    void postBooking_Concurrency_100Users() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개의 스레드 풀
        CountDownLatch latch = new CountDownLatch(threadCount); // 100개의 스레드가 끝날 때까지 대기

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 100명의 유저가 동시에 예약 요청 (멀티 스레드 환경)
        for (long i = 0; i < threadCount; i++) {
            final long userId = userIds.get((int) i);

            executorService.submit(() -> {
                try {
                    BookingRequest request = new BookingRequest(
                            userId,
                            savedAccommodationId,
                            "uuid-" + userId, // 각기 다른 멱등성 키 (동시성 제어만 확인)
                            List.of(new PaymentRequest(PaymentMethod.CARD, 50000L))
                    );
                    bookingService.postBooking(request);
                    successCount.incrementAndGet(); // 성공 횟수 증가
                } catch (Exception e) {
                    // 재고 부족(OutOfStockException) 또는 락 타임아웃 예외 발생 시 여기로 빠짐
                    System.out.println("예약 실패 원인: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown(); // 스레드 작업 완료 알림
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 메인 스레드 대기

        // then: DB에서 숙소를 다시 조회해서 잔여 재고 확인
        Accommodation findAccommodation = accommodationRepository.findById(savedAccommodationId).orElseThrow();

        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        // 재고는 10개였으므로 정확히 10번만 성공하고 90번은 실패해야 한다!
        assertEquals(10, successCount.get());
        assertEquals(90, failCount.get());
        assertEquals(0, findAccommodation.getRemainedStock());
    }

    @Test
    @DisplayName("멱등성 제어: 따닥! 동일한 멱등성 키로 100번의 요청이 동시에 와도 단 1번만 성공해야 한다")
    void postBooking_Idempotency_Duplicated() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 💡 핵심: 1명의 동일한 유저가, 1개의 동일한 멱등성 키로 100번 요청을 보냄
        final long sameUserId = userIds.get(0);
        final String sameIdempotencyKey = "duplicate-key-9999";

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    BookingRequest request = new BookingRequest(
                            sameUserId,
                            savedAccommodationId,
                            sameIdempotencyKey, // 모두 같은 키를 사용!
                            List.of(new PaymentRequest(PaymentMethod.CARD, 50000L))
                    );
                    bookingService.postBooking(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // IdempotencyAspect에서 예외를 던지면 이쪽으로 옴
                    System.out.println("중복 차단 됨: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("멱등성 성공 횟수: " + successCount.get());
        System.out.println("멱등성 실패 횟수: " + failCount.get());

        // then: 100번을 보냈지만, 결제는 딱 1번만 성공해야 함!
        assertEquals(1, successCount.get());
        assertEquals(99, failCount.get());
    }
}

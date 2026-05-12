package com.company.reservationserver.domain.accommodation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalDateTime checkOutTime;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    @Column(name = "remained_stock", nullable = false)
    private Integer remainedStock;

    // DB 상의 재고 차감을 위한 메서드 (Redis 처리 후 최종 반영 시 사용)
    public void deductStock() {
        if (this.remainedStock <= 0) {
            throw new IllegalStateException("재고가 소진되었습니다.");
        }
        this.remainedStock--;
    }
}

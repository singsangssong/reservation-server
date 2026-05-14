package com.company.reservationserver.domain.accommodation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    @Column(name = "accommodation_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "event_start_time", nullable = false)
    private LocalDateTime eventStartTime;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalDateTime checkOutTime;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    @Column(name = "remained_stock", nullable = false)
    private Integer remainedStock;

    @Builder
    public Accommodation(String name, Long price, LocalDateTime eventStartTime,
                         LocalDateTime checkInTime, LocalDateTime checkOutTime,
                         Integer totalStock) {
        this.name = name;
        this.price = price;
        this.eventStartTime = eventStartTime;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalStock = totalStock;
        this.remainedStock = totalStock;
    }

    public boolean isBookable(LocalDateTime now) {
        return !now.isBefore(eventStartTime);
    }

    public void deductStock() {
        if (this.remainedStock <= 0) {
            throw new IllegalStateException("재고가 소진되었습니다.");
        }
        this.remainedStock--;
    }
}

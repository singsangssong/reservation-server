package com.company.reservationserver.domain.user.entity;

import com.company.reservationserver.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_y", nullable = false)
    private Long pointY;

    @Builder
    public User(Long pointY) {
        this.pointY = pointY;
    }

    // 포인트 차감을 위한 비즈니스 메서드 (객체지향적 설계)
    public void deductPoints(Long amount) {
        if (this.pointY < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.pointY -= amount;
    }
}

package com.company.reservationserver.domain.user.entity;

import com.company.reservationserver.support.entity.BaseTimeEntity;
import com.company.reservationserver.domain.user.exception.PointNotEnoughException;
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
    @Column(name = "user_id")
    private Long id;

    @Column(name = "point_y", nullable = false)
    private Long pointY;

    @Builder
    public User(Long pointY) {
        this.pointY = pointY;
    }

    public void deductPoints(Long amount) {
        if (this.pointY < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.pointY -= amount;
    }

    public void deductPoint(Long amount) {
        if (amount <= 0) return;

        if (this.pointY < amount) {
            throw new PointNotEnoughException();
        }
        this.pointY -= amount;
    }
}

package com.company.reservationserver.domain.order.entity;

import com.company.reservationserver.support.entity.BaseTimeEntity;
import com.company.reservationserver.domain.payment.dto.BookingRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Builder
    public Order(Long userId, Long productId, OrderStatus status, Long totalPrice, String idempotencyKey) {
        this.userId = userId;
        this.productId = productId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.idempotencyKey = idempotencyKey;
    }

    public static Order createPending(BookingRequest request) {
        return Order.builder()
                .userId(request.userId())
                .productId(request.accommodationId())
                .status(OrderStatus.PENDING)
                .totalPrice(request.calculateTotalAmount())
                .idempotencyKey(request.idempotencyKey())
                .build();
    }

    public void markAsSuccess() {
        this.status = OrderStatus.SUCCESS;
    }

    public void markAsFailed() {
        this.status = OrderStatus.FAILED;
    }
}

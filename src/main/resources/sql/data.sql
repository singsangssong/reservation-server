-- 1. 테스트 유저 (100,000 포인트 보유)
INSERT INTO users (user_id, point, created_at, updated_at)
VALUES (1, 100000, NOW(), NOW());

-- 2. 테스트용 특가 숙소 상품 (가격: 50,000원, 재고: 10개)
INSERT INTO accommodations (accommodation_id, name, price, event_start_time, check_in_time, check_out_time, total_stock, remained_stock)
VALUES (1, '초특가 오션뷰 펜션', 50000, '2026-05-01 00:00:00', '2026-06-01 15:00:00', '2026-06-02 11:00:00', 10, 10);

# API 명세서 (API_SPEC)

본 문서는 선착순 숙소 예약 시스템에서 제공하는 주요 API의 엔드포인트와 요청/응답 규격을 정의합니다.
모든 API는 `application/json` 형식을 사용하며, 에러 발생 시 공통된 에러 응답 포맷을 반환합니다.

---

## 공통 에러 응답 포맷

API 호출 실패 시 아래와 같은 형식으로 에러가 반환됩니다.

```json
{
  "message": "포인트가 부족합니다."
}
```
- HTTP Status Codes:
  - `400 Bad Request`: 비즈니스 로직 예외 (예: 포인트 부족, 오픈 시간 전, 결제 수단 혼용)
  - `404 Not Found:` 리소스를 찾을 수 없음 (예: 유저 없음, 숙소 없음)
  - `409 Conflict`: 상태 충돌 (예: 재고 소진)
  - `503 Service Unavailable`: 서킷 브레이커 동작 (시스템 과부하로 인한 요청 차단)

---

## 1. 숙소 목록 조회 API
테스터 또는 클라이언트가 예약 가능한 숙소의 목록과 상세 정보(오픈 시간, 재고 등)를 조회합니다.
- URL: `/api/v1/accommodations`
- Method: `GET`

200 OK
```json
[
  {
    "id": 1,
    "name": "초특가 오션뷰 펜션",
    "price": 50000,
    "eventStartTime": "2026-05-01T00:00:00",
    "checkInTime": "2026-06-01T15:00:00",
    "checkOutTime": "2026-06-02T11:00:00",
    "remainedStock": 10
  }
]
```
---

## 2. 체크아웃 정보 조회 API
예약(결제)을 진행하기 전, 유저의 잔여 포인트와 선택한 숙소의 가격 및 정보를 최종적으로 확인합니다.
- URL: `/api/v1/accommodations/{accommodationId}/checkout`
- Method: `GET`

**파라미터**

| Name            | In    | Type    | Description        | Required |
|-----------------|-------|---------|--------------------|---------|
| userId          | query | Long    | 예약을 시도하는 유저의 고유 ID | O |
| accommodationId | path  | Long    | 예약하려는 숙소의 고유 ID    | O |

**200 OK**
```json
{
  "userId": 1,
  "userPoint": 100000,
  "accommodationId": 1,
  "accommodationName": "초특가 오션뷰 펜션",
  "eventStartTime": "2026-05-01T00:00:00",
  "price": 50000,
  "checkInTime": "2026-06-01T15:00:00",
  "checkOutTime": "2026-06-02T11:00:00"
}
```
---

## 3. 숙소 예약 및 결제 API
선착순 숙소 예약을 요청합니다.

> 중복 결제 방지: 네트워크 지연 등으로 인한 중복 클릭 결제를 방지하기 위해 idempotencyKey를 반드시 포함해야 합니다.
- URL: `/api/v1/bookings`
- Method: `POST`

**요청 바디**
```json
{
  "userId": 1,
  "accommodationId": 1,
  "idempotencyKey": "uuid-test-001",
  "payments": [
    {
      "method": "CARD",
      "amount": 50000
    }
  ]
}
```

**200 OK**
```json
{
  "orderId": 101,
  "status": "SUCCESS",
  "totalAmount": 50000,
  "accommodationName": "초특가 오션뷰 펜션",
  "message": "예약이 성공적으로 완료되었습니다."
}
```

**실패 응답**
1. 재고 소진 시
    ```json
    {
      "message": "재고가 소진되었습니다."
    }
    ```
2. 예약 오픈 시간 전 호출 시
    ```json
    {
      "message": "아직 예약 오픈 시간이 아닙니다."
    }
    ```
3. 서버 과부하 / 서킷 브레이커 작동 시
    ```json
    {
      "message": "현재 접속자가 많아 서비스가 지연되고 있습니다. 잠시 후 다시 시도해주세요."
    }
    ```

# 시퀀스 다이어그램 — 이커머스 도메인 (Week 2)

---

## 시나리오 1: 좋아요 등록/취소 (멱등 처리)

멱등성 보장 방식과 `likeCount` 갱신 흐름의 책임 경계를 확인하기 위해 작성한다.

**핵심 포인트**
- 애플리케이션 레벨에서 먼저 존재 여부를 확인해 분기한다. DB UNIQUE 제약은 동시 요청에 대한 최후 방어선이다.
- `likeCount` 갱신은 Like TX 커밋 이후 별도 TX에서 처리한다 (`@TransactionalEventListener(AFTER_COMMIT)`). Like TX가 롤백되면 이벤트 자체가 발행되지 않는다.

```mermaid
sequenceDiagram
    actor User
    participant Facade as LikeFacade
    participant LikeSvc as LikeService
    participant ProdSvc as ProductService

    Note over Facade,ProdSvc: ── 좋아요 등록 ──

    User->>Facade: 좋아요 등록 (userId, productId)
    Note over Facade,LikeSvc: TX 시작
    Facade->>LikeSvc: findByUserIdAndProductId
    alt 이미 존재
        LikeSvc-->>Facade: no-op
    else 존재하지 않음
        LikeSvc->>LikeSvc: INSERT likes
        Note over LikeSvc: UNIQUE 위반 시 200으로 변환
    end
    Note over Facade,LikeSvc: TX 커밋 → LikeEvent 발행
    Note over ProdSvc: AFTER_COMMIT
    ProdSvc->>ProdSvc: product.likeCount += 1

    Note over Facade,ProdSvc: ── 좋아요 취소 ──

    User->>Facade: 좋아요 취소 (userId, productId)
    Note over Facade,LikeSvc: TX 시작
    Facade->>LikeSvc: findByUserIdAndProductId
    alt 존재하지 않음
        LikeSvc-->>Facade: no-op
    else 존재함
        LikeSvc->>LikeSvc: DELETE likes
    end
    Note over Facade,LikeSvc: TX 커밋 → LikeEvent 발행
    Note over ProdSvc: AFTER_COMMIT
    ProdSvc->>ProdSvc: product.likeCount -= 1
```

---

## 시나리오 2: 주문 생성

트랜잭션 경계와 결제 실패 시 보상 트랜잭션 흐름을 확인하기 위해 작성한다.

**핵심 포인트**
- TX가 두 번 열린다. 재고 차감 + 주문 생성(TX1)이 커밋된 뒤 외부 결제를 호출한다. 외부 HTTP를 TX 안에 포함하면 커넥션 점유 시간이 길어져 위험하다.
- 타임아웃 시 주문은 PENDING으로 남는다. 재고는 차감된 상태이며, 추후 배치로 처리한다.
- 재고 차감은 `product_stocks` 테이블에서 처리한다. `products.stock_quantity`는 목록 조회 캐시이므로 AFTER_COMMIT으로 별도 동기화한다.

```mermaid
sequenceDiagram
    actor User
    participant Facade as OrderFacade
    participant ProdSvc as ProductService
    participant OrdSvc as OrderService
    participant Pay as PaymentSystem (외부)

    User->>Facade: 주문 생성 (userId, items)

    Note over Facade,OrdSvc: ── TX1 시작 ──
    loop 각 상품
        Facade->>ProdSvc: stock.hasEnough(quantity)
        alt 재고 부족
            ProdSvc-->>Facade: 재고 부족 예외
        end
        ProdSvc->>ProdSvc: stock.deduct(quantity)
    end
    Facade->>OrdSvc: 주문 생성 (PENDING) + 상품 스냅샷 저장
    Note over Facade,OrdSvc: ── TX1 커밋 ──
    Note over ProdSvc: AFTER_COMMIT<br/>products.stock_quantity 동기화

    Facade->>Pay: 결제 요청

    alt 결제 성공
        Pay-->>Facade: 성공
        Note over Facade,OrdSvc: ── TX2 ──
        Facade->>OrdSvc: order.markAsPaid()
    else 결제 실패
        Pay-->>Facade: 실패
        Note over Facade,OrdSvc: ── TX2 ──
        Facade->>ProdSvc: stock.restore(quantity)
        Facade->>OrdSvc: order.markAsFailed()
    else 타임아웃
        Pay--xFacade: 응답 없음
        Note over Facade: 주문 PENDING 유지<br/>재고 차감된 상태 보존<br/>추후 배치로 처리
    end
```

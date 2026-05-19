# Level 7 — Idempotency (Duplicate Processing)

Port: **8107** · Redis: **6386** · PostgreSQL: **5439**

---

## 1. Problemin kendisi

Aynı sipariş isteği (retry, double-click, gateway replay) **birden fazla kez** işlenir. **Idempotency key** kontrolü yok.

Kısa özet: At-least-once delivery + non-idempotent handler = duplicate orders.

**Kasıtlı olarak yok:** idempotency key store, `INSERT … ON CONFLICT`, dedup table.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[DUPLICATE ORDER CREATED] clientRequestId=…` |
| DB | Aynı `client_request_id` ile 2+ `orders` satırı |
| API | Aynı body ile 2 POST → 2 farklı order id |
| Retry | `SimpleRetryExecutor` duplicate tetikler |

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `OrderService.create` | Her çağrıda yeni UUID order — dedup yok |
| 2 | `POST /api/orders` | Header/body: `clientRequestId` |
| 3 | Aynı request 2× (veya retry) | 2 order row |
| 4 | `SimpleRetryExecutor` | Timeout simülasyonu + retry |

**HTTP:** `http/duplicate-order.http`  
**Load test:** `load-test-scripts/level-7/duplicate_request_spam.py`

# Level 5 — Distributed Lock (Overselling)

Port: **8105** · Redis: **6384** · PostgreSQL: **5437**

---

## 1. Problemin kendisi

Stok düşürme (decrement) **atomik değil** ve **distributed lock yok**. Eşzamanlı satın almalar aynı stok satırını okuyup yazar → **overselling**, stok negatife inebilir.

Kısa özet: “Check-then-act” race — production’da flash sale felaketi.

**Kasıtlı olarak yok:** Redis `SET NX`, Redisson lock, DB `SELECT FOR UPDATE`.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[OVERSOLD PRODUCT]` productId=… finalStock=-N |
| Log | `[CONCURRENT UPDATE DETECTED]` |
| DB | `stock` negatif veya `sold > initial` |
| API | 200 OK sayısı > başlangıç stoku |

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `InventoryService.purchase` | read stock → if ok → write stock-1 (non-atomic) |
| 2 | `POST /api/inventory/{productId}/purchase` | Tek birimlik satış |
| 3 | 100 paralel request | Aynı productId, stock=10 |
| 4 | `InventoryRepository` | `@Version` yok veya ignore — lost update |

**HTTP:** `http/oversell.http`  
**Load test:** `load-test-scripts/level-5/concurrent_purchase.py --concurrency 100`

```bash
# stock=10 iken 100 eşzamanlı purchase → 90+ başarılı sipariş
```

# Level 8 — Race Condition (Lost Update / Stale Write)

Port: **8108** · Redis: **6387** · PostgreSQL: **5440**

---

## 1. Problemin kendisi

İki istek aynı aggregate’i okur, farklı alanları günceller veya aynı alanı yazar — **son yazan kazanır**, diğerinin değişikliği kaybolur (lost update). Cache invalidation gecikmesi ile birleşince **stale write** da oluşur.

Kısa özet: Optimistic locking / versioning / doğru invalidation yok.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[RACE CONDITION TRIGGERED]` |
| Log | `[CONCURRENT UPDATE DETECTED]` |
| DB | `version` artmıyor veya eski version ile overwrite |
| Cache | Eski snapshot tekrar yazılır — stale write |

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `ProductPatchService` | read-modify-write, version check yok |
| 2 | `PATCH /api/products/{id}` × 2 paralel | price + stock aynı anda |
| 3 | Biri kaybeder | Final state tek branch’in değeri |
| 4 | `delayedCacheInvalidate` | Invalidate race — stale cache geri yazılır |

**HTTP:** `http/concurrent-patch.http`  
**Load test:** `load-test-scripts/level-8/lost_update_race.py`

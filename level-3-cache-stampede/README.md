# Level 3 — Cache Stampede (Thundering Herd)

Port: **8103** · Redis: **6382** · PostgreSQL: **5435**

---

## 1. Problemin kendisi

Popüler bir key’in TTL’si aynı anda dolduğunda **binlerce istek** aynı anda cache miss yaşar ve **hepsi DB’ye** gider.

Kısa özet: Redis kısa süreliğine bypass olur; DB connection pool ve CPU tavan yapar.

**Bu level’da kasıtlı olarak yok:** mutex, single-flight, request coalescing.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | Aynı saniyede çok sayıda `[CACHE MISS]` + `[DB QUERY EXECUTED]` |
| Log | `[CACHE STAMPEDE DETECTED]` (aynı key, N paralel DB query) |
| PostgreSQL | Active connections spike, slow queries |
| Redis | Kısa süre sonra çok key yazımı — geç kalınmış populate |
| p99 latency | Dramatik artış |

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `ProductCacheService.getOrLoad` | Miss’te doğrudan DB — lock yok |
| 2 | `POST /api/admin/cache/flush/{id}` | Key silinir, herd tetiklenir |
| 3 | `GET /api/products/{id}` × N concurrent | Her thread ayrı DB SELECT |
| 4 | `SlowQuerySimulator` | Miss süresini uzatır → overlap artar |

**HTTP:** `http/concurrent-requests.http`  
**Load test:** `python cache_stampede_test.py --users 1000`

```bash
# Flush + eşzamanlı istek
python load-test-scripts/level-3/cache_stampede_test.py --users 500 --product-id 1
```

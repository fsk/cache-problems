# Level 4 — Hot Key

Port: **8104** · Redis: **6383** · PostgreSQL: **5436**

---

## 1. Problemin kendisi

Trafiğin büyük kısmı **tek bir Redis key**’ine gider (`product:1` gibi). O key ve bulunduğu node aşırı yüklenir; diğer key’ler boş kalır.

Kısa özet: Cache “dağıtık” görünür ama erişim **düzensiz (skewed)** — tek nokta darboğaz.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[HOT KEY PRESSURE] key=hot:product:1` yüksek frekans |
| Redis | `INFO commandstats` — bir key pattern dominate |
| Latency | Hot key read/write p99 >> cold keys |
| Memory | Tek key büyük payload + yüksek QPS |

Seed’de yüksek `popularity_score` ürünler hot key simüle eder.

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | Seed | `popularity_score` yüksek ürün ID=1 |
| 2 | `GET /api/products/hot/{id}` | Hep aynı cache key |
| 3 | Load test | %90 trafik productId=1 |
| 4 | `HotKeyMetricsFilter` | QPS > eşik → `[HOT KEY PRESSURE]` |

**HTTP:** `http/hot-key-scenario.http`  
**Load test:** `load-test-scripts/level-4/hot_key_flood.py --hot-product-id 1`

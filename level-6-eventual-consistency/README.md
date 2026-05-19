# Level 6 — Eventual Consistency

Port: **8106** · Redis: **6385** · PostgreSQL: **5438** · RabbitMQ: **5673**

---

## 1. Problemin kendisi

Write path: DB güncellenir → event RabbitMQ’ya gider → **gecikmeli** cache/read model güncellenir. Read path hâlâ eski cache veya eski replica’dan okur.

Kısa özet: Sistem **tutarlı görünür** ama pencere boyunca **stale read** — CAP’te availability tercihinin bedeli.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[EVENTUAL CONSISTENCY LAG] lagMs=…` |
| Log | Update sonrası GET hâlâ eski — sonra düzelir |
| RabbitMQ | Queue depth, consumer lag |
| UX | “Az önce güncelledim, hâlâ eski görünüyorum” |

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `ProductWriteService` | DB update + `rabbitTemplate.convertAndSend` |
| 2 | `CacheUpdateListener` | `Thread.sleep(2000)` — kasıtlı gecikme |
| 3 | `PUT` ardından hemen `GET` | Stale cache |
| 4 | 3 sn sonra `GET` | Tutarlı veri |

**HTTP:** `http/eventual-lag.http`  
**Load test:** `load-test-scripts/level-6/read_after_write.py`

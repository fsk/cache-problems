# Redis Distributed Systems Lab — Mimari

## Amaç

Bu monorepo bir **distributed systems laboratory**’dir. Her level **tek bir production problemini** kasıtlı olarak üretir; çözüm implementasyonu öğrenciye bırakılır.

## Tasarım İlkeleri

| İlke | Açıklama |
|------|----------|
| **Tek problem / level** | Level-3 stampede görürken idempotency karışmaz |
| **Intentionally broken** | Her level bilinçli eksik koruma içerir |
| **Bağımsız çalıştırma** | Kendi Postgres + Redis + port |
| **Gözlemlenebilirlik (log)** | `[CACHE MISS]`, `[OVERSOLD]` gibi yapılandırılmış log etiketleri |
| **Çözüm yok (phase 1)** | Lock, coalescing, idempotency key yok |

## Modül Katmanları

```
cache-problems/                    ← BASE MODULE (repo kökü, artifact: cache-problems)
├── level-N-*                      ← Spring Boot app, broken business logic
└── lab-reactor/                   ← Maven aggregator
```

- **cache-problems (kök)**: Paylaşılan altyapı referansı — `src/`, `docker/`. Level’lar kendi logger/domain kodunu taşır.
- **level-\***: Bağımsız Spring Boot uygulaması — kendi `DbQueryLogger`, entity, docker-compose.

## Level Port Haritası

| Level | Uygulama portu | Redis | PostgreSQL |
|-------|----------------|-------|------------|
| level-1-cache-aside (`java/`) | 8101 | 6380 | 5433 |
| level-2-cache-invalidation | 8102 | 6381 | 5434 |
| level-3-cache-stampede | 8103 | 6382 | 5435 |
| level-4-hot-key | 8104 | 6383 | 5436 |
| level-5-distributed-lock | 8105 | 6384 | 5437 |
| level-6-eventual-consistency | 8106 | 6385 | 5438 (+ RabbitMQ 5673) |
| level-7-idempotency | 8107 | 6386 | 5439 |
| level-8-race-condition | 8108 | 6387 | 5440 |

Portlar çakışmayı önlemek için level başına artırılır.

## Cache Aside Akışı (referans — level-1)

```
Client ──GET──► App
                  │
                  ├─ cache hit? ──► Redis ──► response
                  │
                  └─ cache miss ──► PostgreSQL ──► Redis SET ──► response
```

Broken varyantlarda invalidation, single-flight, lock vb. **bilinçli olarak yoktur**.

## Docker Stratejisi

- `docker/docker-compose.template.yml`: Level compose dosyaları için şablon.
- Her `level-N/docker-compose.yml`: Sadece o level’ın servisleri.
- Seed ve load test: level veya `load-test-scripts/` altında.

## Log Kontratı

Tüm leveller `com.lab.distributed.logging.LabLog` sabitlerini kullanır:

- `[CACHE HIT]` / `[CACHE MISS]`
- `[DB QUERY EXECUTED]`
- `[STALE CACHE DETECTED]`
- `[CONCURRENT UPDATE DETECTED]`
- `[OVERSOLD PRODUCT]`
- `[DUPLICATE ORDER CREATED]`
- `[RACE CONDITION TRIGGERED]`

Log formatı: `grep` ile problem repro için tasarlanmıştır.

## Geliştirme Sırası

1. ✅ cache-problems (kök base)
2. ✅ level-1-cache-aside
3. ✅ level-2-cache-invalidation
3. level-2 … level-8 (sırayla)
4. load-test-scripts (level ile birlikte)
5. (Gelecek phase) Observability: Prometheus, Grafana, OTel

## Production Risk Özeti

| Problem | Risk |
|---------|------|
| Cache miss storm | DB connection pool exhaustion, latency SLO breach |
| Stale cache | Yanlış fiyat/stok, finansal kayıp |
| Hot key | Redis single-thread bottleneck, node OOM |
| Overselling | Müşteri güveni, chargeback |
| Duplicate orders | Double charge, inventory drift |
| Eventual consistency | Read-your-writes ihlali, support tickets |

Bu lab’da bu riskler **kontrollü ortamda** tetiklenir; production’da aynı pattern’ler outage üretir.

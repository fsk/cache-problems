# Level 1 — Cache Aside

Port: **8101** · Redis: **6380** · PostgreSQL: **5433**

```
level-1-cache-aside/
├── java/          ← Spring Boot uygulaması (@EnableCaching + renkli loglar)
├── python/        ← PostgreSQL seed script
├── docker-compose.yml
└── docs / http    ← cache-aside-pattern.md, problem-scenario.http
```

---

## 1. Problemin kendisi

**Cache Aside**: okuma önce Redis → yoksa PostgreSQL → sonuç cache'e yazılır.

Spring **`@Cacheable`** ile uygulanır; `[CACHE HIT/MISS/PUT/EVICT]` logları `LoggingCache` üzerinden renkli basılır.

Bu modül kasıtlı olarak sadece saf cache-aside içerir. Stampede koruması, cache warming veya invalidation **yoktur**.

| Davranış | Ne olur |
|----------|---------|
| İlk `GET` | Cache miss → DB query → cache populate |
| İkinci `GET` (aynı id) | Cache hit → DB yok |
| TTL (30 sn) veya manuel evict | Key gider → tekrar miss → DB |

---

## 2. Gözlemlenmesi gereken problem

Terminalde uygulama loglarını izle (`mvn spring-boot:run` — `java/` klasöründen):

| Log etiketi | Renk | Ne zaman |
|-------------|------|----------|
| `[CACHE MISS]` | sarı | Redis'te key yok |
| `[DB QUERY EXECUTED]` | magenta | Sadece miss sonrası |
| `[CACHE PUT]` | mavi | DB'den sonra cache yazımı |
| `[CACHE HIT]` | yeşil | İkinci ve sonraki okumalar |
| `[CACHE EVICT]` | kırmızı | Admin evict |

**Beklenen sıra (product id=1):**

1. `GET /api/products/1` → MISS + DB QUERY + CACHE PUT  
2. `GET /api/products/1` → HIT (DB log **yok**)  
3. `DELETE /api/admin/cache/products/1` → EVICT  
4. `GET /api/products/1` → MISS + DB QUERY tekrar  

```bash
grep -E '\[CACHE|\[DB QUERY'   # uygulama loglarında
```

Redis key örneği (`@Cacheable` cache adı `products`):

```bash
redis-cli -p 6380 KEYS "level1:product:*"
```

---

## 3. Nasıl reproduce edilir

### Adım 0 — Altyapı

```bash
cd level-1-cache-aside
docker compose up -d
```

### Adım 1 — Veri yükle (Python)

```bash
cd level-1-cache-aside/python
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python seed_products.py --count 1000 --truncate
```

### Adım 2 — Uygulamayı başlat (Java)

```bash
cd lab-reactor && mvn clean install -DskipTests

cd ../level-1-cache-aside/java
mvn spring-boot:run
```

IntelliJ: **`java/`** klasörünü Maven modülü olarak açın, SDK **21**.

### Adım 3 — Senaryo

```bash
curl -s http://localhost:8101/api/products/1 | jq .
curl -s http://localhost:8101/api/products/1 | jq .
curl -s -X DELETE http://localhost:8101/api/admin/cache/products/1
curl -s http://localhost:8101/api/products/1 | jq .
```

`data.servedFrom`: ilk okuma `DATABASE`, cache'ten `CACHE`.

HTTP dosyası: `http/problem-scenario.http`

### Kodda nerede?

| Sınıf | Rol |
|-------|-----|
| `ProductCacheLoader` | `@Cacheable` — miss'te DB |
| `ProductService` | `servedFrom` + `@CacheEvict` |
| `LoggingCache` / `LoggingCacheManager` | Renkli cache logları |
| `CacheConfig` | `@EnableCaching` + Redis `CacheManager` |
| `DbQueryLogger` | Renkli `[DB QUERY EXECUTED]` |

Detaylı pattern: [cache-aside-pattern.md](cache-aside-pattern.md)  
Problem analizi: [problems-explain.md](problems-explain.md)

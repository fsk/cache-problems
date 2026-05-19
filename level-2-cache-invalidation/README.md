# Level 2 — Cache Invalidation (Stale Cache)

Port: **8102** · Redis: **6381** · PostgreSQL: **5434**

```
level-2-cache-invalidation/
├── java/       ← Spring Boot (@Cacheable, kasıtlı invalidation yok)
├── python/     ← seed script
└── docker-compose.yml
```

---

## 1. Problemin kendisi

DB'de veri güncellenir ama **cache invalidate edilmez**. Sonraki okumalar eski (stale) değeri döner.

`PUT` → sadece PostgreSQL · `GET` → hâlâ Redis'ten **cache hit**.

---

## 2. Gözlemlenmesi gereken loglar

Loglar `application.yml` üzerinden (`lab.cache`, `lab.db`, Spring cache TRACE):

| Log | Ne zaman |
|-----|----------|
| `[DB QUERY EXECUTED]` | İlk GET (cache miss) |
| `[CACHE NOT INVALIDATED]` | PUT sonrası |
| `[STALE CACHE DETECTED]` | PUT sonrası ilk GET (cache hit, eski fiyat) |
| Spring TRACE | `CacheInterceptor` — cache hit/miss detayı |

**Beklenen API akışı (product id=1, fiyat 100 → 1.99):**

1. `GET` → `source: DATABASE`, fiyat seed değeri  
2. `GET` → `source: CACHE`, aynı fiyat  
3. `PUT {"price": 1.99}` → `source: DATABASE`, fiyat **1.99**  
4. `GET` → `source: CACHE`, fiyat hâlâ **eski** (stale)

---

## 3. Reproduce

```bash
cd level-2-cache-invalidation
docker compose up -d

cd python && python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python seed_products.py --count 100 --truncate

cd ../java && mvn spring-boot:run
```

```bash
curl -s http://localhost:8102/api/products/1 | jq .data.price,.data.source
curl -s http://localhost:8102/api/products/1 | jq .data.price,.data.source
curl -s -X PUT http://localhost:8102/api/products/1 \
  -H 'Content-Type: application/json' -d '{"price": 1.99}' | jq .data.price
curl -s http://localhost:8102/api/products/1 | jq .data.price,.data.source
```

HTTP dosyası: [http/stale-cache.http](http/stale-cache.http)

### Kod

| Sınıf | Rol |
|-------|-----|
| `ProductCacheLoader` | `@Cacheable` okuma |
| `ProductService.updatePrice` | JPA save — **`@CacheEvict` yok** (kasıtlı) |

Redis key örneği: `redis-cli -p 6381 KEYS 'level2:product:*'`

Pattern dokümantasyonu: [cache-invalidation.md](cache-invalidation.md)  
Detaylı problem analizi: [problems-explain.md](problems-explain.md)

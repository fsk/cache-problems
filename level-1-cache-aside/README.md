# Level 1 — Cache Aside

Port: **8101** · Redis: **6380** · PostgreSQL: **5433**

---

## 1. Problemin kendisi

**Cache Aside**: okuma önce Redis → yoksa PostgreSQL → sonuç cache’e yazılır.

Bu modül kasıtlı olarak sadece saf cache-aside içerir. Stampede koruması, cache warming veya invalidation **yoktur**.

| Davranış | Ne olur |
|----------|---------|
| İlk `GET` | Cache miss → DB query → cache populate |
| İkinci `GET` (aynı id) | Cache hit → DB yok |
| TTL (30 sn) veya manuel evict | Key gider → tekrar miss → DB |

---

## 2. Gözlemlenmesi gereken problem

Terminalde uygulama loglarını izle (`mvn spring-boot:run` çıktısı):

| Log etiketi | Ne zaman görünür |
|-------------|------------------|
| `[CACHE MISS]` | Redis’te key yok |
| `[DB QUERY EXECUTED]` | Sadece miss sonrası DB çağrısı |
| `[CACHE PUT]` | DB’den sonra Redis’e yazım |
| `[CACHE HIT]` | İkinci ve sonraki okumalar |
| `[CACHE EVICT]` | Admin evict endpoint |

**Beklenen sıra (product id=1):**

1. `GET /api/products/1` → MISS + DB QUERY + CACHE PUT  
2. `GET /api/products/1` → HIT (DB log **yok**)  
3. `DELETE /api/admin/cache/products/1` → EVICT  
4. `GET /api/products/1` → MISS + DB QUERY tekrar  

`grep` ile filtrele:

```bash
# Uygulama loglarında
grep -E '\[CACHE|\[DB QUERY'
```

Redis’te key kontrolü:

```bash
redis-cli -p 6380 GET "level1:product:1"
redis-cli -p 6380 TTL "level1:product:1"
```

---

## 3. Nasıl reproduce edilir

### Adım 0 — Altyapı

```bash
cd level-1-cache-aside
docker compose up -d
```

Postgres ve Redis healthy olana kadar bekle (~10 sn).

### Adım 1 — Veri yükle (Python)

```bash
cd level-1-cache-aside
python3 -m venv .venv && source .venv/bin/activate
pip install -r scripts/requirements.txt
python scripts/seed_products.py --count 1000 --truncate
```

Büyük dataset için:

```bash
python scripts/seed_products.py --count 100000 --truncate
```

### Adım 2 — Uygulamayı başlat

```bash
# Monorepo kökünden
cd lab-reactor && mvn clean install -DskipTests

cd ../level-1-cache-aside
mvn spring-boot:run
```

### Adım 3 — Senaryoyu çalıştır

**Seçenek A — curl**

```bash
# 1) MISS + DB
curl -s http://localhost:8101/api/products/1 | jq .

# 2) HIT (DB log gelmemeli)
curl -s http://localhost:8101/api/products/1 | jq .

# 3) Evict (TTL simülasyonu)
curl -s -X DELETE http://localhost:8101/api/admin/cache/products/1

# 4) MISS + DB tekrar
curl -s http://localhost:8101/api/products/1 | jq .
```

`data.servedFrom` alanı: ilk okuma `DATABASE`, cache’ten `CACHE`.

**Seçenek B — HTTP dosyası**

IntelliJ / VS Code REST Client ile `http/problem-scenario.http` dosyasını sırayla çalıştır.

### Kodda problem nerede?

| Sınıf | Rol |
|-------|-----|
| `ProductCacheAsideService` | Cache-aside akışı — miss’te DB |
| `ProductRedisCache` | `[CACHE HIT/MISS/PUT/EVICT]` logları |
| `DbQueryLogger` | `[DB QUERY EXECUTED]` — **sadece bu modülde** |
| `CacheAdminController` | `DELETE /api/admin/cache/products/{id}` |

```text
GET /api/products/{id}
  → ProductCacheAsideService.getById()
    → ProductRedisCache.get()     → HIT? return
    → DbQueryLogger.execute()     → MISS: DB
    → ProductRedisCache.put()     → populate TTL=30s
```

### TTL ile reproduce (beklemeden)

Manuel evict yerine 30 saniye bekle → `GET` → miss + DB log tekrar görünür.

---

## Hızlı komut özeti

```bash
cd level-1-cache-aside
docker compose up -d
pip install -r scripts/requirements.txt
python scripts/seed_products.py --count 1000 --truncate
mvn spring-boot:run
```

Detaylı problem analizi: [problems-explain.md](problems-explain.md)

# Cache-Aside Pattern

System design'da veriyi yönetmek ve okuma performansını artırmak için kullanılan bir **caching stratejisi**. **Lazy Loading** (tembel yükleme) olarak da bilinir: veri cache'e, yalnızca uygulama ilk kez ihtiyaç duyduğunda yazılır.

Bu repodaki **Level 1** modülü (`level-1-cache-aside`) bu deseni bilinçli olarak sade tutar: Redis → PostgreSQL → populate. Stampede koruması, warming ve write-path invalidation yoktur — detaylar için [README.md](./README.md).

---

## Cache-Aside nedir?

Uygulama veriye ihtiyaç duyduğunda **önce cache'e**, orada yoksa **ana veritabanına** bakar. DB'den gelen sonucun bir kopyasını cache'e yazar; sonraki istekler mümkün olduğunca cache'ten karşılanır.

**Önemli:** Cache katmanı pasiftir — cache kendiliğinden DB'ye gitmez. Okuma/yazma kararını **uygulama kodu** verir.

### Akış (okuma)

```
┌─────────────┐     1. get(key)      ┌───────┐
│ Application │ ──────────────────► │ Cache │
└─────────────┘                     └───┬───┘
       ▲                                │
       │                    Hit ────────┘ (2a. döndür)
       │                    Miss
       │                                ▼
       │                         ┌──────────┐
       │    4. put(key, value)   │ Database │
       └──────────────────────── │          │
              3. load            └──────────┘
```

| Adım | Terim | Ne olur |
|------|--------|---------|
| 1 | **Cache lookup** | Uygulama önce cache'e sorar |
| 2a | **Cache hit** | Veri bulunursa hemen döner — DB'ye gidilmez |
| 2b | **Cache miss** | Veri yoksa uygulama ana DB'den yükler |
| 3 | **Cache population** | DB sonucunun kopyası cache'e yazılır |
| 4 | **Data usage** | Veri uygulama tarafında kullanılır |

### Yazma (tipik cache-aside)

Yazma işlemlerinde uygulama genelde **önce DB'yi günceller**. Cache'i ya siler (**invalidation**), ya günceller, ya da TTL ile eskiyip düşmesini bekler. Bu modülde write-path invalidation kasıtlı olarak yoktur — stale cache **Level 2**'de ele alınır.

---

## Performansı nasıl artırır?

| Etki | Açıklama |
|------|----------|
| **Daha hızlı okuma** | Bellek (Redis vb.) disk tabanlı DB'ye göre çok daha hızlıdır; sık okunan veri tekrar tekrar cache'ten servis edilir |
| **DB yükünün azalması** | Okumaların önemli kısmı cache'e kayar; DB connection ve I/O baskısı düşer |
| **Ölçeklenebilirlik** | Trafik arttıkça okuma yükü cache katmanına dağıtılabilir |
| **Throughput** | DB yazma ve karmaşık sorgular için kapasite kalır |
| **Düşük gecikme (latency)** | Gerçek zamanlı veya yüksek QPS senaryolarda kritik |

Cache bir sihir değildir: yanlış TTL, invalidation eksikliği veya stampede gibi durumlarda performans ve tutarlılık bozulabilir.

---

## Temel ilkeler

### 1. Lazy loading

Veri, **ilk istekte** (miss) DB'den alınır ve cache'e konur. Önceden tüm veriyi cache'e doldurmak (**cache warming**) ayrı bir stratejidir; Level 1'de yoktur.

### 2. Cache ayrı bir katman

Cache, DB'nin yerine geçmez; uygulama **her iki katmanla da** konuşur. Bu repoda `ProductCacheAsideService` + `ProductRedisCache` bu rolü üstlenir.

### 3. Okuma: uygulama yönetir (cache-aside)

- Okuma: cache → (miss ise) DB → cache'e yaz → döndür  
- Bu, **Read-Through** değildir: Read-Through'da cache miss'te **cache katmanı** DB'yi çağırır ve doldurur; uygulama sadece cache'e sorar.

### 4. Yazma: genelde DB önce

- Yazma: çoğunlukla **DB güncellenir**, cache ayrı ele alınır (silme / güncelleme / TTL).  
- Bu, **Write-Through** değildir: Write-Through'da yazma hem DB hem cache'e **atomik veya eşzamanlı** gider.

| Desen | Kim DB'ye gider? | Miss'te kim doldurur? |
|--------|------------------|------------------------|
| **Cache-aside** | Uygulama | Uygulama |
| **Read-through** | Cache (proxy) | Cache |
| **Write-through** | Uygulama → cache + DB birlikte | Cache yazar |

### 5. Eviction ve kapasite

Cache sınırlıdır. **LRU**, **LFU**, **TTL** gibi politikalar eski veya az kullanılan kayıtları temizler. Level 1'de ürün kayıtları için TTL kullanılır (`level1:product:{id}`).

### 6. Tutarlılık ve süre sonu

Cache ile DB arasında **eventual consistency** beklenir. TTL, manuel **evict** veya write sonrası **invalidation** ile stale veri riski yönetilir.

### 7. Performans ve ölçek

Amaç: sık okunan veriyi bellekten servis ederek latency ve DB yükünü düşürmek. Trafik arttıkça cache katmanı okuma yükünü absorbe eder.

---

## Bu repoda nasıl görülür?

Level 1'de beklenen log sırası (ör. `product id=1`):

1. `GET /api/products/1` → `[CACHE MISS]` → `[DB QUERY EXECUTED]` → `[CACHE PUT]`
2. `GET /api/products/1` → `[CACHE HIT]` (DB logu yok)
3. `DELETE /api/admin/cache/products/1` → `[CACHE EVICT]`
4. `GET /api/products/1` → miss + DB tekrar

İlgili kod: `ProductCacheAsideService`, `ProductRedisCache`.

---

## Cache-aside'ın bilinen riskleri (kısa)

Bu modülde kasıtlı olarak çözülmemiş; diğer level'larda işlenir:

| Risk | Kısa açıklama | Bu repoda |
|------|----------------|-----------|
| **Stale cache** | DB güncellendi, cache eski kaldı | Level 2 |
| **Cache stampede** | Aynı key için çok sayıda eşzamanlı miss → DB'ye yığılma | Level 3 |
| **Hot key** | Tek key'e aşırı trafik | Level 4 |
| **Cache penetration** | Olmayan key'ler sürekli DB'ye gider | İleri seviye |

---

## Spring `@Cacheable` ile ilişki

`@EnableCaching` + `@Cacheable` kullanıldığında da **özde cache-aside** uygulanır (proxy: cache bak → miss'te metot/DB → cache'e yaz). Ancak hit/miss adımları framework arkasında kalır; bu lab gibi adım adım log ve `servedFrom` alanı için **manuel implementasyon** daha öğreticidir.

---

## Özet

**Cache-aside:** Uygulama okumada önce cache'e bakar; miss'te DB'den yükler, cache'i doldurur ve veriyi kullanır. Hızlı erişim ve düşük DB yükü sağlar; tutarlılık, invalidation ve eşzamanlılık **uygulama sorumluluğundadır**. Level 1, bu akışı Redis + PostgreSQL üzerinde gözlemlenebilir şekilde sunar.

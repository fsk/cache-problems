# Level 2 — Cache Invalidation (Stale Cache)

Port: **8102** · Redis: **6381** · PostgreSQL: **5434**

---

## 1. Problemin kendisi

DB’de veri güncellenir ama **cache invalidate edilmez**. Sonraki okumalar eski (stale) değeri döner.

Kısa özet: Write DB’ye gider, read cache’ten — **consistency kopar**.

---

## 2. Gözlemlenmesi gereken problem

| Ne izle | Beklenen davranış |
|---------|-------------------|
| Log | `[STALE CACHE DETECTED]` veya hit sonrası DB ile uyumsuzluk |
| Log | Update sonrası hâlâ `[CACHE HIT]` (eski payload) |
| API | `PUT` fiyat değiştirir, `GET` eski fiyat |
| Risk | Yanlış fiyat/stok → finansal kayıp |

Update path cache’e dokunmaz; read path cache hit alır.

---

## 3. Nasıl kod içerisinde reproduce edilir

| Adım | Nerede | Ne yapar |
|------|--------|----------|
| 1 | `GET /api/products/{id}` | Ürün cache’e yazılır |
| 2 | `PUT /api/products/{id}` | Sadece JPA `save` — **cache delete yok** |
| 3 | `GET` tekrar | Stale değer, `[CACHE HIT]` |
| 4 | `ProductService.update` | Bilinçli olarak `labCache.delete` çağrılmaz |

**HTTP:** `http/stale-cache.http`  
**Load test:** `load-test-scripts/level-2/stale_read_test.py`

```bash
# Örnek akış (level hazır olunca)
# GET → PUT (fiyat) → GET (eski fiyat döner)
```

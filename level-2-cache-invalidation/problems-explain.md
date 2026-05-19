# Level 2 — Cache Invalidation (Stale Cache) Problem

## 1. Problem nedir?

Yazma (update) **sadece PostgreSQL**'e gider; Redis'teki cache kaydı **silinmez veya güncellenmez**. Okuma hâlâ cache'ten **hit** alır ve **eski (stale)** veriyi döner.

## 2. Neden oluşur?

Cache-aside'da yazma path'i cache'i bilinçli yönetmelidir (`@CacheEvict`, `cache.delete`, write-through vb.). Bu level'da `PUT` sonrası **invalidation yok** — read/write path'leri ayrışır ve tutarlılık kopar.

## 3. Production'da nasıl gözükür?

- Kullanıcı fiyat/stok günceller, API eski değer gösterir
- Admin paneli doğru, müşteri uygulaması yanlış (veya tam tersi)
- Uzun TTL ile stale veri saatlerce servis edilir
- Log'da update sonrası hâlâ `[CACHE HIT]` / Spring cache TRACE hit

## 4. Hangi metric'ler etkilenir?

- Cache hit ratio **yanıltıcı şekilde yüksek** (stale hit sayılır)
- Müşteri şikayeti / iş kuralı ihlali (fiyat, stok)
- Veri tutarlılığı SLA ihlali (eventual consistency süresi kontrolsüz)

## 5. Nasıl reproduce edilir?

Bkz. [README.md](README.md) bölüm 3.

## 6. Hangi `.http` dosyası?

`http/stale-cache.http`

## 7. Seed script

`cd python && python seed_products.py --count 100 --truncate`

## 8. Beklenen kötü davranış

1. `GET` → cache dolar  
2. `PUT` → DB güncellenir, `[CACHE NOT INVALIDATED]`  
3. `GET` → `[STALE CACHE DETECTED]`, `source: CACHE`, **eski fiyat**

## 9. Redis neden yetersiz kalır?

Redis doğru çalışıyor — sorun Redis değil, **invalidation eksikliği**. Cache key geçerli olduğu sürece Redis “doğru” eski cevabı vermeye devam eder.

## 10. PostgreSQL neden overload olur?

Bu level'da asıl risk DB overload değil **yanlış veri**. DB güncellenir ama okuma trafiği cache'e gittiği için DB az sorgulanır; tutarsızlık iş etkisine yol açar.

## 11. Gerçek hayat örnekleri

- Sepette indirimli fiyat gösterilir, ödeme sayfasında eski fiyat  
- Stok 0 iken “stokta var” (cache'te kalan kayıt)  
- Profil bio güncellendi, arkadaşlar eski metni görüyor

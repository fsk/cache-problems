# Level 1 — Cache Aside Problem

## 1. Problem nedir?

Cache Aside: uygulama önce Redis’e bakar, yoksa PostgreSQL’den okur ve sonucu cache’e yazar.

## 2. Neden oluşur?

Her cache miss doğrudan DB’ye gider. TTL dolunca veya key silinince trafik tekrar DB’ye düşer.

## 3. Production’da nasıl gözükür?

- p99 latency miss’lerde yükselir
- DB connection pool kullanımı dalgalanır
- Redis hit ratio düşük kalır

## 4. Hangi metric’ler etkilenir?

- `cache_hit_ratio`
- DB QPS, connection count
- API latency p95/p99

## 5. Nasıl reproduce edilir?

Bkz. [README.md](README.md) bölüm 3.

## 6. Hangi `.http` dosyası?

`http/problem-scenario.http`

## 7. Seed script

`cd python && python seed_products.py --count 1000`

## 8. Beklenen kötü davranış

Miss başına DB query; TTL/evict sonrası tekrarlayan DB yükü.

## 9. Redis neden yetersiz kalır?

Redis sadece okumayı hızlandırır; miss ve expiry anlarında DB hâlâ source of truth yükü taşır.

## 10. PostgreSQL neden overload olur?

Koruma yok — her miss = SELECT (+ bu level’da kasıtlı 150ms latency).

## 11. Gerçek hayat örnekleri

E-ticaret ürün detay, haber makalesi, profil sayfası — TTL sonrası flash traffic.

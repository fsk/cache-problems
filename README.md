# cache-problems — Distributed Systems Lab

Production-grade düşünce yapısına yakın, **kasıtlı olarak kırık** bir distributed systems laboratuvarı.

## Klasör yapısı

```
cache-problems/              ← BASE MODULE (Redis, JPA, cache abstraction, src/)
├── src/
├── docker/
├── lab-reactor/             ← Maven reactor (tüm modülleri derler)
├── level-1-cache-aside/       (java/ + python/)
├── level-2-cache-invalidation/
├── …
└── load-test-scripts/
```

**`cache-problems` klasörünün kendisi base modüldür.** Alt klasörler (`level-*`, `common-library`) bunun üzerine inşa edilir.

## Derleme

```bash
# Tüm modülleri derle (önerilen)
cd lab-reactor && mvn clean install -DskipTests

# Sadece base modül
mvn clean install -DskipTests
```

## Modüller

| Modül | Konum | Durum |
|-------|--------|--------|
| **cache-problems** (base) | repo kökü (`src/`) | ✅ |
| `level-1-cache-aside` | `level-1-cache-aside/` | ✅ |
| `level-2` … `level-8` | `level-N-*/` | 🔜 |

Detaylı mimari: [ARCHITECTURE.md](ARCHITECTURE.md)

## Level README formatı

Her `level-N/README.md` üç bölümden oluşur:

1. **Problemin kendisi** — ne olduğu, kısa açıklama  
2. **Gözlemlenmesi gerekenler** — log, metrik, davranış  
3. **Reproduce** — kodda nerede ve nasıl tetiklenir  

## Gereksinimler

- Java 21, Maven 3.9+, Docker, Python 3.11+

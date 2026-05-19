# Level 1 — Python araçları

PostgreSQL'e örnek ürün verisi yükler. Java uygulamasından bağımsızdır.

```bash
cd level-1-cache-aside
docker compose up -d

cd python
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python seed_products.py --count 1000 --truncate
```

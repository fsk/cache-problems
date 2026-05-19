# Level 2 — Python

```bash
cd level-2-cache-invalidation
docker compose up -d

cd python
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python seed_products.py --count 100 --truncate
```

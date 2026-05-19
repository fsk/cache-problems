# Load Test Scripts

Her level için Python scriptleri burada veya `level-N/scripts/` altında yer alacak.

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

Level hazır oldukça:

```bash
python level-1/cache_aside_storm.py --users 500 --base-url http://localhost:8101
```

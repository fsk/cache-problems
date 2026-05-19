#!/usr/bin/env python3
"""
Seed products into level-1 PostgreSQL.

Usage:
  cd level-1-cache-aside/python
  python seed_products.py --count 10000
  python seed_products.py --count 1000 --host localhost --port 5433
"""

from __future__ import annotations

import argparse
import random
import string
import sys
from datetime import datetime, timezone

try:
    import psycopg2
    from psycopg2.extras import execute_batch
except ImportError:
    print("Install dependencies: pip install psycopg2-binary", file=sys.stderr)
    sys.exit(1)


def random_sku() -> str:
    return "SKU-" + "".join(random.choices(string.ascii_uppercase + string.digits, k=8))


def popularity_score(index: int, total: int) -> int:
    """Skew: first ~1% products are very hot (for future hot-key levels)."""
    if index <= max(1, total // 100):
        return random.randint(80, 100)
    if index <= max(1, total // 10):
        return random.randint(30, 79)
    return random.randint(0, 29)


def build_products(count: int) -> list[tuple]:
    now = datetime.now(timezone.utc)
    rows = []
    for i in range(1, count + 1):
        rows.append(
            (
                i,
                f"Product {i}",
                random_sku(),
                round(random.uniform(5.0, 5000.0), 2),
                popularity_score(i, count),
                now,
            )
        )
    return rows


def main() -> None:
    parser = argparse.ArgumentParser(description="Seed level-1 products table")
    parser.add_argument("--count", type=int, default=1000, help="Number of products (default: 1000)")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=5433)
    parser.add_argument("--db", default="lab")
    parser.add_argument("--user", default="lab")
    parser.add_argument("--password", default="lab")
    parser.add_argument("--truncate", action="store_true", help="Truncate products before insert")
    args = parser.parse_args()

    if args.count < 1:
        print("--count must be >= 1", file=sys.stderr)
        sys.exit(1)

    conn = psycopg2.connect(
        host=args.host,
        port=args.port,
        dbname=args.db,
        user=args.user,
        password=args.password,
    )
    conn.autocommit = False

    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    sku VARCHAR(64) NOT NULL,
                    price NUMERIC(12, 2) NOT NULL,
                    popularity_score INT NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """
            )
            if args.truncate:
                cur.execute("TRUNCATE TABLE products")
                print("Truncated products table")

            products = build_products(args.count)
            execute_batch(
                cur,
                """
                INSERT INTO products (id, name, sku, price, popularity_score, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    sku = EXCLUDED.sku,
                    price = EXCLUDED.price,
                    popularity_score = EXCLUDED.popularity_score,
                    updated_at = EXCLUDED.updated_at
                """,
                products,
                page_size=500,
            )
        conn.commit()
        print(f"Seeded {args.count} products into {args.host}:{args.port}/{args.db}")
        print("Try: GET http://localhost:8101/api/products/1")
    except Exception as exc:
        conn.rollback()
        print(f"Seed failed: {exc}", file=sys.stderr)
        sys.exit(1)
    finally:
        conn.close()


if __name__ == "__main__":
    main()

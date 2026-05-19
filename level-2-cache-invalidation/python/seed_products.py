#!/usr/bin/env python3
"""Seed level-2 PostgreSQL (port 5434)."""

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
    print("pip install -r requirements.txt", file=sys.stderr)
    sys.exit(1)


def random_sku() -> str:
    return "SKU-" + "".join(random.choices(string.ascii_uppercase + string.digits, k=8))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--count", type=int, default=100)
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=5434)
    parser.add_argument("--truncate", action="store_true")
    args = parser.parse_args()

    conn = psycopg2.connect(
        host=args.host, port=args.port, dbname="lab", user="lab", password="lab"
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    sku VARCHAR(64) NOT NULL,
                    price NUMERIC(12, 2) NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """
            )
            if args.truncate:
                cur.execute("TRUNCATE TABLE products")

            now = datetime.now(timezone.utc)
            rows = [
                (i, f"Product {i}", random_sku(), round(random.uniform(10.0, 999.0), 2), now)
                for i in range(1, args.count + 1)
            ]
            execute_batch(
                cur,
                """
                INSERT INTO products (id, name, sku, price, updated_at)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name, sku = EXCLUDED.sku,
                    price = EXCLUDED.price, updated_at = EXCLUDED.updated_at
                """,
                rows,
                page_size=200,
            )
        conn.commit()
        print(f"Seeded {args.count} products → {args.host}:{args.port}")
        print("Try: GET http://localhost:8102/api/products/1")
    except Exception as exc:
        conn.rollback()
        print(f"Seed failed: {exc}", file=sys.stderr)
        sys.exit(1)
    finally:
        conn.close()


if __name__ == "__main__":
    main()

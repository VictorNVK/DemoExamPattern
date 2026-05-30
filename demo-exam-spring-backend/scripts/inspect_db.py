import sqlite3
from pathlib import Path

db = Path(__file__).resolve().parent.parent / "storage" / "database" / "book_store_exam.db"
conn = sqlite3.connect(db)
cur = conn.cursor()
cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
print("tables:", [r[0] for r in cur.fetchall()])
for table in ["users", "products", "orders"]:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {table}")
        print(table, "count:", cur.fetchone()[0])
        cur.execute(f"PRAGMA table_info({table})")
        print(table, "cols:", [c[1] for c in cur.fetchall()])
    except Exception as exc:
        print(table, exc)
conn.close()

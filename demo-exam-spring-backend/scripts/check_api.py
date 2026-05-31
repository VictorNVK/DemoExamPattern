import json
import re
import urllib.request
from pathlib import Path

PROPS = Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "application.properties"


def read_backend_port() -> int:
    if not PROPS.exists():
        return 8082
    content = PROPS.read_text(encoding="utf-8")
    match = re.search(r"^server\.port\s*=\s*(\d+)", content, re.MULTILINE)
    return int(match.group(1)) if match else 8082


base = f"http://localhost:{read_backend_port()}"


def post(path: str, payload: dict) -> dict:
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        f"{base}{path}",
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def get(path: str, token: str | None = None) -> object:
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(f"{base}{path}", headers=headers, method="GET")
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


print("backend:", base)

login = post("/api/auth/login", {"login": "admin", "password": "admin"})
print("login:", login)

products = get("/api/products")
print("products:", len(products))

manager_login = post("/api/auth/login", {"login": "manager", "password": "manager"})
orders = get("/api/orders", manager_login["token"])
print("orders:", len(orders))

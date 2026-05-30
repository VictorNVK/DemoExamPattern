import json
import urllib.request

base = "http://localhost:8080"


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


login = post("/api/auth/login", {"login": "admin", "password": "admin"})
print("login:", login)

products = get("/api/products")
print("products:", len(products))

manager_login = post("/api/auth/login", {"login": "manager", "password": "manager"})
orders = get("/api/orders", manager_login["token"])
print("orders:", len(orders))

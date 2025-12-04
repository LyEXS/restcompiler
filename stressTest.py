# stress_test_correct.py
import asyncio
import json
from datetime import datetime

import aiohttp

API_URL = "http://localhost:8080/compile"
CONCURRENT_REQUESTS = 50
TOTAL_REQUESTS = 1000

# Codes C valides qui devraient passer
payloads = [
    # Sans include
    {"code": "int main() { return 0; }"},
    {"code": "int main() { int a=5,b=3; return a+b; }"},
    {"code": "int main() { int i; for(i=0;i<10;i++){} return 0; }"},
    # Avec stdio.h (si votre validateur l'autorise maintenant)
    {"code": "#include <stdio.h>\nint main() { return 0; }"},
    {"code": '#include <stdio.h>\nint main() { printf("test\\n"); return 0; }'},
]


async def make_request(session, request_id):
    payload = payloads[request_id % len(payloads)]

    try:
        async with session.post(API_URL, json=payload, timeout=10) as response:
            status = response.status

            if status == 200:
                data = await response.json()
                return {
                    "id": request_id,
                    "status": status,
                    "success": data.get("compilation", {}).get("status", False),
                    "output": data.get("compilation", {}).get("output", "")[:100],
                }
            else:
                error_text = await response.text()
                return {
                    "id": request_id,
                    "status": status,
                    "error": f"HTTP {status}",
                    "response": error_text[:200],
                }

    except Exception as e:
        return {"id": request_id, "status": 0, "error": str(e)}


async def run_stress_test():
    connector = aiohttp.TCPConnector(limit=CONCURRENT_REQUESTS)

    async with aiohttp.ClientSession(connector=connector) as session:
        tasks = []
        for i in range(TOTAL_REQUESTS):
            task = asyncio.create_task(make_request(session, i))
            tasks.append(task)

        return await asyncio.gather(*tasks)


def analyze_results(results):
    total = len(results)
    successful = sum(1 for r in results if r.get("status") == 200 and r.get("success"))
    failed_http = sum(1 for r in results if r.get("status") != 200)
    errors = sum(1 for r in results if r.get("error") and r.get("status") == 0)

    print(f"\n=== Résultats ===")
    print(f"Total requêtes: {total}")
    print(f"Compilations réussies: {successful} ({successful / total * 100:.1f}%)")
    print(f"Échecs HTTP: {failed_http}")
    print(f"Erreurs réseau: {errors}")

    # Afficher les premiers succès
    successes = [r for r in results if r.get("success")]
    if successes:
        print(f"\nExemple de succès (#{successes[0]['id']}):")
        print(f"  Output: {successes[0].get('output', 'N/A')}")

    # Afficher les premières erreurs
    failures = [r for r in results if not r.get("success") and r.get("status") != 0]
    if failures:
        print(f"\nExemple d'échec (#{failures[0]['id']}):")
        print(f"  Status: {failures[0].get('status')}")
        print(f"  Response: {failures[0].get('response', 'N/A')}")


if __name__ == "__main__":
    print(f"Stress test avec codes valides...")
    results = asyncio.run(run_stress_test())
    analyze_results(results)

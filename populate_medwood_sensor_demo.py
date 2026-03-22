
import requests
import random
import time
import os
from datetime import datetime, timezone

# ✅ Correct production URL
BASE_URL = os.getenv(
    "API_BASE_URL",
    "https://narrate-webapp-tcxs.onrender.com"
)

# ✅ Correct servlet endpoint
CREATE_ENDPOINT = f"{BASE_URL}/AddIndividualServlet2"

TIMEOUT = 15
RETRIES = 3


def now_timestamp():
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def post_with_retry(payload):
    for attempt in range(RETRIES):
        try:
            r = requests.post(CREATE_ENDPOINT, json=payload, timeout=TIMEOUT)

            if r.status_code in (200, 201):
                return True, r.text

            return False, r.text

        except Exception as e:
            if attempt < RETRIES - 1:
                time.sleep(1)
            else:
                return False, str(e)


def create_instance(class_name, name, data=None, obj=None):
    payload = {
        "className": class_name,   # ✅ REQUIRED by servlet
        "individualName": name,
        "dataProperties": [],
        "objectProperties": []
    }

    if data:
        payload["dataProperties"] = [
            {"property": k, "value": str(v)}
            for k, v in data.items()
        ]

    if obj:
        payload["objectProperties"] = [
            {"property": k, "value": v}
            for k, v in obj.items()
        ]

    success, response = post_with_retry(payload)

    if success:
        print(f"✅ Created: {name}")
    else:
        print(f"❌ Error: {name} -> {response}")


# Example test
create_instance(
    "Factory",
    "TEST_FACTORY",
    {
        "factoryID": "F999",
        "factoryName": "Test Factory"
    }
)


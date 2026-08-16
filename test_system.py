import requests
import random
import time

BASE_URL = "http://localhost:8080"

FIRST_NAMES = ["David", "Sara", "Yossi", "Rachel", "Omer", "Michal", "Chen"]
LAST_NAMES = ["Cohen", "Levi", "Mizrachi", "Peretz", "Katz", "Golan"]
ROLES_LIST = ["Admin", "MANAGER", "Employee", "Viewer"]

BASE_TIME_ID = int(time.time())

TEST_EMP_ID = BASE_TIME_ID
VIEWER_EMP_ID = BASE_TIME_ID + 1

def test_create_valid_employee():
    random_role = random.choice(ROLES_LIST)
    data = {
        "id_number": TEST_EMP_ID,
        "first_name": random.choice(FIRST_NAMES),
        "last_name": random.choice(LAST_NAMES),
        "role": {"role": random_role, "salary": random.randint(10000, 30000), "desc": "QA Test Role"},
        "startingDate": 2024
    }
    response = requests.post(f"{BASE_URL}/api/employees", json=data)
    assert response.status_code == 201

def test_prevent_duplicate_employee():
    data = {
        "id_number": TEST_EMP_ID,
        "first_name": "Duplicate",
        "last_name": "Hacker",
        "role": {"role": "Employee", "salary": 10000, "desc": "Worker"},
        "startingDate": 2024
    }
    response = requests.post(f"{BASE_URL}/api/employees", json=data)
    assert response.status_code == 409

def test_get_all_employees():
    response = requests.get(f"{BASE_URL}/api/employees")
    assert response.status_code == 200
    data = response.json()

    if isinstance(data, list):
        assert len(data) > 0
    else:
        assert "employees" in data
        assert len(data["employees"]) > 0

def test_viewer_cannot_create_task():
    viewer_data = {
        "id_number": VIEWER_EMP_ID,
        "first_name": random.choice(FIRST_NAMES),
        "last_name": "TheViewer",
        "role": {"role": "Viewer", "salary": 5000, "desc": "Just looking"},
        "startingDate": 2024
    }
    response_viewer = requests.post(f"{BASE_URL}/api/employees", json=viewer_data)
    assert response_viewer.status_code == 201

    time.sleep(0.5)

    task_data = {
        "task": "Hacker Task",
        "status": "To Do",
        "started": 2024,
        "creator": {"id_number": VIEWER_EMP_ID},
        "assignedEmployee": {"id_number": TEST_EMP_ID}
    }
    response = requests.post(f"{BASE_URL}/api/tasks", json=task_data)

    assert response.status_code == 400
    assert "premission" in response.text
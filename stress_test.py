import requests
import time
import random

BASE_URL = "http://localhost:8080"

ROLES_LIST = ["Admin", "MANAGER", "Employee", "Viewer"]

def create_employee(employee_id):
    random_role = random.choice(ROLES_LIST)
    random_salary = round(random.randint(8000, 35000))
    random_year = random.randint(2015, 2024)

    new_employee_data = {
        "id_number": employee_id,
        "first_name": "QA_Bot",
        "last_name": f"Worker_{employee_id}",
        "role": {
            "role": random_role,
            "salary": random_salary,
            "desc": f"Automated {random_role}"
        },
        "startingDate": random_year
    }

    try:
        response = requests.post(f"{BASE_URL}/api/employees", json=new_employee_data)
        return response.status_code
    except requests.exceptions.ConnectionError:
        return 0

def run_stress_test(num_requests):
    print(f" Starting STRESS TEST: Sending {num_requests} employees to the server...")

    start_time = time.time()

    success_count = 0
    conflict_count = 0
    error_count = 0

    for i in range(3000, 3000 + num_requests):
        status = create_employee(i)

        if status in [200, 201]:
            success_count += 1
        elif status == 409:
            conflict_count += 1
        else:
            error_count += 1

        if (i - 3000 + 1) % 100 == 0:
            print(f" Progress: {i - 3000 + 1} / {num_requests} requests sent...")

    end_time = time.time()
    total_time = end_time - start_time

    print("\n🏁 --- STRESS TEST RESULTS --- 🏁")
    print(f"⏱️ Total Time: {total_time:.2f} seconds")
    print(f"✅ Successful Creations (201): {success_count}")
    print(f"⚠️ Conflicts (409): {conflict_count}")
    print(f"❌ Errors/Fails: {error_count}")

    if total_time > 0:
        print(f"⚡ Server processed: {num_requests / total_time:.2f} requests per second")


if __name__ == "__main__":
    run_stress_test(500)
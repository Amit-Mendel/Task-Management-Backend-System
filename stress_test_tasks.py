import requests
import time
import random

BASE_URL = "http://localhost:8080"

STATUS_LIST = ["To Do", "In Progress", "Review", "Done"]


def create_task(task_index):
    creator_id = random.randint(3000, 3499)
    assignee_id = random.randint(3000, 3499)

    new_task_data = {
        "task": f"Automated Task {task_index} - Check System",
        "status": random.choice(STATUS_LIST),
        "started": 2024,
        "creator": {
            "id_number": creator_id
        },
        "assignedEmployee": {
            "id_number": assignee_id
        }
    }

    try:
        response = requests.post(f"{BASE_URL}/api/tasks", json=new_task_data)
        return response.status_code
    except requests.exceptions.ConnectionError:
        return 0


def run_tasks_stress_test(num_requests):
    print(f" Starting TASKS STRESS TEST: Sending {num_requests} tasks to the server...")

    start_time = time.time()

    success_count = 0
    conflict_count = 0
    error_count = 0

    for i in range(1, num_requests + 1):
        status = create_task(i)

        if status in [200, 201]:
            success_count += 1
        elif status == 409:
            conflict_count += 1
        else:
            error_count += 1

        # הדפסת התקדמות כל 100 משימות
        if i % 100 == 0:
            print(f" Progress: {i} / {num_requests} tasks sent...")

    end_time = time.time()
    total_time = end_time - start_time

    print("\n --- TASKS STRESS TEST RESULTS --- ")
    print(f" Total Time: {total_time:.2f} seconds")
    print(f" Successful Tasks Created (201): {success_count}")
    print(f" Conflicts (409): {conflict_count}")
    print(f" Errors/Fails: {error_count}")

    if total_time > 0:
        print(f" Server processed: {num_requests / total_time:.2f} tasks per second")


if __name__ == "__main__":
    run_tasks_stress_test(1000)
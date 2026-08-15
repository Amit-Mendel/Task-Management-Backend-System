package Management;
import java.util.TreeMap;
import java.util.ArrayList;

public class TaskManager {
	Permissions permission;
	private TreeMap<Integer, Employee> employeesTree;
	private ArrayList<Task> list;
	
	public TaskManager() {
		this.permission = new Permissions();
		this.employeesTree = new TreeMap<>();
		this.list = new ArrayList<>();
	}

	public void updateTaskStatus(Task task, Employee employee, String status) {
		if(permission.updateTaskStatus(task, employee)) {
			if(status.equals("Done")) {
				LinkedListQueue Queue = task.getSubTasksQueue();
				while(!Queue.isEmpty()) {
					Node First = Queue.peek();
					Task subTask = First.getTask();
					if(!subTask.getStatus().equals("Done")) {
						throw new DependencyNotMetException("Cannot complete task: Subtask " + subTask.getTaskID() + " is not Done yet.");
					}
					Queue.dequeue();
				}
			}
		task.setStatus(status);
		System.out.println("Task " + task.getTaskID() + " :Status updated");
		} else {
			throw new UnauthorizedActionException("You dont have premission to do this action.");
		}
	}
	
	public void assignEmployeeToTask(Task task, Employee employee) {
		if(permission.Can_assignEmployeeToTask(task, employee)) {
			task.setAssignedEmployee(employee);
			System.out.println(employee + " was assigned to task " + task.getTaskID());
		}
		else {
			throw new UnauthorizedActionException("You dont have premission to do this action.");
		}
	}
	
	public void DeleteTask(Task task, Employee employee) {
		if(permission.Can_delete_task(employee, task)) {
			System.out.println("Task " + task.getTaskID() + " Deleted by " + employee.getFullName());
			list.remove(task);
		}
		else {
			throw new UnauthorizedActionException("You dont have premission to do this action.");
		}
	}
	
	public void addTask(Task task, Employee employee) {
		if(permission.Can_add_task(employee, task)) {
			list.add(task);
			System.out.println(employee +" created task " + task.getTaskID());
		}
		else {
			throw new UnauthorizedActionException("You dont have premission to do this action.");
		}
	}
	
	public void employeeTasks(Employee employee) {
		if(permission.getEmployeeTasks(employee)) {
			boolean found = false;
			for (Task t : list) {
				if(t.getAssignedEmployee().getID_number() == employee.getID_number()) {
					System.out.println("Task: " + t.getTaskID());
					found = true;
				}
			}
			if(!found) {
				System.out.println("No tasks found for " + employee);
			}
		}
	}
	
	public void addSubTask(Task task, Task subTask, Employee employee) {
		if(permission.Can_addSubTask(task, employee)) {
			task.addSubTask(subTask, employee);;
			System.out.println("SubTask add to " + task.getTaskID());
		} else {
			throw new UnauthorizedActionException("You dont have premission to do this action.");
		}
	}
	
	public void addEmployee(Employee employee) {
		DBTest.saveEmployee(employee);
		this.employeesTree.put(employee.getID_number(), employee);
		System.out.println("Employee added");
	}
	
	public void loadEmployeeToMemory(Employee employee) {
	    employeesTree.put(employee.getID_number(), employee);
	}
	
	public Employee getEmployeeByID(int id) {
		Employee foundEmployee = employeesTree.get(id);
		
		if(foundEmployee == null) {
			System.out.println("Employee with id: " + id + " not Found.");
		}
		return foundEmployee;
	}
	
	public ArrayList<Employee> getAllEmployeesList() {
		return new ArrayList<>(this.employeesTree.values());
	}
	
}

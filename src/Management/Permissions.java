package Management;

public class Permissions {
	
	public Permissions() {
		
	}

	public boolean Can_delete_task(Employee employee, Task task ) {
		if(employee.getRole().getType() != Roles.Viewer && 
				employee.getRole().getType() != Roles.Employee) {
			return true;
		} return false;
	}
	
	public boolean Can_add_task(Employee employee, Task task) {
		if(employee.getRole().getType() != Roles.Viewer)
			return true;
		return false;
	}
	
	public boolean Can_assignEmployeeToTask(Task task, Employee employee) {
		if(employee.getRole().getType() == Roles.Admin || employee.getRole().getType() == Roles.MANAGER)
			return true;
		return false;
	}
	
	public boolean updateTaskStatus(Task task, Employee employee) {
		if((employee.getRole().getType() == Roles.Admin || 
			       employee.getRole().getType() == Roles.MANAGER || 
			       employee.getID_number() == task.getCreator().getID_number() || 
			       employee.getID_number() == task.getAssignedEmployee().getID_number())) {
			        return true;
		} return false;
	}
	
	public boolean getEmployeeTasks(Employee employee) {
		if(employee.getRole().getType() == Roles.Admin || employee.getRole().getType() == Roles.MANAGER) {
			return true;
		} return false;
	}
	
	public boolean Can_addSubTask(Task task, Employee employee) {
		if(employee.getRole().getType() == Roles.Admin || employee.getRole().getType() == Roles.MANAGER) {
			return true;
		} return false;
	}
}

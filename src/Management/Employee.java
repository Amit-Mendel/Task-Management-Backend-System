package Management;

public class Employee {
	private String first_name;
	private String last_name;
	private int id_number;
	private int working_since;
	private Role role;
	
	public Employee(String first_name, String last_name, int id_number, int working_since, Role role) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.id_number = id_number;
		this.working_since = working_since;
		this.role = role;
	}	
	
	public String getFullName() {
		return first_name + ' ' + last_name;
	}
	
	public int getID_number() {
		return id_number;
	}
	
	public int getStartingDate() {
		return working_since;
	}
	
	public Role getRole() {
		return role;
	}

	@Override
	public String toString() {
		return "Employee [first_name=" + first_name + ", last_name=" + last_name + ", id_number=" + id_number
				+ ", working_since=" + working_since + "]";
	}


	
	
}

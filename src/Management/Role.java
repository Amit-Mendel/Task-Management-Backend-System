package Management;

public class Role {
	private Roles role;
	private int salary;
	private String desc;
	
	
	public Role(Roles role,int salary, String desc) {
		super();
		this.role = role;
		this.salary = salary;
		this.desc = desc;
	}
	
	public Roles getType() {
		return role;
	}
	
	public int getSalary() {
		return salary;
	}
	
	public String getDesc() {
		return desc;
	}
}

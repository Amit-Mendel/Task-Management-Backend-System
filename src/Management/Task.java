package Management;

public class Task {
	private int taskID;
	private String task;
	private String status;
	private int started;
	private Employee creator;
	private Employee assignedEmployee;
	private LinkedListQueue subTasksQueue;
	private Task parentTask;
	private int tempParentId;

	public Task(int taskID, String task, String status, int started, Employee creator, Employee assignedEmployee) {
		super();
		this.taskID = taskID;
		this.task = task;
		this.status = status;
		this.started = started;
		this.creator = creator;
		this.assignedEmployee = assignedEmployee;
		this.subTasksQueue = new LinkedListQueue();
		this.parentTask = null;
	}
	
	public int getTaskID() {
		return taskID;
	}
	
	public void setTaskID(int id) {
		this.taskID = id;
	}
	
	public String setStatus(String status) {
		this.status = status;
		return status;
	}
	
	public String getTask() {
		return task;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getStarted() {
		return started;
	}
	
	public Employee getCreator() {
		return creator;
	}
	
	public void setCreator(Employee creator) {
	    this.creator = creator;
	}
	
	public Employee setAssignedEmployee(Employee employee) {
		this.assignedEmployee = employee;
		return assignedEmployee;
	}
	
	public Employee getAssignedEmployee() {
		return assignedEmployee;
	}
	
	public void addSubTask(Task task, Employee employee) {
		subTasksQueue.enqueue(task, employee);
		System.out.println("SubTask added");
	}
	
	public LinkedListQueue getSubTasksQueue() {
		return this.subTasksQueue;
	}
	
	public Task getParentTask() {
		return parentTask;
	}

	public void setParentTask(Task parentTask) {
		this.parentTask = parentTask;
	}
	
	public int getTempParentId() {
		return tempParentId;
	}

	public void setTempParentId(int tempParentId) {
		this.tempParentId = tempParentId;
	}
	
}

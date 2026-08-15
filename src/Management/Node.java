package Management;

public class Node {
	private Node next;
	private Employee name;
	private Task task;
	
	public Node() {
		super();
		this.next = null;
	}
	
	public Node(Employee name, Task task) {
		this.name = name;
		this.task = task;
		this.next = null;
	}
	
	public Node getNext() {
		return next;
	}
	
	public void setNext(Node next) {
		this.next = next;
	}
	
	public Task getTask() {
		return task;
	}
	
	public Employee getEmployee() {
		return name;
	}
}

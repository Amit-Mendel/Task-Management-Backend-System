package Management;

public class LinkedListQueue {
	private Node front, back;
	private int count;
	
	public LinkedListQueue() {
		front=back=null;
		count = 0;
	}
	
	public void enqueue(Task subTask, Employee name) {
		Node newLink = new Node(name, subTask);
		
		if(isEmpty()) {
			front = newLink;
		} else {
			back.setNext(newLink);
		}
		
		back = newLink;
		count++;
	}
	
	public boolean isEmpty() {
		return front == null;
	}
	
	public Node dequeue() {
		if (isEmpty()) {
			throw new IllegalStateException("Cannot dequeue from an empty queue.");
		}
		
		Node result = front;
		front = front.getNext();
		count--;
		
		if(isEmpty()) {
			back = null;
		}
		return result;
	}
	
	public Node peek() {
		if(isEmpty())
			return null;
		return front;
	}
	
	public int size() {
		return count;
	}
}

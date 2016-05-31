package jia;

public class Task {
	public int id;
	public String operation;
	public String destination;
	public String item;
	public int quantity;
	public String responsible;
	public TaskStatus taskStatus;
	
	public Task(int id, 
				String operation, 
				String destination, 
				String item, 
				int quantity,
				String responsible,
				TaskStatus taskStatus)
	{
		this.id = id;
		this.operation = operation;
		this.destination = destination;
		this.item = item;
		this.quantity = quantity;
		this.responsible = responsible;
		this.taskStatus = taskStatus;
	}
}

// Internal action code for project massimJacamoProject

package jia;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.*;
import massimJacamoProject.GlobalPercepts;

public class controller extends DefaultInternalAction {

	/*public enum JobStatus { Evaluation, Accepted, Execution, Completed, Cancelled }
	public enum TaskStatus { Evaluation, Accepted, Assigned, Completed, Cancelled }
	public enum JobType { Auction, Priced }
	
	public static Hashtable<String, GlobalPercepts.Job> auctionJobs;
	public static Hashtable<String, GlobalPercepts.Job> pricedJobs;
	public static Hashtable<String, GlobalPercepts.Shop> shops;
	public static Hashtable<String, JobStatus> jobStatus;
	public static Hashtable<Integer, Task> tasks;
	public static Hashtable<String, List<Integer>> jobTasks;
	public static Hashtable<String, JobType> jobType;
	
	static
	{
		auctionJobs = new Hashtable<String, GlobalPercepts.Job>();
		pricedJobs = new Hashtable<String, GlobalPercepts.Job>();
		shops = new Hashtable<String, GlobalPercepts.Shop>();
		jobStatus = new Hashtable<String, JobStatus>();
		tasks = new Hashtable<Integer, Task>();
		jobTasks = new Hashtable<String, List<Integer>>();
		jobType = new Hashtable<String, JobType>();
	}
	
	public class Task
	{
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
	}*/
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
    	
    	/*
    	if (true)
    	{
    		un.unifies(args[0],  new StringTermImpl(""));
        	un.unifies(args[1],  new NumberTermImpl(-1));
        	un.unifies(args[2],  new StringTermImpl(""));
        	un.unifies(args[3],  new StringTermImpl(""));
        	un.unifies(args[4],  new StringTermImpl(""));
        	un.unifies(args[5],  new NumberTermImpl(0));
        	return un.unifies(args[6],  new NumberTermImpl(0));
    	}
    	*/
    	
    	ControlStructure.shops = GlobalPercepts.getShops();
    	
    	ControlStructure.auctionJobs = GlobalPercepts.getAuctionJobs();
    	ControlStructure.pricedJobs = GlobalPercepts.getPricedJobs();
    	
    	if (ControlStructure.pricedJobs.size() != 0 && ControlStructure.shops.size() != 0)
    	{
	    	Enumeration<String> enumJobKey = ControlStructure.pricedJobs.keys();
	    	
	    	//Check all jobs
	    	while(enumJobKey.hasMoreElements())
	    	{
	    		JobStatus tempJobStatus = JobStatus.Accepted;
	    		String jobKey = enumJobKey.nextElement();
	    		
	    		//Add new job
	    		if (!ControlStructure.jobStatus.containsKey(jobKey))
	    		{
	    			ControlStructure.jobStatus.put(jobKey, JobStatus.Evaluation);
	    			ControlStructure.jobTasks.put(jobKey, new ArrayList<Integer>());
	    			ControlStructure.jobType.put(jobKey, JobType.Priced);
	    		}
	    		
	    		//Check if job should be evaluated
	    		if (ControlStructure.jobStatus.get(jobKey) == JobStatus.Evaluation)
	    		{
		    		//Select job for evaluation
		    		GlobalPercepts.Job job = ControlStructure.pricedJobs.get(jobKey);
		    		
		    		List<String> jobItems = job.ItemNames();
		    		List<Integer> jobQuantities = job.Quantities();
		    		
		    		//Check job requirements (needed items)
		    		for (int i = 0; i < jobItems.size(); i++)
		    		{
		    			//Check if task exists (and get taskKey)
		    			List<Integer> listJobTasks = ControlStructure.jobTasks.get(jobKey);
		    			int taskKey = -1;
		    			for (int j = 0; j < listJobTasks.size() && taskKey == -1; j++)
		    			{
		    				if (ControlStructure.tasks.get(listJobTasks.get(j)).item == jobItems.get(i))
		    				{
		    					//Task found
		    					taskKey = listJobTasks.get(j);
		    				}
		    			}
		    			//If task does not exist
		    			if (taskKey == -1)
		    			{
		    				taskKey = ControlStructure.tasks.size();
		    				//Create new possible task
		    				ControlStructure.tasks.put(taskKey, new Task(taskKey,
		    																null,
	    																	null,
	    																	jobItems.get(i),
	    																	jobQuantities.get(i),
	    																	null,
	    																	TaskStatus.Evaluation));
			    			//Link job and new task
		    				ControlStructure.jobTasks.get(jobKey).add(taskKey);
		    			}
		    			
		    			//Check if task should be evaluated
		    			if (ControlStructure.tasks.get(taskKey).taskStatus == TaskStatus.Evaluation)
		    			{
			    			String shopId = null;
			    			double bestPrice = 0;
			    			Enumeration<String> enumShopKey = ControlStructure.shops.keys();
			    			
			    			//Check all shops
			    			while(enumShopKey.hasMoreElements())
			    			{
			    				String shopKey = enumShopKey.nextElement();
			    				GlobalPercepts.Shop shop = ControlStructure.shops.get(shopKey);
			    				
			    				List<String> shopItems = shop.ItemNames();
			    	    		List<Integer> shopAmount = shop.Amount();
			    	    		List<Double> shopCosts = shop.Costs();
			    	    		
			    	    		//Check if shop contains desired item
			    	    		if (shopItems.contains(jobItems.get(i)))
			    	    		{
			    	    			int index = shopItems.indexOf(jobItems.get(i));
			    	    			//Check if shop contains desired quantity
			    	    			if (shopAmount.get(index) >= jobQuantities.get(i))
			    	    			{
			    	    				//Check if better price
			    	    				if (shopId == null || shopCosts.get(index) < bestPrice)
			    	    				{
			    	    					shopId = shop.Id();
			        	    				bestPrice = shopCosts.get(index);
			    	    				}
			    	    			}
			    	    		}
			    			}
			    			
			    			//Update task
			    			if (shopId != null)
			    			{
			    				ControlStructure.tasks.get(taskKey).operation = "buy";
			    				ControlStructure.tasks.get(taskKey).destination = shopId;
			    				ControlStructure.tasks.get(taskKey).taskStatus = TaskStatus.Accepted;
			    			}
			    			else
			    			{
			    				tempJobStatus = JobStatus.Evaluation;
			    			}
		    			}
		    		}
		    		ControlStructure.jobStatus.replace(jobKey, tempJobStatus);
	    		}
	    	}
    	}
    	
    	/**************************************************************/
    	/**************************************************************/
    	/**************************************************************/
    	//Return a task to be assigned
    	String jobId = null;
    	int taskId = -1;
    	
    	if (ControlStructure.tasks.size() != 0)
    	{
	    	Enumeration<String> enumJobId = ControlStructure.jobStatus.keys();
	    	while(enumJobId.hasMoreElements() && taskId == -1)
	    	{
	    		String tempJobId = enumJobId.nextElement();
	    		if (ControlStructure.jobStatus.get(tempJobId) == JobStatus.Accepted)
	    		{
	    			jobId = tempJobId;
	    			//Select!
	    			List<Integer> tasksList = ControlStructure.jobTasks.get(tempJobId);
	    			for (int i = 0; i < tasksList.size() && taskId == -1; i++)
	    			{
	    				if (ControlStructure.tasks.get(i).taskStatus == TaskStatus.Accepted)
	    				{
	    					//Task found!
	    					taskId = i;
	    				}
	    			}
	    		}
	    	}
    	}
    	
    	String operation = "";
    	String destination = "";
    	String item = "";
    	int quantity = 0;
    	int existingTask = 1;
    	if (taskId != -1)
    	{
    		operation = ControlStructure.tasks.get(taskId).operation;
    		destination = ControlStructure.tasks.get(taskId).destination;
    		item = ControlStructure.tasks.get(taskId).item;
    		quantity = ControlStructure.tasks.get(taskId).quantity;
    		existingTask = 2;
    	}
    	else
    	{
    		jobId = "Priced Jobs size: " + ControlStructure.pricedJobs.size() + ", Shops size: " + ControlStructure.shops.size();
    	}
    	
    	un.unifies(args[0],  new StringTermImpl(jobId));
    	un.unifies(args[1],  new NumberTermImpl(taskId));
    	un.unifies(args[2],  new StringTermImpl(operation));
    	un.unifies(args[3],  new StringTermImpl(destination));
    	un.unifies(args[4],  new StringTermImpl(item));
    	un.unifies(args[5],  new NumberTermImpl(quantity));
    	return un.unifies(args[6],  new NumberTermImpl(existingTask));
    	
    	/*un.unifies(args[0],  new StringTermImpl("1A"));
    	un.unifies(args[1],  new NumberTermImpl(1));
    	un.unifies(args[2],  new StringTermImpl("buy"));
    	un.unifies(args[3],  new StringTermImpl("shop1"));
    	un.unifies(args[4],  new StringTermImpl("item1"));
    	un.unifies(args[5],  new NumberTermImpl(1));
    	return un.unifies(args[6],  new NumberTermImpl(1));*/
    }
}

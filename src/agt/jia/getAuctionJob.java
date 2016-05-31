// Internal action code for project massimJacamoProject

package jia;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import massimJacamoProject.GlobalPercepts;

public class getAuctionJob extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
    	
    	/*
    	if (true)
    	{
    		un.unifies(args[0],  new StringTermImpl(""));
        	un.unifies(args[1],  new NumberTermImpl(45));
        	return un.unifies(args[2],  new NumberTermImpl(1));
    	}
    	*/
    	
    	ControlStructure.shops = GlobalPercepts.getShops();
    	
    	ControlStructure.auctionJobs = GlobalPercepts.getAuctionJobs();
    	//ControlStructure.pricedJobs = GlobalPercepts.getPricedJobs();
    	
    	//Auction job chosen for bid
    	String auctionJobKey = "";
    	
    	if (ControlStructure.auctionJobs.size() != 0 && ControlStructure.shops.size() != 0)
    	{
	    	Enumeration<String> enumJobKey = ControlStructure.auctionJobs.keys();
	    	
	    	//Check all jobs
	    	while(enumJobKey.hasMoreElements() && auctionJobKey == "")
	    	{	
	    		JobStatus tempJobStatus = JobStatus.Try;
	    		String jobKey = enumJobKey.nextElement();
	    		
	    		//Add new job
	    		if (!ControlStructure.jobStatus.containsKey(jobKey))
	    		{
	    			ControlStructure.jobStatus.put(jobKey, JobStatus.Evaluation);
	    			ControlStructure.jobTasks.put(jobKey, new ArrayList<Integer>());
	    			ControlStructure.jobType.put(jobKey, JobType.Auction);
	    		}
	    		
	    		//Check if job should be evaluated
	    		if (ControlStructure.jobStatus.get(jobKey) == JobStatus.Evaluation)
	    		{
		    		//Select job for evaluation
		    		GlobalPercepts.Job job = ControlStructure.auctionJobs.get(jobKey);
		    		
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
		    		if (tempJobStatus == JobStatus.Try)
		    		{
		    			auctionJobKey = jobKey;
		    		}
	    		}
	    	}
    	}
    	
    	double maximumBid = 0;
    	int existingAuction = 1;
    	if (auctionJobKey != "")
    	{
    		maximumBid = ControlStructure.auctionJobs.get(auctionJobKey).MaximumBid();
    		existingAuction = 2;
    	}
    	else
    	{
    		auctionJobKey = "Auction Jobs size: " + ControlStructure.auctionJobs.size() + ", Shops size: " + ControlStructure.shops.size();
    	}
    	
    	un.unifies(args[0],  new StringTermImpl(auctionJobKey));
    	un.unifies(args[1],  new NumberTermImpl(maximumBid));
    	return un.unifies(args[2],  new NumberTermImpl(existingAuction));
    }
}

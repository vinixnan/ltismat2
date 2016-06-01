// Internal action code for project massimJacamoProject

package jia;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import massimJacamoProject.GlobalPercepts;

public class getPendingTask extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
    	
    	/*
    	 * Internal action to identify an auction win,
    	 * check the unassigned tasks and return a task to be assigned
    	 */
    	
    	ControlStructure.takenJobs = GlobalPercepts.getTakenJobs();
    	
    	if (ControlStructure.takenJobs.size() != 0)
    	{
    		Enumeration<String> enumTakenJobId = ControlStructure.takenJobs.keys();
    		while(enumTakenJobId.hasMoreElements())
    		{
    			String takenJobId = enumTakenJobId.nextElement();
    			if (ControlStructure.jobStatus.get(takenJobId) == JobStatus.Try)
    			{
    				ControlStructure.jobStatus.replace(takenJobId, JobStatus.Accepted);
    			}
    		}
    	}
    	
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
    		jobId = "";
    	}
    	
    	un.unifies(args[0],  new StringTermImpl(jobId));
    	un.unifies(args[1],  new NumberTermImpl(taskId));
    	un.unifies(args[2],  new StringTermImpl(operation));
    	un.unifies(args[3],  new StringTermImpl(destination));
    	un.unifies(args[4],  new StringTermImpl(item));
    	un.unifies(args[5],  new NumberTermImpl(quantity));
    	return un.unifies(args[6],  new NumberTermImpl(existingTask));
    }
}

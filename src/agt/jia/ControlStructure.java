package jia;

import java.util.Hashtable;
import java.util.List;

import massimJacamoProject.GlobalPercepts;

enum JobStatus { Evaluation, Try, Accepted, Execution, Completed, Cancelled }
enum TaskStatus { Evaluation, Accepted, Assigned, Completed, Cancelled }
enum JobType { Auction, Priced }

public class ControlStructure {
	/*
	 * Stores the jobs, the facilities, the generated tasks and useful information
	 */
	public static Hashtable<String, GlobalPercepts.Job> auctionJobs;
	public static Hashtable<String, GlobalPercepts.Job> pricedJobs;
	public static Hashtable<String, String> takenJobs;
	public static Hashtable<String, GlobalPercepts.Shop> shops;
	public static Hashtable<String, JobStatus> jobStatus;
	public static Hashtable<Integer, Task> tasks;
	public static Hashtable<String, List<Integer>> jobTasks;
	public static Hashtable<String, JobType> jobType;
	
	static
	{
		auctionJobs = new Hashtable<String, GlobalPercepts.Job>();
		pricedJobs = new Hashtable<String, GlobalPercepts.Job>();
		takenJobs = new Hashtable<String, String>();
		shops = new Hashtable<String, GlobalPercepts.Shop>();
		jobStatus = new Hashtable<String, JobStatus>();
		tasks = new Hashtable<Integer, Task>();
		jobTasks = new Hashtable<String, List<Integer>>();
		jobType = new Hashtable<String, JobType>();
	}
}

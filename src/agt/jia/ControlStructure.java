package jia;

import java.util.Hashtable;
import java.util.List;

import massimJacamoProject.GlobalPercepts;

enum JobStatus { Evaluation, Try, Accepted, Execution, Completed, Cancelled }
enum TaskStatus { Evaluation, Accepted, Assigned, Completed, Cancelled }
enum JobType { Auction, Priced }
enum VehicleStatus { Available, Unavailable }

enum Cars { vehicle1, vehicle2, vehicle3, vehicle4 }
enum Drones { vehicle5, vehicle6, vehicle7, vehicle8 }
enum Motorcycles { vehicle9, vehicle10, vehicle11, vehicle12 }
enum Trucks { vehicle13, vehicle14, vehicle15, vehicle16 }

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
	
	public static Hashtable<String, VehicleStatus> vehicleStatus;
	
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
		vehicleStatus = new Hashtable<String, VehicleStatus>();
		vehicleStatus.put("vehicle1", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle2", VehicleStatus.Available);
		vehicleStatus.put("vehicle3", VehicleStatus.Available);
		vehicleStatus.put("vehicle4", VehicleStatus.Available);
		vehicleStatus.put("vehicle5", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle6", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle7", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle8", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle9", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle10", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle11", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle12", VehicleStatus.Unavailable);
		vehicleStatus.put("vehicle13", VehicleStatus.Available);
		vehicleStatus.put("vehicle14", VehicleStatus.Available);
		vehicleStatus.put("vehicle15", VehicleStatus.Available);
		vehicleStatus.put("vehicle16", VehicleStatus.Available);
	}
}

/**
 * 
 */
package massimJacamoProject;

import java.io.IOException;

import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.ManagementException;
import eis.iilang.Action;
import jason.stdlib.list;
import massim.javaagents.Agent;
import massim.javaagents.AgentsInterpreter;
import massim.javaagents.agents.CityUtil;

/**
 * Provides the interface between the City environment and Jacamo
 * @author Leno
 *
 */
public class CityAgentInterface {


	AgentsInterpreter interpreter=null;
	EnvironmentInterfaceStandard ei = null;
	int step;

	public CityAgentInterface(){
		step = 1;
		Agent.setEnvironmentInterface(ei);
	}
	/**
	 * Connect in the server, code based on massim.javaagents.App
	 *
	 */
	public void connect() {
		System.out.println("PHASE 1: INSTANTIATING INTERPRETER");
		
		String agentInterpreterFile = ProjectConstants.INTERPRETER_FILE;
		
		interpreter = new AgentsInterpreter(agentInterpreterFile);
		System.out.println("interpreter loaded");

		// load the interface
		System.out.println("");
		System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
				
		try {
			ei = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("environment-interface loaded");

		// start the interface
		try {
			ei.start();
		} catch (ManagementException e) {
			e.printStackTrace();
		}
		System.out.println("environment-interface started");

		System.out.println("");
		System.out.println("PHASE 3: CONNECTING INTERPRETER AND ENVIRONMENT");
		//  connect to environment
		interpreter.addEnvironment(ei);
		System.out.println("interpreter and environment connected");
				
		//  run stepwise
		System.out.println("");
		System.out.println("PHASE 4: RUNNING");
		
		
		new Thread(new Scheduler(this)).start();

	}
	 
	 
	void step(){
		interpreter.step();
		//GlobalPercepts.debug();
	}
	
	

	/**
	 * Executes the gotoAction for a given agent
	 * @param opUserName agent name
	 * @param lat latitude
	 * @param lon longitude
	 */
	public Action goToAction(String opUserName, double lat, double lon) {
		Action action =  CityUtil.action("goto","lat="+lat+" lon="+lon);
		return action;
	}
	/**
	 * Executes the goto action for a given agent
	 * @param opUserName agent name
	 * @param id Facility to be the destination
	 */
	public Action goToAction(String opUserName, String id) {
		Action action =  CityUtil.action("goto","facility="+id);
		return action; 
		
	}
	
	private class Scheduler implements Runnable{
		private CityAgentInterface instance;

		public Scheduler(CityAgentInterface instance){
			this.instance = instance;
		}

		public void run() {
			while(true){
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("STEP " + step);
				instance.step();
				step ++;
				System.out.println("");
			}
		}
		
	}
	/*
	 * 
	 * Actions
	 * 
	 */
	/**
	 * Returns a skip Action
	 * @param agentID the agent ID
	 * @return the action 
	 */
	public Action skipAction(String agentID) {
		Action action =  CityUtil.action("skip");
		return action;
	}
	/**
	 * Returns a buy action
	 * @param agentID agent executing the action
	 * @param id item_id
	 * @param amount item quantity
	 * @return the action
	 */
	public Action buyAction(String agentID, String id, int amount) {
		Action action =  CityUtil.action("buy","item="+id+" amount="+amount);
		return action; 
	}
	/**
	 * Returns a give action
	 * @param agentID agent executing the action
	 * @param agent destination agent
	 * @param item item_id
	 * @param amount quantity
	 * @return
	 */
	public Action giveAction(String agentID, String agent, String item, int amount) {
		Action action =  CityUtil.action("give","agent="+agent+" item="+item+" amount="+amount);
		return action;
	}
	/**
	 * Returns a receive action
	 * @param agentID agent executing the action
	 * @return
	 */
	public Action receiveAction(String agentID) {
		Action action =  CityUtil.action("receive");
		return action;
	}
	/**
	 * Returns a store action
	 * @param agentID agent executing the action
	 * @param item item to store
	 * @param amount quantity
	 * @return
	 */
	public Action storeAction(String agentID, String item, int amount) {
		Action action =  CityUtil.action("store","item="+item+" amount="+amount);
		return action;
	}
	/**
	 * Returns a retrieve action
	 * @param agentID agent executing the action
	 * @param item item to retrieve
	 * @param amount quantity
	 * @return
	 */
	public Action retrieveAction(String agentID, String item, int amount) {
		Action action =  CityUtil.action("retrieve","item="+item+" amount="+amount);
		return action;
	}
	/**
	 * Returns a retrieve_delivered action
	 * @param agentID agent executing the action
	 * @param item item to retrieve
	 * @param amount quantity
	 * @return
	 */
	public Action retrieveDeliveredAction(String agentID, String item, int amount) {
		Action action =  CityUtil.action("retrieve_delivered","item="+item+" amount="+amount);
		return action;
	}
	/**
	 * Returns a dump action
	 * @param agentID Agente executing the action
	 * @param item item to dump
	 * @param amount quantity
	 * @return
	 */
	public Action dumpAction(String agentID, String item, int amount) {
		Action action =  CityUtil.action("dump","item="+item+" amount="+amount);
		return action;
	}
	/**
	 * Returns an assemble action
	 * @param agentID agent executing the action
	 * @param item item_id
	 * @return
	 */
	public Action assembleAction(String agentID, String item) {
		Action action =  CityUtil.action("assemble","item="+item);
		return action;
	}
	/**
	 * returns a assist_assemble action
	 * @param agentID agent executing the action
	 * @param assembler agent to assist
	 * @return
	 */
	public Action assistAssembleAction(String agentID, String assembler) {
		Action action =  CityUtil.action("assist_assemble","assembler="+assembler);
		return action;
	}
	/**
	 * Returns a deliver_job action
	 * @param agentID agent executing the action
	 * @param job job to deliver items
	 * @return
	 */
	public Action deliverJobAction(String agentID, String job) {
		Action action =  CityUtil.action("deliver_job","job="+job);
		return action;
	}
	/**
	 * Creates a charge action
	 * @param agentID agent executing the action
	 * @return
	 */
	public Action chargeAction(String agentID) {
		Action action =  CityUtil.action("charge");
		return action;
	}
	/**
	 * Creates a bid_for_job action
	 * @param agentID who is executing the action
	 * @param job job to bid
	 * @param price price of the bid
	 * @return
	 */
	public Action bidForJobAction(String agentID, String job, double price) {
		Action action =  CityUtil.action("bid_for_job","job="+job+" price="+price);
		return action;
	}
	/**
	 * Returns a post_job action for auctioned jobs
	 * @param agentID who is executing the action
	 * @param max_price maximum price for a bid
	 * @param fine fine for not delivering the job
	 * @param active_steps number of steps to complete the job 
	 * @param auction_steps number of steps in which the auction is active
	 * @param storage storage to deliver the items
	 * @param item_ids list of items
	 * @param amounts list of amounts
	 * @return
	 */
	public Action postAuctionJobAction(String agentID, double max_price, double fine, int active_steps,
			int auction_steps, String storage, list item_ids, list amounts) {
		String parameters = "type=auction "+
							"max_price="+max_price+" "+
							"fine="+fine+" "+
							"active_steps="+active_steps+" "+
							"auction_steps="+auction_steps+" "+
							"storage="+storage+" ";
		throw new RuntimeException("post_job not implemented yet");
							
	}
	/**
	 * Returns a post_job action for priced jobs
	 * @param agentID who is executing the action
	 * @param price job price
	 * @param active_steps times for completion
	 * @param storage storage to delivery
	 * @param item_ids ids to delivery
	 * @param amounts quantities
	 * @return
	 */
	public Action postPricedJobAction(String agentID, double price, int active_steps, String storage, list item_ids,
			list amounts) {
		String parameters = "type=priced "+
							"price="+price+" "+
							"active_steps="+active_steps+" "+
							"storage="+storage+" ";
		throw new RuntimeException("post_job not implemented yet");
	}
	
	/**
	 * Returns a call_breakdown_service action
	 * @param agentID who is executing the action
	 * @return
	 */
	public Action callBreakdownServiceAction(String agentID) {
		Action action =  CityUtil.action("call_breakdown_service");
		return action;
	}
	/**
	 * Returns a continue action
	 * @param agentID who is executing the action
	 * @return
	 */
	public Action continueAction(String agentID) {
		Action action =  CityUtil.action("continue");
		return action;
	}
	/**
	 * Returns an abort action
	 * @param agentID who is executing the action
	 * @return
	 */
	public Action abortAction(String agentID) {
		Action action =  CityUtil.action("abort");
		return action;
	}


}

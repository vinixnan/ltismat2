// CArtAgO artifact code for project massimJacamoProject

package massimJacamoProject;

import cartago.Artifact;
import cartago.OPERATION;
import eis.iilang.Action;
import jason.stdlib.list;
/**
 * Artifact that implements the inferface between an agent in the CityWolrd and our implementation in JACAMO.
 * @author Leno
 *
 */
public class CityWorld extends Artifact {

	//Object with a Actual implementation of a connecting agent
	private static CityAgentInterface cityAgentInterface;


	private Action scheduledAction=null;
	private String agentID;


	void init(String agentID) {
		this.agentID = agentID;

		System.out.println("OK City world\n");

		//Only creates this interface one time
		if(cityAgentInterface==null){
			cityAgentInterface = new CityAgentInterface();
			//Connect to the server
			cityAgentInterface.connect();
		}

		//These two commands MUST be used after connection.
		CityAgent ag = CityAgent.getAgent(agentID);
		ag.registerArtifact(this);

	}


	/*
	 * 
	 * Operations
	 * 
	 * 
	 */
	/**
	 * goto action
	 * @param lat latitude
	 * @param lon longitude
	 */
	@OPERATION
	void goTo(double lat, double lon) {
		if(scheduledAction==null){
			Action a = cityAgentInterface.goToAction(this.agentID,lat,lon);
			scheduledAction = a;
		}

	}
	/**
	 * goto action
	 * @param id facility ID
	 */
	@OPERATION
	void goTo(String id){
		if(scheduledAction==null){
			Action a = cityAgentInterface.goToAction(this.agentID,id);
			scheduledAction = a;
		}
	}

	/**
	 * buy action
	 * @param id item id
	 * @param amount quantity to buy
	 */
	@OPERATION
	void buy(String id, int amount){
		if(scheduledAction==null){
			Action a = cityAgentInterface.buyAction(this.agentID,id,amount);
			scheduledAction = a;
		}
	}

	/**
	 * give action
	 * @param agent agent id
	 * @param item item id
	 * @param amount quantity to give
	 */
	@OPERATION
	void give(String agent, String item,int amount){
		if(scheduledAction==null){
			Action a = cityAgentInterface.giveAction(this.agentID,agent,item,amount);
			scheduledAction = a;
		}
	}
	/**
	 * receive action
	 */
	@OPERATION
	void receive(){
		if(scheduledAction==null){
			Action a = cityAgentInterface.receiveAction(this.agentID);
			scheduledAction = a;
		}
	}

	/**
	 * retrieve action
	 * @param item item to retrieve
	 * @param amount quantity to retrieve
	 */
	@OPERATION
	void retrieve(String item,int amount){
		if(scheduledAction==null){
			Action a = cityAgentInterface.retrieveAction(this.agentID,item,amount);
			scheduledAction = a;
		}
	}
	/**
	 * retrieve_delivered action
	 * @param item item id
	 * @param amount quantity to retrieve
	 */
	@OPERATION
	void retrieve_delivered(String item,int amount){
		if(scheduledAction==null){
			Action a = cityAgentInterface.retrieveDeliveredAction(this.agentID,item,amount);
			scheduledAction = a;
		}
	}
	/**
	 * dump action
	 * @param item item id
	 * @param amount qunatity
	 */
	@OPERATION
	void dump(String item,int amount){
		if(scheduledAction==null){
			Action a = cityAgentInterface.dumpAction(this.agentID,item,amount);
			scheduledAction = a;
		}
	}
	/**
	 * Assemble Action
	 * @param item item id to assemble
	 */
	@OPERATION
	void assemble(String item){
		if(scheduledAction==null){
			Action a = cityAgentInterface.assembleAction(this.agentID,item);
			scheduledAction = a;
		}
	}
	/**
	 * assist_assemble action
	 * @param assembler agent id to assist when assembling items
	 */
	@OPERATION
	void assist_assemble(String assembler){
		if(scheduledAction==null){
			Action a = cityAgentInterface.assistAssembleAction(this.agentID,assembler);
			scheduledAction = a;
		}
	}

	/**
	 * deliver_job action
	 * @param job id of the job to deliver
	 */
	@OPERATION
	void deliver_job(String job){
		if(scheduledAction==null){
			Action a = cityAgentInterface.deliverJobAction(this.agentID,job);
			scheduledAction = a;
		}
	}

	/**
	 * Charge action
	 */
	@OPERATION
	void charge(){
		if(scheduledAction==null){
			Action a = cityAgentInterface.chargeAction(this.agentID);
			scheduledAction = a;
		}
	}
	
	/**
	 * bid_for_job action
	 * @param job job id
	 * @param price price to bid
	 */
	@OPERATION
	void bid_for_job(String job, double price){
		if(scheduledAction==null){
			Action a = cityAgentInterface.bidForJobAction(this.agentID,job,price);
			scheduledAction = a;
		}
	}
	
	/**
	 * post_job action for auctioned jobs
	 * @param max_price auction max_price
	 * @param fine fine for not completing the job
	 * @param active_steps number of steps in each the auction is valid
	 * @param auction_steps number of steps to complete the auction
	 * @param storage delivery storage
	 * @param item_ids list of items that compose the auction
	 * @param amounts number of items to compose the job
	 */
	@OPERATION
	void post_auction_job(double max_price, double fine, int active_steps, int auction_steps, String storage,list item_ids, list amounts ){
		if(scheduledAction==null){
			Action a = cityAgentInterface.postAuctionJobAction(this.agentID, max_price,  fine,  active_steps,  auction_steps,  storage, item_ids,  amounts);
			scheduledAction = a;
		}
	}
	/**
	 * post_job action for priced jobs
	 * @param price price
	 * @param active_steps number of steps in which the job is active
	 * @param storage storage to delivery
	 * @param item_ids list of items
	 * @param amounts list of items quantities
	 */
	@OPERATION
	void post_priced_job(double price, int active_steps, String storage,list item_ids, list amounts ){
		if(scheduledAction==null){
			Action a = cityAgentInterface.postPricedJobAction(this.agentID, price, active_steps, storage,item_ids, amounts);
			scheduledAction = a;
		}
	}

	/**
	 * call_breakdown_service action
	 */
	@OPERATION
	void call_breakdown_service(){
		if(scheduledAction==null){
			Action a = cityAgentInterface.callBreakdownServiceAction(this.agentID);
			scheduledAction = a;
		}
	}
	
	/**
	 * continue action
	 */
	@OPERATION
	void continue_a(){
		if(scheduledAction==null){
			Action a = cityAgentInterface.continueAction(this.agentID);
			scheduledAction = a;
		}
	}

	/**
	 * skip action
	 */
	@OPERATION
	void skip(){
		if(scheduledAction==null){
			Action a = skipAction(this.agentID);
			scheduledAction = a;
		}
	}
	
	/**
	 * abort action
	 */
	@OPERATION
	void abort(){
		if(scheduledAction==null){
			Action a = cityAgentInterface.abortAction(this.agentID);
			scheduledAction = a;
		}
	}
	/*
	 * 
	 * Support Functions
	 * 
	 */
	/**
	 * Returns the scheduled action
	 * @return the action
	 */
	public Action getScheduledAction() {

		Action ret = this.scheduledAction;

		//System.out.println(ret);
		return ret;
	}


	/**
	 * Standard skip action
	 * @param agentID agent ID
	 * @return the skip action
	 */
	public static Action skipAction(String agentID) {
		Action a = cityAgentInterface.skipAction(agentID);
		//Action a = cityAgentInterface.goToAction(agentID,51.4847,-0.0454);
		return a;
	}


	public void clearSchedule() {
		this.scheduledAction = null;

	}


}


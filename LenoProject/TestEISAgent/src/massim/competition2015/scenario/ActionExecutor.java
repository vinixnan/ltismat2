package massim.competition2015.scenario;

import java.util.*;
import java.util.Map.Entry;

import massim.competition2015.MapSimulationAgentState;
import massim.competition2015.MapSimulationWorldState;
import massim.competition2015.scenario.exceptions.WrongParameterException;

/**
 * This class in the one in charge of executing the agents actions.
 */
public class ActionExecutor {


	private static float RANDOM_FAILURE_CHANCE_STATUS = 1.0f;

	// Return Messages
	private final static String SUCCESSFUL = "successful";
	private final static String FAILED_UNKNOWN_ERROR = "failed_unknown_error";
	private final static String FAILED_COUNTERPAT = "failed_counterpat";
	private final static String FAILED_LOCATION = "failed_location";
    private final static String FAILED_NO_ROUTE = "failed_no_route";
	private final static String FAILED_UNKNOWN_ITEM = "failed_unknown_item";
	private final static String FAILED_UNKNOWN_AGENT = "failed_unknown_agent";
	private final static String FAILED_ITEM_AMOUNT = "failed_item_amount";
	private final static String FAILED_CAPACITY = "failed_capacity";
	private final static String FAILED_UNKNOWN_FACILITY = "failed_unknown_facility";
	private final static String FAILED_WRONG_FACILITY = "failed_wrong_facility";
	private final static String FAILED_TOOLS = "failed_tools";
	private final static String FAILED_ITEM_TYPE = "failed_item_type";
	private final static String FAILED_UNKNOWN_JOB = "failed_unknown_job";
	private final static String FAILED_JOB_STATUS = "failed_job_status";
	private final static String FAILED_JOB_TYPE = "failed_job_type";
	private final static String PARTIAL_SUCCESS = "successful_partial"; // part of the job was delivered, still active.
	private final static String FAILED_WRONG_PARAM = "failed_wrong_param";
	private final static String FAILED_RANDOM = "failed_random";
	private final static String FAILED = "failed";
	private final static String UNKNOWN_ACTION = "unknownAction";
	private final static String USELESS = "useless";
	
	
	// Action names
	private final static String GO_TO = "goto";
	private final static String RECEIVE = "receive";
	private final static String GIVE = "give";
	private final static String STORE = "store";
	private final static String RETRIEVE = "retrieve";
	private final static String ASSEMBLE = "assemble";
	private final static String ASSIST_ASSEMBLE = "assist_assemble";
	private final static String BUY = "buy";
	private final static String DELIVER_JOB = "deliver_job";
	private final static String RETRIEVE_DELIVERED = "retrieve_delivered";
	private final static String BID_FOR_JOB = "bid_for_job";
	private final static String POST_JOB = "post_job";
	private final static String DUMP = "dump";
	private final static String CHARGE = "charge";
	private final static String CALL_BREAKDOWN_SERVICE = "call_breakdown_service";
	
	private final static String CONTINUE = "continue";
	private final static String ABORT = "abort";

	private final static String RANDOM_FAIL = "randomFail";
	private final static String SKIP = "skip";
	private final static String NO_ACTION = "noAction";
	

	
	private static Map<String, List<MapSimulationAgentState>> assemblyAsstistants;
	private static List<MapSimulationAgentState> assemblyAgents;

	private static Random random;

	/**
	 * Executes the actions of all agents.
	 * 
	 * @param world
	 */
	public static void execute(MapSimulationWorldState world) {
		//
		clearFlagsPre(world);
		performNewActions(world);
		assembleItems(world);
		clearFlagsPost(world);

		runJobGenerators(world); //make sure this executes before checkjobs
		checkJobs(world);

		reStockShops(world);
		chargeInAllStations(world);
		// updateStatus(world);
	}


	/**
	 * Clears the information about the actions executed in the previous step.
	 * Also determines random failure of actions.
	 * 
	 * @param world
	 */
	private static void clearFlagsPre(MapSimulationWorldState world) {
		if (assemblyAgents == null) assemblyAgents = new ArrayList<>();
		// else assemblyAgents.clear();
		if (assemblyAsstistants == null) assemblyAsstistants= new HashMap<>();
		// else assemblyAsstistants.clear();
		for (MapSimulationAgentState agentState : world.getAgents()) {
			agentState.lastAction = agentState.action;
			agentState.lastActionParam = agentState.param;
			agentState.lastActionResult = "";
			
			if (!(CONTINUE.equalsIgnoreCase(agentState.action) || 
					SKIP.equalsIgnoreCase(agentState.action))){
				if (!GO_TO.equalsIgnoreCase(agentState.action)){
					agentState.route = null;
				}
				if (!CHARGE.equalsIgnoreCase(agentState.action)){
					if (agentState.chargingStation!=null){
						agentState.chargingStation.removeAgent(agentState);
					}
				}
			}
			
			
			if (randomFail()) {
				agentState.action = RANDOM_FAIL;
				agentState.param = "";
			}
			
		}
	}
	
	/**
	 * Clears the information about the actions executed in the previous step.
	 * Also determines random failure of actions.
	 * 
	 * @param world
	 */
	private static void clearFlagsPost(MapSimulationWorldState world) {
		assemblyAgents.clear();
		assemblyAsstistants.clear();
		for (MapSimulationAgentState agentState : world.getAgents()) {
			agentState.action = "";
			agentState.param = "";
		}
	}

	/**
	 * Returns true if the action should fail.
	 * 
	 * @return
	 */
	private static boolean randomFail() {
		if (random == null) {
			random = new Random();
		}
		return random.nextInt(1000) < RANDOM_FAILURE_CHANCE_STATUS * 10; 
	}

	

	/**
	 * Executes the new actions
	 * 
	 * @param world
	 */
	private static void performNewActions(MapSimulationWorldState world) {
		ArrayList<MapSimulationAgentState> agents = new ArrayList<>(world.getAgents());
		Collections.shuffle(agents);

		for (MapSimulationAgentState agentState : agents) {
			try {
				if (GO_TO.equalsIgnoreCase(agentState.action)){
					handleGotoAction(world, agentState);
				} else if (GIVE.equalsIgnoreCase(agentState.action)){
					handleGiveAction(world, agentState);
				} else if (RECEIVE.equalsIgnoreCase(agentState.action)){
					handleReceiveAction(world, agentState);
				} else if (STORE.equalsIgnoreCase(agentState.action)){
					handleStoreAction(world, agentState);
				} else if (RETRIEVE.equalsIgnoreCase(agentState.action)){
					handleRetrieveAction(world, agentState, false);
				} else if (ASSEMBLE.equalsIgnoreCase(agentState.action)){
					handleAssembleAction(world, agentState);
				} else if (ASSIST_ASSEMBLE.equalsIgnoreCase(agentState.action)){
					handleAssistAssembleAction(world, agentState);
				} else if (BUY.equalsIgnoreCase(agentState.action)){
					handleBuyAction(world, agentState);
				//} else if (BID.equalsIgnoreCase(agentState.action)){
				//	handleBidAction(world, agentState);
				} else if (DELIVER_JOB.equalsIgnoreCase(agentState.action)){
					handleDeliverJobAction(world, agentState);
				} else if (RETRIEVE_DELIVERED.equalsIgnoreCase(agentState.action)){
					handleRetrieveAction(world, agentState, false);
				} else if (BID_FOR_JOB.equalsIgnoreCase(agentState.action)){
					handleBidForJobAction(world, agentState);
				} else if (DUMP.equalsIgnoreCase(agentState.action)){
					handleDumpAction(world, agentState);
				} else if (CHARGE.equalsIgnoreCase(agentState.action)){
					handleChargeAction(world, agentState);
				} else if (POST_JOB.equalsIgnoreCase(agentState.action)){
					handlePostJobAction(world, agentState);
                } else if (CALL_BREAKDOWN_SERVICE.equalsIgnoreCase(agentState.action)){
                    handleCallServiceAction(world, agentState);
				} else if (CONTINUE.equalsIgnoreCase(agentState.action) || 
						SKIP.equalsIgnoreCase(agentState.action)){
					if (agentState.chargingStation != null){
						handleChargeAction(world, agentState);
					}
					else if (agentState.route != null){
						handleGotoAction(world, agentState);
					} else {
						// Do anything?
						// yes, set action result
						agentState.lastActionResult = SUCCESSFUL;
					}
				} else if (ABORT.equalsIgnoreCase(agentState.action)) {
					if (agentState.chargingStation != null){
						agentState.chargingStation.removeAgent(agentState);
					}
					agentState.route = null;
					agentState.lastActionResult = SUCCESSFUL;
				} else if (RANDOM_FAIL.equalsIgnoreCase(agentState.action)) {
					agentState.lastActionResult = FAILED_RANDOM;
				} else if (NO_ACTION.equalsIgnoreCase(agentState.action)) {
					// agentState.lastActionResult = FAILED;
				} else {
					agentState.lastAction = UNKNOWN_ACTION;
					agentState.lastActionResult = FAILED;
				}
			} catch (WrongParameterException e){
				agentState.lastActionResult = FAILED_WRONG_PARAM;
			} catch (Exception e){
				if ("".equals(agentState.lastActionResult) ){
					agentState.lastActionResult = FAILED_UNKNOWN_ERROR;
				}	
			}

		}
		
	}

    private static void handleCallServiceAction(MapSimulationWorldState world, MapSimulationAgentState agentState) {
        if (world.currentStep > agentState.servicePrevStep + 1){
            agentState.serviceTimer = 0;
        }
        agentState.servicePrevStep = world.currentStep;
        agentState.serviceTimer += 1;

        if(agentState.serviceTimer == world.getConfig().serviceTime){
            agentState.setCharge(agentState.role.batteryCapacity);
            agentState.serviceTimer = 0;
            agentState.teamState.money -= world.getConfig().serviceFee;
        }

        agentState.lastActionResult = SUCCESSFUL;
    }


    private static void chargeInAllStations(MapSimulationWorldState world) {
		
		for (Facility facility : world.getAllFacilities()){
			if (facility instanceof ChargingStation) {
				ChargingStation station = (ChargingStation) facility;
				station.increaseCharges();
			}
		}
		
	}


	private static void checkJobs(MapSimulationWorldState world) {
		for (Job job:world.getAllJobs()){
			
			int currentStep = world.currentStep.intValue();
			
			if (job.firstStepActive == world.currentStep.intValue()){
				job.activate();

				//add job to teamstate if it is an auction
				if(job instanceof AuctionJob && ((AuctionJob) job).getAuctionWinner()!=null){
					world.getTeamState( ((AuctionJob) job).getAuctionWinner()).takenJobs.add(job);
				}

				continue;
			} 
			if (job instanceof AuctionJob) {
				AuctionJob aJob = (AuctionJob) job;
				if (aJob.firstStepAuction == currentStep){
					aJob.setStatus(Job.AUCTION);
				} else if (aJob.lastStepActive == currentStep && aJob.active()){
					world.getTeamState(aJob.getAuctionWinner()).money -= aJob.getFine();
					TeamState posterTeam = world.getTeamState(aJob.poster);
					if (posterTeam != null){
						posterTeam.money += aJob.getFine();
					}
				}
			} else if (job instanceof PricedJob) {
				PricedJob pJob = (PricedJob) job;
				
				if (pJob.lastStepActive == currentStep && pJob.active()){
					pJob.setStatus(Job.CANCELLED);
				}
			}
		}
	}
	

	private static void reStockShops(MapSimulationWorldState world) {
		for (Facility facility : world.getAllFacilities()){
			if (facility instanceof Shop) {
				Shop shop = (Shop) facility;
				shop.restock(world.currentStep);
			}
		}
	}



	/** 
	 * Give a number of items to a team mate agent that is performing the receive
	 * action.
	 * @param world
	 * @param agentState
	 * @throws WrongParameterException 
	 */
	private static void handleGiveAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		MapSimulationAgentState receiver = world.getAgent(params.get("agent"));
		
		if (receiver == null) {
			agent.lastActionResult = FAILED_UNKNOWN_AGENT;
			return;
		}

		// receiver is not "receiving" the load.
		if (!RECEIVE.equalsIgnoreCase(receiver.action)){
			agent.lastActionResult = FAILED_COUNTERPAT ;
			return;
		}
		
		// receiver is not in range
		if (!inRange(agent.location, receiver.location)){
			agent.lastActionResult = FAILED_LOCATION ;
			return;
		}
		
		Item item = world.getItem(params.get("item"));
		
		// Item does not exist
		if (item == null) {
			agent.lastActionResult = FAILED_UNKNOWN_ITEM;
			return;
		}
		
		int currQuantity = agent.getItemCount(item);
		int quantityToGive = Integer.parseInt(params.get("amount"));
		
		// Compare quantity.
		if (quantityToGive > currQuantity){
			agent.lastActionResult = FAILED_ITEM_AMOUNT;
			return;
		}
		
		int load = quantityToGive * item.volume;
		
		// Ensure receiver can receive
		if (load > receiver.getRemainingCapacity()){
			agent.lastActionResult = FAILED_CAPACITY;
			return;
		}
		
		// Make transfer.
		receiver.addItem(item, quantityToGive);
		agent.removeItem(item, quantityToGive);
		
		agent.lastActionResult = SUCCESSFUL;
		receiver.lastActionResult = SUCCESSFUL;
	}
	
	/** 
	 * Receive items from another agent.
	 * action.
	 * @param world
	 * @param agentState
	 */
	private static void handleReceiveAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) {

		//TODO make FAILED if nothing was received
		
		// The actual action is implemented by the receiver.
		if ("".equals(agent.lastActionResult)){
			agent.lastActionResult = SUCCESSFUL;
		}
	}
	
	/** 
	 * Store a number of items in a storage location
	 * @param world
	 * @param agentState
	 * @throws WrongParameterException 
	 */
	private static void handleStoreAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);

		Facility facility = world.getFacilityAtLocation(agent.location);

		// Agent not in facility
		if (facility == null){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}
		
		// Facility is not a storage
		if (!(facility instanceof Storage)){
			agent.lastActionResult = FAILED_WRONG_FACILITY;
			return;
		}
		Storage storage = (Storage)facility;
		
		Item item = world.getItem(params.get("item"));
		
		// Item does not exist
		if (item == null) {
			agent.lastActionResult = FAILED_UNKNOWN_ITEM;
			return;
		}
		
		int currQuantity = agent.getItemCount(item);
		int quantityToStore = Integer.parseInt(params.get("amount"));
		
		// the agent doesn't hold the item he wants to store!
		if (currQuantity < quantityToStore){
			agent.lastActionResult = FAILED_ITEM_AMOUNT;
			return;
		}
		
		int load = quantityToStore * item.volume;
		
		// Ensure receiver can receive
		if (load > storage.getRemainingCapacity()){
			agent.lastActionResult = FAILED_CAPACITY;
			return;

		}
		
		// Make transfer.
		storage.addItem(agent.team, item, quantityToStore);
		agent.removeItem(item, quantityToStore);
		agent.teamState.money -= storage.price*load;
		
		agent.lastActionResult = SUCCESSFUL;
	}


	/** 
	 * Store a number of items in a storage location
	 * @param world
	 * @param agentState
	 * @throws WrongParameterException 
	 */
	private static void handleChargeAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		
		Facility facility = world.getFacilityAtLocation(agent.location);

		// Agent not in facility
		if (facility == null){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}
		
		// Facility is not a charging station
		if (!(facility instanceof ChargingStation)){
			agent.lastActionResult = FAILED_WRONG_FACILITY;
			return;
		}
		
		ChargingStation station = (ChargingStation)facility;
		station.addAgent(agent);
		
		agent.lastActionResult = SUCCESSFUL;
	}
	
	
	private static void handleRetrieveAction(MapSimulationWorldState world,
			MapSimulationAgentState agent, boolean delivered) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		
		Facility facility = world.getFacilityAtLocation(agent.location);

		// Agent not in facility
		if (facility == null){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}

		// Facility is not a storage
		if (!(facility instanceof Storage)){
			agent.lastActionResult = FAILED_WRONG_FACILITY;
			return;
		}
		Storage storage = (Storage)facility;
		
		Item item = world.getItem(params.get("item"));

		// Item does not exist
		if (item == null) {
			agent.lastActionResult = FAILED_UNKNOWN_ITEM;
			return;
		}
		
		int currQuantity;
		if (!delivered){
			currQuantity = storage.getItemCount(agent.team, item);
		} else {
			currQuantity = storage.getDeliveredItemCount(agent.team, item);
		}
		int quantityToRetrieve = Integer.parseInt(params.get("amount"));
		
		// the storage doesn't hold the item he wants to store!
		if (currQuantity < quantityToRetrieve){
			agent.lastActionResult = FAILED_ITEM_AMOUNT;
			return;
		}
		
		int load = quantityToRetrieve * item.volume;
		
		// Ensure receiver can receive
		if (load > agent.getRemainingCapacity()){
			agent.lastActionResult = FAILED_CAPACITY;
			return;
		}
		
		// Make transfer.
		if (!delivered){
			storage.retrieveItem(agent.team, item, quantityToRetrieve);
		} else {
			storage.retrieveDeliveredItem(agent.team, item, quantityToRetrieve);
		}
		
		agent.addItem(item, quantityToRetrieve);

		agent.lastActionResult = SUCCESSFUL;
	}

	
	/** 
	 * Give a number of items to a team mate agent that is performing the receive
	 * action.
	 * @param world
	 * @param agentState
	 * @throws WrongParameterException 
	 */
	private static void handleDumpAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		
		Facility facility = world.getFacilityAtLocation(agent.location);

		// Agent not in facility
		if (facility == null){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}

		// Facility is not a dump location
		if (!(facility instanceof DumpLocation)){
			agent.lastActionResult = FAILED_WRONG_FACILITY;
			return;
		}
		DumpLocation dumpLoc = (DumpLocation)facility;
		
		Item item = world.getItem(params.get("item"));
		
		// Item does not exist
		if (item == null) {
			agent.lastActionResult = FAILED_UNKNOWN_ITEM;
			return;
		}
		
		int currQuantity = agent.getItemCount(item);
		int quantityToDump = Integer.parseInt(params.get("amount"));
		
		// the agent doesn't hold the item he wants to store!
		if (currQuantity < quantityToDump){
			agent.lastActionResult = FAILED_ITEM_AMOUNT;
			return;
		}

		// Make transfer.
		agent.removeItem(item, quantityToDump);
		agent.teamState.money -= dumpLoc.price*quantityToDump*item.volume;

		agent.lastActionResult = SUCCESSFUL;
	}
	
	
	private static void handleBuyAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {

		Map <String,String> params = parseParams(agent.param);
		
		Facility facility = world.getFacilityAtLocation(agent.location);

		// Agent not in facility
		if (facility == null){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}

		// Facility is not a storage
		if (!(facility instanceof Shop)){
			agent.lastActionResult = FAILED_WRONG_FACILITY;
			return;
		}
		Shop shop = (Shop)facility;
		
		Item item = world.getItem(params.get("item"));

		// Item does not exist
		if (item == null) {
			agent.lastActionResult = FAILED_UNKNOWN_ITEM;
			return;
		}
		
		int currQuantity = shop.getItemCount(item);
		int quantityToBuy = Integer.parseInt(params.get("amount"));
		
		// the storage doesn't hold the item he wants to store!
		if (currQuantity < quantityToBuy){
			agent.lastActionResult = FAILED_ITEM_AMOUNT;
			return;
		}
		
		int load = quantityToBuy * item.volume;
		
		// Ensure receiver can receive
		if (load > agent.getRemainingCapacity()){
			agent.lastActionResult = FAILED_CAPACITY;
			return;
		}
		
		// Make transfer.
		shop.retrieveItem(item, quantityToBuy);
		agent.addItem(item, quantityToBuy);
		agent.teamState.money -=shop.getItemCost(item)*quantityToBuy;
		
		agent.lastActionResult = SUCCESSFUL;
	}

	
	private static void handleAssembleAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) {
		assemblyAgents.add(agent);
		if (assemblyAsstistants.get(agent.name) == null){
			assemblyAsstistants.put(agent.name, new ArrayList<MapSimulationAgentState>());
		}
	}
	
	private static void handleAssistAssembleAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		String assemblerName = params.get("assembler");
		MapSimulationAgentState assembler = world.getAgent(assemblerName);
		if (assembler == null){
			agent.lastActionResult = FAILED_UNKNOWN_AGENT;
			return;
		} else if (!ASSEMBLE.equals(assembler.action)){
			agent.lastActionResult = FAILED_COUNTERPAT;
			return;
		}
		if (assemblyAsstistants.get(assemblerName) == null){
			List <MapSimulationAgentState> l = new ArrayList<>();
			l.add(agent);
			assemblyAsstistants.put(assemblerName, l);
		}
		else {
			assemblyAsstistants.get(assemblerName).add(agent);
		}
	}
	
	
	private static void assembleItems(MapSimulationWorldState world){
		for (MapSimulationAgentState assembler: assemblyAgents){
			try {
				List<MapSimulationAgentState> assistants = assemblyAsstistants.get(assembler.name);
				Iterator<MapSimulationAgentState> assistentsIt;
				
				Map <String,String> params = parseParams(assembler.param);
				Item item = world.getItem(params.get("item"));
				
				boolean assemblerFailed = false;
				
				if (item == null) {
					assembler.lastActionResult = FAILED_UNKNOWN_ITEM;
					assemblerFailed = true;
				}
				else if (!item.userAssembled){
					assembler.lastActionResult = FAILED_ITEM_TYPE;
					assemblerFailed = true;
				}
				
				
				Facility ws = world.getFacilityAtLocation(assembler.location);
				
				if (ws == null || !(ws instanceof Workshop)){
					assembler.lastActionResult = FAILED_WRONG_FACILITY;
					assemblerFailed = true;
				} 
				else {
					if (!inRange(ws.location, assembler.location)){
						assembler.lastActionResult = FAILED_LOCATION;
						assemblerFailed = true;
					}
					
					assistentsIt = assistants.iterator();
					while(assistentsIt.hasNext()){
						MapSimulationAgentState assistant = assistentsIt.next();
						if (!inRange(ws.location, assistant.location)){
							assistant.lastActionResult = FAILED_LOCATION;
							assistentsIt.remove();
						}
					}
				}
				
				if (assemblerFailed){
					for (MapSimulationAgentState assistant : assistants){
						assistant.lastActionResult = FAILED_COUNTERPAT;
					}
					continue;
				}
				
				boolean tools = true;
				for (Item tool : item.toolsNeeded.keySet()){
					int needed = item.toolsNeeded.get(tool);
					if (assembler.canUseTool(tool)){
						needed -= assembler.getItemCount(tool);
					}
					if (needed <= 0) continue;
					assistentsIt = assistants.iterator();
					while(needed>0 && assistentsIt.hasNext()){
						MapSimulationAgentState assistant = assistentsIt.next();
						if (assistant.canUseTool(tool)){
							needed -= assistant.getItemCount(tool);
						}
					}
					if (needed > 0){
						tools = false;
						break;
					}
				}
				if (!tools){
					assembler.lastActionResult = FAILED_TOOLS;
					for (MapSimulationAgentState assistant : assistants){
						assistant.lastActionResult = FAILED_TOOLS;
					}
					continue;
				}
				
				int volumeFreed = 0; // To check if assembler will have enough space after assembly
				boolean products = true;
				for (Item product : item.itemsConsumed.keySet()){
					int needed = item.itemsConsumed.get(product);
					int assemblerUses = Math.min(needed, assembler.getItemCount(product));
					needed -= assemblerUses;
					volumeFreed +=  assemblerUses * item.volume;
					if (needed <= 0) continue;
					assistentsIt = assistants.iterator();
					while(needed>0 && assistentsIt.hasNext()){
						MapSimulationAgentState assistant = assistentsIt.next();
						needed -= assistant.getItemCount(product);
					}
					if (needed > 0){
						products = false;
						break;
					}
				}
				if (!products){
					assembler.lastActionResult = FAILED_ITEM_AMOUNT;
					for (MapSimulationAgentState assistant : assistants){
						assistant.lastActionResult = FAILED_ITEM_AMOUNT;
					}
					continue;
				}
				if (item.volume > assembler.getRemainingCapacity() + volumeFreed){
					assembler.lastActionResult = FAILED_CAPACITY;
					for (MapSimulationAgentState assistant : assistants){
						assistant.lastActionResult = FAILED_COUNTERPAT;
					}
					continue;
				}
				
				for (Item product : item.itemsConsumed.keySet()){
					int needed = item.itemsConsumed.get(product);
					if (assembler.getItemCount(product) > 0) {
						int used = Math.min(needed, assembler.getItemCount(product));
						assembler.removeItem(product, used);
						needed -= used;
					}
					if (needed <= 0) continue;
					assistentsIt = assistants.iterator();
					while(needed>0 && assistentsIt.hasNext()){
						MapSimulationAgentState assistant = assistentsIt.next();
						int used = Math.min(needed, assistant.getItemCount(product));
						assistant.removeItem(product, used);
						needed -= used;
					}
				}
				
				
				assembler.addItem(item, 1);
				
				assembler.lastActionResult = SUCCESSFUL;
				for (MapSimulationAgentState assistant : assistants){
					assistant.lastActionResult = SUCCESSFUL;
				}
				
			} catch (WrongParameterException e){
				assembler.lastActionResult = FAILED_WRONG_PARAM;
				try {
					for (MapSimulationAgentState assistant : assemblyAsstistants.get(assembler.name)){
						assistant.lastActionResult = FAILED_COUNTERPAT;
					}
				} catch (NullPointerException e1){
					// do nothing.
				}
			} catch (Exception e){
				assembler.lastActionResult = FAILED_UNKNOWN_ERROR;
				try {
					for (MapSimulationAgentState assistant : assemblyAsstistants.get(assembler.name)){
						assistant.lastActionResult = FAILED_COUNTERPAT;
					}
				} catch (NullPointerException e1){
					// do nothing.
				}
			}
		}
	}
	
	
	
	
	/** 
	 * Give a number of items to a team mate agent that is performing the receive
	 * action.
	 * @param world
	 * @param agentState
	 * @throws WrongParameterException 
	 */
	private static void handleDeliverJobAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		
		Map <String,String> params = parseParams(agent.param);
		Job job = world.getJobsNamesMap().get(params.get("job"));

		if (job == null){
			agent.lastActionResult = FAILED_UNKNOWN_JOB;
			return;
		}
		if (!job.active()){
			agent.lastActionResult = FAILED_JOB_STATUS;
			return;
		}
		if (agent.team.equals(job.poster)){
			agent.lastActionResult = FAILED_JOB_STATUS;
			return;
		}
		
		Storage storage = (Storage)world.getFacility(job.storageId);
		
		// storage is not in range
		if (!inRange(agent.location, storage.location)){
			agent.lastActionResult = FAILED_LOCATION;
			return;
		}
		
		if (job instanceof AuctionJob) {
			AuctionJob auctionJob = (AuctionJob) job;
			if (!auctionJob.isAssigned() ||
					!agent.team.equals(auctionJob.getAuctionWinner())){
				agent.lastActionResult = FAILED_JOB_STATUS;
				return;
			}
		}
		
		int totalUsed = 0;
		for (Entry<Item, Integer> itemEntry : job.itemsRequired.entrySet()){
			Item item = itemEntry.getKey();
			int used = job.deliverItem(item, agent.getItemCount(item), agent.team);
			agent.removeItem(item, used);
			totalUsed += used;
		}
		if (totalUsed > 0){
			
			if (job.checkCompleted(agent.team)) {
				int reward = job.getReward();
				agent.teamState.money += reward;
				TeamState posterTeam = world.getTeamState(job.poster);
				if (posterTeam != null){
					posterTeam.money -= reward;
				}
				agent.lastActionResult = SUCCESSFUL;
			} else {
				agent.lastActionResult = PARTIAL_SUCCESS;
			}
		} else {
			agent.lastActionResult = USELESS;
		}
		

		
	}
	
	private static void handleBidForJobAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		Job job = world.getJobsNamesMap().get(params.get("job"));
		AuctionJob aJob;
		
		if (job == null){
			agent.lastActionResult = FAILED_UNKNOWN_JOB;
			return;
		} else if (job instanceof AuctionJob) {
			 aJob = (AuctionJob) job;
		} else {
			agent.lastActionResult = FAILED_JOB_TYPE;
			return;
		}
		
		if(!aJob.inAuction()){
			agent.lastActionResult = FAILED_JOB_STATUS;
			return;
		}
		
		int price = Integer.parseInt(params.get("price"));
		aJob.addBid(agent.team, price);
		
		agent.lastActionResult = SUCCESSFUL;
	}
	
	
	private static void handlePostJobAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		String type = params.get("type");
		
		Job job;
		if("auction".equalsIgnoreCase(type)){
			AuctionJob aJob = new AuctionJob(new Random(System.nanoTime()));
			aJob.maxPrice = Integer.parseInt(params.get("max_price"));
			aJob.fine = Integer.parseInt(params.get("fine"));
			int auctionSteps = Integer.parseInt(params.get("auction_steps"));
			aJob.firstStepAuction = world.currentStep;
			aJob.firstStepActive = world.currentStep + auctionSteps;
			job = aJob;
		} else if ("priced".equalsIgnoreCase(type)){
			PricedJob pJob = new PricedJob(new Random(System.nanoTime()));
			int price = Integer.parseInt(params.get("price"));
			pJob.price = price;
			pJob.firstStepActive = world.currentStep;
			job = pJob;
		} else {
			agent.lastActionResult = FAILED_JOB_TYPE;
			return;
		}
		
		job.poster=agent.team;

		agent.teamState.postedJobs.add(job);
		
		int activeSteps = Integer.parseInt(params.get("active_steps"));
		job.lastStepActive = job.firstStepActive + activeSteps;
		
		String storageId = params.get("storage");
		Facility storage = world.getFacility(storageId);
		if(!(storage instanceof Storage)){
			agent.lastActionResult = FAILED_UNKNOWN_FACILITY;
			return;
		}
		job.storageId = storageId;
		
		int i = 1;
		String itemParam = "item1";
		String amountParam = "amount1";
		
		while (params.get(itemParam)!=null){
			String itemName = params.get(itemParam);
			Item item = world.getItem(itemName);
			if (item == null){
				agent.lastActionResult = FAILED_UNKNOWN_ITEM;
				return;
			}
			int amount = Integer.parseInt(params.get(amountParam));
			job.addRequiredItem(item, amount);
			i++;
			itemParam = "item"+i;
			amountParam = "amount"+i;
		}
		world.addJob(job);
		agent.lastActionResult = SUCCESSFUL;
	}

	
	private static void handleGotoAction(MapSimulationWorldState world,
			MapSimulationAgentState agent) throws WrongParameterException {
		Map <String,String> params = parseParams(agent.param);
		String lonString = params.get("lon");
		String latString = params.get("lat");
		String facilityName = params.get("facility");
		
		if (lonString != null && latString != null){
            double lat = Double.parseDouble(latString);
            double lon = Double.parseDouble(lonString);
			Location destination = new Location(lon, lat);
            //check if coordinates are within map bounds
            if (lon >= world.getConfig().minLon && lon <= world.getConfig().maxLon
                    && lat >= world.getConfig().minLat && lat <= world.getConfig().maxLat){
                Route route = world.cityMap.getNewRoute(agent.location, destination, agent.role.roads);
                agent.route = route;

                if(route != null) {
                    Route backRoute = world.cityMap.getNewRoute(destination, agent.location, agent.role.roads);
                    if (backRoute == null) agent.route = null;
                }
            }

		} else if (facilityName != null) {
			Facility facility = world.getFacility(facilityName);
			if (facility == null){
				agent.lastActionResult = FAILED_UNKNOWN_FACILITY;
				return;
			}
			Route route = world.cityMap.getNewRoute(agent.location, facility.location, agent.role.roads);
			agent.route = route;

            Route backRoute = world.cityMap.getNewRoute(facility.location, agent.location, agent.role.roads);
            if (backRoute == null) agent.route = null;

		}else {
			if(agent.route == null || lonString != null || latString != null){
				agent.lastActionResult = FAILED_WRONG_PARAM;
				return;
			}
		}

        if(agent.route != null) {
            agent.advance();
            agent.lastActionResult = SUCCESSFUL;
        }
        else{
            agent.lastActionResult = FAILED_NO_ROUTE;
        }
	}
	

	private static boolean inRange(Location loc1,
			Location loc2) {
		if (loc1 == null || loc2 == null){
			return false;
		}
		return loc1.equals(loc2);
	}
	
	



	private static Map<String, String> parseParams(String param) throws WrongParameterException {
		Map<String,String> paramsMap = new HashMap<>();
		try{
			String[] properties = param.split(" ");
			for (String prop: properties){
				if ("".equals(prop.trim())) continue;
				String[] entry = prop.split("=");
				paramsMap.put(entry[0].toLowerCase(),entry[1].toLowerCase());
			}
		} catch (Exception e) {
			// System.err.println("Error parsing parameter. Bad format."); // TODO 2015:: log error properly
			throw new WrongParameterException(e);
		}
		return paramsMap;
	}

	private static void runJobGenerators(MapSimulationWorldState world) {
		world.stepJobGeneration();
	}

}

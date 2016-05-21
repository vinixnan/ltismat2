/**
 * 
 */
package massimJacamoProject;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import eis.iilang.Parameter;
import eis.iilang.Percept;
import massimJacamoProject.GlobalPercepts.ChargingStation;

/**
 * Stores the percepts that are the same for all agents
 * @author leno
 *
 */
public class GlobalPercepts {

	static GlobalPercepts globalSingleton; 
	//stores Auctioned jobs
	static Hashtable<String,Job> auctionJobs;
	//stores Priced jobs
	static Hashtable<String,Job> pricedJobs;
	//Posted jobs
	static Hashtable<String,String> postedJobs;
	//Tanken Jobs
	static Hashtable<String,String> takenJobs;
	//True when the simulation is over
	static boolean bye;
	//Stores chargingStations
	static Hashtable<String,ChargingStation> chargingStations;
	//Stores dumpStations
	static Hashtable<String,DumpStation> dumpFacilities;
	//entities
	static Hashtable<String,EntityRecord> entities;
	//Product List
	static Hashtable<String,Product> products;
	//Shop list
	static Hashtable<String,Shop> shops;
	//Storages list
	static Hashtable<String,Storage> storages;
	//Workshop list
	static Hashtable<String,Workshop> workshops;
	//Roles
	static Hashtable<String,Role> roles;
	//Current ranking
	static int ranking;
	//Current simulation ID
	static String simulationID;
	//Map ID
	static String map;
	//Teams total money
	static double money;
	//current step
	static int step;
	//Total number of steps
	static int steps;
	//Last message sent by the server
	static double timestamp;
	//seed Capital
	static double seedCapital;
	//Team identification
	static String team;
	
	//Action deadline
	static double deadline;

	//Initializations
	static{
		auctionJobs = new Hashtable<String,Job>();
		pricedJobs = new Hashtable<String,Job>();
		postedJobs = new Hashtable<String,String>();
		takenJobs = new Hashtable<String,String>();
		chargingStations = new Hashtable<String,ChargingStation>();
		dumpFacilities = new Hashtable<String,DumpStation>();
		entities = new Hashtable<String,EntityRecord>();
		products = new Hashtable<String,Product>();
		shops = new Hashtable<String,Shop>();
		storages = new Hashtable<String,Storage>();
		workshops = new Hashtable<String,Workshop>();
		roles = new Hashtable<String,Role>();
		globalSingleton = new GlobalPercepts();
	}
	/**
	 * This method must be used to get a instance of the Global Percept class
	 * @return
	 */
	public static GlobalPercepts getInstance(){
		return globalSingleton;
	}


	/**
	 * Process global perceptions
	 * @param percept
	 * @return if a global perception was fount
	 */
	public  boolean processGlobalPercept(Percept percept) {

		if(percept.getName().equals("auctionJob")){

			updateAuctionJob(percept.getParameters());
			//System.out.println("Auction Jobs:::::\n"+auctionJobs+"\n\n");
			return true;
		}
		if(percept.getName().equals("bye")){
			bye = true;
			return true;
		}
		if(percept.getName().equals("chargingStation")){
			updateChargingStation(percept.getParameters());
			//System.out.println("ChargingStations:::::\n"+chargingStations+"\n\n");
			return true;
		}
		if(percept.getName().equals("dump")){
			updateDumpingStation(percept.getParameters());
			//System.out.println("Dump:::::\n"+dumpFacilities+"\n\n");
			return true;
		}
		if(percept.getName().equals("entity")){
			updateEntity(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("id")){
			GlobalPercepts.simulationID = percept.getParameters().getFirst().toString();
			return true;
		}
		if(percept.getName().equals("jobPosted")){
			updatePostedJob(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("jobTaken")){
			updateTakenJob(percept.getParameters());
		}
		if(percept.getName().equals("map")){
			GlobalPercepts.map = percept.getParameters().getFirst().toString();
			return true;
		}
		if(percept.getName().equals("money")){
			GlobalPercepts.money = Double.parseDouble(percept.getParameters().getFirst().toString());
			return true;
		}
		if(percept.getName().equals("pricedJob")){
			updatePricedJob(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("product")){
			updateProduct(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("ranking")){
			GlobalPercepts.ranking = Integer.parseInt(percept.getParameters().getFirst().toString());
			return true;
		}
		if(percept.getName().equals("role")){
			updateRole(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("shop")){
			updateShop(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("step")){
			GlobalPercepts.step = Integer.parseInt(percept.getParameters().getFirst().toString());
			return true;
		}
		if(percept.getName().equals("steps")){
			GlobalPercepts.steps = Integer.parseInt(percept.getParameters().getFirst().toString()); //Remove brackets and convert
			return true;
		}
		if(percept.getName().equals("storage")){
			updateStorage(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("timestamp")){
			GlobalPercepts.timestamp = Double.parseDouble(percept.getParameters().getFirst().toString());
			return true;
		}
		if(percept.getName().equals("workshop")){
			updateWorkshop(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("visibleChargingStation")){
			updateChargingStation(percept.getParameters());
			return true;
		}
		if(percept.getName().equals("seedCapital")){
			GlobalPercepts.seedCapital = Double.parseDouble(percept.getParameters().getFirst().toString());
			return true;
		}
		if(percept.getName().equals("team")){
			GlobalPercepts.team = percept.getParameters().getFirst().toString();
			return true;
		}
		if(percept.getName().equals("deadline")){
			GlobalPercepts.deadline = Double.parseDouble(percept.getParameters().getFirst().toString());
			return true;
		}


		return false;		
	}
	/**
	 * Creates workshop stations
	 * @param parameters Parameters: 1. name 2. latitude 3. longitude 4. price
	 */
	private void updateWorkshop(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();
		
		if(!workshops.containsKey(id)){
			double lat = Double.parseDouble(parameters.get(1).toString());
			double lon = Double.parseDouble(parameters.get(2).toString());
			double price = Double.parseDouble(parameters.get(3).toString());
			Workshop ws = new Workshop(id,lat,lon,price);
			workshops.put(id, ws);
		}
		
	}


	/**
	 * Creates / updates a storage facility
	 * @param parameters Parameters: 1. name 2. latitude 3. longitude 4. price 5. total capacity
6. used capacity 7. item name 8. amount stored 9. amount delivered
	 */
	private void updateStorage(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();
		int usedCapacity = Integer.parseInt(parameters.get(5).toString());
		String itemList = parameters.getLast().toProlog();
		List<String> itemNames = new ArrayList<String>();
		List<Integer> amountStored = new ArrayList<Integer>();
		List<Integer> amountDelivered = new ArrayList<Integer>();
		
		splitItemListWithDelivered(itemList,itemNames,amountStored,amountDelivered);
		
		if(storages.containsKey(id)){
			Storage storage = storages.get(id);
			storage.updateStorageInformation(usedCapacity,itemNames,amountStored,amountDelivered);
		}
		else{ //New storage
			double lat = Double.parseDouble(parameters.get(1).toString());
			double lon = Double.parseDouble(parameters.get(2).toString());
			double price = Double.parseDouble(parameters.get(3).toString());
			int totalCapacity = Integer.parseInt(parameters.get(4).toString());
			
			Storage storage = new Storage(id, lat, lon, price, totalCapacity, usedCapacity, itemNames, amountStored,amountDelivered);
		    storages.put(id, storage);
			
			
		}
		
	}


	/**
	 * Updates/Creates Shop information
	 *  Parameters: 1. name 2. latitude 3. longitude 4. item name
	 *  OR:
	 *  Parameters: 1. name 2. latitude 3. longitude 4. item name 5. cost
6. amount 7. restock interval
	 * @param parameters
	 */
	private void updateShop(LinkedList<Parameter> parameters) {
		
		String id = parameters.getFirst().toString();
		
		String itemList = parameters.getLast().toProlog();
		
		//Check if the agent can see quantities
		boolean isNear = itemList.indexOf("availableItem(") > -1;
		//If the shop was already seen
		if(shops.containsKey(id)){
			//If agent is near and can see detailed information
			if(isNear){
				
			}
		}else{
			double lat = Double.parseDouble(parameters.get(1).toString());
			double lon = Double.parseDouble(parameters.get(2).toString());
			List<String> itemNames = new ArrayList<String>();
			Shop shop = null;
			//If agent can't see detailed information
			if(isNear){
				List<Double> costs = new ArrayList<Double>();
				List<Integer> amount = new ArrayList<Integer>();
				List<Double> restock = new ArrayList<Double>();
				splitAvailableItem(itemList,itemNames,costs,amount,restock);
				shop = new Shop(id,lat,lon,itemNames,costs,amount,restock);
				
			}else{
				splitItemList(itemList,itemNames);
				shop = new Shop(id, lat, lon, itemNames);
			}
			shops.put(id, shop);
		}
		
	}


	


	/**
	 * Instantiate role information
	 * @param parameters Parameters: 1. role name 2. speed 3. load capacity 4. battery capacity
		95. a tool the agent can use (containing list is of arbitrary length, possibly
		empty)
	 */
	private void updateRole(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();
		
		//if the role already exists, there is no need to add it again
		if(!roles.containsKey(id)){
			double speed = Double.parseDouble(parameters.get(1).toString());
			int capacity = Integer.parseInt(parameters.get(2).toString());
			double battery = Double.parseDouble(parameters.get(3).toString());
			String toolList = parameters.get(4).toProlog();
			
			List<String> toolNames = new ArrayList<String>();
			splitToolNames(toolList,toolNames);
			//System.out.println("ID "+id);
			Role role = new Role(id, speed, capacity, battery,toolNames);
			roles.put(id, role);
		}
		
	}


	


	/**
	 * Create or update the product information
	 * @param parameters Parameters: 1. name 2. volume 3. item name 4. amount 5. item name
6. amount
Note: If the list is empty, the product cannot be assembled.
	 */
	private void updateProduct(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();
		
		//If the product is new
		if(!products.containsKey(id)){
			int volume = Integer.parseInt(parameters.get(1).toString());
			String materialList = parameters.get(2).toProlog();
			List<String> components = new ArrayList<String>();
			List<Integer> quantities = new ArrayList<Integer>();
			
			List<String> toolNames = new ArrayList<String>();
			List<Integer> toolQuantities = new ArrayList<Integer>();
			
			splitItemProdList(materialList, components,quantities, toolNames,toolQuantities);
			Product prod = new Product(id,volume,components, quantities,toolNames,toolQuantities);
			products.put(id, prod);
		}
		

		
	}


	

	/**
	 * If posted by the team, create/update priced job. If not, create the job if needed
	 * @param parameters Parameters: 1. id 2. storage 3. begin 4. end 5. reward 6. item name
	 * 7. amount 8. delivered
	 * Note: Parameter 8 is only visible, if the job was posted by the agent’s
	 * team.
	 */
	private void updatePricedJob(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();
		String items = parameters.get(5).toProlog();

		List<String> itemNames = new ArrayList<String>();
		List<Integer> quantities = new ArrayList<Integer>();

		//If the job already exists, it is only needed to update the quantity
		if(!pricedJobs.containsKey(id)){

			//If the job was posted by the team
			if(postedJobs.containsKey(id)){
				List<Integer> delivered = new ArrayList<Integer>();
				splitItemListWithDelivered(items,itemNames,quantities,delivered);
				Job job = pricedJobs.get(id);
				job.updateDelivered(quantities);
			}
		}else{//The job does not exist yet
			String storage = parameters.get(1).toString();
			int beginStep = Integer.parseInt(parameters.get(2).toString());
			int endStep = Integer.parseInt(parameters.get(3).toString());
			double reward = Double.parseDouble(parameters.get(4).toString());
			Job job= null;

			//If the job was posted by the team
			if(postedJobs.containsKey(id)){
				List<Integer> delivered = new ArrayList<Integer>();
				splitItemListWithDelivered(items,itemNames,quantities,delivered);
				job = new Job(id,storage,beginStep,endStep,reward,itemNames,quantities);
				job.updateDelivered(quantities);			
			}
			else{// the job was not posted by the team
				splitItemList(items, itemNames, quantities,true);
				job = new Job(id,storage,beginStep,endStep,reward,itemNames,quantities);
			}
			pricedJobs.put(id, job);
		}



	}


	


	/**
	 * If needed, update the jobs that have been taken by the agents
	 * @param parameters  the job identifier
	 */
	private void updateTakenJob(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();

		if(!takenJobs.containsKey(id)){
			takenJobs.put(id, id);
		}

	}


	/**
	 * Creates a new posted job in the variable, if needed
	 * @param parameters the job identifier
	 */
	private void updatePostedJob(LinkedList<Parameter> parameters) {
		String id = parameters.getFirst().toString();

		if(!postedJobs.containsKey(id)){
			postedJobs.put(id, id);
		}

	}


	/**
	 * Update entities
	 * @param parameters Parameters: 1. name 2. team 3. latitude 4. longitude 5. role
	 */
	private void updateEntity(LinkedList<Parameter> parameters) {
		String id = parameters.get(0).toString();
		//If the entity already exists, only the position is updated
		if(entities.containsKey(id)){
			EntityRecord entity = entities.get(id);
			double lat = Double.parseDouble(parameters.get(2).toString());
			double lon = Double.parseDouble(parameters.get(3).toString());

			entity.updatePosition(lat,lon);
		}else{
			//New entity
			String team = parameters.get(1).toString();
			double lat = Double.parseDouble(parameters.get(2).toString());
			double lon = Double.parseDouble(parameters.get(3).toString());
			String role = parameters.get(4).toString();

			EntityRecord entity = new EntityRecord(id, team, lat, lon, role);
			entities.put(id, entity);
		}

	}


	/**
	 * Updates the dumping stations
	 * @param parameters Parameters: 1. name 2. latitude 3. longitude 4. price
	 */
	private void updateDumpingStation(LinkedList<Parameter> parameters) {
		String id = parameters.get(0).toString();

		//Dump facilities records dont need to be updated
		if(!dumpFacilities.containsKey(id)){
			double lat = Double.parseDouble(parameters.get(1).toString());
			double lon = Double.parseDouble(parameters.get(2).toString());
			double price = Double.parseDouble(parameters.get(3).toString());

			DumpStation dump = new DumpStation(id,lat,lon,price);
			dumpFacilities.put(id, dump);
		}	
	}


	/**
	 * Updates the charging station
	 * @param parameters Parameters: 1. name 2. latitude 3. longitude 4. ch. rate 5. price 6. slots
	 */
	private void updateChargingStation(LinkedList<Parameter> parameters) {
		//If there is 6 parameters, it is a new chargingStation information, if there is 7, we should only update
		boolean visible = parameters.size()==7;
		String id = parameters.get(0).toString();
		//If the charging station is already stored...
		if(visible && chargingStations.containsKey(id)){
			ChargingStation chargeS = chargingStations.get(id);

			//Queue Size in the charging station
			int queueSize = Integer.parseInt(parameters.get(6).toString());
			chargeS.updateQueueSize(queueSize);
		}else{  //New charging station perception
			if(!chargingStations.containsKey(id)){
				double lat = Double.parseDouble(parameters.get(1).toString());
				double lon = Double.parseDouble(parameters.get(2).toString());
				double chargeRate = Double.parseDouble(parameters.get(3).toString());
				double price = Double.parseDouble(parameters.get(4).toString());
				int slots = Integer.parseInt(parameters.get(5).toString());

				ChargingStation chargeS = new ChargingStation(id, lat, lon, chargeRate, price, slots);
				chargingStations.put(id, chargeS);
			}
		}

	}


	/**
	 * Includes a new job in the list
	 * @param parameters Parameters: 1. id 2. storage 3. begin step 4. end step 5. fine 6. maximum
bid 7. item name 8. amount
	 */
	private void updateAuctionJob(LinkedList<Parameter> parameters) {

		String id = parameters.get(0).toString();
		//Check if auction is already included in the variable
		if(!auctionJobs.containsKey(id)){


			String storage = parameters.get(1).toString();
			int beginStep = Integer.parseInt(parameters.get(2).toString());
			int endStep = Integer.parseInt(parameters.get(3).toString());
			double fine = Double.parseDouble(parameters.get(4).toString());
			double maximumBid = Double.parseDouble(parameters.get(5).toString());

			List<String> itemNames=new ArrayList<String>(); List<Integer> quantities = new ArrayList<Integer>();
			splitItemList(parameters.get(6).toProlog(),itemNames,quantities,true);

			Job job = new Job(id,storage,beginStep,endStep,fine,maximumBid,itemNames,quantities);
			auctionJobs.put(id, job);
		}


	}
	
	/**
	 * Available itens list
	 * @param itemList variable with the list [availableItem(<Id4>,<Num5>,<Num6>,<Num7>),...]
	 * @param itemNames list of items
	 * @param costs item costs
	 * @param amount item quantities
	 * @param restock restock interval
	 */
	private void splitAvailableItem(String itemList, List<String> itemNames, List<Double> costs, List<Integer> amount,
			List<Double> restock) {
		//Remove brackets:
		itemList = itemList.substring(1,itemList.length()-1);
		
		String[] splits = itemList.split("(availableItem\\()|(,availableItem\\()"); 
		
		
		for(int i=1;i<splits.length;i++){
			String item = splits[i];

			//Remove last parenthesis
			item = item.substring(0, item.length() -1);

			String[] itemInfo = item.split(",");
			String itemID = itemInfo[0];
			double costsI = Double.parseDouble(itemInfo[1]);
			int quantity = Integer.parseInt(itemInfo[2]);
			double restockI = Double.parseDouble(itemInfo[3]);
			
			itemNames.add(itemID);
			amount.add(quantity);
			costs.add(costsI);
			restock.add(restockI);			
		} 
		
	}
	/**
	 * Split tools for role information
	 * @param toolList [<Identifier5>,....]
	 * @param toolNames variable to store return
	 */
	private void splitToolNames(String toolList, List<String> toolNames) {
		//Remove brackets:
		toolList = toolList.substring(1,toolList.length()-1);
		
		if(!toolList.equals("")){
			String[] splits = toolList.split(",");
			
			for(String tool : splits){
				toolNames.add(tool);
			}
		}
		
	}
	/**
	 * Split material list for Product information
	 * @param materialList text with: [consumed(<Id3>,<Num4>),...,tools(<Id5>,<Num6>),...]
	 * @param components variable to store component names
	 * @param quantities quantities for each component
	 * @param toolNames name of needed tool
	 * @param toolQuantities quantity of tool
	 */
	private void splitItemProdList(String materialList, List<String> components, List<Integer> quantities,
			List<String> toolNames, List<Integer> toolQuantities) {
		
		//System.out.println("FULL Material: "+materialList);
		//Remove brackets:
		materialList = materialList.substring(1,materialList.length()-1);
		//Find first tool
		Matcher mt = Pattern.compile("tools\\(.*\\)").matcher(materialList);
		
		
		
		String consumedInfo,toolInfo;
		if(mt.find()){ //At least one tool exists
			consumedInfo = materialList.substring(0,mt.start()-1);
			toolInfo = materialList.substring(mt.start());
		}else{//Only materials are described
			consumedInfo = materialList;
			toolInfo = "";
		}
		
		//Consumed Information
		String[] splits = consumedInfo.split("(consumed\\()|(,consumed\\()"); //will result in <id>,<num>)
		
		for(int i=1;i<splits.length;i++){
			String item = splits[i];

			//Remove last parenthesis
			item = item.substring(0, item.length() -1);
			//System.out.println("Item Product: "+item);
			String[] itemInfo = item.split(",");
			String consumedName = itemInfo[0];
			int quantity = Integer.parseInt(itemInfo[1]);
			
			components.add(consumedName);
			quantities.add(quantity);
			
		} 
		//Tool Information
		if(!toolInfo.equals("")){
			splits = consumedInfo.split("(tools\\()|(,tools\\()"); //will result in <id>,<num>)
			
			for(int i=1;i<splits.length;i++){
				String item = splits[i];

				//Remove last parenthesis
				item = item.substring(0, item.length() -1);

				String[] toolsInfo = item.split(",");
				String toolName = toolsInfo[0];
				int quantity = Integer.parseInt(toolsInfo[1]);
				
				toolNames.add(toolName);
				toolQuantities.add(quantity);
			} 
		}
		
	}

	/**
	 * get the list of items with delivered quantities
	 * @param parameter to split
	 * @param itemNames variable to be used to include itemNames
	 * @param quantities variable to be used to include quantities
	 * @param delivered quantity delivered for each item
	 */
	private void splitItemListWithDelivered(String parameter, List<String> itemNames, List<Integer> quantities,
			List<Integer> delivered) {
		// The format is [item(<Id>,<quant>,<delivered),....]
				//Remove brackets:
				parameter = parameter.substring(1,parameter.length()-1);

				String[] splits = parameter.split("(item\\()|(,item\\()"); //will result in <id>,<num>)

				//Include each item in the variable
				for(int i=1;i<splits.length;i++){
					String item = splits[i];

					//Remove last parenthesis
					item = item.substring(0, item.length() -1);

					String[] itemInfo = item.split(",");
					String itemName = itemInfo[0];
					int quantity = Integer.parseInt(itemInfo[1]);
					int deliveredQ = Integer.parseInt(itemInfo[2]);

					itemNames.add(itemName);
					quantities.add(quantity);
					delivered.add(deliveredQ);
				} 
		
	}

	/**
	 * get the list of items
	 * @param parameter to split
	 * @param itemNames variable to be used to include itemNames
	 * @param quantities variable to be used to include quantities
	 */
	private void splitItemList(String parameter, List<String> itemNames, List<Integer> quantities, boolean useQuantity) {
		// The format is [item(<Id>,<quant>),....]
		//Remove brackets:
		parameter = parameter.substring(1,parameter.length()-1);

		String[] splits = parameter.split("(item\\()|(,item\\()"); //will result in <id>,<num>)

		//Include each item in the variable
		for(int i=1;i<splits.length;i++){
			String item = splits[i];

			//Remove last parenthesis
			item = item.substring(0, item.length() -1);

			String[] itemInfo = item.split(",");
			String itemName = itemInfo[0];
			
			if(useQuantity){
				int quantity = Integer.parseInt(itemInfo[1]);
				quantities.add(quantity);
			}
			itemNames.add(itemName);
			
		}
	}
	
	/**
	 * get list of items without quantity
	 * @param parameter item list
	 * @param itemNames variable to return
	 */
	private void splitItemList(String parameter,List<String> itemNames){
		splitItemList(parameter,itemNames,null,false);
	}

	/*
	 * 
	 * Classes for store info about facilities or other Objects in the CityWorld
	 */
	/**
	 * Stores data about jobs
	 * @author leno
	 *
	 */
	public class Job {
		boolean auctioned;
		String id;
		String storage;
		int beginStep;
		int endStep;

		/*
		 * Auction Jobs
		 */
		double fine;
		double maximumBid;
		//--
		/*
		 * Priced Jobs
		 */
		double reward;
		List<Integer> deliveredItens;
		//--

		List<String> itemNames;
		List<Integer> quantities;
		/**
		 * Constructor for auctioned jobs
		 */
		public Job(String id, String storage, int beginStep,int endStep,double fine,double maximumBid, List<String> items, List<Integer> quantities){
			this.auctioned = true;
			this.id = id;
			this.storage = storage;
			this.beginStep = beginStep;
			this.endStep = endStep;
			this.fine = fine;
			this.maximumBid = maximumBid;
			this.itemNames = items;
			this.quantities = quantities;
		}
		/**
		 * Update what items were already delivered
		 * @param delivered list of quantities delivered
		 */
		public void updateDelivered(List<Integer> delivered) {
			if(auctioned){
				throw new RuntimeException("updateDelived can be used only for price jobs");
			}
			this.deliveredItens = delivered;


		}
		/**
		 * Constructor for priced jobs
		 */
		public Job(String id, String storage,int beginStep,int endStep, double reward, List<String> items, List<Integer> quantities){
			this.auctioned = false;
			this.id = id;
			this.storage = storage;
			this.beginStep = beginStep;
			this.endStep = endStep;
			this.reward = reward;
			this.itemNames = items;
			this.quantities = quantities;
			//this.deliveredItens = new Hashtable<String,Integer>();
		}

	}
	/**
	 * Class with data about a charging station
	 * @author leno
	 *
	 */
	public class ChargingStation{
		String id;
		double lat;
		double lon;
		double chargingRate;
		double price;
		int slots;
		int queueSize;

		public ChargingStation(String id, double lat, double lon, double chargingRate, double price, int slots){
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.chargingRate = chargingRate;
			this.price = price;
			this.slots = slots;
		}

		public void updateQueueSize(int queueSize) {
			this.queueSize = queueSize;

		}

		public String getID() {
			return this.id;
		}
	}
	/**
	 * Class to score data about a dump station
	 * @author leno
	 *
	 */
	public class DumpStation{
		String id;
		double lat;
		double lon;
		double price;

		public DumpStation(String id, double lat, double lon, double price){
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.price = price;
		}
		
		public String getID() {
			return this.id;
		}
	}


	/**
	 * Class to store entity information
	 * @author leno
	 *
	 */
	public class EntityRecord{
		String id;
		String team;
		double lat;
		double lon;
		String role;


		public EntityRecord(String id, String team, double lat, double lon, String role){
			this.id = id;
			this.team = team;
			this.lat = lat;
			this.lon = lon;
			this.role = role;
		}


		public void updatePosition(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}


		public String getRole() {
			// TODO Auto-generated method stub
			return role;
		}
	}
	/**
	 * Stores product information
	 * @author leno
	 *
	 */
	public class Product{
		String id;
		int volume;
		List<String> components;
		List<Integer> quantities;
		List<String> toolNames;
		List<Integer> toolQuantities;

		public Product(String id, int volume, List<String> components, List<Integer> quantities,List<String> toolNames,List<Integer> toolQuantities){
			this.id = id;
			this.volume = volume;
			this.components = components;
			this.quantities = quantities;
			this.toolNames = toolNames;
			this.toolQuantities = toolQuantities;
		}
	}
	/**
	 * Stores information about shops
	 * @author leno
	 *
	 */
	public class Shop{
		String id;
		double lat;
		double lon;
		List<String> itemNames; 
		List<Double> costs;
		List<Integer> amount;
		List<Double> restock;
		/**
		 * Creates a new shop with product information
		 */
		public Shop(String id, double lat, double lon, List<String> itemList){
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.itemNames = itemList;			
		}
		/**
		 * Create a new shop with initialized quantities
		 */
		public Shop(String id, double lat, double lon, List<String> itemNames, List<Double> costs,
				List<Integer> amount, List<Double> restock) {
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.itemNames = itemNames;
			this.costs = costs;
			this.amount = amount;
			this.restock = restock;
		}
		
		public String getID() {
			return this.id;
		}

	}
	/**
	 * Stores information about storages
	 * @author leno
	 *
	 */
	public class Storage{
		String id;
		double lat;
		double lon;
		double price;
		double totalCapacity;
		double usedCapacity;
		//Hashtable<String,Integer> itemStored;
		//Hashtable<String,Integer> itemDelivered;
		List<String> itemNames;
		List<Integer> amountStored;
		List<Integer> amountDelivered;


		public Storage(String id, double lat, double lon, double price, int totalCapacity, int usedCapacity,
				List<String> itemNames, List<Integer> amountStored, List<Integer> amountDelivered) {
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.price = price;
			this.totalCapacity = totalCapacity;
			this.usedCapacity = usedCapacity;
			this.itemNames = itemNames;
			this.amountStored = amountStored;
			this.amountDelivered = amountDelivered;
		}


		public void updateStorageInformation(int usedCapacity, List<String> itemNames, List<Integer> amountStored,
				List<Integer> amountDelivered) {
			this.usedCapacity = usedCapacity;
			this.itemNames = itemNames;
			this.amountStored = amountStored;
			this.amountDelivered = amountDelivered;
			
		}
		
		public String getID() {
			return this.id;
		}
	}
	/**
	 * Information about Workshops
	 * @author leno
	 *
	 */
	public class Workshop{
		String id;
		double lat;
		double lon;
		double price;

		public Workshop(String id, double lat, double lon,double price){
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.price = price;
		}
		
		public String getID() {
			return this.id;
		}
	}
	
	/**
	 * Information about Roles.
	 * @author leno
	 *
	 */
	public class Role{
		String id;
		double speed;
		int loadCapacity;
		double batteryCapacity;
		List<String> tools;

		public Role(String id, double speed, int capacity, double battery, List<String> toolNames) {
			this.id = id;
			this.speed = speed;
			this.loadCapacity = capacity;
			this.batteryCapacity = battery;
			this.tools = toolNames;
		}

		public String getId() {
			return id;
		}

		public double getSpeed() {
			return speed;
		}

		public int getLoadCapacity() {
			return loadCapacity;
		}

		public double getBatteryCapacity() {
			return batteryCapacity;
		}
	}

	/**
	 * debug function for tests
	 */
	public static void debug() {
		GlobalPercepts percepts = GlobalPercepts.getInstance();
		System.out.println("Global Percepts Debug:");
		System.out.println("-----------------------");
		System.out.println("Auction Jobs:  "+percepts.auctionJobs.toString());
		System.out.println("Priced Jobs:   "+percepts.pricedJobs.toString());
		System.out.println("Posted Jobs:   "+percepts.postedJobs.toString());
		System.out.println("Taken Jobs:    "+percepts.takenJobs.toString());
		System.out.println("Bye:           "+bye);
		System.out.println("Charg. Stat.:  "+percepts.chargingStations.toString());
		System.out.println("Dumps:         "+percepts.dumpFacilities.toString());
	    System.out.println("Entities:      "+percepts.entities.toString());
		System.out.println("Products:      "+percepts.products.toString());
		System.out.println("Shops:         "+percepts.shops.toString());
		System.out.println("Storages:      "+percepts.storages.toString());
		System.out.println("Workshops:     "+percepts.workshops.toString());
		System.out.println("Roles:         "+percepts.roles.toString());
		System.out.println("Ranking:       "+percepts.ranking);
		System.out.println("Simulation:    "+simulationID);
		System.out.println("MAP:           "+map);
		System.out.println("Money:         "+money);
		System.out.println("Step:          "+step);
		System.out.println("Steps:         "+steps);
		System.out.println("Timestamp:     "+timestamp);
		System.out.println("SeedCapital:   "+seedCapital);
		System.out.println("Team:          "+team);
		System.out.println("Deadline:      "+deadline);
		System.out.println("-----------------------");
	}


	/**
	 * Returns the nearest charging station in relation to a given point
	 * @param lat latitude
	 * @param lon longitude
	 * @return the charging station
	 */
	public static ChargingStation getNearestChargingStation(double lat, double lon) {
		//Find the closest charging station
		double distance = Double.MAX_VALUE;
		ChargingStation closestChSt = null;
		//iterates over all charging stations
		for(ChargingStation chargingSt : chargingStations.values()){
			double distCurrent =  DistanceCalculator.distance(lat, lon, chargingSt.lat, chargingSt.lon);
			if(distCurrent<distance){
				closestChSt = chargingSt;
				distance = distCurrent;
			}
		}
		
		return closestChSt;
	}
	
	/**
	 * Returns the nearest dumping station in relation to a given point
	 * @param lat latitude
	 * @param lon longitude
	 * @return the dumping station
	 */
	public static DumpStation getNearestDumpStation(double lat, double lon) {
		//Find the closest charging station
		double distance = Double.MAX_VALUE;
		DumpStation closestChSt = null;
		//iterates over all charging stations
		for(DumpStation chargingSt : dumpFacilities.values()){
			double distCurrent =  DistanceCalculator.distance(lat, lon, chargingSt.lat, chargingSt.lon);
			if(distCurrent<distance){
				closestChSt = chargingSt;
				distance = distCurrent;
			}
		}
		
		return closestChSt;
	}
	
	/**
	 * Returns the nearest Shop in relation to a given point
	 * @param lat latitude
	 * @param lon longitude
	 * @return the Shop
	 */
	public static Shop getNearestShop(double lat, double lon) {
		//Find the closest charging station
		double distance = Double.MAX_VALUE;
		Shop closestChSt = null;
		//iterates over all charging stations
		for(Shop chargingSt : shops.values()){
			double distCurrent =  DistanceCalculator.distance(lat, lon, chargingSt.lat, chargingSt.lon);
			if(distCurrent<distance){
				closestChSt = chargingSt;
				distance = distCurrent;
			}
		}
		
		return closestChSt;
	}
	
	/**
	 * Returns the nearest Storage in relation to a given point
	 * @param lat latitude
	 * @param lon longitude
	 * @return the Storage
	 */
	public static Storage getNearestStorage(double lat, double lon) {
		//Find the closest charging station
		double distance = Double.MAX_VALUE;
		Storage closestChSt = null;
		//iterates over all charging stations
		for(Storage chargingSt : storages.values()){
			double distCurrent =  DistanceCalculator.distance(lat, lon, chargingSt.lat, chargingSt.lon);
			if(distCurrent<distance){
				closestChSt = chargingSt;
				distance = distCurrent;
			}
		}
		
		return closestChSt;
	}
	
	/**
	 * Returns the nearest Workshop in relation to a given point
	 * @param lat latitude
	 * @param lon longitude
	 * @return the Workshop
	 */
	public static Workshop getNearestWorkshop(double lat, double lon) {
		//Find the closest charging station
		double distance = Double.MAX_VALUE;
		Workshop closestChSt = null;
		//iterates over all charging stations
		for(Workshop chargingSt : workshops.values()){
			double distCurrent =  DistanceCalculator.distance(lat, lon, chargingSt.lat, chargingSt.lon);
			if(distCurrent<distance){
				closestChSt = chargingSt;
				distance = distCurrent;
			}
		}
		
		return closestChSt;
	}
	
	/**
	 * Returns the agent role or null
	 * @param agentName agent name
	 * @return the Role
	 */
	public static Role getRoleByAgentName(String agentName){
		EntityRecord ent = entities.get(agentName);
		return roles.get(ent.getRole());
	}
}

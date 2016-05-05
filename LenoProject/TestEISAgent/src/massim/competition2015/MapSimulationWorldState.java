package massim.competition2015;

import java.util.*;
import java.util.Map.Entry;

import massim.competition2015.configuration.*;
import massim.competition2015.configuration.FacilityConfiguration.FacilityStock;
import massim.competition2015.scenario.*;
import massim.competition2015.scenario.Shop.ShopStock;
import massim.framework.simulation.WorldState;
import massim.gridsimulations.SimulationWorldState;

import static massim.framework.util.DebugLog.LOGLEVEL_DEBUG;
import static massim.framework.util.DebugLog.LOGLEVEL_NORMAL;
import static massim.framework.util.DebugLog.log;

/**
 * Holds the current state of a map simulation (2015 Map Scenario)
 */
public class MapSimulationWorldState extends SimulationWorldState implements WorldState {

	private static final long serialVersionUID = -6316439899157240323L;

	// World model values


	/**
	 * A map from an agent's name to its current state. It is used as a cache to
	 * provide fast access to this information that is heavily accessed during
	 * the simulation execution.
	 */
	protected Map<String, MapSimulationAgentState> agentNamesMap;

	/**
	 * An ArrayList holding all agents that take part in the simulation.
	 */
	protected ArrayList<MapSimulationAgentState> agents;
	
	/**
	 * A map from an agent's location to its current state. It is used as a cache to
	 * provide fast access to this information that is heavily accessed during
	 * the simulation execution.
	 */
	protected Map<Location, MapSimulationAgentState> agentLocationMap;

	protected ArrayList<Facility> facilities;
	protected Map<String, Facility> facilityNamesMap;
	protected Map<Location, Facility> facilityLocatiosMap;
	
	protected ArrayList<Item> items;
	protected Map<String, Item> itemsNamesMap;
	
	protected ArrayList<Job> jobs;
	protected Map<String, Job> jobsNamesMap;
	

	public Map<String, MapSimulationAgentState> getAgentNamesMap() {
		return agentNamesMap;
	}



	public void setAgentNamesMap(Map<String, MapSimulationAgentState> agentNamesMap) {
		this.agentNamesMap = agentNamesMap;
	}



	public Map<Location, MapSimulationAgentState> getAgentLocationMap() {
		return agentLocationMap;
	}



	public void setAgentLocationMap(
			Map<Location, MapSimulationAgentState> agentLocationMap) {
		this.agentLocationMap = agentLocationMap;
	}


	public Collection<Facility> getAllFacilities() {
		return new ArrayList<Facility>(facilities);
	}

	public Facility getFacility(String name) {
		return facilityNamesMap.get(name);
	}



	public void setFacilityNamesMap(Map<String, Facility> facilityNamesMap) {
		this.facilityNamesMap = facilityNamesMap;
	}


	public Facility getFacilityAtLocation(Location loc) {
		return facilityLocatiosMap.get(loc);
	}




	public void setFacilityLocatiosMap(Map<Location, Facility> facilityLocatiosMap) {
		this.facilityLocatiosMap = facilityLocatiosMap;
	}



	public Item getItem(String id) {
		return itemsNamesMap.get(id);
	}



	public void setItemsNamesMap(Map<String, Item> itemsNamesMap) {
		this.itemsNamesMap = itemsNamesMap;
	}


	public Collection<Job> getAllJobs() {
		return new ArrayList<Job>(jobs);
	}
	
	public Map<String, Job> getJobsNamesMap() {
		return jobsNamesMap;
	}



	public void setJobsNamesMap(Map<String, Job> jobsNamesMap) {
		this.jobsNamesMap = jobsNamesMap;
	}

	/**
	 * The configuration of this simulation.
	 */
	protected MapSimulationConfiguration config;

	/**
	 * An ArrayList holding the states of all the teams that take part in the
	 * simulation.
	 */
	public ArrayList<TeamState> teamsStates;

	
	/**
	 * The city map - graph implementation of the map.
	 */
	public CityMap cityMap;

	protected double minLat;
	protected double minLon;
	protected double maxLat;
	protected double maxLon;
	protected double proximity;
	protected double cellSize;

	private Location mapCenter;
    private Set<String> carRoads = new HashSet<>(Arrays.asList("road"));

	// store products' basecosts
	public Map<Item, Integer> productBaseCosts = new HashMap<>();

	// Used for deciding anything that is random
	private Random random;

	/**
	 * Creates a simulation state as defined by <code>config</code>
	 *
	 * @param config
	 */
	public MapSimulationWorldState(MapSimulationConfiguration config) {
		this.simulationName = config.simulationName;
		
		this.agents = new ArrayList<>();
		this.agentNamesMap = new HashMap<>();

		this.teamsStates = new ArrayList<>();

		this.config = config;
		
		this.minLat = config.minLat;
		this.minLon = config.minLon;
		this.maxLat = config.maxLat;
		this.maxLon = config.maxLon;

		this.proximity = config.proximity;
		this.cellSize = config.cellSize;
		Location.setProximity(config.proximity);
		
		this.cityMap = new CityMap(config.mapName, cellSize, proximity);

        if(config.randomAgentLocs || config.generateFacilities){
            this.mapCenter = this.cityMap.getNearestRoad(new Location(config.mapCenterLon, config.mapCenterLat));
        }

		if(config.generateProducts){
			generateProducts();
            log(LOGLEVEL_NORMAL, "Products generated.");
		}
		else{
			addProducts();
		}

		if(config.generateFacilities){
			generateFacilities();
            log(LOGLEVEL_NORMAL, "Facilities generated.");
		}
		else{
			addFacilities();
		}

		if(config.generateJobs){
			createJobGenerator();
		}
		else{
			addJobs();
		}
	}

	/**
	 * Creates job datastructures.
	 * Doesn't really create a JobGenerator (yet).
	 */
	private void createJobGenerator() {
		this.jobs = new ArrayList<>();
		this.jobsNamesMap = new HashMap<>();
	}

	private void generateFacilities() {

		this.facilities = new ArrayList<>();
		this.facilityNamesMap = new HashMap<>();
		this.facilityLocatiosMap = new HashMap<>();

		double quad = config.quadSize;

		//generate chargingStations
		for (double a = minLat; a < maxLat; a += quad){
			for (double b = minLon; b < maxLon; b += quad){
				// (a,b) = corner of the current quad
				int numberOfFacs = 0;
				if(config.densityChargingStations < 1){
					if(getRandom().nextFloat() < config.densityChargingStations){
						numberOfFacs = 1;
					}
				}
				else{
					numberOfFacs = new Float(config.densityChargingStations).intValue();
				}
				for(int i = 0; i < numberOfFacs; i++){

					Location loc;
					try {
						loc = getFreeLocation(a, b, true, 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue; //ignore this facility since it couldn't be placed
					}

					createAndAddChargingStation(loc);
				}
			}
		}

		//generate dumps
		for (double a = minLat; a < maxLat; a += quad){
			for (double b = minLon; b < maxLon; b += quad){
				// (a,b) = corner of the current quad
				int numberOfFacs = 0;
				if(config.densityDumps < 1){
					if(getRandom().nextFloat() < config.densityDumps){
						numberOfFacs = 1;
					}
				}
				else{
					numberOfFacs = new Float(config.densityDumps).intValue();
				}
				for(int i = 0; i < numberOfFacs; i++){
					Location loc;
					try {
                        loc = getFreeLocation(a, b, true, 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue; //ignore this facility since it couldn't be placed
					}

                    createAndAddDumpLocation(loc);
				}
			}
		}

		//generate workshops
		for (double a = minLat; a < maxLat; a += quad){
			for (double b = minLon; b < maxLon; b += quad){
				// (a,b) = corner of the current quad
				int numberOfFacs = 0;
				if(config.densityWorkshops < 1){
					if(getRandom().nextFloat() < config.densityWorkshops){
						numberOfFacs = 1;
					}
				}
				else{
					numberOfFacs = new Float(config.densityWorkshops).intValue();
				}
				for(int i = 0; i < numberOfFacs; i++){
					Location loc;
					try {
                        loc = getFreeLocation(a, b, true, 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue; //ignore this facility since it couldn't be placed
					}

					createAndAddWorkshop(loc);
				}
			}
		}

		//generate Storages
		for (double a = minLat; a < maxLat; a += quad){
			for (double b = minLon; b < maxLon; b += quad){
				// (a,b) = corner of the current quad
				int numberOfFacs = 0;
				if(config.densityStorages < 1){
					if(getRandom().nextFloat() < config.densityStorages){
						numberOfFacs = 1;
					}
				}
				else{
					numberOfFacs = new Float(config.densityStorages).intValue();
				}
				for(int i = 0; i < numberOfFacs; i++){
					Location loc;
					try {
                        loc = getFreeLocation(a, b, true, 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue; //ignore this facility since it couldn't be placed
					}

					createAndAddStorage(loc);
				}
			}
		}

		//generate Shops
		Set<Item> usedItems = new HashSet<>();
		Vector<Shop> shops = new Vector<>();
		for (double a = minLat; a < maxLat; a += quad){
			for (double b = minLon; b < maxLon; b += quad){
				// (a,b) = corner of the current quad
				int numberOfFacs = 0;
				if(config.densityShops < 1){
					if(getRandom().nextFloat() < config.densityShops){
						numberOfFacs = 1;
					}
				}
				else{
					numberOfFacs = new Float(config.densityShops).intValue();
				}
				for(int i = 0; i < numberOfFacs; i++){
					Location loc;
					try {
                        loc = getFreeLocation(a, b, true, 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue; //ignore this facility since it couldn't be placed
					}

                    shops.add(createAndAddShop(loc, usedItems));
				}
			}
		}

        if (numberOfShops == 0){
            while (true){
                try {
                    Location loc = getFreeLocation(config.minLat, config.minLon, false, -1);
                    shops.add(createAndAddShop(loc, usedItems));
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; //ignore this facility since it couldn't be placed
                }
            }
        }
        if (numberOfStorages == 0){
            while (true){
                try {
                    Location loc = getFreeLocation(config.minLat, config.minLon, false, -1);
                    createAndAddStorage(loc);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; //ignore this facility since it couldn't be placed
                }
            }
        }
        if (numberOfWorkshops == 0){
            while (true){
                try {
                    Location loc = getFreeLocation(config.minLat, config.minLon, false, -1);
                    createAndAddWorkshop(loc);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; //ignore this facility since it couldn't be placed
                }
            }
        }
        if (numberOfChargingStations == 0){
            while (true){
                try {
                    Location loc = getFreeLocation(config.minLat, config.minLon, false, -1);
                    createAndAddChargingStation(loc);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; //ignore this facility since it couldn't be placed
                }
            }
        }
        if (numberOfDumpLocations == 0){
            while (true){
                try {
                    Location loc = getFreeLocation(config.minLat, config.minLon, false, -1);
                    createAndAddDumpLocation(loc);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; //ignore this facility since it couldn't be placed
                }
            }
        }


		//add all as of yet unsold items to a random shop each
		Set<Item> unusedItems = new HashSet<>(this.items);
		unusedItems.removeAll(usedItems);
		for(Item i: unusedItems){
			//generate stock
			ShopStock sst = generateShopStock(i);
			//add to random shoppe
			Shop randomShop = shops.get(getRandom().nextInt(shops.size()));
			randomShop.stock.put(i, sst);
		}
	}

    private int numberOfShops = 0;
    private Shop createAndAddShop(Location loc, Set<Item> usedItems) {
        Shop sh = new Shop();
        sh.name = "shop"+numberOfShops++;
        sh.location = loc;
        //generate shoppe stuff
        sh.stock = new HashMap<>();
        int prodAmount = config.shopProdMin + getRandom().nextInt(config.shopProdMax-config.shopProdMin+1);
        List<Item> unusedItems = new LinkedList<>(this.items); //draw products for the shop only from a list of items that are not yet sold here
        for(int j = 0; j < prodAmount; j++){
            //draw a random item
            int prodNumber = getRandom().nextInt(unusedItems.size());
            Item it = unusedItems.get(prodNumber);
            unusedItems.remove(prodNumber);
            usedItems.add(it); //add to list of items used by ANY shop
            //generate stock for the item
            ShopStock sStock = generateShopStock(it);
            sh.stock.put(sStock.item, sStock);
        }

        addFacility(sh);
        return sh;
    }

    private int numberOfStorages = 0;
    private void createAndAddStorage(Location loc) {
        Storage st = new Storage();
        st.name = "storage"+numberOfStorages++;
        st.location = loc;
        st.price = config.storageCostMin + getRandom().nextInt(config.storageCostMax-config.storageCostMin+1);
        st.totalCapacity = config.storageCapacityMin + getRandom().nextInt(config.storageCapacityMax-config.storageCapacityMin+1);
        addFacility(st);
    }

    private int numberOfWorkshops = 0;
    private void createAndAddWorkshop(Location loc) {
        Workshop ws = new Workshop();
        ws.name = "workshop"+numberOfWorkshops++;
        ws.location = loc;
        ws.price = config.wsCostMin + getRandom().nextInt(config.wsCostMax-config.wsCostMin+1);
        addFacility(ws);
    }

    private int numberOfDumpLocations = 0;
    private void createAndAddDumpLocation(Location loc) {
        DumpLocation d = new DumpLocation();
        d.name = "dump"+numberOfDumpLocations++;
        d.location = loc;
        d.price = config.dumpCostMin + getRandom().nextInt(config.dumpCostMax-config.dumpCostMin+1);
        addFacility(d);
    }

    private int numberOfChargingStations = 0;
    private void createAndAddChargingStation(Location loc) {
        ChargingStation cs = new ChargingStation();
        cs.name = "charging"+numberOfChargingStations++;
        cs.location = loc;
        cs.chargingRate = config.rateMin + random.nextInt(config.rateMax-config.rateMin+1);
        cs.fuelPrice = config.csCostMin + getRandom().nextInt(config.csCostMax-config.csCostMin+1);
        cs.maxConcurrentCharging = config.concurrMin + getRandom().nextInt(config.concurrMax-config.concurrMin+1);
        addFacility(cs);
    }

    /**
	 * Generates ShopStock for a single item according to config values.
	 * @param i the item
	 * @return the ShopStock object
	 */
	public ShopStock generateShopStock(Item i){
		ShopStock sStock =  new ShopStock();
		sStock.item = i;

		//draw amount
		sStock.amount = config.shopAmountMin + getRandom().nextInt(config.shopAmountMax-config.shopAmountMin+1);

		//draw price modifier
		float costFactor = config.shopPriceAddMin + getRandom().nextInt(config.shopPriceAddMax-config.shopPriceAddMin+1);
		costFactor/=100.0f; //scale
		costFactor += 1; //make sure it is > 1

		//draw "assembled" addition (if necessary)
		float assembledAddition = 0.0f;
		if(i.userAssembled){
			assembledAddition = config.shopAssembleAddMin + getRandom().nextInt(config.shopAssembleAddMax-config.shopAssembleAddMin+1);
			assembledAddition/=100.0f;
		}
        assembledAddition += 1;

		sStock.cost = new Float(productBaseCosts.get(sStock.item) * costFactor * assembledAddition).intValue();
        sStock.restock = config.shopRestockMin + getRandom().nextInt(config.shopRestockMax-config.shopRestockMin+1);
		return sStock;
	}

	/**
	 * Returns a free location in the world or in
     * the quad denoted by upper left corner (a,b) (determined by facilityLocationsMap)
	 * @param a beginning of the quad (lat) or minLat of the map (implicit)
	 * @param b beginning of the quad (lon) or minLon of the map (implicit)
     * @param quad search the quad (else the whole map)
     * @param maxTries try this often to find a free location (negative value = possibly infinitely often)
	 * @return a free location
	 * @throws Exception if a free location hasn't been found after the specified number of random tries
	 */
	private Location getFreeLocation(double a, double b, boolean quad, int maxTries) throws Exception {
		double lonRange;
        double latRange;
        if(quad){
            lonRange = config.quadSize;
            latRange = config.quadSize;
        }
        else{
            lonRange = config.maxLon-config.minLon;
            latRange = config.maxLat-config.minLat;
            a = config.minLat;
            b = config.minLon;
        }
        Location loc;
		int tries = 0;
        boolean tryAgain;
		do {
            tryAgain = false;
			//get an unused location
			double lat = a + getRandom().nextDouble() * latRange;
			double lon = b + getRandom().nextDouble() * lonRange;
            loc = this.cityMap.getNearestRoad(new Location(lon, lat));

            //check reachability from and of center
            Route centerRoute = this.cityMap.getNewRoute(loc, this.mapCenter, carRoads);
            Route centerBackRoute = this.cityMap.getNewRoute(this.mapCenter, loc, carRoads);
            if (centerRoute == null || centerBackRoute == null){
                tryAgain = true;
            }
		} while( (tryAgain || this.facilityLocatiosMap.containsKey(loc) ) && tries++ != maxTries);

		if(tries == maxTries){
			throw new Exception("Impossible to find free location in due time. Decrease proximity, density or increase quadSize.");
		}

		return loc;
	}

	/**
	 * Get a random point within min and max lat/lon bounds snapped to the closest road.
	 * @return see above
	 */
	private Location getRandomSnappedPoint(Set<String> roads) {
		Location loc;
        for (int i = 0; i < 1000; i++) {
            double latDiff = config.maxLat - config.minLat;
            double lonDiff = config.maxLon - config.minLon;
            double lat = config.minLat + getRandom().nextDouble() * latDiff;
            double lon = config.minLon + getRandom().nextDouble() * lonDiff;
            loc = this.cityMap.getNearestRoad(new Location(lon, lat));
            Route centerRoute = this.cityMap.getNewRoute(loc, this.mapCenter, roads);
            Route centerBackRoute = this.cityMap.getNewRoute(this.mapCenter, loc, roads);
            if(centerRoute != null && centerBackRoute != null){
                return loc;
            }
        }
        return null;
	}

	private void addFacility(Facility f){
		this.facilities.add(f);
		this.facilityNamesMap.put(f.name, f);
		this.facilityLocatiosMap.put(f.location, f);
	}

	private void updateRoleTools(Vector<Item> tools) {
		Vector<RoleConfiguration> roles = new Vector<>(config.rolesConfMap.values());
		for(RoleConfiguration role: roles){
			role.tools = new HashSet<>();
		}
		for (Item tool: tools){
			int roleNumber = getRandom().nextInt(roles.size());
			if(roles.get(roleNumber).loadCapacity>tool.volume) {
				roles.get(roleNumber).tools.add(tool.name);
			}
			else{
				config.getRoleConf("Truck").tools.add(tool.name);
			}

			if(getRandom().nextInt(100)<50){ //potentially add tool to a second role (it could be the role cannot carry the tool)
				roleNumber = getRandom().nextInt(roles.size());
				roles.get(roleNumber).tools.add(tool.name);
			}
		}
	}

	private void generateProducts() {
		int productsMinAmount = config.productsMinAmount;
		int productsMaxAmount = config.productsMaxAmount;
		int productsMinVolume = config.productsMinVolume;
		int productsMaxVolume = config.productsMaxVolume;

		int productAmount = productsMinAmount + getRandom().nextInt(productsMaxAmount-productsMinAmount+1);

		this.items = new ArrayList<>();
		this.itemsNamesMap = new HashMap<>();

		//store assembled items for later
		Vector<Item> assembledItems = new Vector<>();

		Vector<Item> toolItems = new Vector<>();

		//create basic items
		for(int i = 0; i < productAmount; i++){
			Item item = new Item();

			item.name = "item"+i;

			int assembled = getRandom().nextInt(100);
			item.userAssembled = assembled < (config.assembled*100);
			if(item.userAssembled) {
				assembledItems.add(item);
			}
			else{
				//item is not assembled, thus decide value and volume
				item.volume = productsMinVolume + getRandom().nextInt(productsMaxVolume-productsMinVolume+1);
				this.productBaseCosts.put(item, config.productValueMin + getRandom().nextInt(config.productValueMax-config.productValueMin+1));
				//for assembled items, this is calculated once the requirements are decided
			}

			this.items.add(item);
			this.itemsNamesMap.put(item.name, item);
		}

		//determine tools
		int numberOfTools = (int)((float)config.toolPercentage*(float)this.items.size()/100.0f);
		Vector<Item> tempItems = new Vector<>(this.items);
		Collections.shuffle(tempItems, getRandom());
		for(int i = 0; i < numberOfTools; i++){
			toolItems.add(tempItems.get(i));
		}

		//Now: Determine requirements for assembled items.

		//all items which can be used as requirements
		Vector<Item> requirementItems = new Vector<>(this.items);
		requirementItems.removeAll(assembledItems);

		for(Item item: assembledItems){
			int requiredAmount = config.productsReqMin + getRandom().nextInt(config.productsReqMax-config.productsReqMin+1);
			Collections.shuffle(requirementItems, getRandom());
			int volume = 0;
			int baseValue = 0;

			//ensure that we only require as much items as we can
			if(requiredAmount > requirementItems.size()) requiredAmount = requirementItems.size();

			for(int i = 0; i < requiredAmount; i++){
				Item requirement = requirementItems.get(i);
				//decide amount
				int amount = config.productsReqAmountMin + getRandom().nextInt(config.productsReqAmountMax-config.productsReqAmountMin+1);
				//add value and volume of components
				volume += (amount * requirement.volume);
				baseValue += (amount * this.productBaseCosts.get(requirement));
				if(toolItems.contains(requirement)){
					//req is a tool
					item.toolsNeeded.put(requirement, amount);
				}
				else{
					//req is a "material"
					item.itemsConsumed.put(requirement, amount);
				}
			}
			//subtract random percentage from volume (up to 50%)
			float perc = getRandom().nextFloat();
			volume -= perc * 0.5f * volume;

			//ensure that at least the truck can carry this item
			if(volume > config.getRoleConf("Truck").loadCapacity){
				volume = new Float(config.getRoleConf("Truck").loadCapacity * 0.9f).intValue();
			}

			item.volume = volume;
            log(LOGLEVEL_NORMAL, item.name+" Volume: "+volume);
			this.productBaseCosts.put(item, baseValue);

			//this item can now be a requirement for other items (since it definitely goes back to non-assembled materials)
			requirementItems.add(item);
		}

		//update which role can use which tool
		updateRoleTools(toolItems);
	}


	/**
	 * getter for the ArrayList holding all agents that take part in the
	 * simulation.
	 *
	 * @return
	 */
	public ArrayList<MapSimulationAgentState> getAgents() {
		return this.agents;
	}

	/**
	 * setter for the ArrayList holding all agents that take part in the
	 * simulation.
	 *
	 * @param agents
	 */
	public void setAgents(ArrayList<MapSimulationAgentState> agents) {
		this.agents = agents;
	}

	/**
	 * getter for the configuration object.
	 *
	 * @return
	 */
	public MapSimulationConfiguration getConfig() {
		return this.config;
	}

	/**
	 * Returns the state of an agent given its name.
	 *
	 * @param agentName
	 * @return
	 */
	public MapSimulationAgentState getAgent(String agentName) {
		return this.agentNamesMap.get(agentName);
	}

	// private static int team1Used = 0;
	// private static int team2Used = 0;
	// private static String team1Name = "";
	/**
	 * Adds <code>agent</code> to the currently simulation, and situates it in a
	 * random node in the map.
	 * 
	 * @param agent
	 */
	public void addAgent(MapSimulationAgentState agent, List<Location> agentPositions, boolean newPosition, int index) {
		
		this.agents.add(agent);
		this.agentNamesMap.put(agent.name, agent);

		if (getTeamNr(agent.team) == -1) {
			int idx = this.teamsStates.size();
			this.teamsStates.add(new massim.competition2015.scenario.TeamState(agent.team, idx, this.config.seedcapital));
		}
		agent.teamState = this.getTeamState(agent.team);
		if(config.randomAgentLocs){
			//generate new positions
			if(newPosition){
				Location loc = getRandomSnappedPoint(agent.role.roads);
				agentPositions.add(loc);
				agent.location = loc;
			}
			else{
				agent.location = agentPositions.get(index - agentPositions.size());
			}
		}
	}

	public void addProducts() {
		
		this.items = new ArrayList<>();
		this.itemsNamesMap = new HashMap<>();
		
		for (ProductConfiguration prodConf : getConfig().productsConf){
			Item item = new Item();
			item.name = prodConf.id;
			item.volume = prodConf.volume;
			item.userAssembled = prodConf.userAssembled;
			this.items.add(item);
			this.itemsNamesMap.put(item.name, item);
		}
		for (ProductConfiguration prodConf : getConfig().productsConf){
			if (prodConf.userAssembled){
				for(Entry <String,Integer> prodEntry : prodConf.itemsConsumed.entrySet()){
					this.itemsNamesMap.get(prodConf.id).itemsConsumed.put(
							this.itemsNamesMap.get(prodEntry.getKey()),
							prodEntry.getValue());
				}
				for(Entry <String,Integer> prodEntry : prodConf.toolsNeeded.entrySet()){
					this.itemsNamesMap.get(prodConf.id).toolsNeeded.put(
							this.itemsNamesMap.get(prodEntry.getKey()),
							prodEntry.getValue());
				}
			}
		}
		
	}
	
	public void addFacilities() {
		
		this.facilities = new ArrayList<>();
		this.facilityNamesMap = new HashMap<>();
		this.facilityLocatiosMap = new HashMap<>();
		
		for (FacilityConfiguration facConf : getConfig().facilitiesConf){
			
			
			Location location = new Location(facConf.lon,facConf.lat);
			Facility newFac = null;
			if ("shop".equals(facConf.type)){
				Shop sh = new Shop();
				sh.name = facConf.id;
				sh.location = location;
				sh.stock = new HashMap<>();
				for (FacilityStock facStock : facConf.stock){
					ShopStock sStock =  new ShopStock();
					sStock.item = this.itemsNamesMap.get(facStock.id);
					sStock.amount = facStock.amount;
					sStock.cost = facStock.cost;
					sStock.restock = facStock.restock;
					sh.stock.put(sStock.item, sStock);
				}
				newFac = sh;

			} else if ("workshop".equals(facConf.type)){
				Workshop ws = new Workshop();
				ws.name = facConf.id;
				ws.location = location;
				ws.price = facConf.cost;
				newFac = ws;
				
			} else if ("storage".equals(facConf.type)){
				Storage st = new Storage();
				st.name = facConf.id;
				st.location = location;
				st.price = facConf.cost;
				st.totalCapacity = facConf.capacity;
				newFac = st;
				
			} else if ("dump".equals(facConf.type)){
				DumpLocation dl = new DumpLocation();
				dl.name = facConf.id;
				dl.location = location;
				dl.price = facConf.cost;
				newFac = dl;
				
			} else if ("charging".equals(facConf.type)){
				ChargingStation cs = new ChargingStation();
				cs.name = facConf.id;
				cs.location = location;
				cs.chargingRate = facConf.rate;
				cs.fuelPrice = facConf.cost;
				cs.maxConcurrentCharging = facConf.concurrent;
				newFac = cs;
			}
			
			if (newFac != null){
				if (this.facilityNamesMap.containsKey(newFac.name) ||
						this.facilityLocatiosMap.containsKey(newFac.location)){
					throw new Error("Configuration error: facilit with duplicated id or location");
				}
				
				this.facilities.add(newFac);
				this.facilityNamesMap.put(newFac.name, newFac);
				this.facilityLocatiosMap.put(newFac.location, newFac);
			}
			
		}
		
	}
	
	public void addJob(Job job) {
		this.jobs.add(job);
		this.jobsNamesMap.put(job.id, job);
	}
	
	public void addJobs() {
		
		this.jobs = new ArrayList<>();
		this.jobsNamesMap = new HashMap<>();
		
		
		for (JobConfiguration jobConf : getConfig().jobsConf){
			Job job = null;
			if ("priced".equals(jobConf.type)){
				PricedJob pJob = new PricedJob(getRandom(),jobConf.id);
				pJob.price = jobConf.reward;
				job=pJob;
				
			} else if ("auction".equals(jobConf.type)){
				AuctionJob aJob = new AuctionJob(getRandom(), jobConf.id);
				aJob.fine=jobConf.fine;
				aJob.maxPrice = jobConf.maxReward;
				aJob.firstStepAuction = jobConf.firstStepAuction;
				job=aJob;
			}
			job.poster = "system";
			job.firstStepActive = jobConf.firstStepActive;
			job.lastStepActive = jobConf.lastStepActive;
			job.storageId = jobConf.storageId;
			Facility storage = this.facilityNamesMap.get(job.storageId);
			if (! (storage instanceof Storage)) {
				throw new Error("Configuration error: non-existing storage for job");
			}
			job.storage = (Storage) storage;
			for (Entry<String,Integer> prodEntry : jobConf.products.entrySet()){
				job.addRequiredItem(
						itemsNamesMap.get(prodEntry.getKey()),
						prodEntry.getValue());
			}
			this.jobs.add(job);
			this.jobsNamesMap.put(job.id, job);
			
		}
		
	}

	/**
	 * Returns a numeric representation of the team name, -1 if the team has not
	 * been added to the teams list of the simulation.
	 * 
	 * The number representation is arbitrary, possibly affected by the order in
	 * which teams are added, and should remain for the duration of the match.
	 * 
	 * @param name
	 * @return
	 */
	// TODO stick to random team numbering?
	public int getTeamNr(String name) {
		massim.competition2015.scenario.TeamState ts = getTeamState(name);
		return ts != null ? ts.teamIdx : -1;
	}

	/**
	 * Provides a numeric representation of the team name, null if the number
	 * does not correspond to any team.
	 * 
	 * The number representation is arbitrary, possibly affected by the order in
	 * which teams are added, and should remain for the duration of the match.
	 * 
	 * @param name
	 * @return
	 */
	// TODO stick to random team numbering?
	public String getTeamName(int number) {
		if (number >= 0 && number < this.teamsStates.size()) {
			assert (this.teamsStates.get(number).teamIdx == number);
			return this.teamsStates.get(number).name;
		}
		return null;
	}

	/**
	 * Returns the state of team given its name.
	 * 
	 * @param name
	 * @return
	 */
	public TeamState getTeamState(String name) {
		for (TeamState ts : this.teamsStates) {
			if (ts.name.equals(name)) {
				return ts;
			}
		}
		return null;
	}

	/**
	 * returns the <code>Random</code> object. It always returns the same object
	 * to avoid creating new ones and initializing them with the same seed in
	 * successive calls that are performed to close together, resulting in
	 * repetition of the random number generated.
	 * 
	 * @return
	 */
	public Random getRandom() {
		if (this.random == null) {
			// random = new Random(System.currentTimeMillis());
			this.random = new Random(this.config.randomSeed);
		}
		return this.random;
	}

	private int nextJobStep = 1;
	private List<Storage> storages;

	/**
	 * Returns the set of storages (initialises it on first call)
	 * @return s.a.
	 */
	private List<Storage> getStorages(){
		if(this.storages==null) {
			this.storages = new Vector<Storage>();
			for (Facility f : this.facilities) {
				if (f instanceof Storage) {
					this.storages.add((Storage) f);
				}
			}
		}
		return this.storages;
	}

	/**
	 * Generates jobs for this step (if configured).
	 * Called by ActionExecutor in each step
	 */
	public void stepJobGeneration() {
		if( (!config.generateJobs) || !(currentStep == nextJobStep)){
			return; //no job to generate this time
		}

		//draw time of next job creation event
		Float nextJobStepCd =  new Float(-Math.log(1.0f - getRandom().nextFloat()) / config.jobRate);

        nextJobStep = Math.max(1, Math.round(nextJobStepCd)) + currentStep;

		//generate Job:

		boolean auction = getRandom().nextInt(100) < config.jobAuctionPerc;
		boolean badJob = getRandom().nextInt(100) < config.jobBad;

		Job job;

		//draw job value
		int value = config.jobValueMin + getRandom().nextInt(config.jobValueMax-config.jobValueMin+1);

		//draw required items based on value
		int actualValue = 0;
		Map<Item, Integer> reqItems = new HashMap<>();
		while(actualValue < value){
			//add another item
			Item it = this.items.get(getRandom().nextInt(this.items.size())); //draw random item
			//draw random amount
			int amount = 1 + getRandom().nextInt(config.jobProductMaxAmount);

			actualValue += amount * this.productBaseCosts.get(it);
			if(reqItems.containsKey(it)){
				reqItems.put(it, reqItems.get(it)+amount);
			}
			else{
				reqItems.put(it, amount);
			}
		}

		//draw reward (based on actual value)
		int reward;
		if(badJob){
			float sub = 1 + getRandom().nextInt(config.jobRewardSub);
			reward = new Float(actualValue - ((float)actualValue*sub/100.0f)).intValue();
		}
		else{
			float add = 1 + getRandom().nextInt(config.jobRewardAdd);
			reward = new Float(actualValue + ((float)actualValue*add/100.0f)).intValue();
		}

		if(auction){
			//generate an auction job
			job = new AuctionJob(getRandom());
			AuctionJob auctionJob = (AuctionJob)job;

			//draw and set job times
			auctionJob.firstStepAuction = currentStep;
			int auctionTime = config.jobAuctionTimeMin + getRandom().nextInt(config.jobAuctionTimeMax-config.jobAuctionTimeMin+1);
			auctionJob.firstStepActive = currentStep + auctionTime;

			//draw maxReward Limit
			float limit = 1 + getRandom().nextInt(config.jobAuctionMaxRewardAdd);
			auctionJob.maxPrice = new Float(reward + (float)reward*limit/100.0f).intValue();

			//draw fine
			int fineMod = 1 + getRandom().nextInt(config.jobAuctionFineSub+config.jobAuctionFineAdd);
			if(fineMod > config.jobAuctionFineSub){
				auctionJob.fine = new Float(actualValue + (float)actualValue * (float)(fineMod-config.jobAuctionFineSub)/100.0f).intValue();
			}
			else{
				auctionJob.fine = new Float(actualValue - (float)actualValue * (float)(fineMod)/100.0f).intValue();
			}
		}
		else{
			//generate a priced job
			job = new PricedJob(getRandom());
			PricedJob pricedJob = (PricedJob)job;

			pricedJob.firstStepActive = currentStep;
			pricedJob.price = reward;
		}

		job.poster = "system";

		//calculate end of job
		int activeTime = config.jobTimeMin + getRandom().nextInt(config.jobTimeMax-config.jobTimeMin+1);
		job.lastStepActive = job.firstStepActive + activeTime;

		//draw a random storage
		Storage storage = getStorages().get(getRandom().nextInt(getStorages().size()));
		job.storage = storage;
		job.storageId = storage.name;

		//add required items
		job.itemsRequired = reqItems;

		//add job
		this.jobs.add(job);
		this.jobsNamesMap.put(job.id, job);
	}
}

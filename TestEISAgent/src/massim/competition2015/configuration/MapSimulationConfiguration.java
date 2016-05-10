package massim.competition2015.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import massim.competition2015.configuration.FacilityConfiguration.FacilityStock;
import massim.framework.simulation.DefaultSimpleSimulationConfiguration;
import massim.framework.util.XMLCodec;
import massim.server.ServerSimulationConfiguration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static massim.framework.util.DebugLog.*;

/**
 * This class holds the simulation configuration specified in the XML config file.
 */
public class MapSimulationConfiguration extends DefaultSimpleSimulationConfiguration implements Serializable, ServerSimulationConfiguration, XMLCodec.XMLDecodable{
	
	private static final long serialVersionUID = 5802657031982257279L;	
	
	/**
	 * Tournament's name.
	 */
	public String tournamentName = "";
	
	/**
	 * Simulation's name.
	 */
	public String simulationName = "";
	
	private Vector<String> teamNames;
	
	/**
	 * The max number of steps that this simulation should run if not finalized or interrupted before.
	 */
	public int maxNumberOfSteps;
	
	/**
	 * The number of agents taking part in the simulation.
	 */
	public int numberOfAgents;
	
	/**
	 * The number of teams taking part in the simulation.
	 */
	public int numberOfTeams;
	
	/**
	 * The number of agents taking part in each team.
	 */
	public int agentsPerTeam;
	
	
	/////// Scenario config

	/**
	 * For choosing city-map to use.
	 */
	public String mapName;

	/**
	 * ?
	 */
	public int seedcapital;
	
	/**
	 * The number of agents taking part in each team.
	 */
	public double interest;
	
	/**
	 * Minimum longitude, to define usable area of the map.
	 */
	public double minLon;
	
	/**
	 * Minimum latitude, to define usable area of the map.
	 */
	public double minLat;
	
	/**
	 * Maximum longitude, to define usable area of the map.
	 */
	public double maxLon;
	
	
	/**
	 * Maximum latitude, to define usable area of the map.
	 */
	public double maxLat;
	
	/**
	 * defines how close two locations need to be from each other to be considered the same.
	 */
	public double proximity;
	
	/**
	 * used to define the number of cell between nodes of the map.
	 */
	public double cellSize;

	/**
	 * Values for random product generation
	 */
	public boolean generateJobs = false;
	public boolean generateFacilities = false;
	public boolean generateProducts = false;
	public boolean randomAgentLocs = false;

	public int productsMinAmount = 0;
	public int productsMaxAmount = 0;
	public int productsMinVolume = 0;
	public int productsMaxVolume = 0;
	public int productsReqMin = 0;
	public int productsReqMax = 0;
	public int productsReqAmountMin = 0;
	public int productsReqAmountMax = 0;
	public int productValueMin = 0;
	public int productValueMax = 0;

	public float assembled = .0f;
	public int toolPercentage = 0;

	/**
	 * Values for random facility generation
	 */
	public double mapCenterLat = 0;
	public double mapCenterLon = 0;
	public float quadSize = 0;
	public float densityChargingStations = 1.0f;
	public int rateMin = 0;
	public int rateMax = 0;
	public int csCostMin = 0;
	public int csCostMax = 0;
	public int concurrMin = 0;
	public int concurrMax = 0;

	public float densityDumps = 1.0f;
	public int dumpCostMin = 0;
	public int dumpCostMax = 0;

	public float densityStorages = 1.0f;
	public int storageCostMin = 0;
	public int storageCostMax = 0;
	public int storageCapacityMin = 0;
	public int storageCapacityMax = 0;

	public float densityShops = 1.0f;
	public int shopProdMin = 0;
	public int shopProdMax = 0;
	public int shopPriceAddMin = 0;
	public int shopPriceAddMax = 0;
	public int shopAmountMin = 0;
	public int shopAmountMax = 0;
	public int shopRestockMin = 0;
	public int shopRestockMax = 0;
	public int shopAssembleAddMin = 0;
	public int shopAssembleAddMax = 0;

	public float densityWorkshops = 1.0f;
	public int wsCostMin = 0;
	public int wsCostMax = 0;

	/**
	 * Values for random job generation
	 */
	public float jobRate = 0.04f;
	public int jobAuctionPerc = 0;
	public int jobTimeMin = 0;
	public int jobTimeMax = 0;
	public int jobValueMin = 0;
	public int jobValueMax = 0;
	public int jobRewardSub = 0;
	public int jobRewardAdd = 0;
	public int jobBad = 0;
	public int jobProductMaxAmount = 0;

	public int jobAuctionTimeMin = 0;
	public int jobAuctionTimeMax = 0;
	public int jobAuctionFineSub = 0;
	public int jobAuctionFineAdd = 0;
	public int jobAuctionMaxRewardAdd = 0;

	/**
	 * Values for Call Service action
	 */
	public int serviceTime = 10;
	public int serviceFee = 100;
	
	//////// Configuration maps
	/*/**
	 * A map from action names to their configurations.
	 */
	// public HashMap<String, ActionConfiguration> actionsConfMap;
	
	
	/**
	 * A set of action names.
	 */
	public Vector<String> actionsNames;
	
	/**
	 * A map from role names to their configurations.
	 */
	public HashMap<String, RoleConfiguration> rolesConfMap;
	
	/**
	 * A map from product names to their configurations.
	 */
	public Vector<ProductConfiguration> productsConf; 
	
	/**
	 * A map from jobs names to their configurations.
	 */
	public Vector<JobConfiguration> jobsConf;
	
	/**
	 * A map from jobs names to their configurations.
	 */
	public Vector<FacilityConfiguration> facilitiesConf; 
	
	
	/**
	 * The seed that will be used for the random graph generator
	 */
	public long randomSeed;

	/* (non-Javadoc)
	 * @see massim.server.ServerSimulationConfiguration#setSimulationName(java.lang.String)
	 */
	public void setSimulationName(String name) {
		simulationName = name;
	}

	/* (non-Javadoc)
	 * @see massim.server.ServerSimulationConfiguration#setTournamentName(java.lang.String)
	 */
	public void setTournamentName(String name) {
		tournamentName = name;
	}

	@Override
	public void setTeamName(int n, String name) {
		if (teamNames == null){
			teamNames = new Vector<String>();
		}
		if (n>= teamNames.size()){
			teamNames.setSize(n+1);
		}
		teamNames.set(n,name);
	}
	
	public Vector<String> getTeamNames() {
		return teamNames;
	}
	
	/*/**
	 * Returns the configuration object of the action whose name is given as a parameter.
	 * @param name The name of the action.
	 * @return an <code>ActionConfiguration</code> object.
	 */
	/* public ActionConfiguration getActionConf(String name){
		return actionsConfMap.get(name);
	} */
	
	/**
	 * Returns the configuration object of the role whose name is given as a parameter.
	 * @param name The name of the role.
	 * @return a <code>RoleConfiguration</code> object.
	 */
	public RoleConfiguration getRoleConf(String name){
		return rolesConfMap.get(name);
	}
	



	/**
	 * Populates this object from the contents of an XML subtree with its root in <code>source</code> (taken
	 * from the configuration file).
	 * @param source
	 */
	@Override
 	public void decodeFromXML(Element source) {
		try{
			maxNumberOfSteps = Integer.decode(source.getAttribute("maxNumberOfSteps"));		
			numberOfAgents  =  Integer.decode(source.getAttribute("numberOfAgents")); 
			numberOfTeams  =   Integer.decode(source.getAttribute("numberOfTeams"));
//			agentsPerTeam  =   Integer.decode(source.getAttribute("agentsPerTeam"));
			seedcapital  =  Integer.decode(source.getAttribute("seedcapital")); 
			interest  =   Double.parseDouble(source.getAttribute("interest"));
			minLon    =   Double.parseDouble(source.getAttribute("minLon"));
			minLat    =   Double.parseDouble(source.getAttribute("minLat"));
			maxLon    =   Double.parseDouble(source.getAttribute("maxLon"));
			maxLat    =   Double.parseDouble(source.getAttribute("maxLat"));
			proximity    =   Double.parseDouble(source.getAttribute("proximity"));
			cellSize    =   Double.parseDouble(source.getAttribute("cellSize"));
            serviceTime = Integer.decode(source.getAttribute("serviceTime"));
            serviceFee = Integer.decode(source.getAttribute("serviceFee"));

		}catch(NumberFormatException e){
			log(LOGLEVEL_ERROR,"Error in config.");
		}
		
		try{
			randomSeed =   Long.decode(source.getAttribute("randomSeed"));
		}catch(NumberFormatException nfe){
			log(LOGLEVEL_NORMAL,"No random seed specified - taking system time.");
			randomSeed = System.currentTimeMillis();
		}
		log(LOGLEVEL_NORMAL,"Seed for map generation: "+randomSeed);
		
		mapName  =  source.getAttribute("map"); 
		
		// actionsConfMap = new HashMap<>();
		rolesConfMap = new HashMap<>();
		productsConf = new Vector<>();
		jobsConf = new Vector<>();
		facilitiesConf = new Vector<>();
		actionsNames = new Vector<>();
		
		
		NodeList nl = source.getChildNodes();
		for (int i=0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if ("actions".equals(n.getNodeName())){
				
				NodeList actionNodes = n.getChildNodes();
				
				for (int j=0; j < actionNodes.getLength(); j++) {
					Node actionNode = actionNodes.item(j);
					if ("action".equals(actionNode.getNodeName())){
						Element actionElement = (Element)actionNode;
						String name = actionElement.getAttribute("name");
						actionsNames.add(name);
						// ActionConfiguration ac = new ActionConfiguration();
						// ac.name = name;
						// actionsConfMap.put(ac.name, ac);
						
					}
				}
			} else if ("roles".equals(n.getNodeName())){
				
				try {
					NodeList roleNodes = n.getChildNodes();
					
					for (int j=0; j < roleNodes.getLength(); j++) {
						Node roleNode = roleNodes.item(j);
						if ("role".equals(roleNode.getNodeName())){
							Element roleElement = (Element)roleNode;
							RoleConfiguration rc = new RoleConfiguration();
							rc.name = roleElement.getAttribute("name");
							rc.speed =           Integer.decode(roleElement.getAttribute("speed"));
							rc.loadCapacity =    Integer.decode(roleElement.getAttribute("loadCapacity"));
							rc.batteryCapacity = Integer.decode(roleElement.getAttribute("batteryCapacity"));
							
							NodeList nl2 = roleElement.getChildNodes();
							for (int k=0; k < nl2.getLength(); k++) {
								Node classNode = nl2.item(k);
								if ("actions".equals(classNode.getNodeName())){
									NodeList actionNodes = classNode.getChildNodes();								
									for (int l=0; l < actionNodes.getLength(); l++) {
										Node actionNode = actionNodes.item(l);
										if ("action".equals(actionNode.getNodeName())){
											Element actionElement = (Element)actionNode;
											rc.actions.add(actionElement.getAttribute("name"));
										}
									}
								} else if ("roads".equals(classNode.getNodeName())){
									NodeList roadNodes = classNode.getChildNodes();								
									for (int l=0; l < roadNodes.getLength(); l++) {
										Node roadNode = roadNodes.item(l);
										if ("road".equals(roadNode.getNodeName())){
											Element roadElement = (Element)roadNode;
											rc.roads.add(roadElement.getAttribute("name"));
										}
									}
								}  else if ("tools".equals(classNode.getNodeName())){
									NodeList toolNodes = classNode.getChildNodes();								
									for (int l=0; l < toolNodes.getLength(); l++) {
										Node toolNode = toolNodes.item(l);
										if ("tool".equals(toolNode.getNodeName())){
											Element toolElement = (Element)toolNode;
											rc.tools.add(toolElement.getAttribute("id"));
										}
									}
								}						
							}
							
							rolesConfMap.put(rc.name, rc);						
						}
					}
				} catch (NumberFormatException e) {
					log(LOGLEVEL_ERROR,"Error in roles config.");
				}
			} else if ("products".equals(n.getNodeName())){
				try {
					NodeList nodes = n.getChildNodes();
					
					for (int j=0; j < nodes.getLength(); j++) {
						Node node = nodes.item(j);
						if ("product".equals(node.getNodeName())){
							
							Element element = (Element)node;
							ProductConfiguration pc = new ProductConfiguration();
							pc.id = element.getAttribute("id");
							pc.volume = Integer.decode(element.getAttribute("volume"));
							pc.userAssembled = Boolean.parseBoolean(element.getAttribute("userAssembled"));
							NodeList nl2 = element.getChildNodes();
							for (int k=0; k < nl2.getLength(); k++) {
								Node reqNode = nl2.item(k);
								if ("requirements".equals(reqNode.getNodeName())){
									NodeList reqNodes = reqNode.getChildNodes();								
									for (int l=0; l < reqNodes.getLength(); l++) {
										Node prodNode = reqNodes.item(l);
										if ("product".equals(prodNode.getNodeName())){
											Element prodElement = (Element)prodNode;
											if (Boolean.parseBoolean(prodElement.getAttribute("consumed"))) {
												pc.itemsConsumed.put(prodElement.getAttribute("id"), 
														Integer.decode(prodElement.getAttribute("amount")));
											} else {
												pc.toolsNeeded.put(prodElement.getAttribute("id"), 
														Integer.decode(prodElement.getAttribute("amount")));
											}
											
										}
									}
								} else if ("location".equals(reqNode.getNodeName())){
									
								}
							}
							productsConf.add(pc);
						}
					}
				} catch (NumberFormatException e) {
					log(LOGLEVEL_ERROR,"Error in products config.");
				}
			} else if ("facilities".equals(n.getNodeName())){
				
				try {
					NodeList nodes = n.getChildNodes();
					for (int j=0; j < nodes.getLength(); j++) {
						Node node = nodes.item(j);
						if ("facility".equals(node.getNodeName())){
							
							Element element = (Element)node;
							FacilityConfiguration fac = new FacilityConfiguration();
							fac.id = element.getAttribute("id");
							fac.type = element.getAttribute("type");
							if("workshop".equals(fac.type)){
								fac.cost = Integer.decode(element.getAttribute("cost"));
							} else if("storage".equals(fac.type)){
								fac.cost = Integer.decode(element.getAttribute("cost"));
								fac.capacity = Integer.decode(element.getAttribute("capacity"));
							} else if("dump".equals(fac.type)){
								fac.cost = Integer.decode(element.getAttribute("cost"));
							} else if("charging".equals(fac.type)){
								fac.cost = Integer.decode(element.getAttribute("cost"));
								fac.rate = Integer.decode(element.getAttribute("rate"));
								fac.concurrent = Integer.decode(element.getAttribute("concurrent"));
							} else if("shop".equals(fac.type)){
								fac.stock = new ArrayList<>();
							}
							
							NodeList nl2 = element.getChildNodes();
							for (int k=0; k < nl2.getLength(); k++) {
								Node reqNode = nl2.item(k);
								if ("products".equals(reqNode.getNodeName()) && "shop".equals(fac.type)){
									NodeList reqNodes = reqNode.getChildNodes();								
									for (int l=0; l < reqNodes.getLength(); l++) {
										Node prodNode = reqNodes.item(l);
										if ("product".equals(prodNode.getNodeName())){
											Element prodElement = (Element)prodNode;
											FacilityStock fs = new FacilityStock();
											fs.id = prodElement.getAttribute("id");
											fs.cost = Integer.decode(prodElement.getAttribute("cost"));
											fs.amount = Integer.decode(prodElement.getAttribute("amount"));
											fs.restock = Integer.decode(prodElement.getAttribute("restock"));
											fac.stock.add(fs);
										}
									}
								} else if ("location".equals(reqNode.getNodeName())){
									Element prodElement = (Element)reqNode;
									fac.lon = Double.parseDouble(prodElement.getAttribute("lon"));
									fac.lat = Double.parseDouble(prodElement.getAttribute("lat"));
								}
							}
							
							facilitiesConf.add(fac);
						}
					}
				} catch (NumberFormatException e) {
					log(LOGLEVEL_ERROR,"Error in facilities config.");
				}

			} else if ("jobs".equals(n.getNodeName())){
				
				try {
					NodeList nodes = n.getChildNodes();
					
					for (int j=0; j < nodes.getLength(); j++) {
						Node node = nodes.item(j);
						if ("job".equals(node.getNodeName())){
							
							Element element = (Element)node;
							JobConfiguration job = new JobConfiguration();
							job.id = element.getAttribute("id");
							job.type = element.getAttribute("type");
							job.storageId = element.getAttribute("storageId");
							job.firstStepActive = Integer.decode(element.getAttribute("firstStepActive"));
							job.lastStepActive = Integer.decode(element.getAttribute("lastStepActive"));
							if("priced".equals(job.type)){
								job.reward = Integer.decode(element.getAttribute("reward"));
							} else if("auction".equals(job.type)){
								job.firstStepAuction = Integer.decode(element.getAttribute("firstStepAuction"));
								job.maxReward = Integer.decode(element.getAttribute("maxReward"));
								job.fine = Integer.decode(element.getAttribute("fine"));
							}
							job.products = new HashMap<>();
							NodeList nl2 = element.getChildNodes();
							for (int k=0; k < nl2.getLength(); k++) {
								Node reqNode = nl2.item(k);
								if ("products".equals(reqNode.getNodeName())){
									NodeList reqNodes = reqNode.getChildNodes();								
									for (int l=0; l < reqNodes.getLength(); l++) {
										Node prodNode = reqNodes.item(l);
										if ("product".equals(prodNode.getNodeName())){
											Element prodElement = (Element)prodNode;
											job.products.put(prodElement.getAttribute("id"),
													Integer.decode(prodElement.getAttribute("amount")));
										}
									}
								}
							}
							
							jobsConf.add(job);
						}
					}
				} catch (NumberFormatException e) {
					log(LOGLEVEL_ERROR,"Error in Jobs config.");
				}
			}
		    else if ("generate".equals(n.getNodeName())) {
				Element generateEl = (Element)n;

				if(generateEl.getAttribute("products").equals("true")){
					this.generateProducts = true;
					log(LOGLEVEL_NORMAL ,"Products will be generated.");
				}
				if(generateEl.getAttribute("facilities").equals("true")){
					this.generateFacilities = true;
                    log(LOGLEVEL_NORMAL, "Facilities will be generated.");
				}
				if(generateEl.getAttribute("jobs").equals("true")){
					this.generateJobs = true;
                    log(LOGLEVEL_NORMAL, "Jobs will be generated.");
				}
				if(generateEl.getAttribute("agentLoc").equals("true")){
					this.randomAgentLocs = true;
                    log(LOGLEVEL_NORMAL, "Agents will be positioned randomly.");
				}

				if (this.generateFacilities || this.randomAgentLocs){
					this.mapCenterLat = Double.parseDouble(generateEl.getAttribute("mapCenterLat"));
					this.mapCenterLon = Double.parseDouble(generateEl.getAttribute("mapCenterLon"));
				}

				try {
					NodeList nodes = n.getChildNodes();
					for (int j=0; j < nodes.getLength(); j++) {
						Node node = nodes.item(j);
						if ("products".equals(node.getNodeName()) && this.generateProducts){
							Element elProducts = (Element) node;
							this.productsMinAmount = Integer.parseInt(elProducts.getAttribute("min"));
							this.productsMaxAmount = Integer.parseInt(elProducts.getAttribute("max"));
							this.productsMinVolume = Integer.parseInt(elProducts.getAttribute("minVol"));
							this.productsMaxVolume = Integer.parseInt(elProducts.getAttribute("maxVol"));
							this.productsReqMin = Integer.parseInt(elProducts.getAttribute("minReq"));
							this.productsReqMax = Integer.parseInt(elProducts.getAttribute("maxReq"));
							this.productsReqAmountMin = Integer.parseInt(elProducts.getAttribute("reqAmountMin"));
							this.productsReqAmountMax = Integer.parseInt(elProducts.getAttribute("reqAmountMax"));
							this.productValueMin = Integer.parseInt(elProducts.getAttribute("valueMin"));
							this.productValueMax = Integer.parseInt(elProducts.getAttribute("valueMax"));

							this.assembled = Float.parseFloat(elProducts.getAttribute("assembled"));
							this.toolPercentage = Integer.parseInt(elProducts.getAttribute("toolPercentage"));

						}
						else if ("facilities".equals(node.getNodeName()) && this.generateFacilities){
							Element elFacilities = (Element) node;
							this.quadSize = Float.parseFloat(elFacilities.getAttribute("quadSize"));
							NodeList facNodes = elFacilities.getChildNodes();
							for (int k=0; k < facNodes.getLength(); k++) {
								Node iNode = facNodes.item(k);
								if("chargingStations".equals(iNode.getNodeName())){
									Element elCS = (Element) iNode;
									this.densityChargingStations = Float.parseFloat(elCS.getAttribute("density"));
									this.rateMin = Integer.parseInt(elCS.getAttribute("rateMin"));
									this.rateMax = Integer.parseInt(elCS.getAttribute("rateMax"));
									this.csCostMin = Integer.parseInt(elCS.getAttribute("costMin"));
									this.csCostMax = Integer.parseInt(elCS.getAttribute("costMax"));
									this.concurrMin = Integer.parseInt(elCS.getAttribute("concurrMin"));
									this.concurrMax = Integer.parseInt(elCS.getAttribute("concurrMax"));
								}
								else if("dumps".equals(iNode.getNodeName())){
									Element elDump = (Element) iNode;
									this.densityDumps = Float.parseFloat(elDump.getAttribute("density"));
									this.dumpCostMin = Integer.parseInt(elDump.getAttribute("costMin"));
									this.dumpCostMax = Integer.parseInt(elDump.getAttribute("costMax"));
								}
								else if("workshops".equals(iNode.getNodeName())){
									Element elWS = (Element) iNode;
									this.densityWorkshops = Float.parseFloat(elWS.getAttribute("density"));
									this.wsCostMin = Integer.parseInt(elWS.getAttribute("costMin"));
									this.wsCostMax = Integer.parseInt(elWS.getAttribute("costMax"));
								}
								else if("storages".equals(iNode.getNodeName())){
									Element elStorage = (Element) iNode;
									this.densityStorages = Float.parseFloat(elStorage.getAttribute("density"));
									this.storageCostMin = Integer.parseInt(elStorage.getAttribute("costMin"));
									this.storageCostMax = Integer.parseInt(elStorage.getAttribute("costMax"));
									this.storageCapacityMin = Integer.parseInt(elStorage.getAttribute("capacityMin"));
									this.storageCapacityMax = Integer.parseInt(elStorage.getAttribute("capacityMax"));
								}
								else if("shops".equals(iNode.getNodeName())){
									Element elShop = (Element) iNode;
									this.densityShops = Float.parseFloat(elShop.getAttribute("density"));
									this.shopProdMin = Integer.parseInt(elShop.getAttribute("minProd"));
									this.shopProdMax = Integer.parseInt(elShop.getAttribute("maxProd"));
									this.shopPriceAddMin = Integer.parseInt(elShop.getAttribute("priceAddMin"));
									this.shopPriceAddMax = Integer.parseInt(elShop.getAttribute("priceAddMax"));
									this.shopAmountMin = Integer.parseInt(elShop.getAttribute("amountMin"));
									this.shopAmountMax = Integer.parseInt(elShop.getAttribute("amountMax"));
									this.shopRestockMin = Integer.parseInt(elShop.getAttribute("restockMin"));
									this.shopRestockMax = Integer.parseInt(elShop.getAttribute("restockMax"));
									this.shopAssembleAddMin = Integer.parseInt(elShop.getAttribute("assembleAddMin"));
									this.shopAssembleAddMax = Integer.parseInt(elShop.getAttribute("assembleAddMax"));

								}
//								else{
//									log(LOGLEVEL_ERROR,"Error in config. Unrecognized node in <generated/>: "+iNode.getNodeName());
//								}
							}

						}
						else if ("jobs".equals(node.getNodeName()) && this.generateJobs){
							Element elJobs = (Element)node;
							this.jobRate = Float.parseFloat(elJobs.getAttribute("rate"));
							this.jobTimeMin = Integer.parseInt(elJobs.getAttribute("timeMin"));
							this.jobTimeMax = Integer.parseInt(elJobs.getAttribute("timeMax"));
							this.jobValueMin = Integer.parseInt(elJobs.getAttribute("valueMin"));
							this.jobValueMax = Integer.parseInt(elJobs.getAttribute("valueMax"));
							this.jobRewardSub = Integer.parseInt(elJobs.getAttribute("rewardSub"));
							this.jobRewardAdd = Integer.parseInt(elJobs.getAttribute("rewardAdd"));
							this.jobBad = Integer.parseInt(elJobs.getAttribute("badJob"));
                            this.jobAuctionPerc = Integer.parseInt(elJobs.getAttribute("auctionPerc"));
							this.jobProductMaxAmount = Integer.parseInt(elJobs.getAttribute("productMaxAmount"));

							NodeList nlJobs = elJobs.getChildNodes();
							for(int k = 0; k < nlJobs.getLength(); k++){
								Node nodeJobType = nlJobs.item(k);
								if(nodeJobType.getNodeName().equalsIgnoreCase("auction")){
									Element auctionNode = (Element)nodeJobType;
									this.jobAuctionTimeMin = Integer.parseInt(auctionNode.getAttribute("auctionTimeMin"));
									this.jobAuctionTimeMax = Integer.parseInt(auctionNode.getAttribute("auctionTimeMax"));
									this.jobAuctionFineSub = Integer.parseInt(auctionNode.getAttribute("fineSub"));
									this.jobAuctionFineAdd = Integer.parseInt(auctionNode.getAttribute("fineAdd"));
									this.jobAuctionMaxRewardAdd = Integer.parseInt(auctionNode.getAttribute("maxRewardAdd"));
                                }
								else if (nodeJobType.getNodeName().equalsIgnoreCase("priced")){
									//nothing to parse
								}
							}
						}
					}
				} catch (NumberFormatException e) {
					log(LOGLEVEL_ERROR, "Error in 'Generate' config.");
				}
			}
		}
		
	}
	
	
}

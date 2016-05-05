package massim.competition2015;

import massim.competition2015.scenario.*;
import massim.framework.Perception;
import massim.framework.util.XMLCodec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;


/**
 * This class holds the information that will be sent to an agent as a normal perception.
 *
 * IMPORTANT: CHANGES TO THE STRUCTURE HAVE TO BE REFLECTED IN THE CORRESPONDING ENTITY IN eismassim
 */
public class MapSimulationAgentPerception implements Perception,
		XMLCodec.XMLEncodable{

	private static final long serialVersionUID = -5791498406627754380L;
	
	/**
	 * The state of the agent to which this perception belongs.
	 */
	private MapSimulationAgentState self;
	
	/**
	 * The state of the team of the agent owner of this perception.
	 */
	private TeamState team;
	
	/**
	 * The set of agents in the simulation
	 */
	public List<MapSimulationAgentState> agents;

	/**
	 * Set of facilities in the simulation
	 */
	public Collection<Facility> facilities;

	/**
	 * Set of jobs in the simulation
	 */
	public Collection<Job> jobs;

	/**
	 * The current simulation step
	 */
	public int step;

	/**
	 * The facility the agent currently inhabits (might be null)
	 */
	public Facility facility;
	
	
	/**
	 * Constructs for an empty perception.
	 */
	public MapSimulationAgentPerception(MapSimulationWorldState world, MapSimulationAgentState ag){
		this.step = world.currentStep;
		this.self = ag;
		this.team = ag.teamState;
		this.agents = world.agents;
		this.facilities = world.getAllFacilities();
		this.jobs = world.getAllJobs();
		this.facility = world.getFacilityAtLocation(self.location);
	}
	
	/**
	 * Encodes the contents of this perception object to the right XML format,
	 * according to the protocol description.
	 */
	public void encodeToXML(Element target) {

		/*
		 * <simulation step="X"/>
		 */
		Element elSimulation = target.getOwnerDocument().createElement("simulation");
		elSimulation.setAttribute("step", String.valueOf(step));
		target.appendChild(elSimulation);
		
		// Info about self
		Document doc = target.getOwnerDocument();
		Element elSelf = doc.createElement("self");
		elSelf.setAttribute("name", self.name);
		elSelf.setAttribute("charge", String.valueOf(self.getBatteryCharge()));
		elSelf.setAttribute("load", String.valueOf(self.load));
		elSelf.setAttribute("lastAction", String.valueOf(self.lastAction));
		elSelf.setAttribute("lastActionParam", String.valueOf(self.lastActionParam));
		elSelf.setAttribute("lastActionResult", String.valueOf(self.lastActionResult));
		elSelf.setAttribute("batteryCapacity", String.valueOf(self.role.batteryCapacity));
		elSelf.setAttribute("loadCapacity", String.valueOf(self.role.loadCapacity));
		elSelf.setAttribute("lat", String.valueOf(self.location.getLat()));
		elSelf.setAttribute("lon", String.valueOf(self.location.getLon()));
		elSelf.setAttribute("routeLength", self.route==null?"0": String.valueOf(self.route.getRouteLength()));

		if(this.facility != null){
			elSelf.setAttribute("inFacility", this.facility.name);
			if(facility instanceof ChargingStation){
				elSelf.setAttribute("fPosition", String.valueOf(((ChargingStation) facility).getQueuePosition(self)));
			}
			else{
				elSelf.setAttribute("fPosition", "-1");
			}
		}
		else{
			elSelf.setAttribute("inFacility", "none");
			elSelf.setAttribute("fPosition", "-1");
		}

		Element elItems = doc.createElement("items");
		for(Map.Entry<Item, Integer> e: self.products.entrySet()){
			Element elItem = doc.createElement("item");
			elItem.setAttribute("name", e.getKey().name);
			elItem.setAttribute("amount", String.valueOf(e.getValue()));
			elItems.appendChild(elItem);
		}
		elSelf.appendChild(elItems);

		//Possibly add complete route here
		if(self.route != null){
			Element elRoute = doc.createElement("route");
			List<Location> wp = self.route.getWaypoints();
			int count = 0;
			for(Location l: wp){
				Element n = doc.createElement("n");
				n.setAttribute("i", String.valueOf(count));
				n.setAttribute("lat", String.valueOf(l.getLat()));
				n.setAttribute("lon", String.valueOf(l.getLon()));
				elRoute.appendChild(n);
				count++;
			}
			elSelf.appendChild(elRoute);
		}

		target.appendChild(elSelf);
		
		
		// Info about team		
		Element elTeam = doc.createElement("team");
		elTeam.setAttribute("money", String.valueOf(team.money));
		//elTeam.setAttribute("rank", String.valueOf(team.ranking));

		Element elJobsTaken = doc.createElement("jobs-taken");
		for(Job j: team.takenJobs){
			Element elJob = doc.createElement("job");
			elJob.setAttribute("id", j.id);
			elJobsTaken.appendChild(elJob);
		}
		elTeam.appendChild(elJobsTaken);

		Element elJobsPosted = doc.createElement("jobs-posted");
		for(Job j: team.postedJobs){
			Element elJob = doc.createElement("job");
			elJob.setAttribute("id", j.id);
			elJobsPosted.appendChild(elJob);
		}
		elTeam.appendChild(elJobsPosted);

		target.appendChild(elTeam);

		//Info about other entities
		Element elEntities = doc.createElement("entities");
		for(MapSimulationAgentState ag: agents){
			Element elEntity = doc.createElement("entity");
			elEntity.setAttribute("name", ag.name);
			elEntity.setAttribute("team", ag.team);
			elEntity.setAttribute("lat", String.valueOf(ag.location.getLat()));
			elEntity.setAttribute("lon", String.valueOf(ag.location.getLon()));
			elEntity.setAttribute("role", ag.roleName);
			elEntities.appendChild(elEntity);
		}
		target.appendChild(elEntities);

		//Info about facilities
		Element elFacilities = doc.createElement("facilities");
		for(Facility f: facilities){

			Element elFacility;

			//add type-specific info
			if(f instanceof ChargingStation){
				elFacility = doc.createElement("chargingStation");

				elFacility.setAttribute("rate", String.valueOf(((ChargingStation) f).chargingRate));
				elFacility.setAttribute("price", String.valueOf(((ChargingStation) f).fuelPrice));
				elFacility.setAttribute("slots", String.valueOf(((ChargingStation) f).maxConcurrentCharging));
				if(f.equals(this.facility)){
					//agent is in facility
					Element elInfo = doc.createElement("info");
					elInfo.setAttribute("qSize", String.valueOf(((ChargingStation) f).getQueueSize()));
					elFacility.appendChild(elInfo);
				}
			}
			else if(f instanceof DumpLocation){
				elFacility = doc.createElement("dumpLocation");
				elFacility.setAttribute("price", String.valueOf(((DumpLocation) f).price));
			}
			else if(f instanceof Shop){
				elFacility = doc.createElement("shop");
				for(Map.Entry<Item, Shop.ShopStock> e: ((Shop) f).stock.entrySet()){
					Element elItem = doc.createElement("item");
					elItem.setAttribute("name", e.getKey().name);

					if(f.equals(this.facility)){
						Element info = doc.createElement("info");
						info.setAttribute("cost", String.valueOf(e.getValue().cost));
						info.setAttribute("amount", String.valueOf(e.getValue().amount));
						info.setAttribute("restock", String.valueOf(e.getValue().restock));
						elItem.appendChild(info);
					}

					elFacility.appendChild(elItem);
				}
			}
			else if(f instanceof Storage){
				elFacility = doc.createElement("storage");
				elFacility.setAttribute("price", String.valueOf(((Storage) f).price));
				elFacility.setAttribute("totalCapacity", String.valueOf(((Storage) f).totalCapacity));
				elFacility.setAttribute("usedCapacity", String.valueOf(((Storage) f).usedCapacity));
				for(Map.Entry<Item, Integer> e: ((Storage) f).getTeamStored(self.team).entrySet()){
					Element elItem = doc.createElement("item");
					elItem.setAttribute("name", e.getKey().name);
					elItem.setAttribute("stored", String.valueOf(((Storage) f).getItemCount(self.team, e.getKey())));
					elItem.setAttribute("delivered", String.valueOf(((Storage) f).getDeliveredItemCount(self.team, e.getKey())));
				}
			}
			else if(f instanceof Workshop){
				elFacility = doc.createElement("workshop");
				elFacility.setAttribute("price", String.valueOf(((Workshop) f).price));
			}
			else{
				elFacility = doc.createElement("unknownFacility");
			}
			//add general info
			elFacility.setAttribute("name", f.name);
			elFacility.setAttribute("lat", String.valueOf(f.location.getLat()));
			elFacility.setAttribute("lon", String.valueOf(f.location.getLon()));
			elFacilities.appendChild(elFacility);
		}
		target.appendChild(elFacilities);

		Element elJobs = doc.createElement("jobs");
		for(Job j: jobs){

			if(!(j.getStatus()==Job.ACTIVE || j.getStatus() == Job.AUCTION)){
				continue;
			}

			Element elJob;
			if(j instanceof AuctionJob){

				if(((AuctionJob) j).firstStepAuction > step){
					continue;
				}

				elJob = doc.createElement("auctionJob");
				elJob.setAttribute("fine", String.valueOf(((AuctionJob) j).fine));
				elJob.setAttribute("maxBid", String.valueOf(((AuctionJob) j).maxPrice));
				if(((AuctionJob) j).isAssigned()){
					Element elResult = doc.createElement("result");
					elResult.setAttribute("winner", ((AuctionJob) j).getAuctionWinner());
					elJob.appendChild(elResult);
				}
			}
			else if(j instanceof PricedJob){
				elJob = doc.createElement("pricedJob");
				elJob.setAttribute("reward", String.valueOf(j.getReward()));
			}
			else{
				elJob = doc.createElement("unknownJob");
			}

			elJob.setAttribute("id", j.id);
			elJob.setAttribute("storage", j.storageId);
			elJob.setAttribute("begin", String.valueOf(j.firstStepActive));
			elJob.setAttribute("end", String.valueOf(j.lastStepActive));

			elItems = doc.createElement("items");
			for(Map.Entry<Item, Integer> e: j.itemsRequired.entrySet()){
				Element elItem = doc.createElement("item");
				elItem.setAttribute("name", e.getKey().name);
				elItem.setAttribute("amount", String.valueOf(e.getValue()));
				if(j.poster.equals(self.team)){
					//add delivered items if job was posted by this agent's team
					elItem.setAttribute("delivered", String.valueOf(j.getTeamDelivered(j.fulfiller).get(e.getKey())));
				}
				elItems.appendChild(elItem);
			}
			elJob.appendChild(elItems);
			elJobs.appendChild(elJob);
		}
		target.appendChild(elJobs);
	}
}

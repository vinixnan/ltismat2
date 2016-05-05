package massim.competition2015;

import java.util.Map;

import massim.competition2015.configuration.RoleConfiguration;
import massim.competition2015.scenario.ChargingStation;
import massim.competition2015.scenario.Item;
import massim.competition2015.scenario.Location;
import massim.competition2015.scenario.Route;
import massim.competition2015.scenario.TeamState;
import massim.framework.simulation.AgentState;

/**
 * This class holds the current state of an agent in the 2015 Map scenario simulation.
 */
public class MapSimulationAgentState implements AgentState {

	private static final long serialVersionUID = 5737421381825241511L;
	
	
	
	
	// Agent data
	/**
	 * The name of the agent's team
	 */
	public String team;
	
	/**
	 * The state of the agent's team
	 */
	public TeamState teamState;
	
	/**
	 * The name of the agent
	 */
	public String name;
	
	/**
	 * The name of the agent's role
	 */
	public String roleName;
	
	/**
	 * Role data
	 */
	public RoleConfiguration role = null;
	
	/**
	 * The agent's current battery.
	 */
	private int battery;
	
	/**
	 * Agent's location.
	 */
	public Location location = null;
	
	
	/**
	 * Agent's current load's total volume.
	 */
	public int load = 0;
	
	/**
	 * Agents current list of products.
	 */
	public Map<Item, Integer> products;
	
	
	
	
	
	// Action data
	/**
	 *  Holds the name of the action that the agents wants to execute in the current step.
	 */
	public String action;
	
	/**
	 * Holds the parameter String for the action that the agents wants to execute in the current step.
	 */
	public String param;
	
	/**
	 * Holds a route that the agent is to follow in subsequent steps.
	 */
	public Route route = null;
	
	/**
	 * FIXME Holds a route that the agent is to follow in subsequent steps.
	 */
	public ChargingStation chargingStation = null;
	
	
	
	/**
	 * Holds the name of the last executed action, to be used in next perception as well as in
	 * monitoring.
	 */
	public String lastAction;
	
	/**
	 * Holds the result of the last executed action, to be used in next perception as well as in
	 * monitoring.
	 */
	public String lastActionResult;
	
	/**
	 * Holds the parameter String of the last executed action, to be used in next perception as well as in
	 * monitoring.
	 */
	public String lastActionParam;

    /**
     * Used for CallBreakdownService action
     */
	public int servicePrevStep = -10;
    public int serviceTimer = 0;

	public int getItemCount(Item item){
		if (!products.containsKey(item)){
			return 0;
		}
		Integer c = products.get(item);
		if (c == null) return 0;
		return c;
	}
	
	public int getRemainingCapacity(){
		return role.loadCapacity - load;
	}
	
	public boolean addItem(Item item, int amount){
		if (amount*item.volume > getRemainingCapacity()) return false;
		int current = 0;
		if (products.containsKey(item)){
			current = products.get(item);
		}
		products.put(item, current+amount);
		load += amount*item.volume;
		return true;
	}
	
	public boolean removeItem(Item item, int amount){
		if (!products.containsKey(item)){
			return false;
		}
		int current = products.get(item);
		if (current < amount){
			return false;
		}
		if (current == amount){
			products.remove(item);
		} else {
			products.put(item, current-amount);
		}
		load -= amount*item.volume;
		return true;
	}
	
	public boolean canUseTool(Item tool){
		return role.tools.contains(tool.name);
	}
	
	
	
	public int getBatteryCharge(){
		return battery;
	}
	
	public boolean isBatteryFull(){
		return battery == this.role.batteryCapacity;
	}
	
	public boolean isBatteryEmpty(){
		return battery == this.role.batteryCapacity;
	}
	
	public void setCharge(int amount){
		this.battery = amount;
	}
	
	public void increaseCharge(int amount, int price){
		teamState.money -= price*Math.min(amount, this.role.batteryCapacity-this.battery);
		this.battery = Math.min(this.battery+amount, this.role.batteryCapacity);
	}
	
	
	public void advance(){
		if (this.route == null) {
			return;
		}
		if (this.battery <= 5){
			this.route = null;
			this.battery = 0;
			return;
		}
		
		this.battery -= 10;  // TODO 2016:: define battery cost of moving! (role depending? time-based or distance based?)
		Location newLoc = this.route.advance(this.role.speed); 
		if (newLoc != null){
			this.location = newLoc;
		}
		if (this.route.completed()){
			this.route = null;
		}
		
	}
	
	// Perceptions
	// private WorldPrecept perceptions;

}

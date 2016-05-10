/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import massim.competition2015.MapSimulationAgentState;

/**
 * @author fschlesinger
 *
 */
public class ChargingStation extends Facility implements Serializable {


	private static final long serialVersionUID = 1L;

	public int chargingRate;
	public int fuelPrice;
	public int maxConcurrentCharging;
	private Set<MapSimulationAgentState> charging;
	private Queue<MapSimulationAgentState> queue;
	
	
	/**
	 * Initializer
	 */
	public ChargingStation() {
		this.queue  = new LinkedList<>();
		this.charging = new HashSet<>();
	}
	
	public int getQueueSize(){
		return this.queue.size();
	}
	
	public boolean isFull(){
		return this.charging.size() == this.maxConcurrentCharging;
	}

	public void addAgent(MapSimulationAgentState agent){
		if (this.charging.contains(agent)){
			return;
		}
		
		if (!this.queue.contains(agent)){
			agent.chargingStation = this;
			if (isFull()){
				this.queue.add(agent);
			} else {
				this.charging.add(agent);
			}
		}
	}
	
	public void increaseCharges(){
		ArrayList<MapSimulationAgentState> remove = new ArrayList<>();
		for (MapSimulationAgentState agent : this.charging){
			agent.increaseCharge(this.chargingRate, this.fuelPrice);
			if (agent.isBatteryFull()){
				remove.add(agent);
			}
		}
		for (MapSimulationAgentState agent : remove){
			removeAgent(agent);
		}
	}
	
	public void removeAgent(MapSimulationAgentState agent){
		agent.chargingStation = null;
		
		if (this.charging.contains(agent)){
			this.charging.remove(agent);
			if (!this.queue.isEmpty()){
				this.charging.add(this.queue.poll());
			}
			return;
		}
		
		if (this.queue.contains(agent)){
			this.queue.remove(agent);
		}
	}

	public int getQueuePosition(MapSimulationAgentState agent){
		if(this.queue instanceof LinkedList){
			return ((LinkedList) this.queue).indexOf(agent);
		}
		else{
			return -1;
		}
	}
}

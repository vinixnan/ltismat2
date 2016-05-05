package massim.competition2013;

import java.util.HashMap;
import java.util.Map;

import massim.competition2013.scenario.GraphNode;
import massim.framework.simulation.AgentState;

/**
 * This class holds the current state of an agent in the 2013 Mars scenario simulation.
 */
public class GraphSimulationAgentState implements AgentState {


	private static final long serialVersionUID = 5737421381825241511L;
	
	
	// Agent data
	/**
	 * The name of the agent's team
	 */
	public String team;
	
	/**
	 * The name of the agent
	 */
	public String name;
	
	/**
	 * The name of the agent's role
	 */
	public String roleName;
	
	/**
	 * The agent's maximum energy (that can be increased via the <code>buy</code> action).
	 */
	public int maxEnergy;
	
	/**
	 * The agent's maximum energy if the agent is disabled (when <code>health == 0</code>).
	 */
	public int maxEnergyDisabled;
	
	/**
	 * The agent's current energy.
	 */
	public int energy;
	
	/**
	 * The agent's maximum health (that can be increased via the <code>buy</code> action).
	 */
	public int maxHealth;
	
	/**
	 * The agent's current health.
	 */
	public int health;
	
	/**
	 * The agent's strength (that can be increased via the <code>buy</code> action).
	 */
	public int strength;
	
	/**
	 * The agent's visibility range (that can be increased via the <code>buy</code> action).
	 */
	public int visRange;
	
	/**
	 * The current node of this agent (that is, the agent's position within the map).
	 */
	public GraphNode node;
	
	
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
	 * Holds the agent's last relevant effective range (to add the surveyed edges to the percept)
	 */
	public int lastEffectiveRange = -1;
	
	//  Action flags
	/**
	 * A flag to indicate that the agent was attacked in the current step. The need for this flag is
	 * because some actions are specified to fail when the agent is under attack.
	 */
	public boolean attacked;
	
	/**
	 * A mapping from vertices to the distance between the agent and the vertex
	 */
	private Map<String, Integer> distances = new HashMap<>();
	
	
	/**
	 * Setter for the current node of this agent (that is, the agent's position within the map).
	 * @param node
	 */
	public void setNode(GraphNode node) {
		this.node = node;		
	}

	/**
	 * Setter for the name of the action that this agent wants to execute in this simulation step.
	 * @param action
	 */
	public void setAction(String action) {
		this.action = action;		
	}
	
	/**
	 * Resets the mapping of distances to an empty map
	 */
	public void resetDistances(){
		this.distances = new HashMap<>();
	}
	
	/**
	 * Stores the pair of vertex and distance in the map
	 * @param vertex
	 * @param distance
	 */
	public void addDistance(String vertex, int distance){
		this.distances.put(vertex, distance);
	}
	
	/**
	 * Getter for the distance between the agent and a given vertex
	 * @param vertexName the name of the vertex
	 * @return the distance of the vertex or -1, if the name is invalid or the vertex is out of the vis. range of the agent
	 */
	public int getDistance(String vertexName){
		Integer ret = this.distances.get(vertexName);
		return ret==null? -1 : ret;
	}

	
	// Perceptions
	// private WorldPrecept perceptions;

}

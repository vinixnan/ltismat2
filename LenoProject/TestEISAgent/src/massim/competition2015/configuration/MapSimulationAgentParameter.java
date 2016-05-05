package massim.competition2015.configuration;

import massim.gridsimulations.SimulationAgentParameter;


/**
 * This class holds the agent-specific part of the simulation configuration (defined in the config file).
 */
public class MapSimulationAgentParameter extends SimulationAgentParameter {
	
	/**
	 * The name of the agent
	 */
	public String name;
	
	/**
	 * The name of the agent's role
	 */
	public String roleName;
	
	/**
	 * Agents initial location, longitude.
	 */
	public double lon;
	
	/**
	 * Agents initial location, longitude.
	 */
	public double lat;

}

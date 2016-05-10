package massim.competition2015;

import java.util.HashMap;
import java.util.Vector;

import massim.competition2015.configuration.MapSimulationAgentParameter;
import massim.competition2015.configuration.MapSimulationConfiguration;
import massim.competition2015.scenario.Location;
import massim.framework.Action;
import massim.framework.AgentParameter;
import massim.framework.FinalPerception;
import massim.framework.InitialStickyPerception;
import massim.framework.Perception;
import massim.framework.UniqueSimulationAgent;
import massim.framework.connection.UsernamePasswordAccount;
import massim.framework.simulation.AbstractSimulationAgent;
import massim.framework.simulation.AgentState;
import massim.framework.simulation.WorldState;

/**
 * This class deals with the agent state, his actions and perceptions.
 */
public class MapSimulationAgent extends AbstractSimulationAgent {
	
	private MapSimulationAgentState agentstate = null;
	private MapSimulationAgentAction action = null;
	// private MapSimulationWorldState simulationstate = null;



	/**
	 * The constructor instantiates the agentstate and the action
	 */
	public MapSimulationAgent() {
		this.agentstate = new MapSimulationAgentState();
		this.action = new MapSimulationAgentAction();
	}


	/**
	 * Configures the agent according to the <code>AgentParameter</code> as parsed from the configuration file.
	 * <code>agentpar</code> must be an instance of <code>MapSimulationAgentParameter</code>. The information
	 * included in <code>agentpar</code> is the name of the agent, the name of its role, and the name of its team
	 *
	 * @param agentpar agent parameters to use
	 */
	public void setAgentParameter(AgentParameter agentpar) {

		super.setAgentParameter(agentpar);

		// get Team
		MapSimulationAgentParameter agentConfig = (MapSimulationAgentParameter) agentpar;
		this.agentstate.team = agentConfig.getTeam().toString();

		// get Username
		if (this.getAgent() instanceof UniqueSimulationAgent) {
			UniqueSimulationAgent agent = (UniqueSimulationAgent) this
					.getAgent();
			if (agent.getIdentifier() instanceof UsernamePasswordAccount) {
				UsernamePasswordAccount upa = (UsernamePasswordAccount) agent
						.getIdentifier();
				this.agentstate.name = upa.getUsername();
			} else {
				if (agentConfig.name != null){
					this.agentstate.name = agentConfig.name;
				} else {
					this.agentstate.name = "";
				}
			}
		} else {
			if (agentConfig.name != null){
				this.agentstate.name = agentConfig.name;
			} else {
				this.agentstate.name = "";
			}
		}
		this.agentstate.roleName = agentConfig.roleName;
		
		this.agentstate.location = new Location(agentConfig.lon, agentConfig.lat);
	}


	/**
	 * Initializes the agent internal values. The <code>AgentParameter</code> must have been set previously,
	 * by calling <code>setAgentParameter</code>.
	 * @param config the current configuration being used
	 */
	public void initialize(MapSimulationConfiguration config) {

		this.agentstate.role = config.getRoleConf(this.agentstate.roleName);

		this.agentstate.setCharge(this.agentstate.role.batteryCapacity);
		this.agentstate.load = 0;
		this.agentstate.products = new HashMap<>();

		this.agentstate.lastAction = "skip";
		this.agentstate.lastActionResult = "successful";
		this.agentstate.param = "";

	}

	@Override
	public AgentState getAgentState() {
		return this.agentstate;
	}

	/**
	 * This method only calculates private agent perceptions. Shared perceptions must be added
	 * externally
	 */
	@Override
	public Perception createPerception(WorldState simstate,
			AgentState[] agentstates) {

		massim.competition2015.MapSimulationWorldState worldState = (massim.competition2015.MapSimulationWorldState) simstate;
		MapSimulationAgentPerception perc = new MapSimulationAgentPerception(worldState, agentstate);

		return perc;
	}



	@Override
	public void processAction(Action a, WorldState simstate,
			AgentState[] agentstates) {
		// Do nothing
	}

	@Override
	public InitialStickyPerception createInitialPerception(WorldState simstate,
			AgentState[] agentstates) {

		MapSimulationWorldState simulationstate = (MapSimulationWorldState) simstate;
		MapSimulationAgentInitialPerception perc = new MapSimulationAgentInitialPerception();

		perc.self = this.agentstate;
		perc.steps = simulationstate.config.maxNumberOfSteps;
		perc.map = simulationstate.config.mapName;
		perc.products.addAll(((MapSimulationWorldState) simstate).items);
		
		perc.role = this.agentstate.roleName;

		perc.teamMembers = new Vector<>();
		for (AgentState as : agentstates) {
			MapSimulationAgentState otherAgent = (MapSimulationAgentState)as;
			if (otherAgent.team.equals(this.agentstate.team) && !otherAgent.equals(this.agentstate)){
				perc.teamMembers.add(otherAgent);
			}
		}
		return perc;
	}

	@Override
	public FinalPerception createFinalPerception(WorldState simstate,
			AgentState[] agentstates) {
        MapSimulationWorldState simulationstate = (MapSimulationWorldState) simstate;
		MapSimulationAgentFinalPerception perc = new MapSimulationAgentFinalPerception();
		perc.score = simulationstate.getTeamState(this.agentstate.team).money;
		perc.ranking = simulationstate.getTeamState(this.agentstate.team).ranking;
		return perc;
	}


	/**
	 * Sets the action received from the client-side agent to the agent state, for execution in the current step.
	 * @param newAction
	 */
	public void setAction(Action newAction) {
		
		if (newAction instanceof MapSimulationAgentAction){
			//set action
			this.action = (MapSimulationAgentAction) newAction;
			this.agentstate.action = this.action.type;
			this.agentstate.param = this.action.param;
		}	
		else {
			//set invalid action
			this.action = new MapSimulationAgentAction();
			this.action.type = "noAction";
			this.agentstate.action = this.action.type;
			this.agentstate.param = "";
		}
	}



	
	

}

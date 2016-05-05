package massim.competition2013;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import massim.competition2013.scenario.GraphEdge;
import massim.competition2013.scenario.GraphNode;
import massim.competition2013.scenario.RoleConfiguration;
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
public class GraphSimulationAgent extends AbstractSimulationAgent {

	
	private GraphSimulationAgentState agentstate = null;
	private GraphSimulationAgentAction action = null;
	// private GraphSimulationWorldState simulationstate = null;

	
	
	/**
	 * The constructor instantiates the agentstate and the action
	 */
	public GraphSimulationAgent() {		
		this.agentstate = new GraphSimulationAgentState();
		this.action = new GraphSimulationAgentAction();
	}
	
	
	/**
	 * Configures the agent according to the <code>AgentParameter</code> as parsed from the configuration file.
	 * <code>agentpar</code> must be an instance of <code>GraphSimulationAgentParameter</code>. The information
	 * included in <code>agentpar</code> is the name of the agent, the name of its role, and the name of its team
	 * 
	 * @param agentpar agent parameters to use
	 */
	public void setAgentParameter(AgentParameter agentpar) {
		
		super.setAgentParameter(agentpar);
		
		// get Team
		GraphSimulationAgentParameter graphPar = (GraphSimulationAgentParameter) agentpar;
		this.agentstate.team = graphPar.getTeam().toString();
		
		// get Username
		if (this.getAgent() instanceof UniqueSimulationAgent) {
			UniqueSimulationAgent agent = (UniqueSimulationAgent) this
					.getAgent();
			if (agent.getIdentifier() instanceof UsernamePasswordAccount) {
				UsernamePasswordAccount upa = (UsernamePasswordAccount) agent
						.getIdentifier();
				this.agentstate.name = upa.getUsername();
			} else {
				if (graphPar.name != null){
					this.agentstate.name = graphPar.name;
				} else {
					this.agentstate.name = "";
				}
			}
		} else {
			if (graphPar.name != null){
				this.agentstate.name = graphPar.name;
			} else {
				this.agentstate.name = "";
			}
		}
		this.agentstate.roleName = graphPar.roleName;
	}
	
	
	/**
	 * Initializes the agent internal values. The <code>AgentParameter</code> must have been set previously,
	 * by calling <code>setAgentParameter</code>.
	 * @param config the current configuration being used
	 */
	public void initialize(GraphSimulationConfiguration config) {
		
		RoleConfiguration roleConf = config.getRoleConf(this.agentstate.roleName);
		
		this.agentstate.maxEnergy = roleConf.maxEnergy;
		this.agentstate.maxEnergyDisabled = roleConf.maxEnergyDisabled;
		this.agentstate.energy = roleConf.maxEnergy;
		this.agentstate.maxHealth = roleConf.maxHealth;
		this.agentstate.health = roleConf.maxHealth;
		this.agentstate.strength = roleConf.strength;
		this.agentstate.visRange = roleConf.visRange;
		
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
		
		GraphSimulationWorldState worldState = (GraphSimulationWorldState) simstate;
		GraphSimulationAgentPerception perc = new GraphSimulationAgentPerception();
		
		perc.self = this.agentstate;
		perc.team = worldState.getTeamState(this.agentstate.team);
		perc.step = worldState.currentStep.intValue();

		// Add agent's node
		perc.nodes.add(this.agentstate.node);
		perc.agents.addAll(this.agentstate.node.agents);

		// Add nodes in visibility range
		LinkedList<GraphNode> thisStepNodes = new LinkedList <>();
		LinkedList<GraphNode> prevStepNodes = new LinkedList <>();
		prevStepNodes.add(this.agentstate.node);
		this.agentstate.resetDistances();	//remove possibly outdated distances
		this.agentstate.addDistance(this.agentstate.node.name, 0);
		for (int i = 0; i < this.agentstate.visRange; i++) {

			for (GraphNode node : prevStepNodes) {

				// Add edges connecting nodes from previous step
				List<GraphEdge> neighborEdges = worldState.getConnectedEdges(node);
				for (GraphEdge edge : neighborEdges) {
					perc.edges.add(edge);
					
					GraphNode neighbor = opositeNode(edge, node);
					if (perc.nodes.add(neighbor)){
						thisStepNodes.add(neighbor);
						perc.agents.addAll(neighbor.agents);
						this.agentstate.addDistance(neighbor.name, i+1);	//store the distance
					}
				}

			}			
			prevStepNodes = thisStepNodes;
			thisStepNodes = new LinkedList <>();
		}

		if ("probe".equals(this.agentstate.lastAction) && "successful".equals(this.agentstate.lastActionResult) ){
			if(this.agentstate.lastActionParam.equals("")){
				perc.probedNodes.add(this.agentstate.node);
			}
			else{
				GraphNode node = worldState.getNode(this.agentstate.lastActionParam);
				if(node != null){
					perc.probedNodes.add(node);
				}
			}
		} 
		
		else if ("survey".equals(this.agentstate.lastAction) && "successful".equals(this.agentstate.lastActionResult) ){
			int range = this.agentstate.lastEffectiveRange;
			Set<GraphNode> nodes = new HashSet<>();
			nodes.add(this.agentstate.node);
			Set<GraphNode> previousNodes = new HashSet<>();
			previousNodes.add(this.agentstate.node);
			for(int i = 0; i < range; i++){
				Set<GraphNode> nextNodes = new HashSet<>();
				for(GraphNode node: previousNodes){
					for(GraphEdge edge: worldState.getConnectedEdges(node)){
						perc.surveyedEdges.add(edge);
						if(nodes.add(edge.node1)){
							nextNodes.add(edge.node1);
						}
						if(nodes.add(edge.node2)){
							nextNodes.add(edge.node2);
						}
					}
				}
				previousNodes = nextNodes;
			}
//			List<GraphEdge> connectedEdges = worldState.getConnectedEdges(agentstate.node);
//			perc.surveyedEdges.addAll(connectedEdges);
		} 
		else if ("inspect".equals(this.agentstate.lastAction) && "successful".equals(this.agentstate.lastActionResult) ){
			if(this.agentstate.lastActionParam.equals("")){
				for (GraphSimulationAgentState inspectedAgent : this.agentstate.node.agents) {
					if (!this.agentstate.team.equals(inspectedAgent.team)) {
						perc.inspectedAgents.add(inspectedAgent);
					}
				}
			}
			else{
				GraphSimulationAgentState ag = worldState.getAgent(this.agentstate.lastActionParam);
				if(ag != null && !this.agentstate.team.equals(ag.team)){
					perc.inspectedAgents.add(ag);
				}
			}
		}
		return perc;
	}
	
	/**
	 * Returns the <code>GraphNode</code> of <code>edge</code> in the opposite side to <code>node</code>.
	 * If <code>node</code> is not one of the <code>edge</code> nodes, returns <code>null</code>
	 * @param edge
	 * @param node
	 */
	private GraphNode opositeNode(GraphEdge edge, GraphNode node) {
		if (node.equals(edge.node1)){
			return edge.node2;
		}		
		if (node.equals(edge.node2)){
			return edge.node1;
		}
		return null;
	}

	@Override
	public void processAction(Action a, WorldState simstate,
			AgentState[] agentstates) {
		// Do nothing
	}

	@Override
	public InitialStickyPerception createInitialPerception(WorldState simstate,
			AgentState[] agentstates) {

		GraphSimulationWorldState simulationstate = (GraphSimulationWorldState) simstate;
		GraphSimulationAgentInitialPerception perc = new GraphSimulationAgentInitialPerception();
		
		perc.self = this.agentstate;
		perc.steps = simulationstate.config.maxNumberOfSteps;
		perc.vertices = simulationstate.nodes.size();
		perc.edges = simulationstate.edges.size();
		perc.role = this.agentstate.roleName;
		
		perc.teamMembers = new Vector<>();
		for (AgentState as : agentstates) {
			GraphSimulationAgentState otherAgent = (GraphSimulationAgentState)as;
			if (otherAgent.team.equals(this.agentstate.team) && !otherAgent.equals(this.agentstate)){
				perc.teamMembers.add(otherAgent);
			}
		}		
		return perc;
	}

	@Override
	public FinalPerception createFinalPerception(WorldState simstate,
			AgentState[] agentstates) {GraphSimulationWorldState simulationstate = (GraphSimulationWorldState) simstate;
		GraphSimulationAgentFinalPerception perc = new GraphSimulationAgentFinalPerception();
		perc.score = simulationstate.getTeamState(this.agentstate.team).summedScore;
		perc.ranking = simulationstate.getTeamState(this.agentstate.team).ranking;
		return perc;
	}


	/**
	 * Sets the action received from the client-side agent to the agent state, for execution in the current step.
	 * @param newAction
	 */
	public void setAction(Action newAction) {	
		if (newAction instanceof massim.framework.InvalidAction) {			
			//set invalid action
			this.action = new GraphSimulationAgentAction();
			this.action.type = "noAction";
			this.agentstate.action = this.action.type;
			this.agentstate.param = "";
		} 
		else if (newAction instanceof GraphSimulationAgentAction){
			//set action
			this.action = (GraphSimulationAgentAction) newAction;
			this.agentstate.action = this.action.type;
			this.agentstate.param = this.action.param;
		}	
	}



	
	

}

package massim.competition2015;

import static massim.framework.util.DebugLog.LOGLEVEL_CRITICAL;
import static massim.framework.util.DebugLog.LOGLEVEL_NORMAL;
import static massim.framework.util.DebugLog.log;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import massim.competition2015.configuration.MapSimulationConfiguration;
import massim.competition2015.scenario.ActionExecutor;
import massim.competition2015.scenario.Location;
import massim.competition2015.scenario.TeamState;
import massim.framework.Action;
import massim.framework.Perception;
import massim.framework.SimulationConfiguration;
import massim.framework.simulation.AbstractSimulation;
import massim.framework.simulation.AgentState;
import massim.framework.simulation.SimulationAgent;
import massim.framework.simulation.WorldState;

/**
 * This is the main class for the MapSimulation (2015 Map Scenario).
 */
public class MapSimulation extends AbstractSimulation{
	
	MapSimulationConfiguration config = null;
	MapSimulationWorldState state = null;
	
	@Override
	public void configureSimulation(SimulationConfiguration c) {
		super.configureSimulation(c);
		config = (MapSimulationConfiguration) c;
	}

	@Override
	public boolean isFinished() {
		return getSteps() >= config.maxNumberOfSteps;
	}

	@Override
	public WorldState getSimpleSimulationState() {
		return state;
	}

	@Override
	public void initializeSimpleSimulation() {
		// create graph
		state = new MapSimulationWorldState(config);
		
		// add the agents
		SimulationAgent agents[] = this.getAgents();
		ArrayList<Location> agentPositions = new ArrayList<>(); //used to store the agent's positions to ensure same placement for both teams
		for (int i = 0; i < agents.length; i++) {
			MapSimulationAgent simAgent = (MapSimulationAgent) agents[i];
			simAgent.initialize(config);
			state.addAgent( (MapSimulationAgentState)simAgent.getAgentState(), agentPositions, i < agents.length/2, i);
		}
	}

	@Override
	public void preSimulationStep() {
		// do nothing
	}

	@Override
	public void postSimulationStep() {
		
		state.currentStep = this.getSteps();
		for (TeamState ts : state.teamsStates){
			ts.updateMoney();
		}
		log(LOGLEVEL_NORMAL, "Simulation at step: " + this.getSteps());
	}

	@Override
	public void runAgents() {
		class MyPair {
			public Future<Action> action;
			public SimulationAgent agent;
		}
		
		SimulationAgent[] agents = getAgents();
		WorldState simstate = getSimpleSimulationState();
		AgentState[] agentstates = new AgentState[agents.length];
		
		// Assemble agent states to one array
		for (int i=0;i<agents.length;i++) agentstates[i]=agents[i].getAgentState();
		
				
		ArrayList<MyPair> actions = new ArrayList<MyPair>();
		
		HashMap<AgentState, MapSimulationAgentPerception> perceptionsMap = new HashMap<>();

		// Create perceptions.
		for(int i=0;i<agents.length;i++) {
			// Calculate private perceptions and put them in the map.
			perceptionsMap.put(
					agents[i].getAgentState(),
					(MapSimulationAgentPerception)agents[i].createPerception(simstate, agentstates));
		}		
		
		// Send perceptions to agents and retrieve future object for actions.
		for (int i=0;i<agents.length;i++) {		
			MyPair m = new MyPair();
			m.action = agents[i].getAgent().concurrentGetAction(
					perceptionsMap.get(agents[i].getAgentState()));
			m.agent = agents[i];
			actions.add(m);
		}
		try {
			for (int i=0;i<agents.length;i++) {
				MyPair m = actions.get(i);
				// Only set the action. It will get executed in simulationStep().
				((MapSimulationAgent)m.agent).setAction(m.action.get());
			}
			
		} catch (ExecutionException e) {
			log(LOGLEVEL_CRITICAL,"Execution of getAction failed, shouldn't happen");
		} catch (InterruptedException e) {
			log(LOGLEVEL_CRITICAL,"Thread interrupted, shouldn't happen");
		}
		
	}

	@Override
	public void simulationStep() {
		ActionExecutor.execute(state);		
	}

		
	/**
	 * This method prepares and sends the initial perceptions to the agents for starting the simulation
	 */
	public void runInitAgents() {
		SimulationAgent[] agents = getAgents();
		WorldState simstate = (WorldState) getSimpleSimulationState();
		AgentState[] agentstates = new AgentState[agents.length];
		
		// Assemble agent states to one array
		for (int i=0;i<agents.length;i++) agentstates[i]=agents[i].getAgentState();
		// Let agents act.
		for(int i=0;i<agents.length;i++) {
			Perception p = agents[i].createInitialPerception(simstate,agentstates);
			// TODO: use concurretGetAction?
			agents[i].getAgent().getAction(p);
		}
	}
	
	/**
	 * This method prepares and sends the final perceptions to the agents when the simulation is finished
	 */
	public void runFinalAgents() {
		SimulationAgent[] agents = getAgents();
		WorldState simstate = (WorldState) getSimpleSimulationState();
		AgentState[] agentstates = new AgentState[agents.length];
		
		// Assemble agent states to one array
		for (int i=0;i<agents.length;i++) agentstates[i]=agents[i].getAgentState();
		// Let agents act.
		for(int i=0;i<agents.length;i++) {
			Perception p = agents[i].createFinalPerception(simstate,agentstates);
			// TODO: use concurretGetAction?
			agents[i].getAgent().getAction(p);
		}
	}

	@Override
	public String finalizeSimpleSimulation() {
		// Calculate ranking
		ArrayList<TeamState> rankings = new ArrayList<TeamState>(state.teamsStates);
		Collections.sort(rankings, new Comparator<TeamState>() {
			
			@Override
			public int compare(TeamState t1, TeamState t2) {
				if (t1.money > t2.money){
					return -1;
				}
				if (t1.money < t2.money){
					return 1;
				}
				return 0;
			}
		});
		
		int i = 1;
		for (TeamState teamState : rankings) {
			teamState.ranking = i++;
		}
		
		if(rankings.get(0).money == rankings.get(1).money){
			return "draw"; //this is okay, each team gets 1 point
		}
		//dirty score hack since score is handled deep in the Server class
		//the winning team gets 3 points (-1  if the money did not increase)
		return "MAP127569:"+rankings.get(0).name+":"+(rankings.get(0).money > config.seedcapital);
	}

}

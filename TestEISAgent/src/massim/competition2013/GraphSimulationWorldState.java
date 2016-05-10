package massim.competition2013;

import static massim.framework.util.DebugLog.LOGLEVEL_NORMAL;
import static massim.framework.util.DebugLog.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import massim.competition2013.scenario.GraphEdge;
import massim.competition2013.scenario.GraphGenerator;
import massim.competition2013.scenario.GraphGenerator2013;
import massim.competition2013.scenario.GraphGeneratorTriangBalOpt;
import massim.competition2013.scenario.GraphGeneratorTriangulationBalanced;
import massim.competition2013.scenario.GraphNode;
import massim.competition2013.scenario.TeamState;
import massim.framework.simulation.WorldState;
import massim.gridsimulations.SimulationWorldState;

/**
 * Holds the current state of a graph simulation (2013 Mars Scenario)
 */
public class GraphSimulationWorldState extends SimulationWorldState implements WorldState {

	private static final long serialVersionUID = -6316439899157240323L;

	// World model values
	/**
	 * The nodes conforming the map graph.
	 */
	protected Vector<GraphNode> nodes;

	/**
	 * The edges conforming the map graph.
	 */
	protected Vector<GraphEdge> edges;

	/**
	 * A map holding the list of nodes directly connected to each node. It is
	 * used as a cache to provide fast access to this information that is
	 * heavily accessed during the simulation execution.
	 */
	protected Map<GraphNode, List<GraphNode>> neighborsMap;

	/**
	 * A map holding the list of edges connected to each node. It is used as a
	 * cache to provide fast access to this information that is heavily accessed
	 * during the simulation execution.
	 */
	protected Map<GraphNode, List<GraphEdge>> connectedEdgesMap;

	/**
	 * A map from an agent's name to its current state. It is used as a cache to
	 * provide fast access to this information that is heavily accessed during
	 * the simulation execution.
	 */
	protected Map<String, GraphSimulationAgentState> agentNamesMap;

	/**
	 * A map from a node's name to its current state. It is used as a cache to
	 * provide fast access to this information that is heavily accessed during
	 * the simulation execution.
	 */
	protected Map<String, GraphNode> nodeNamesMap;

	/**
	 * A vector holding all agents that take part in the simulation.
	 */
	protected Vector<GraphSimulationAgentState> agents;

	/**
	 * The configuration of this simulation.
	 */
	protected GraphSimulationConfiguration config;

	/**
	 * A vector holding the states of all the teams that take part in the
	 * simulation.
	 */
	public Vector<TeamState> teamsStates;

	/**
	 * The width of the abstract grid to which the graph is subscribed.
	 */
	protected int sizeX;

	/**
	 * The height of the abstract grid to which the graph is subscribed.
	 */
	protected int sizeY;

	// Used for randomly situating the agents in the map.
	private Random random;

	/**
	 * The generator used for generating the graph
	 */
	private GraphGenerator generator;

	/**
	 * Creates a simulation state as defined by <code>config</code>
	 * 
	 * @param config
	 */
	public GraphSimulationWorldState(GraphSimulationConfiguration config) {
		this.simulationName = config.simulationName;
		this.nodes = new Vector<>();
		this.edges = new Vector<>();
		this.agents = new Vector<>();
		this.agentNamesMap = new HashMap<>();
		this.nodeNamesMap = new HashMap<>();

		this.teamsStates = new Vector<>();

		this.config = config;

		// Check whether the number of nodes fits the grid, to avoid an infinite
		// loop when creating the graph
		int numberOfNodes;
		if (config.numberOfNodes > (config.gridWidth + 1) * (config.gridHeight + 1)) {
			numberOfNodes = (config.gridWidth + 1) * (config.gridHeight + 1);
		} else {
			numberOfNodes = config.numberOfNodes;
		}

		// read the map generator to use from config and instantiate it via
		// reflection
		try {
			Class generatorClass = Class.forName("massim.competition2013.scenario." + config.mapGenerator);
			this.generator = (GraphGenerator) generatorClass.newInstance();
		} catch (Exception e) {
			log(LOGLEVEL_NORMAL, "Couldn't find specified map generator - using standard one");
			this.generator = new GraphGenerator2013();
		}

		if (this.generator instanceof GraphGenerator2013) {
			// set the thinning
			((GraphGenerator2013) this.generator).setThinning(config.thinning); // FIXME
		}

		this.generator.generate(this.nodes, this.edges, numberOfNodes, config.gridWidth, config.gridHeight, config.cellWidth, config.minNodeWeight, config.maxNodeWeight, config.minEdgeCost, config.maxEdgeCost, getRandom(), config.randomWeight, config.gradientWeight, config.optimaWeight,
				config.blurIterations, config.optimaPercentage);
		populateMapsCahes();

		this.sizeX = config.cellWidth * (config.gridWidth + 1);
		this.sizeY = config.cellWidth * (config.gridHeight + 1);

		// add inverse mappings in case of symmetrical generation (it's used
		// later for agent placement)
		if (this.generator instanceof GraphGeneratorTriangulationBalanced || this.generator instanceof GraphGeneratorTriangBalOpt || this.generator instanceof GraphGenerator2013) {
			HashMap<GraphNode, GraphNode> cp;
			if (this.generator instanceof GraphGeneratorTriangulationBalanced) {
				cp = ((GraphGeneratorTriangulationBalanced) this.generator).getCounterparts();
			} else if (this.generator instanceof GraphGeneratorTriangBalOpt) {
				cp = ((GraphGeneratorTriangBalOpt) this.generator).getCounterparts();
			} else {
				cp = ((GraphGenerator2013) this.generator).getCounterparts();
			}

			HashMap<GraphNode, GraphNode> cpCopy = new HashMap<>(cp);
			for (GraphNode n : cpCopy.keySet()) {
				cp.put(cpCopy.get(n), n);
			}
		}
	}

	/**
	 * Initialize the maps used as cache for faster data access.
	 */
	protected void populateMapsCahes() {
		this.neighborsMap = new HashMap<>(this.nodes.size());
		this.connectedEdgesMap = new HashMap<>(this.nodes.size());
		for (GraphNode node : this.nodes) {
			this.neighborsMap.put(node, new ArrayList<GraphNode>());
			this.connectedEdgesMap.put(node, new ArrayList<GraphEdge>());
			this.nodeNamesMap.put(node.name, node);
		}

		for (GraphEdge edge : this.edges) {
			this.neighborsMap.get(edge.node1).add(edge.node2);
			this.neighborsMap.get(edge.node2).add(edge.node1);
			this.connectedEdgesMap.get(edge.node1).add(edge);
			this.connectedEdgesMap.get(edge.node2).add(edge);
		}
	}

	/**
	 * getter for the vector holding all agents that take part in the
	 * simulation.
	 * 
	 * @return
	 */
	public Vector<GraphSimulationAgentState> getAgents() {
		return this.agents;
	}

	/**
	 * setter for the vector holding all agents that take part in the
	 * simulation.
	 * 
	 * @param agents
	 */
	public void setAgents(Vector<GraphSimulationAgentState> agents) {
		this.agents = agents;
	}

	/**
	 * getter for the configuration object.
	 * 
	 * @return
	 */
	public GraphSimulationConfiguration getConfig() {
		return this.config;
	}

	/**
	 * Returns the state of an agent given its name.
	 * 
	 * @param agentName
	 * @return
	 */
	public GraphSimulationAgentState getAgent(String agentName) {
		return this.agentNamesMap.get(agentName);
	}

	/**
	 * Returns the node object representation given its name.
	 * 
	 * @param nodeName
	 * @return
	 */
	public GraphNode getNode(String nodeName) {
		return this.nodeNamesMap.get(nodeName);
	}

	/**
	 * Returns the list of the nodes directly connected to <code>node</code>
	 * 
	 * @param node
	 * @return
	 */
	public List<GraphNode> getNeighborNodes(GraphNode node) {
		return Collections.unmodifiableList(this.neighborsMap.get(node));
	}

	/**
	 * Returns the list of the edges connected to <code>node</code>
	 * 
	 * @param node
	 * @return
	 */
	public List<GraphEdge> getConnectedEdges(GraphNode node) {
		return Collections.unmodifiableList(this.connectedEdgesMap.get(node));
	}

	/**
	 * getter for the vector holding all nodes of the map.
	 * 
	 * @return
	 */
	public Collection<GraphNode> getNodes() {
		return this.nodes;
	}

	/**
	 * Adds <code>agent</code> to the currently simulation, and situates it in a
	 * random node in the map.
	 * 
	 * @param agent
	 */
	public void addAgent(GraphSimulationAgentState agent, Vector<Integer> agentPositions, boolean newPosition) {

		GraphNode node = null;

		if (this.generator instanceof GraphGeneratorTriangulationBalanced || this.generator instanceof GraphGeneratorTriangBalOpt || this.generator instanceof GraphGenerator2013) {

			int index = -1;

			if (newPosition) {
				index = getRandom().nextInt(this.nodes.size());
				agentPositions.add(index);
				node = this.nodes.get(index);
			} else {
				index = agentPositions.get(0);
				agentPositions.remove(0);

				if (this.generator instanceof GraphGeneratorTriangulationBalanced) {
					node = ((GraphGeneratorTriangulationBalanced) this.generator).getCounterparts().get(this.nodes.get(index));
				} else if (this.generator instanceof GraphGeneratorTriangBalOpt) {
					node = ((GraphGeneratorTriangBalOpt) this.generator).getCounterparts().get(this.nodes.get(index));
				} else {
					node = ((GraphGenerator2013) this.generator).getCounterparts().get(this.nodes.get(index));
				}

			}
		} else {
			node = this.nodes.get(getRandom().nextInt(this.nodes.size()));
		}

		node.agents.add(agent);
		agent.setNode(node);
		this.agents.add(agent);
		this.agentNamesMap.put(agent.name, agent);

		if (getTeamNr(agent.team) == -1) {
			int idx = this.teamsStates.size();
			this.teamsStates.add(new TeamState(agent.team, idx));
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
		TeamState ts = getTeamState(name);
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
	private Random getRandom() {
		if (this.random == null) {
			// random = new Random(System.currentTimeMillis());
			this.random = new Random(this.config.randomSeed);
		}
		return this.random;
	}

}

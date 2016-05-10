package massim.competition2013;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import massim.competition2013.GraphPolicy.EdgeDummy;
import massim.competition2013.monitor.Definitions;
import massim.competition2013.scenario.GraphEdge;
import massim.competition2013.scenario.GraphNode;
import massim.competition2013.scenario.TeamState;
import massim.framework.Observer;
import massim.framework.SimulationConfiguration;
import massim.framework.SimulationState;
import massim.framework.simulation.AgentState;
import massim.framework.simulation.SimulationStateImpl;
import massim.gridsimulations.SimulationRMIXMLDocumentObserver;
import massim.gridsimulations.SimulationWorldState;
import massim.gridsimulations.SimulationXMLStatisticsObserver;

/**
 * This VisualizationObserver takes care about the visualization (svg-files).
 * 
 */
public class GraphSimulationVisualizationObserver implements Observer {

	public static String simulationName;
	public String tournamentName;
	protected GraphPolicy output;
	protected String outputFolder;
	// protected int htaccess = 1;
	protected int laststep = -1;

	private long maxSizeX;
	private long maxSizeY;

	private String visualisationobserverOutputPath;

	private boolean profiling = false;

	private Color[] neededColors = new Color[] { Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.WHITE, Color.BLACK, Color.YELLOW.darker(), Color.RED.darker(), Color.GREEN.darker().darker(), Color.GREEN, Color.BLUE, GraphPolicy.COLOR_TEAM1_ZONE, GraphPolicy.COLOR_TEAM2_ZONE,
			GraphPolicy.BLUE, GraphPolicy.GREEN, Color.GRAY, new Color(GraphPolicy.COLOR_TEAM1_ZONE.getRed(), GraphPolicy.COLOR_TEAM1_ZONE.getGreen(), GraphPolicy.COLOR_TEAM1_ZONE.getBlue(), 170),
			new Color(GraphPolicy.COLOR_TEAM2_ZONE.getRed(), GraphPolicy.COLOR_TEAM2_ZONE.getGreen(), GraphPolicy.COLOR_TEAM2_ZONE.getBlue(), 170) };

	public static final String CSS_CONTENT = ".E{stroke:rgba(150,150,150,0.78431374);stroke-linecap:round;stroke-linejoin:round}\n" + ".TxtNrml{font-weight:bold;font-size:32px;fill:black}\n" + ".Tb{font-size:24px;font-weight:bold;fill:black}\n"
			+ ".T1{fill:rgba(0,77,56,1.0);stroke:rgba(0,77,56,1.0);stroke-width:50;stroke-linecap:round;stroke-linejoin:round}\n" + ".T2{fill:rgba(0,55,86,1.0);stroke:rgba(0,55,86,1.0);stroke-width:50;stroke-linecap:round;stroke-linejoin:round}\n"
			+ ".T1n{stroke-width:0;fill:rgba(0,77,56,1.0);stroke:rgba(0,77,56,1.0)}\n" + ".T2n{stroke-width:0;fill:rgba(0,55,86,1.0);stroke:rgba(0,55,86,1.0)}\n" + ".T1e{stroke-linecap:round;stroke-linejoin:round;stroke-width:50;stroke:rgba(0,77,56,1.0)}\n"
			+ ".T2e{stroke-linecap:round;stroke-linejoin:round;stroke-width:50;stroke:rgba(0,55,86,1.0)}\n" + ".Tb2{font-size:18px;font-weight:bold;fill:black}\n" + ".TxtAch{font-size:18px;font-weight:bold;fill:black;text-anchor:start}\n"
			+ ".N{stroke:rgba(150,150,150,0.78431374);fill:rgba(214,214,214,0.78431374);stroke-width:0}\n" + ".sd{stroke-dasharray:30,30;stroke-dashoffset:30;}";

	public Map<String, String> fillClasses = new HashMap<>();
	public Map<String, String> strokeClasses = new HashMap<>();
	public Map<Integer, String> strokeWidthClasses = new HashMap<>();
	public Map<String, String> arrowDefinitions= new HashMap<>();

	public GraphSimulationVisualizationObserver(String visualisationobserverOutputPath) {
		this.visualisationobserverOutputPath = visualisationobserverOutputPath;
		this.output = new GraphPolicy(this);

		initClassMaps();
	}

	public GraphSimulationVisualizationObserver() {
		this.output = new GraphPolicy(this);

		initClassMaps();
	}

	private void initClassMaps() {

		int i = 0;
		for (Color c : this.neededColors) {
			String crgb = GraphPolicy.makeRGB(c);
			this.fillClasses.put(crgb, "f" + i);
			this.strokeClasses.put(crgb, "s" + i);
			i++;
		}
		for (int j = 1; j <= 11; j++) {
			this.strokeWidthClasses.put(j, "sw" + j);
		}
	}

	public void notifySimulationStart() {

		this.laststep = -1;
	}

	public void notifySimulationConfiguration(SimulationConfiguration simconf) {

		GraphSimulationConfiguration simconfig = (GraphSimulationConfiguration) simconf;
		simulationName = simconfig.simulationName;
		this.tournamentName = simconfig.tournamentName;

		// create svg-output-file.
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		this.outputFolder = this.visualisationobserverOutputPath + System.getProperty("file.separator") + simulationName + "_" + df.format(dt);
		this.output.createFolder(this.outputFolder);

		// FIXME observers shouldn't communicate via static strings
		SimulationXMLStatisticsObserver.simulationName = simulationName + "_" + df.format(dt);
		SimulationRMIXMLDocumentObserver.simulationName = simulationName + "_" + df.format(dt);
		GraphSimulationXMLStatisticsObserver.simulationName = simulationName + "_" + df.format(dt);
		GraphSimulationRMIXMLDocumentObserverWebInterface.outputFolder = this.visualisationobserverOutputPath + System.getProperty("file.separator");
		GraphSimulationXMLStatisticsObserver.outputFolder = this.visualisationobserverOutputPath + System.getProperty("file.separator");

		this.output.setTeamNames(simconfig.getTeamNames());

		this.maxSizeX = simconfig.gridHeight * simconfig.cellWidth;
		this.maxSizeY = simconfig.gridWidth * simconfig.cellWidth;

	}

	public void notifySimulationEnd() {

		// -> profiling
		if (this.profiling) {
			Double mean = .0;
			for (Double l : this.output.intervals) {
				mean += l;
			}
			mean /= (new Double(this.output.intervals.size()));
			System.out.println("Mean-time: " + mean + "ms");
		}
		// <- profiling

		this.output.createPreviewSvg();
	}

	public void notifySimulationState(SimulationState state) {

		SimulationStateImpl tcstate = (SimulationStateImpl) state;

		if (this.laststep == -1) {
			createStyleSheet();
			drawBackground((SimulationWorldState) tcstate.simulationState);
		}

		if (this.laststep != tcstate.steps) {
			drawSimulation(tcstate.steps, (SimulationWorldState) tcstate.simulationState, (AgentState[]) tcstate.agentStates);
		}
	}

	private void createStyleSheet() {
		File css = new File(this.outputFolder + System.getProperty("file.separator") + "style.css");
		try {
			FileWriter out = new FileWriter(css);
			out.write(CSS_CONTENT);

			for (String crgb : this.fillClasses.keySet()) {
				out.write("\n." + this.fillClasses.get(crgb) + "{fill:" + crgb + "}");
			}
			for (String crgb : this.strokeClasses.keySet()) {
				out.write("\n." + this.strokeClasses.get(crgb) + "{stroke:" + crgb + "}");
			}
			for (Integer width : this.strokeWidthClasses.keySet()) {
				out.write("\n." + this.strokeWidthClasses.get(width) + "{stroke-width:" + width + "}");
			}

			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected synchronized void drawBackground(SimulationWorldState s_state) {

		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;

		this.output.create();
		this.output.drawGraph("graph", state.sizeX, state.sizeY, state.nodes, state.edges);

		this.output.drawFixedInformation(state, this.maxSizeX);

		this.output.save(this.maxSizeX, this.maxSizeY);
	}

	protected synchronized void drawSimulation(int step, SimulationWorldState s_state, AgentState[] agentstates) {

		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;
		this.output.create();

		// A: Draw Zones

		long startTime = System.nanoTime();

		// A.1. filter edges and nodes
		Map<String, Collection<GraphNode>> filteredNodes = filterNodes(state.nodes);
		Map<String, Collection<EdgeDummy>> filteredEdges = filterEdges(state.edges, state.nodes);

		// A.2. render
		for (String domTeam : filteredEdges.keySet()) {
			Color domColor;
			String style;
			if (domTeam.equals(this.output.getTeamOne())) {
				domColor = GraphPolicy.COLOR_TEAM1_ZONE;
				style = "T1";
			} else {
				domColor = GraphPolicy.COLOR_TEAM2_ZONE;
				style = "T2";
			}
			this.output.drawZones(filteredNodes.get(domTeam), filteredEdges.get(domTeam), domColor, style);
		}

		// ->profiling
		if (this.profiling) {
			long time = System.nanoTime() - startTime;
			if (time > this.output.maxTime) {
				this.output.maxTime = time;
			}
			if (time < this.output.minTime) {
				this.output.minTime = time;
			}
			System.out.println("+++ time: " + time / 1000000.0 + "ms");
			System.out.println("+++ maxTime: " + this.output.maxTime / 1000000.0 + "ms");
			System.out.println("+++ minTime: " + this.output.minTime / 1000000.0 + "ms");

			this.output.intervals.add(time / 1000000.0);
		}
		// <-profiling

		// A.3. render probing-information
		// for(GraphNode n: state.nodes){
		//
		// Vector<TeamState> tStates = state.teamsStates;
		// Vector<TeamState> probeTeams = new Vector<TeamState>();
		// for(TeamState ts: tStates){
		// if(ts.getProbedNodes().contains(n.name)){
		// probeTeams.add(ts);
		// }
		// }
		//
		// output.drawProbing(n, probeTeams);
		// }

		// B: Draw Agents
		Map<GraphSimulationAgentState, Point> agPositions = new HashMap<>();
		Map<String, GraphNode> nodeMap = new HashMap<>();
		for (GraphNode n : state.nodes) {
			nodeMap.put(n.name, n);
		}
		for (GraphNode n : state.nodes) {

			Vector<GraphSimulationAgentState> ags = n.agents;

			float offset = GraphPolicy.NODE_RADIUS + 26 + 4.0f;
			float angle = 3.14159f / 16.0f;
			float agentsPerCircle = 6.0f;
			int nextCircle = (int) agentsPerCircle;
			int i = 0;

			// Draw Agents per Node
			for (GraphSimulationAgentState ag : ags) {

				i++;

				int agx = (int) (Math.sin(angle) * offset) + n.x;
				int agy = (int) (Math.cos(angle) * offset) + n.y;

				agPositions.put(ag, new Point(agx, agy));

				Color c;

				if (ag.team.equals(this.output.getTeamOne())) {
					c = GraphPolicy.GREEN;
				} else {
					c = GraphPolicy.BLUE;
				}

				this.output.drawAgent(ag, c, agx, agy);

				angle += 2.0f * 3.14159f / agentsPerCircle;

				if (i == nextCircle) {
					offset += 2.0f * (float) GraphPolicy.AGENT_RADIUS + 4.0f;
					agentsPerCircle = (float) ((int) (offset * 3.14159f / ((float) GraphPolicy.AGENT_RADIUS + 1.0f)));
					nextCircle += (int) agentsPerCircle;
				}
			}// all agents for the current node drawn
		}// all agents drawn

		// B.2 draw action-target lines
		// clear the arrowDefinitions first.
		this.arrowDefinitions.clear();
		// draw action-target lines
		for (GraphSimulationAgentState ag : state.agents) {
			String action = ag.lastAction;

			if (ag.lastActionParam == null || ag.lastActionParam.equals("")) {
				continue;
			}

			Color c;

			if (ag.team.equals(this.output.getTeamOne())) {
				c = GraphPolicy.GREEN;
			} else {
				c = GraphPolicy.BLUE;
			}

			if (action.equalsIgnoreCase("attack")) {
				String target = ag.lastActionParam;

				for (GraphSimulationAgentState ag2 : state.agents) {
					if (ag2.name.equalsIgnoreCase(target)) {

						if (ag.node != ag2.node) {
							// draw connection
							Point agXY = agPositions.get(ag);
							Point ag2XY = agPositions.get(ag2);
							this.output.drawTargetLine(agXY.x, agXY.y, ag2XY.x, ag2XY.y, ag, ag.lastAction, ag.lastActionResult, c);
						}

						break;
					}
				}
			} else if (action.equalsIgnoreCase("inspect")) {
				String target = ag.lastActionParam;

				for (GraphSimulationAgentState ag2 : state.agents) {
					if (ag2.name.equalsIgnoreCase(target)) {

						if (ag.node != ag2.node) {
							// draw connection
							Point agXY = agPositions.get(ag);
							Point ag2XY = agPositions.get(ag2);
							this.output.drawTargetLine(agXY.x, agXY.y, ag2XY.x, ag2XY.y, ag, ag.lastAction, ag.lastActionResult, c);
						}

						break;
					}
				}
			} else if (action.equalsIgnoreCase("repair")) {
				String target = ag.lastActionParam;

				for (GraphSimulationAgentState ag2 : state.agents) {
					if (ag2.name.equalsIgnoreCase(target)) {

						if (ag.node != ag2.node) {
							// draw connection
							Point agXY = agPositions.get(ag);
							Point ag2XY = agPositions.get(ag2);
							this.output.drawTargetLine(agXY.x, agXY.y, ag2XY.x, ag2XY.y, ag, ag.lastAction, ag.lastActionResult, c);
						}

						break;
					}
				}
			} else if (action.equalsIgnoreCase("probe")) {
				String target = ag.lastActionParam;
				GraphNode nTarget = nodeMap.get(target);

				if (!(nTarget.x == ag.node.x && nTarget.y == ag.node.y)) {
					// draw connection to target node
					Point agXY = agPositions.get(ag);
					this.output.drawTargetLine(agXY.x, agXY.y, nTarget.x, nTarget.y, ag, ag.lastAction, ag.lastActionResult, c);
				}
			}
		}

		// C: Draw statistical information
		int y = this.output.drawFirstTable(state.teamsStates, this.maxSizeX, state.currentStep);
		int achY = y + 26;
		y = this.output.drawSecondTable(state.agents, this.maxSizeX, y + 26);
		this.output.listAchievements(achY, this.maxSizeX + 1050, state.teamsStates);

		this.laststep = step;

		this.output.save(this.maxSizeX, this.maxSizeY);
	}

	private Map<String, Collection<GraphNode>> filterNodes(Vector<GraphNode> nodes) {

		Map<String, Collection<GraphNode>> ret = new HashMap<>();

		for (GraphNode n : nodes) {

			String domTeam = n.getDominatorTeam();

			if (domTeam != null) {
				Collection<GraphNode> coll = ret.get(domTeam);
				if (coll == null) {
					coll = new Vector<>();
					ret.put(domTeam, coll);
				}
				coll.add(n);
			}

		}

		return ret;
	}

	private Map<String, Collection<EdgeDummy>> filterEdges(Vector<GraphEdge> edges, Vector<GraphNode> nodes) {

		Map<String, Collection<EdgeDummy>> ret = new HashMap<>();

		HashMap<GraphNode, Vector<EdgeDummy>> incoming = new HashMap<>();
		HashMap<GraphNode, Vector<EdgeDummy>> outgoing = new HashMap<>();
		for (GraphNode n : nodes) {
			incoming.put(n, new Vector<EdgeDummy>());
			outgoing.put(n, new Vector<EdgeDummy>());
		}

		for (GraphEdge e : edges) {

			String domTeam = null;
			if (e.node1.getDominatorTeam() != null && e.node1.getDominatorTeam() == e.node2.getDominatorTeam()) {
				domTeam = e.node1.getDominatorTeam();
			}

			if (domTeam != null) {

				// add to collection
				Collection<EdgeDummy> coll = ret.get(domTeam);
				if (coll == null) {
					coll = new Vector<>();
					ret.put(domTeam, coll);
				}

				// add the edge
				EdgeDummy e1 = this.output.new EdgeDummy();
				e1.node1 = e.node1;
				e1.node2 = e.node2;
				e1.weight = e.weight;
				coll.add(e1);

				incoming.get(e.node2).add(e1);
				outgoing.get(e.node1).add(e1);

				// generate the inverse edge and add
				EdgeDummy e2 = this.output.new EdgeDummy();
				e2.node1 = e.node2;
				e2.node2 = e.node1;
				e2.weight = e.weight;
				coll.add(e2);

				incoming.get(e.node1).add(e2);
				outgoing.get(e.node2).add(e2);
			}
		}

		for (GraphNode n : nodes) {
			for (EdgeDummy e1 : incoming.get(n)) {
				for (EdgeDummy e2 : outgoing.get(n)) {
					e1.succ.add(e2);
				}
			}
			for (EdgeDummy e1 : outgoing.get(n)) {
				for (EdgeDummy e2 : incoming.get(n)) {
					e1.pred.add(e2);
				}
			}
		}

		return ret;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

}

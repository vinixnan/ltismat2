package massim.competition2012;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import massim.competition2012.GraphPolicy.EdgeDummy;
import massim.competition2012.scenario.GraphEdge;
import massim.competition2012.scenario.GraphNode;
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
public class GraphSimulationVisualizationObserver implements Observer{
	
	public static String simulationName;
	public String tournamentName;
	protected GraphPolicy output;
	protected String outputFolder;
//	protected int htaccess = 1;
	protected int laststep = -1;
	
	private long maxSizeX;
	private long maxSizeY;
	
	private String visualisationobserverOutputPath;
	
	private boolean profiling = false;
	
	public static final String CSS_CONTENT = 
			".GrRd{stroke:rgba(150,150,150,0.78431374);stroke-linecap:round;stroke-linejoin:round} \n " +
			".TxtNrml{font-weight:bold;font-size:32px;fill:black}  \n " +
			".Tb{font-size:24px;font-weight:bold;fill:black}  \n " +
			".T1{fill:rgba(0,77,56,1.0);stroke:rgba(0,77,56,1.0);stroke-width:50;stroke-linecap:round;stroke-linejoin:round}  \n " +
			".T2{fill:rgba(0,55,86,1.0);stroke:rgba(0,55,86,1.0);stroke-width:50;stroke-linecap:round;stroke-linejoin:round}  \n " +
			".T1n{stroke-width:0;fill:rgba(0,77,56,1.0);stroke:rgba(0,77,56,1.0)}  \n " +
			".T2n{stroke-width:0;fill:rgba(0,55,86,1.0);stroke:rgba(0,55,86,1.0)}  \n " +
			".T1e{stroke-linecap:round;stroke-linejoin:round;stroke-width:50;stroke:rgba(0,77,56,1.0)}  \n " +
			".T2e{stroke-linecap:round;stroke-linejoin:round;stroke-width:50;stroke:rgba(0,55,86,1.0)}  \n " +
			".Tb2{font-size:18px;font-weight:bold;fill:black}  \n " +
			".TxtAch{font-size:18px;font-weight:bold;fill:black;text-anchor:start}  \n " +
			".GrN{stroke:rgba(150,150,150,0.78431374);fill:rgba(214,214,214,0.78431374);stroke-width:0}";
	
	public GraphSimulationVisualizationObserver(String visualisationobserverOutputPath) {
		this.visualisationobserverOutputPath = visualisationobserverOutputPath;
		this.output = new GraphPolicy();
	}
	
	public GraphSimulationVisualizationObserver(){
		this.output = new GraphPolicy();
	}
	
	public void notifySimulationStart() {
		
		laststep=-1;
	}
	
	public void notifySimulationConfiguration(SimulationConfiguration simconf) {
		
		GraphSimulationConfiguration simconfig = (GraphSimulationConfiguration) simconf;
		simulationName = simconfig.simulationName;
		tournamentName = simconfig.tournamentName;
		
		// create svg-output-file.
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		outputFolder = visualisationobserverOutputPath + System.getProperty("file.separator") + simulationName + "_" + df.format(dt);
		output.createFolder(outputFolder);
		
		
		//FIXME observers shouldn't communicate via static strings
		SimulationXMLStatisticsObserver.simulationName = simulationName + "_" + df.format(dt);
		SimulationRMIXMLDocumentObserver.simulationName = simulationName + "_" + df.format(dt);
		GraphSimulationXMLStatisticsObserver.simulationName = simulationName + "_" + df.format(dt);
		GraphSimulationRMIXMLDocumentObserverWebInterface.outputFolder = visualisationobserverOutputPath + System.getProperty("file.separator");
		GraphSimulationXMLStatisticsObserver.outputFolder = visualisationobserverOutputPath + System.getProperty("file.separator");
		
		
		output.setTeamNames(simconfig.getTeamNames());
		
		this.maxSizeX = simconfig.gridHeight*simconfig.cellWidth;
		this.maxSizeY = simconfig.gridWidth*simconfig.cellWidth;

	}
	
	public void notifySimulationEnd() {
		
		//-> profiling
		if(profiling){
			Double mean = .0;
			for (Double l : output.intervals){
				mean += l;
			}
			mean /= (new Double(output.intervals.size()));
			System.out.println("Mean-time: " + mean + "ms");
		}
		//<- profiling
		
		output.createPreviewSvg();
	}
	
	public void notifySimulationState(SimulationState state) {
		
		SimulationStateImpl tcstate = (SimulationStateImpl) state;
		
		if(laststep == -1) {
			createStyleSheet();
			drawBackground((SimulationWorldState)tcstate.simulationState);
		}
		
		if (laststep != tcstate.steps) {
			drawSimulation(tcstate.steps,  
					(SimulationWorldState)tcstate.simulationState, 
					(AgentState[]) tcstate.agentStates);
		}
	}

	private void createStyleSheet() {
		File css = new File(outputFolder+System.getProperty("file.separator")+"style.css");
		try {
			FileWriter out = new FileWriter(css);
			out.write(CSS_CONTENT);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected synchronized void drawBackground(SimulationWorldState s_state) {
		
		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;
		
		output.create();
		output.drawGraph("graph", state.sizeX, state.sizeY, state.nodes, state.edges);
		
		output.drawFixedInformation(state, maxSizeX);

		output.save(maxSizeX, maxSizeY);
	}

	protected synchronized void drawSimulation(int step,SimulationWorldState s_state,
			AgentState[] agentstates) {
		
		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;
		output.create();
		
		// A: Draw Zones
		
		long startTime = System.nanoTime();
		
		// A.1. filter edges and nodes
		Map<String,Collection<GraphNode>> filteredNodes = filterNodes(state.nodes);
		Map<String,Collection<EdgeDummy>> filteredEdges = filterEdges(state.edges, state.nodes);
		
		// A.2. render
		for ( String domTeam : filteredEdges.keySet() ) {
			Color domColor;
			String style;
			if(domTeam.equals(output.getTeamOne())){
				domColor = GraphPolicy.COLOR_TEAM1_ZONE;
				style = "T1";
			}
			else{
				domColor = GraphPolicy.COLOR_TEAM2_ZONE;
				style = "T2";
			}
			output.drawZones(filteredNodes.get(domTeam), 
					filteredEdges.get(domTeam), domColor, style);
		}
		
		//->profiling
		if(profiling){
			long time = System.nanoTime() - startTime;
			if ( time > output.maxTime ) {
				output.maxTime = time;
			}
			if ( time < output.minTime ) {
				output.minTime = time;
			}
			System.out.println("+++ time: " + time / 1000000.0 + "ms");
			System.out.println("+++ maxTime: " + output.maxTime / 1000000.0 + "ms");
			System.out.println("+++ minTime: " + output.minTime / 1000000.0 + "ms");
			
			output.intervals.add(time/1000000.0);
		}
		//<-profiling
		
		// B: Draw Agents
		for (GraphNode n : state.nodes){
			
			Vector<GraphSimulationAgentState> ags = n.agents;
			
			float offset = GraphPolicy.NODE_RADIUS + GraphPolicy.AGENT_RADIUS + 4.0f;
			float angle = 3.14159f / 16.0f;
			float agentsPerCircle = 6.0f;
			int nextCircle = (int) agentsPerCircle;
			int i = 0;	
			
			// Draw Agents per Node
			for (GraphSimulationAgentState ag : ags){
				
				i++;
				
				int agx = (int) (Math.sin(angle) * offset) + n.x;
				int agy = (int) (Math.cos(angle) * offset) + n.y;
				
				Color c;
				
				if(ag.team.equals(output.getTeamOne())){
					c = GraphPolicy.GREEN;
				}
				else{
					c = GraphPolicy.BLUE;
				}
				
				output.drawAgent(ag, c, agx, agy);
				
				angle += 2.0f * 3.14159f / agentsPerCircle;
				
				if (i == nextCircle){
					offset += 2.0f * (float)GraphPolicy.AGENT_RADIUS +4.0f;
					agentsPerCircle = (float)((int)(offset * 3.14159f / ((float)GraphPolicy.AGENT_RADIUS + 1.0f)));
					nextCircle += (int)agentsPerCircle;
				}	
			}// all agents for the current node drawn
		}// all agents drawn
		
		// C: Draw statistical information
		int y = output.drawFirstTable(state.teamsStates, maxSizeX, state.currentStep);
		y = output.drawSecondTable(state.agents, maxSizeX, y+20);
		output.listAchievements(y, maxSizeX, state.teamsStates);

		laststep = step;

		output.save(maxSizeX, maxSizeY);
	}

	private Map<String, Collection<GraphNode>> filterNodes(Vector<GraphNode> nodes) {
		
		Map<String, Collection<GraphNode>> ret = 
				new HashMap<String, Collection<GraphNode>>();
		
		for ( GraphNode n : nodes ) {
			
			String domTeam = n.getDominatorTeam();
			
			if ( domTeam != null ) {
				Collection<GraphNode> coll = ret.get(domTeam);
				if ( coll == null ) {
					coll = new Vector<GraphNode>();
					ret.put(domTeam, coll);
				}
				coll.add(n);
			}	
			
		}
		
		return ret;
	}

	private Map<String, Collection<EdgeDummy>> filterEdges(Vector<GraphEdge> edges, Vector<GraphNode> nodes) {
		
		Map<String, Collection<EdgeDummy>> ret = 
				new HashMap<String, Collection<EdgeDummy>>();
		
		HashMap<GraphNode, Vector<EdgeDummy>> incoming = new HashMap<GraphNode, Vector<EdgeDummy>>();
		HashMap<GraphNode, Vector<EdgeDummy>> outgoing = new HashMap<GraphNode, Vector<EdgeDummy>>();
		for(GraphNode n : nodes){
			incoming.put(n, new Vector<EdgeDummy>());
			outgoing.put(n, new Vector<EdgeDummy>());
		}
		
		for ( GraphEdge e : edges ) {
			
			String domTeam = null;
			if (e.node1.getDominatorTeam() != null 
					&& e.node1.getDominatorTeam() == e.node2.getDominatorTeam()) {
				domTeam = e.node1.getDominatorTeam();
			}
			
			if ( domTeam != null ) {
				
				// add to collection
				Collection<EdgeDummy> coll = ret.get(domTeam);
				if ( coll == null ) {
					coll = new Vector<EdgeDummy>();
					ret.put(domTeam, coll);
				}

				// add the edge
				EdgeDummy e1 = output.new EdgeDummy();
				e1.node1 = e.node1;
				e1.node2 = e.node2;
				e1.weight = e.weight;
				coll.add(e1);
				
				incoming.get(e.node2).add(e1);
				outgoing.get(e.node1).add(e1);
				
				// generate the inverse edge and add
				EdgeDummy e2 = output.new EdgeDummy();
				e2.node1 = e.node2;
				e2.node2 = e.node1;
				e2.weight = e.weight;
				coll.add(e2);
				
				incoming.get(e.node1).add(e2);
				outgoing.get(e.node2).add(e2);
			}	
		}
		
		for(GraphNode n : nodes){
			for(EdgeDummy e1 : incoming.get(n)){
				for(EdgeDummy e2 : outgoing.get(n)){
					e1.succ.add(e2);
				}
			}
			for(EdgeDummy e1 : outgoing.get(n)){
				for(EdgeDummy e2 : incoming.get(n)){
					e1.pred.add(e2);
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public void start() {}

	@Override
	public void stop() {}
	

}

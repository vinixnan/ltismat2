package massim.competition2011;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import massim.competition2011.scenario.GraphEdge;
import massim.competition2011.scenario.GraphNode;
import massim.framework.Observer;
import massim.framework.SimulationConfiguration;
import massim.framework.SimulationState;
import massim.framework.simulation.AgentState;
import massim.framework.simulation.SimulationStateImpl;
import massim.gridsimulations.SimulationRMIXMLDocumentObserver;
import massim.gridsimulations.SimulationWorldState;
import massim.gridsimulations.SimulationXMLStatisticsObserver;
import massim.competition2011.GraphSimulationRMIXMLDocumentObserverWebInterface;


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
		output.createPreviewSvg();
	}
	
	public void notifySimulationState(SimulationState state) {
		
		SimulationStateImpl tcstate = (SimulationStateImpl) state;
		
		if(laststep == -1) {
			drawBackground((SimulationWorldState)tcstate.simulationState);
		}
		
		if (laststep != tcstate.steps) {
			drawSimulation(tcstate.steps,  
					(SimulationWorldState)tcstate.simulationState, 
					(AgentState[]) tcstate.agentStates);
		}
	}

	protected synchronized void drawBackground(SimulationWorldState s_state) {
		
		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;
		
		output.create();
		output.drawGraph("graph", state.sizeX, state.sizeY, state.nodes, state.edges);

		output.save(maxSizeX, maxSizeY);
	}

	protected synchronized void drawSimulation(int step,SimulationWorldState s_state,
			AgentState[] agentstates) {
		
		GraphSimulationWorldState state = (GraphSimulationWorldState) s_state;
		output.create();
		
		//Draw colored Edges
		for (GraphEdge e : state.edges){
			
			String domTeam = e.node1.getDominatorTeam();
			if (domTeam != null && domTeam == e.node2.getDominatorTeam()){
				
				output.drawEdge(e);
			}
		}
		
		//Draw colored Nodes
		for (GraphNode n : state.nodes){
			
			String domTeam = n.getDominatorTeam();
			if(domTeam != null){

				output.drawNode(n);
			}
			
			Vector<GraphSimulationAgentState> ags = n.agents;
			
			float offset = GraphPolicy.NODE_RADIUS + GraphPolicy.AGENT_RADIUS + 4.0f;
			float angle = 3.14159f / 16.0f;
			float agentsPerCircle = 6.0f;
			int nextCircle = (int) agentsPerCircle;
			int i = 0;	
			String str ="";
			
			//Draw Agents per Node
			for (GraphSimulationAgentState ag : ags){
				
				i++;
				
				int agx = (int) (Math.sin(angle) * offset) + n.x;
				int agy = (int) (Math.cos(angle) * offset) + n.y;
				
				String color;
				
				if(ag.team.equals(output.getTeamOne())){
					color = GraphPolicy.COLOR_GREEN;
				}
				else{
					color = GraphPolicy.COLOR_BLUE;
				}
				
				str = ag.health > 0? "rgb(0,0,0)": "rgb(120,120,120)";
				
				output.drawRoundRect(agx - (GraphPolicy.AGENT_RADIUS+3), agy - (GraphPolicy.AGENT_RADIUS+3), 
						2*(GraphPolicy.AGENT_RADIUS+3), 2*(GraphPolicy.AGENT_RADIUS+3), false, "", "rgb(0,0,0)", 1);
				
				output.drawRoundRect(agx - (GraphPolicy.AGENT_RADIUS+2), agy - (GraphPolicy.AGENT_RADIUS+2), 
						2*(GraphPolicy.AGENT_RADIUS+2), 2*(GraphPolicy.AGENT_RADIUS+2), false, "", color, 2);
				
				output.drawRoundRect(agx - (GraphPolicy.AGENT_RADIUS+0), agy - (GraphPolicy.AGENT_RADIUS+0), 
						2*(GraphPolicy.AGENT_RADIUS+0), 2*(GraphPolicy.AGENT_RADIUS+0), true, str, str, 1);
				
//				str = "" + ag.energy + "|" + ag.health;
				//use agent's role's abbreviation as label
				str = ag.roleName.substring(0,2).toUpperCase();
				output.drawText(agx-9, agy+5, str, "rgb(255,255,255)");
				
				angle += 2.0f * 3.14159f / agentsPerCircle;
				
				if (i == nextCircle){
					offset += 2.0f * (float)GraphPolicy.AGENT_RADIUS +4.0f;
					agentsPerCircle = (float)((int)(offset * 3.14159f / ((float)GraphPolicy.AGENT_RADIUS + 1.0f)));
					nextCircle += (int)agentsPerCircle;
				}	
			}
		}

		//Draw statistical information
		int y = output.drawFirstTable(state.teamsStates, maxSizeX, state.currentStep);
		y = output.drawSecondTable(state.agents, maxSizeX, y+20);
		output.listAchievements(y, maxSizeX, state.teamsStates);

		laststep = step;

		output.save(maxSizeX, maxSizeY);
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

}

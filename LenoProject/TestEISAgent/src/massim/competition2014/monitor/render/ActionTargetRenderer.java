package massim.competition2014.monitor.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import massim.competition2014.monitor.Definitions;
import massim.competition2014.monitor.graph.AgentInfo;
import massim.competition2014.monitor.graph.EdgeInfo;
import massim.competition2014.monitor.graph.NodeInfo;
import massim.competition2014.monitor.graph.TeamInfo;
import massim.competition2014.scenario.GraphNode;

/**
 * Used to render the lines from an agent to the target of its last action
 * @author Tobias
 *
 */
public class ActionTargetRenderer extends Renderer {
	
	private Polygon arrowHead;
	
	private Map<String, Point> nodeMap;
	
	private AffineTransform tx = new AffineTransform();
	
	public ActionTargetRenderer(){
		
		arrowHead = new Polygon();  
		arrowHead.addPoint( 0, 12);
		arrowHead.addPoint( -12, -12);
		arrowHead.addPoint( 12, -12);
	}
	

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges,
			Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo,
			String selectedAgent, Graphics2D g2d) {
		
		if(nodeMap == null){
			initializeMap(nodes);
		}
		
		//determine every agent's current positions in case no other renderer has done it yet
		//positions correspond to that of the PolygonAgentsRenderer-class!
		for(NodeInfo node: nodes){
			Vector<AgentInfo> sortedAgents = new Vector<AgentInfo>(node.agents);
			Collections.sort(sortedAgents, new Comparator<AgentInfo>() {
				@Override
				public int compare(AgentInfo o1, AgentInfo o2) {
					if(o1.name.hashCode() > o2.name.hashCode()){
						return 1;
					}
					else if(o1.name.hashCode() < o2.name.hashCode()){
						return -1;
					}
					else{
						return 0;
					}
				}
			});
			PolygonAgentsRenderer.determineAgentPositions(sortedAgents);
		}
		
		for(AgentInfo ag: agents){
			
			String action = ag.lastAction;
			
			if(action.equalsIgnoreCase("attack")){
				String target = ag.lastActionParam;
				
				for(AgentInfo ag2: agents){
					if(ag2.name.equalsIgnoreCase(target)){
						
						if(ag.node != ag2.node){
							//draw connection
							drawConnection(ag.x, ag.y, ag2.x, ag2.y, ag, ag.lastAction, ag.lastActionResult, g2d);
						}
						
						break;
					}
				}
			}
			else if(action.equalsIgnoreCase("inspect")){
				String target = ag.lastActionParam;
				
				for(AgentInfo ag2: agents){
					if(ag2.name.equalsIgnoreCase(target)){
						
						if(ag.node != ag2.node){
							//draw connection
							drawConnection(ag.x, ag.y, ag2.x, ag2.y, ag, ag.lastAction, ag.lastActionResult, g2d);
						}
						
						break;
					}
				}
			}
			else if(action.equalsIgnoreCase("repair")){
				String target = ag.lastActionParam;
				
				for(AgentInfo ag2: agents){
					if(ag2.name.equalsIgnoreCase(target)){
						
						if(ag.node != ag2.node){
							//draw connection
							drawConnection(ag.x, ag.y, ag2.x, ag2.y, ag, ag.lastAction, ag.lastActionResult, g2d);
						}
						
						break;
					}
				}
			}
			else if(action.equalsIgnoreCase("probe")){
				String target = ag.lastActionParam;
				
				Point p = nodeMap.get(target);
				
				if(p != null){
					
					if(! (p.x == ag.node.x && p.y == ag.node.y) ){
						//draw connection to target node
						drawConnection(ag.x, ag.y, p.x, p.y, ag, ag.lastAction, ag.lastActionResult, g2d);
					}
				}
			}
		}

	}
	
	/**
	 * Draws a connection from source to target, style depending on the action
	 */
	private void drawConnection(int x, int y, int x2, int y2, AgentInfo ag,
			String lastAction, String lastActionResult, Graphics2D g2d) {
		
		//draw dashed line in team-color
		Color agentColor = null;
		agentColor = Definitions.agentColors[ag.team];
		g2d.setColor(agentColor);
		float[] dashPattern = { 30, 30, 30, 30 };
	    g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT,
	                                  BasicStroke.JOIN_MITER, 10,
	                                  dashPattern, 0));
		g2d.drawLine(x, y, x2, y2);
		
		//draw dashed line according to action
		if(lastAction.equalsIgnoreCase("attack")){
			g2d.setColor(Color.ORANGE);
		}
		else if(lastAction.equalsIgnoreCase("inspect")){
			g2d.setColor(Color.MAGENTA);
		}
		else if(lastAction.equalsIgnoreCase("probe")){
			g2d.setColor(Color.CYAN);
		}
		else if(lastAction.equalsIgnoreCase("repair")){
			g2d.setColor(Color.PINK);
		}
	    g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT,
	                                  BasicStroke.JOIN_MITER, 10,
	                                  dashPattern, 30));
		g2d.drawLine(x, y, x2, y2);
		
		//draw an arrowhead
		Line2D.Double line = new Line2D.Double(x, y, x2, y2);
		
		int midX = x2 - (x2-x)/2;
		int midY = y2 - (y2-y)/2;
		
		tx.setToIdentity();
	    double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
	    tx.translate(midX, midY);
	    tx.rotate((angle-Math.PI/2d));  

	    Shape ah = tx.createTransformedShape(arrowHead);
	    g2d.fill(ah);
	}

	private void initializeMap(Vector<NodeInfo> nodes) {
		nodeMap = new HashMap<String, Point>();
		for(NodeInfo n: nodes){
			nodeMap.put(n.name, new Point(n.x, n.y));
		}
	}

}

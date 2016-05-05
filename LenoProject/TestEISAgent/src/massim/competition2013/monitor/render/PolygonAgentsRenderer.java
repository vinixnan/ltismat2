package massim.competition2013.monitor.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;

import massim.competition2013.monitor.Definitions;
import massim.competition2013.monitor.graph.AgentInfo;
import massim.competition2013.monitor.graph.EdgeInfo;
import massim.competition2013.monitor.graph.NodeInfo;
import massim.competition2013.monitor.graph.TeamInfo;

public class PolygonAgentsRenderer extends Renderer {

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges,
			Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo, String selectedAgent, Graphics2D g2d) {

		// get the relevant nodes, i.e. nodes with agents
		HashSet<NodeInfo> relevantNodes = new HashSet<NodeInfo>();
		for ( AgentInfo agent : agents ) {
			relevantNodes.add(agent.node);
		}
		
		//sort nodes to prevent possible flickering of agents
		Vector<NodeInfo> sortedNodes = new Vector<NodeInfo>(relevantNodes);
		Collections.sort(sortedNodes, new Comparator<NodeInfo>() {
			@Override
			public int compare(NodeInfo o1, NodeInfo o2) {
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
		
		AgentInfo selectedAg = null; //store the selected agent to render it later
		
		// go through all nodes and render their agents
		// agents are put on circles around the node
		for ( NodeInfo node : sortedNodes ) {
			
			//sort agents to prevent 'flickering'
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
			
			for ( AgentInfo ag : sortedAgents ) {
				
				// render the agent
				boolean selected = ag.name.equals(selectedAgent);
				
				if(selected){
					//selected agent will be rendered later, so that it is on top of other agents
					selectedAg = ag;
				}
				else{
					renderAgent(ag,g2d, selected);
				}				
			
			}  // all agents drawn
		} // all relevant nodes handled
		//re-render the selected agent, so that it comes out on top of the others
		if(selectedAg != null){
			renderAgent(selectedAg, g2d, true);
		}
		
	}

	/**
	 * Renders an individual agent.
	 * 
	 * @param ag
	 * @param g2d
	 * @param selected
	 */
	private void renderAgent(AgentInfo ag, Graphics2D g2d, boolean selected) {
		
		// rendering the agent body
		Color agentColor = null;
		agentColor = Definitions.agentColors[ag.team];
		
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(agentColor);
		
		int boxWidth = 54;
		
		//draw agent-polygon
		g2d.setColor(agentColor);
		if(ag.role.equalsIgnoreCase("explorer")){
			//circle
			g2d.fillOval(ag.x-boxWidth/3, ag.y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2 );
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			
			g2d.drawOval(ag.x-boxWidth/3, ag.y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2 );
			
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));

		}
		else if(ag.role.equalsIgnoreCase("repairer")){
			//octagon
			Polygon o = new Polygon();
			o.addPoint(ag.x - boxWidth/6, ag.y - boxWidth/6);
			o.addPoint(ag.x + boxWidth/6, ag.y - boxWidth/6);
			o.addPoint(ag.x + boxWidth/3, ag.y);
			o.addPoint(ag.x + boxWidth/3, ag.y + boxWidth/3);
			o.addPoint(ag.x + boxWidth/6, ag.y + boxWidth/2);
			o.addPoint(ag.x - boxWidth/6, ag.y + boxWidth/2);
			o.addPoint(ag.x - boxWidth/3, ag.y + boxWidth/3);
			o.addPoint(ag.x - boxWidth/3, ag.y);
			g2d.fillPolygon(o);
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			g2d.drawPolygon(o);
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));
		}
		else if(ag.role.equalsIgnoreCase("saboteur")){
			//diamond
			Polygon o = new Polygon();
			o.addPoint(ag.x, ag.y - boxWidth/6);
			o.addPoint(ag.x + boxWidth/3, ag.y + boxWidth/6);
			o.addPoint(ag.x, ag.y + boxWidth/2);
			o.addPoint(ag.x - boxWidth/3, ag.y + boxWidth/6);
			g2d.fillPolygon(o);
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			g2d.drawPolygon(o);
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));

		}
		else if(ag.role.equalsIgnoreCase("sentinel")){
			//square
			g2d.fillRect(ag.x-boxWidth/3, ag.y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2);
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			g2d.drawRect(ag.x-boxWidth/3, ag.y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2);
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));
		}
		else if(ag.role.equalsIgnoreCase("inspector")){
			//downwards triangle
			Polygon o = new Polygon();
			o.addPoint(ag.x - boxWidth/2, ag.y - boxWidth/6);
			o.addPoint(ag.x + boxWidth/2, ag.y - boxWidth/6);
			o.addPoint(ag.x, ag.y + boxWidth/2);
			g2d.fillPolygon(o);
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			g2d.drawPolygon(o);
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));
		}
		
		//draw red X if agent is disabled
		if ( ag.health == 0 ) {
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(Color.RED.darker());
			g2d.drawLine(ag.x - boxWidth/3, ag.y - boxWidth/6, ag.x + boxWidth/3, ag.y + boxWidth/2);
			g2d.drawLine(ag.x + boxWidth/3, ag.y - boxWidth/6, ag.x - boxWidth/3, ag.y + boxWidth/2);
		}
		g2d.setStroke(new BasicStroke(1));
		
		//draw status-box
		g2d.setColor(agentColor);
		if(selected){
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(Color.WHITE);
		}
		g2d.drawRect(ag.x-boxWidth/2, ag.y-boxWidth/2, boxWidth, boxWidth/3);
		g2d.setColor(agentColor);
		g2d.setStroke(new BasicStroke(1));
		
		String result = ag.lastActionResult;
		if(result.equalsIgnoreCase("successful")){
			g2d.setColor(Color.green.darker().darker());
		}
		else if(result.startsWith("failed")){
			if(result.equalsIgnoreCase("failed_in_range")){
				g2d.setColor(Color.YELLOW.darker());
			}
			else{
				g2d.setColor(Color.RED.darker());
			}
		}
		g2d.fillRect(ag.x-boxWidth/2+1, ag.y-boxWidth/2+1, boxWidth-2, boxWidth/3-2);
		
		//draw agent-id
		Font font = new Font("Arial", Font.PLAIN, 14);
		Font oldFont = g2d.getFont();
		g2d.setFont(font);
		g2d.setColor(Color.WHITE.brighter());
		String agID = parseID(ag.name);
		
		int idWidth = g2d.getFontMetrics().stringWidth(agID);
		int idHeight = g2d.getFontMetrics().getHeight();
		
		int idX = ag.x - idWidth/2;
		int idY = ag.y + (boxWidth/6) + (idHeight/2);
		
		g2d.drawString(agID, idX, idY);
		g2d.setFont(oldFont);
		
		//draw the last action and its result
		String action = ag.lastAction;
		
		if(action.equalsIgnoreCase("attack")){
			drawSword(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("parry")){
			drawShield(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("inspect")){
			drawMagnifyingGlass(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("survey")){
			drawGlasses(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);		
		}
		else if(action.equalsIgnoreCase("probe")){
			drawDrill(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("recharge")){
			drawLightning(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("skip")){
			// nothing to do
		}
		else if(action.equalsIgnoreCase("repair")){
			drawWrench(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("goto")){
			drawCompass(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("buy")){
			String param = ag.lastActionParam;
			
			if(param != null && !param.equals("")){
				g2d.drawChars("+1".toCharArray(), 0, 2, ag.x - boxWidth/2, ag.y - boxWidth/6);
			}
			
			if(param.equalsIgnoreCase("sensor")){
				drawGlasses(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);	
			}
			else if(param.equalsIgnoreCase("battery")){
				drawLightning(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
			}
			else if(param.equalsIgnoreCase("shield")){
				drawHeart(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
			}
			else if(param.equalsIgnoreCase("sabotageDevice")){
				drawSword(ag.x - boxWidth/6, ag.y - boxWidth/2, boxWidth, g2d);
			}
		}
	}
	
	private void drawCompass(int agx, int agy, int boxWidth, Graphics2D g2d) {
		g2d.fillOval(agx + (boxWidth/18)*5, agy, boxWidth/9, boxWidth/9);
		g2d.fillOval(agx + (boxWidth/18)*5, agy + (boxWidth/9)*2, boxWidth/9, boxWidth/9);
		g2d.fillOval(agx + boxWidth/9, agy + boxWidth/9, boxWidth/9, boxWidth/9);
		g2d.fillOval(agx + (boxWidth/9)*4, agy + boxWidth/9, boxWidth/9, boxWidth/9);
		
		g2d.drawLine(agx + (boxWidth/9)*2, agy + boxWidth/6, agx + (boxWidth/9)*4, agy + boxWidth/6);
		g2d.drawLine(agx + (boxWidth/3), agy + boxWidth/9, agx + (boxWidth/3), agy + (boxWidth/9)*2);
	}
	
	private void drawHeart(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx + (boxWidth/9)*2, agy);
		p.addPoint(agx + (boxWidth/3), agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/9)*4, agy);
		p.addPoint(agx + (boxWidth/9)*5, agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/9)*5, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/3), agy + (boxWidth/3));
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/9));
		g2d.fillPolygon(p);
	}

	private void drawWrench(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx + (boxWidth/9), agy);
		p.addPoint(agx + (boxWidth/3), agy);
		p.addPoint(agx + (boxWidth/3), agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/3)*2, agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/3)*2, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/3), agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/3), agy + (boxWidth/3));
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/3));
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/9)*2, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/9)*2, agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/9));
		g2d.fillPolygon(p);
	}

	private void drawLightning(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx + boxWidth/3, agy);
		p.addPoint(agx + boxWidth/3, agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/9)*4, agy + (boxWidth/9));
		p.addPoint(agx + (boxWidth/9)*2, agy + (boxWidth/3));
		p.addPoint(agx + (boxWidth/9)*2, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/9), agy + (boxWidth/9)*2);
		g2d.fillPolygon(p);
	}

	private void drawDrill(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx + boxWidth/6, agy);
		p.addPoint(agx + boxWidth/2, agy);
		p.addPoint(agx + boxWidth/3, agy + boxWidth/3);
		g2d.fillPolygon(p);
	}

	private void drawGlasses(int agx, int agy, int boxWidth, Graphics2D g2d) {
		
		g2d.drawOval(agx, agy + (boxWidth/9), (boxWidth/9)*2, (boxWidth/9)*2);
		g2d.drawOval(agx + boxWidth/3, agy + boxWidth/9, (boxWidth/9)*2, (boxWidth/9)*2);
		g2d.drawLine(agx + (boxWidth/9)*2, agy + (boxWidth/9)*2, agx + (boxWidth/3), agy + (boxWidth/9)*2);
		
		g2d.drawLine(agx, agy + (boxWidth/9)*2, agx + (boxWidth/9)*2, agy);
		g2d.drawLine(agx + (boxWidth/9)*5, agy + (boxWidth/9)*2, agx + (boxWidth/3)*2, agy);
	}

	private void drawMagnifyingGlass(int agx, int agy, int boxWidth, Graphics2D g2d) {
		
		Stroke s = g2d.getStroke();
		
		g2d.setStroke(new BasicStroke(3));
		
		g2d.drawLine(agx + (boxWidth/9)*2, agy + (boxWidth/9), agx + (boxWidth/9)*4, agy + (boxWidth/9)*2);
		g2d.fillOval(agx + boxWidth/9, agy, (boxWidth/9)*2, (boxWidth/9)*2);
		
		g2d.setStroke(s);
	}

	private void drawShield(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx + boxWidth/6, agy);
		p.addPoint(agx + boxWidth/2, agy);
		p.addPoint(agx + boxWidth/2, agy + (boxWidth/9)*2);
		p.addPoint(agx + boxWidth/3, agy + boxWidth/3);
		p.addPoint(agx + boxWidth/6, agy + (boxWidth/9)*2);
		g2d.fillPolygon(p);
	}

	/**
	 * Draws a sword. As in draw with a pen.
	 */
	private void drawSword(int agx, int agy, int boxWidth, Graphics2D g2d) {
		Polygon p = new Polygon();
		p.addPoint(agx, agy);
		p.addPoint(agx + boxWidth/9, agy);
		p.addPoint(agx + (boxWidth/9)*4, agy + boxWidth/6);
		p.addPoint(agx + (boxWidth/9)*5, agy);
		p.addPoint(agx + (boxWidth/9)*5, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/3)*2, agy + (boxWidth/9)*2);
		p.addPoint(agx + (boxWidth/9)*5, agy + boxWidth/3);
		p.addPoint(agx + boxWidth/2, agy + (boxWidth/18)*5);
		p.addPoint(agx + (boxWidth/9)*4, agy + boxWidth/3);
		p.addPoint(agx + (boxWidth/9)*2, agy + boxWidth/3);
		p.addPoint(agx + (boxWidth/9)*4, agy + (boxWidth/9)*2);
		p.addPoint(agx, agy + boxWidth/9);
		g2d.fillPolygon(p);
	}

	/**
	 * It is assumed, that the 1 to 2 last characters of the agent's name represent its ID (and are a number)
	 */
	private String parseID(String name) {
		
		try{
			Integer.parseInt(name.substring(name.length()-2));
			return name.substring(name.length()-2);
		} catch(NumberFormatException n){
			return name.substring(name.length()-1);
		}
	}
	
	static void determineAgentPositions(Vector<AgentInfo> sortedAgents){
		float offset = Definitions.nodeRadius + 26 + 4.0f;
		float angle = 3.14159f / 16.0f;
		float agentsPerCircle = 6.0f;
		int nextCircle = (int) agentsPerCircle;
		int count = 0;	
		
		for(AgentInfo ag: sortedAgents){
			count ++;
			
			// agent position on the current circle
			ag.x = (int) (Math.sin(angle) * offset) + ag.node.x;
			ag.y = (int) (Math.cos(angle) * offset) + ag.node.y;
			
			// increase the angle
			angle += 2.0f * 3.14159f / agentsPerCircle;
			
			// go to next circle if current circle is full
			if (count == nextCircle) {
				offset += 2.0f * (float)Definitions.agentRadius +4.0f;
				agentsPerCircle = (float)((int)(offset * 3.14159f / ((float)Definitions.agentRadius + 1.0f)));
				nextCircle += (int)agentsPerCircle;
			}	
		}
	}

}

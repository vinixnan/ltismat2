package massim.competition2015.monitor.render;

import massim.competition2015.monitor.Definitions;
import massim.competition2015.monitor.data.*;

import java.util.Set;
import java.util.HashSet;
import java.awt.*;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * Created by ta10 on 18.13.15.
 */
public class AgentsRenderer extends WaypointPainter<Waypoint> implements Renderer {

	
	public AgentsRenderer(){
		
		this.setRenderer(new WaypointRenderer<Waypoint>() {
			@Override
		    public void paintWaypoint(Graphics2D g2d, JXMapViewer map, Waypoint wp) {
				
				
				
				// g2d.scale(2, 2);
				
				if (!(wp instanceof AgentWaypoint)){
					return;
				}
				
				AgentInfo ag = ((AgentWaypoint)wp).getAgentInfo();
				boolean selected = ((AgentWaypoint)wp).selected;
				Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
				int x = (int)point.getX();
				int y = (int)point.getY();
				
				Color agentColor = Definitions.agentColors[ag.team];
				
				Color bgColor; 
		        if (selected){
	        		bgColor = Color.WHITE;
		        }  else {
		        	bgColor = Color.BLACK;
		        }
		        if(ag.role.equalsIgnoreCase("Car")){
		        	//if (selected){
		        		g2d.setColor(bgColor);
		        		drawCar(g2d, x, y, 8, 12);
		        	//}
				    g2d.setColor(agentColor);
		        	drawCar(g2d, x, y, 4, 8);
		        	
		        } else if(ag.role.equalsIgnoreCase("Drone")){
		        	//if (selected){
		        		g2d.setColor(bgColor);
		        		drawDrone(g2d, x, y, 8,2);
				        
		        	//}
		        	g2d.setColor(agentColor);
		        	drawDrone(g2d, x, y, 4,1);
		        	
		        } else if(ag.role.equalsIgnoreCase("Truck")){
		        	//if (selected){
		        		g2d.setColor(bgColor);
		        		drawTruck(g2d, x, y, 8, 12);
		        	//}
				    g2d.setColor(agentColor);
		        	drawTruck(g2d, x, y, 4, 8);
		        	
		        	
		        } else if(ag.role.equalsIgnoreCase("Motorcycle")){
		        	//if (selected){
		        		g2d.setColor(bgColor);	
		        		drawMotorcycle(g2d, x, y, 8, 12);
		        	//}
		        	g2d.setColor(agentColor);
		        	drawMotorcycle(g2d, x, y, 4, 8);
		        	
		        } else {
		        	
				}
		    }
		});
		
	}
	
	
	
	
	
	private void drawCar(Graphics2D g2d, int x, int y, int width1, int width2){
		g2d.setStroke(new BasicStroke(width2, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND));
    	g2d.drawLine(x-16,y,x+16,y);
    	g2d.setStroke(new BasicStroke(width1, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2d.drawLine(x-10,y,x-8,y-9);
        g2d.drawLine(x+10,y,x+8,y-9);
        g2d.drawLine(x-8,y-9,x+8,y-9);
        g2d.setStroke(new BasicStroke(width1, BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
        g2d.drawLine(x,y,x,y-9);
        g2d.drawOval(x-13, y+1, 5, 5);
        g2d.drawOval(x+8, y+1, 5, 5);
	}
	
	private void drawDrone(Graphics2D g2d, int x, int y, int width1, int width2){
		/*
		g2d.setStroke(new BasicStroke(width1));
        g2d.drawLine(x-10,y-10,x+10,y+10);
        g2d.drawLine(x-10,y+10,x+10,y-10);
        g2d.setStroke(new BasicStroke(width2));
       /*/ 
		g2d.setStroke(new BasicStroke(width1));
        g2d.drawLine(x-9,y-9,x+9,y+9);
        g2d.drawLine(x-9,y+9,x+9,y-9);
        g2d.setStroke(new BasicStroke(width2));
        g2d.drawOval(x-17, y-17, 16, 16);
        g2d.drawOval(x-17, y+1, 16, 16);
        g2d.drawOval(x+1, y-17, 16, 16);
        g2d.drawOval(x+1, y+1, 16, 16);
        //*/
	}
	
	private void drawTruck(Graphics2D g2d, int x, int y, int width1, int width2){
		g2d.setStroke(new BasicStroke(width2, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND));
    	g2d.drawLine(x-16,y,x+16,y);
    	g2d.setStroke(new BasicStroke(width1, BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
        g2d.drawLine(x-16,y,x-14,y-9);
        // g2d.drawLine(x+10,y,x+8,y-9);
        g2d.drawLine(x-14,y-9,x-6,y-9);
        g2d.drawLine(x-6,y,x-6,y-9);
        g2d.drawOval(x-13, y+1, 5, 5);
        //g2d.drawOval(x+4, y+1, 5, 5);
        g2d.drawOval(x+10, y+1, 5, 5);
        
        g2d.drawRect(x, y-10, 16, 5);
        g2d.fillRect(x, y-10, 16, 5);
        
	}
	
	private void drawMotorcycle(Graphics2D g2d, int x, int y, int width1, int width2){
    	
		g2d.setStroke(new BasicStroke(width1, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND));
        g2d.drawLine(x-13,y+4,x-2,y-11);
    	
    	g2d.drawLine(x-4,y-4,x+14,y-4);
    	
    	g2d.setStroke(new BasicStroke(width1, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
    	g2d.drawLine(x-2,y+2,x+13,y+2);
    	g2d.drawLine(x-2,y+1,x-6,y-4);
    	
    	g2d.drawLine(x+10,y+2,x+5,y-3);
    	
        g2d.drawOval(x-17, y+1, 7, 7);
        g2d.fillOval(x-17, y+1, 7, 7);
        g2d.drawOval(x+10, y+1, 7, 7);
        g2d.fillOval(x+10, y+1, 7, 7);
	}	
	
	
	@Override
	public void setData(WorldInfo world, String selectedAgent, String selectedFacility) {
		
		Set<Waypoint> waypoints = new HashSet<>();
		
		for ( AgentInfo ag : world.agents) {
			waypoints.add(new AgentWaypoint(ag, ag.name.equals(selectedAgent)));
		}
		this.setWaypoints(waypoints);
	}

	
	private static class AgentWaypoint extends DefaultWaypoint {
		
		private AgentInfo agentInfo;
		public boolean selected = false;
		
		public AgentInfo getAgentInfo(){
			return agentInfo;
		}
		
		public AgentWaypoint(AgentInfo agent, boolean selected){
			super(agent.lat, agent.lon);
			this.agentInfo = agent;
			this.selected = selected;
		}
	}
	
	
	
	
    // public final int nodeRadius = 1;

    /*
    @Override
    public void render(
            Vector<NodeInfo> nodes,
            Vector<EdgeInfo> edges,
            Vector<AgentInfo> agents,
            Vector<TeamInfo> teamsInfo,
            Vector<JobInfo> jobs,
            Vector<FacilityInfo> facilities,
            String selectedAgent,
            Graphics2D g2d) {

        // draw all agents
        for ( AgentInfo ag : agents) {

        	int x = lonToX(ag.lon);
        	int y = latToY(ag.lat);

        	renderAgent(ag, x, y, g2d, false);

        } // draw all agents
    }
    */
    
    
	/* *
	 * Renders an individual agent.
	 * 
	 * @param ag
	 * @param g2d
	 * @param selected
	 *//*
	private void renderAgent(AgentInfo ag, int x, int y, Graphics2D g2d, boolean selected) {
		
		// rendering the agent body
		Color agentColor = null;
		agentColor = Definitions.agentColors[ag.team];
		
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(agentColor);
		
		int boxWidth = 54;
		
		//draw agent-polygon
		g2d.setColor(agentColor);
		if(ag.role.equalsIgnoreCase("Car")){
			//circle
			g2d.fillOval(x-boxWidth/3, y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2 );
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			
			g2d.drawOval(x-boxWidth/3, y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2 );
			
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));

		}
		else if(ag.role.equalsIgnoreCase("Drone")){
			//octagon
			Polygon o = new Polygon();
			o.addPoint(x - boxWidth/6, y - boxWidth/6);
			o.addPoint(x + boxWidth/6, y - boxWidth/6);
			o.addPoint(x + boxWidth/3, y);
			o.addPoint(x + boxWidth/3, y + boxWidth/3);
			o.addPoint(x + boxWidth/6, y + boxWidth/2);
			o.addPoint(x - boxWidth/6, y + boxWidth/2);
			o.addPoint(x - boxWidth/3, y + boxWidth/3);
			o.addPoint(x - boxWidth/3, y);
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
		else if(ag.role.equalsIgnoreCase("Truck")){
			//diamond
			Polygon o = new Polygon();
			o.addPoint(x, y - boxWidth/6);
			o.addPoint(x + boxWidth/3, y + boxWidth/6);
			o.addPoint(x, y + boxWidth/2);
			o.addPoint(x - boxWidth/3, y + boxWidth/6);
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
		else if(ag.role.equalsIgnoreCase("Motorcicle")){
			//square
			g2d.fillRect(x-boxWidth/3, y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2);
			
			g2d.setStroke(new BasicStroke(2));
			if(selected){
				g2d.setColor(Color.WHITE);
			}
			else{
				g2d.setColor(Color.BLACK);
			}
			g2d.drawRect(x-boxWidth/3, y-boxWidth/6, (boxWidth/3)*2, (boxWidth/3)*2);
			g2d.setColor(agentColor);
			g2d.setStroke(new BasicStroke(1));
		}
		else //if(ag.role.equalsIgnoreCase("inspector"))
		{
			//downwards triangle
			Polygon o = new Polygon();
			o.addPoint(x - boxWidth/2, y - boxWidth/6);
			o.addPoint(x + boxWidth/2, y - boxWidth/6);
			o.addPoint(x, y + boxWidth/2);
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
		if ( ag.charge == 0 ) {
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(Color.RED.darker());
			g2d.drawLine(x - boxWidth/3, y - boxWidth/6, x + boxWidth/3, y + boxWidth/2);
			g2d.drawLine(x + boxWidth/3, y - boxWidth/6, x - boxWidth/3, y + boxWidth/2);
		}
		g2d.setStroke(new BasicStroke(1));
		
		//draw status-box
		g2d.setColor(agentColor);
		if(selected){
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(Color.WHITE);
		}
		g2d.drawRect(x-boxWidth/2, y-boxWidth/2, boxWidth, boxWidth/3);
		g2d.setColor(agentColor);
		g2d.setStroke(new BasicStroke(1));
		
		String result = ag.lastActionResult;
		if(result.equalsIgnoreCase("successful")){
			g2d.setColor(Color.green.darker().darker());
		}
		else if(result.startsWith("failed")){
			g2d.setColor(Color.RED.darker());
		}
		g2d.fillRect(x-boxWidth/2+1, y-boxWidth/2+1, boxWidth-2, boxWidth/3-2);
		
		//draw agent-id
		Font font = new Font("Arial", Font.PLAIN, 14);
		Font oldFont = g2d.getFont();
		g2d.setFont(font);
		g2d.setColor(Color.WHITE.brighter());
		String agID = parseID(ag.name);
		
		int idWidth = g2d.getFontMetrics().stringWidth(agID);
		int idHeight = g2d.getFontMetrics().getHeight();
		
		int idX = x - idWidth/2;
		int idY = y + (boxWidth/6) + (idHeight/2);
		
		g2d.drawString(agID, idX, idY);
		g2d.setFont(oldFont);
		
		//draw the last action and its result
		/*
		String action = ag.lastAction;
		if(action.equalsIgnoreCase("attack")){
			drawSword(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("parry")){
			drawShield(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("inspect")){
			drawMagnifyingGlass(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("survey")){
			drawGlasses(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);		
		}
		else if(action.equalsIgnoreCase("probe")){
			drawDrill(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("recharge")){
			drawLightning(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("skip")){
			// nothing to do
		}
		else if(action.equalsIgnoreCase("repair")){
			drawWrench(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("goto")){
			drawCompass(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
		}
		else if(action.equalsIgnoreCase("buy")){
			String param = ag.lastActionParam;
			
			if(param != null && !param.equals("")){
				g2d.drawChars("+1".toCharArray(), 0, 2, x - boxWidth/2, y - boxWidth/6);
			}
			
			if(param.equalsIgnoreCase("sensor")){
				drawGlasses(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);	
			}
			else if(param.equalsIgnoreCase("battery")){
				drawLightning(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
			}
			else if(param.equalsIgnoreCase("shield")){
				drawHeart(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
			}
			else if(param.equalsIgnoreCase("sabotageDevice")){
				drawSword(x - boxWidth/6, y - boxWidth/2, boxWidth, g2d);
			}
		}
		*//*
	}
    */
	
	/*
	 * It is assumed, that the 1 to 2 last characters of the agent's name represent its ID (and are a number)
	 *//*
	private String parseID(String name) {
		
		try{
			Integer.parseInt(name.substring(name.length()-2));
			return name.substring(name.length()-2);
		} catch(NumberFormatException n){
			return name.substring(name.length()-1);
		}
	}
    */
    
}

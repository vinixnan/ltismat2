package massim.competition2014.monitor.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;

import massim.competition2014.monitor.Definitions;
import massim.competition2014.monitor.graph.AgentInfo;
import massim.competition2014.monitor.graph.EdgeInfo;
import massim.competition2014.monitor.graph.NodeInfo;
import massim.competition2014.monitor.graph.TeamInfo;

/**
 * Renders a set of edges in the traditional manner.
 * 
 * @author tristanbehrens
 *
 */
class EdgesRenderer extends Renderer {

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo,
			String selectedAgent, Graphics2D g2d) {

		// draw all edges
		for ( EdgeInfo e : edges ) {
			
			// draw a colored line if dominated
			int domTeam = -1;
			if (e.node1.dominatorTeam != -1 && e.node1.dominatorTeam == e.node2.dominatorTeam) {
				domTeam = e.node1.dominatorTeam;
				
				g2d.setColor(new Color(000,000,000)); // TODO externalize color
				g2d.setStroke(new BasicStroke(4));
				g2d.drawLine(e.node1.x, e.node1.y,
							 e.node2.x, e.node2.y);
			}			
			if( domTeam < -1 || domTeam >= Definitions.teamDomColors.length) {
				throw new AssertionError("Implement (not enough colors defined)");
			}
			
			// draw a white line
			g2d.setColor(domTeam == -1? new Color(200,200,200): Definitions.teamDomColors[domTeam]);  // TODO externalize color
			g2d.setStroke(new BasicStroke(2));
			g2d.drawLine(e.node1.x, e.node1.y,
						 e.node2.x, e.node2.y);

			// draw the weight
			int x = (int) (( e.node1.x + e.node2.x) / 2.0f);
			int y = (int) (( e.node1.y + e.node2.y) / 2.0f);
			g2d.setStroke(new BasicStroke(1));
			g2d.fillOval(x-7, y-7, 13, 13);
			g2d.setColor(new Color(0,0,0));      // TODO externalize color
			String str = "" + e.weight;
			g2d.drawChars(str.toCharArray(), 0, str.length(), x-4, y+4);
			
		} // all edges drawn
		
					
	
	}
	
	
}
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
 * Renders the topology of a map without numbers.
 * The thickness of the edges represent their weights.
 * The size of the nodes represent their values.
 * Shows when a node has been probed.
 * 
 * @author federico
 *
 */
class NoNumbersProbedNodesTopologyRenderer extends Renderer {

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents,
			Vector<TeamInfo> teamsInfo, String selectedAgent, Graphics2D g2d) {
		
		g2d.setColor(Definitions.edgesColor);
		for ( EdgeInfo edge: edges ) {
			int width = 2 + 1 * edge.weight;
			g2d.setStroke(new BasicStroke(width,
		            BasicStroke.CAP_ROUND,
		            BasicStroke.JOIN_ROUND));
			g2d.drawLine(edge.node1.x, edge.node1.y, edge.node2.x, edge.node2.y);
			
		}

		g2d.setColor(Definitions.nodesColor);
		for ( NodeInfo node : nodes ) {
			
			int width = 10 + 6 * node.weight;
			g2d.setStroke(new BasicStroke(width,
		            BasicStroke.CAP_ROUND,
		            BasicStroke.JOIN_ROUND));
			g2d.drawLine(node.x, node.y, node.x, node.y);
			for ( TeamInfo team:teamsInfo){
				if (team.provedNodes.contains(node.name)){
					Color teamColor =Definitions.teamDomColors[team.number];
					// Set new color with alpha channel.
					Color nodeColor = g2d.getColor();
					g2d.setColor(new Color(teamColor.getRed(),
							               teamColor.getGreen(),
							               teamColor.getBlue(),
							               170
										  )
					);
					g2d.fillArc(node.x-width/3, node.y-width/3, 2*width/3, 2*width/3, team.number*180+60, 180);
					g2d.setColor(nodeColor); //reset color
				}
			}
		}
	}
}
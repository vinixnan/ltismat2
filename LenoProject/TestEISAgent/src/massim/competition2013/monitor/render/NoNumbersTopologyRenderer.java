package massim.competition2013.monitor.render;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Vector;

import massim.competition2013.monitor.Definitions;
import massim.competition2013.monitor.graph.AgentInfo;
import massim.competition2013.monitor.graph.EdgeInfo;
import massim.competition2013.monitor.graph.NodeInfo;
import massim.competition2013.monitor.graph.TeamInfo;

/**
 * Renders the topology of a map without numbers.
 * The thickness of the edges represent their weights.
 * The size of the nodes represent their values.
 * 
 * @author tristanbehrens
 *
 */
class NoNumbersTopologyRenderer extends Renderer {

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
			
			int width = 4 + 6 * node.weight;
			g2d.setStroke(new BasicStroke(width,
		            BasicStroke.CAP_ROUND,
		            BasicStroke.JOIN_ROUND));
			g2d.drawLine(node.x, node.y, node.x, node.y);
		
		}

	}

}
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
import massim.competition2014.monitor.graph.Util;

/**
 * Renders a set of nodes in the traditionam manner.
 * 
 * @author tristanbehrens
 *
 */
class NodesRenderer extends Renderer {

	public String selectedNode = "";
	
	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo,
			String selectedAgent, Graphics2D g2d) {
		
		// draw all nodes
		for ( NodeInfo n : nodes) {
								
			// special render for selected node
			// TODO: implement parameter selectedNode.
			if ( n.name.equals(selectedNode) ) {
				
				g2d.setColor(new Color(255,255,255));   // TODO externalize color
				g2d.setStroke(new BasicStroke(6));
				g2d.drawOval(n.x - Definitions.nodeRadius, n.y - Definitions.nodeRadius, 2*Definitions.nodeRadius, 2*Definitions.nodeRadius);

			}
			
			int domTeam = n.dominatorTeam;
			if( domTeam < -1 || domTeam >= Definitions.teamDomColors.length) {
				throw new AssertionError("Implement (not enough colors defined)");
			}
			g2d.setColor(new Color(0,0,0));   // TODO externalize color
			g2d.setStroke(new BasicStroke(1));
			g2d.fillOval(n.x - (Definitions.nodeRadius + 0 ), n.y - (Definitions.nodeRadius + 0 ), 2*(Definitions.nodeRadius + 0 ), 2*(Definitions.nodeRadius + 0 ));
			
			g2d.setColor(domTeam == -1? new Color(155,155,155): Definitions.teamDomColors[domTeam]); // TODO externalize color
			g2d.setStroke(new BasicStroke(2));
			g2d.drawOval(n.x - Definitions.nodeRadius, n.y - Definitions.nodeRadius, 2*Definitions.nodeRadius, 2*Definitions.nodeRadius);
			
			g2d.setColor(new Color(0,0,0));   // TODO externalize color
			g2d.setStroke(new BasicStroke(1));
			g2d.drawOval(n.x - (Definitions.nodeRadius + 1), n.y - (Definitions.nodeRadius + 1), 2*(Definitions.nodeRadius + 1), 2*(Definitions.nodeRadius + 1));
						
			TeamInfo team = null;
			if (selectedNode != null){
				NodeInfo node = Util.searchNode(selectedNode, nodes);
				if (node != null){
					team = Util.searchTeam(node.dominatorTeamName, teamsInfo);
				}
			} else if (selectedAgent != null){
				AgentInfo agent = Util.searchAgent(selectedAgent, nodes);
				if (agent != null){
					team = Util.searchTeam(agent.teamName, teamsInfo);
				}
			}
			
			if (team != null && team.provedNodes != null && team.provedNodes.contains(n.name)){
				g2d.setColor(new Color(128,255,255));  // TODO externalize color
			} else {
				g2d.setColor(new Color(255,255,255));   // TODO externalize color
			}
			String str = "" + n.weight;
			g2d.drawChars(str.toCharArray(), 0, str.length(), n.x-4, n.y+4);
			
			// draw all agents
			float offset = Definitions.nodeRadius + Definitions.agentRadius + 4.0f;
			float angle = 3.14159f / 16.0f;
			float agentsPerCircle = 6.0f;
			int nextCircle = (int) agentsPerCircle;
			int count = 0;			
			for ( AgentInfo ag : n.agents ) {
				
				count ++;
				
				// agent position on a circle
				ag.x = (int) (Math.sin(angle) * offset) + n.x;
				ag.y = (int) (Math.cos(angle) * offset) + n.y;
					
				// selected agent
				if ( ag.name.equals(selectedAgent) ) {
					g2d.setColor(new Color(255,255,255));   // TODO externalize color
					g2d.setStroke(new BasicStroke(6));
					g2d.drawRoundRect(ag.x - Definitions.agentRadius, ag.y - Definitions.agentRadius, 2*Definitions.agentRadius, 2*Definitions.agentRadius, 3, 3);
				}
				
				// invariant
				if( ag.team < 0 || ag.team >= Definitions.agentColors.length) {
					throw new AssertionError("Implement (not enough colors defined)");
				}

				Color agentColor = Definitions.agentColors[ag.team];
				
				g2d.setStroke(new BasicStroke(1));
				g2d.setColor(ag.health > 0? new Color(0,0,0): new Color(120,120,120));   // TODO externalize color
				g2d.fillRoundRect(ag.x - (Definitions.agentRadius+0), ag.y - (Definitions.agentRadius+0), 2*(Definitions.agentRadius+0), 2*(Definitions.agentRadius+0), 6, 6);
				
				g2d.setColor(agentColor);
				g2d.setStroke(new BasicStroke(2));
				g2d.drawRoundRect(ag.x - Definitions.agentRadius, ag.y - Definitions.agentRadius, 2*Definitions.agentRadius, 2*Definitions.agentRadius, 6, 6);
				
				g2d.setColor(new Color(0,0,0));    // TODO externalize color
				g2d.setStroke(new BasicStroke(1));
				g2d.drawRoundRect(ag.x - (Definitions.agentRadius+1), ag.y -  (Definitions.agentRadius+1), 2* (Definitions.agentRadius+1), 2* (Definitions.agentRadius+1), 6, 6);
				
				g2d.setColor(new Color(255,255,255));     // TODO externalize color

				str = ag.role.substring(0, 2).toUpperCase();
				g2d.drawChars(str.toCharArray(), 0, str.length(), ag.x-8, ag.y+5);
				
				angle += 2.0f * 3.14159f / agentsPerCircle;
				
				if (count == nextCircle) {
					offset += 2.0f * (float)Definitions.agentRadius +4.0f;
					agentsPerCircle = (float)((int)(offset * 3.14159f / ((float)Definitions.agentRadius + 1.0f)));
					nextCircle += (int)agentsPerCircle;
				}					
			
			} // all agents drawn
			
		} // all nodes drawn	
	}
	
	
}
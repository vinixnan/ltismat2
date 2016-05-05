package massim.competition2014.monitor.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Vector;

import massim.competition2014.monitor.Definitions;
import massim.competition2014.monitor.graph.AgentInfo;
import massim.competition2014.monitor.graph.EdgeInfo;
import massim.competition2014.monitor.graph.NodeInfo;
import massim.competition2014.monitor.graph.TeamInfo;

/**
 * Renders the agents without numbers or identifiers.
 * Differentiates between enabled and disabled states.
 * Renderes the last actions color- and shape-coded.
 * 
 * @author tristanbehrens
 *
 */
class NoNumbersAgentsRenderer extends Renderer {

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges,
			Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo, String selectedAgent, Graphics2D g2d) {

		// get the relevant nodes, i.e. nodes with agents
		HashSet<NodeInfo> relevantNodes = new HashSet<NodeInfo>();
		for ( AgentInfo agent : agents ) {
			relevantNodes.add(agent.node);
		}
		
		// go through all nodes and render their agents
		// agents are put on circles around the node
		for ( NodeInfo node : relevantNodes ) {
			
			// draw all agents
			float offset = Definitions.nodeRadius + Definitions.agentRadius + 4.0f;
			float angle = 3.14159f / 16.0f;
			float agentsPerCircle = 6.0f;
			int nextCircle = (int) agentsPerCircle;
			int count = 0;			
			for ( AgentInfo ag : node.agents ) {
				
				// count the agents
				count ++;
				
				// agent position on the current circle
				ag.x = (int) (Math.sin(angle) * offset) + node.x;
				ag.y = (int) (Math.cos(angle) * offset) + node.y;
				
				// render the agent
				boolean selected = ag.name.equals(selectedAgent);
				renderAgent(ag,g2d, selected);

				// increase the angle
				angle += 2.0f * 3.14159f / agentsPerCircle;
				
				// go to next circle if current circle is full
				if (count == nextCircle) {
					offset += 2.0f * (float)Definitions.agentRadius +4.0f;
					agentsPerCircle = (float)((int)(offset * 3.14159f / ((float)Definitions.agentRadius + 1.0f)));
					nextCircle += (int)agentsPerCircle;
				}					
			
			}  // all agents drawn
			
		} // all relevant nodes handled
		
	}

	/**
	 * Renders an individual agent.
	 * 
	 * @param ag
	 * @param g2d
	 * @param selected
	 */
	private void renderAgent(AgentInfo ag, Graphics2D g2d, boolean selected) {
		
		// rendering the action
		if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("attack")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(Color.YELLOW.brighter());
			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (ag.x + radius * Math.sin(angle));
				int y1 = (int) (ag.y + radius * Math.cos(angle));
				int x2 = (int) (ag.x - radius * Math.sin(angle));
				int y2 = (int) (ag.y - radius * Math.cos(angle));
				g2d.drawLine(x1, y1, x2, y2);
			}
		}
		else if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("parry")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(Color.CYAN.brighter());
			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (ag.x + radius * Math.sin(angle));
				int y1 = (int) (ag.y + radius * Math.cos(angle));
				int x2 = (int) (ag.x - radius * Math.sin(angle));
				int y2 = (int) (ag.y - radius * Math.cos(angle));
				g2d.drawLine(x1, y1, x2, y2);
			}
		}
		else if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("repair")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(Color.PINK);
			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (ag.x + radius * Math.sin(angle));
				int y1 = (int) (ag.y + radius * Math.cos(angle));
				int x2 = (int) (ag.x - radius * Math.sin(angle));
				int y2 = (int) (ag.y - radius * Math.cos(angle));
				g2d.drawLine(x1, y1, x2, y2);
			}
		}
		else {
			Color actionColor = null;
			if ( ag.lastActionResult.equals("successful") ) {
				if ( ag.lastAction.equals("attack") ) {
					actionColor = Color.YELLOW.brighter();
				}
				else if ( ag.lastAction.equals("parry") ) {
					actionColor = Color.CYAN.brighter();
				}
				else if ( ag.lastAction.equals("survey") ) {
					actionColor = Color.GREEN.brighter();
				}
				else if ( ag.lastAction.equals("probe") ) {
					actionColor = Color.GREEN.brighter();
				}
				else if ( ag.lastAction.equals("inspect") ) {
					actionColor = Color.GREEN.brighter();
				}
			}
			else if ( ag.lastActionResult.startsWith("failed") ) {
				actionColor = Color.RED;
			}
			if ( actionColor != null ) {
				g2d.setStroke(new BasicStroke(1));
				g2d.setColor(actionColor);
				int radius = Definitions.agentRadius + 8;
				g2d.fillOval(ag.x - radius, ag.y - radius, 2 * radius, 2 * radius);
				
			}
		}
		
		// rendering the agent body
		Color agentColor = null;
		agentColor = Definitions.agentColors[ag.team];
		int height = Definitions.agentRadius;
		int width = Definitions.agentRadius;
		if ( ag.health == 0 ) {
			//agentColor = Color.gray.darker().darker();
			//height /= 2;
		}

		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(agentColor);
		g2d.fillOval(ag.x - width, ag.y - height, 2 * width, 2* height);

		if (selected) {
			g2d.setStroke(new BasicStroke(1));
			g2d.setColor(Color.WHITE);
			g2d.fillOval(ag.x - width/2, ag.y - height/2, width, height);
		}
		
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(agentColor.darker());
		g2d.drawOval(ag.x - width, ag.y - height, 2 * width, 2* height);

		//agentColor = Definitions.agentColors[ag.team];
		//g2d.setStroke(new BasicStroke(4));
		//g2d.setColor(agentColor);
		//g2d.drawOval(ag.x - width, ag.y - height, 2 * width, 2* height);

		if ( ag.health == 0 ) {
			g2d.setStroke(new BasicStroke(6));
			g2d.setColor(agentColor.darker());
			g2d.drawLine(ag.x + width, ag.y + height, ag.x - width, ag.y - height);
			g2d.drawLine(ag.x - width, ag.y + height, ag.x + width, ag.y - height);
		}
	}
}
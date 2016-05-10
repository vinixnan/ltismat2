package massim.competition2014.monitor.render;

import java.awt.Graphics2D;
import java.util.Vector;

import massim.competition2014.monitor.graph.AgentInfo;
import massim.competition2014.monitor.graph.EdgeInfo;
import massim.competition2014.monitor.graph.NodeInfo;
import massim.competition2014.monitor.graph.TeamInfo;

/**
 * Renders a game graph. Has a static factory method for instantiating
 * different renderer. Hides the renderers' implementation from the
 * client.
 * 
 * @author tristanbehrens
 *
 */
public abstract class Renderer {
	
	public enum VisMode {
		MODE_2013,
		MODE_2011,
		MODE_2012
	}
	
	/**
	 * Render the relevant information.
	 * @param nodes
	 * @param edges
	 * @param agents
	 * @param teamsInfo
	 * @param selectedAgent
	 * @param g2d
	 */
	abstract public void render(Vector<NodeInfo> nodes, 
			Vector<EdgeInfo> edges, 
			Vector<AgentInfo> agents,
			Vector<TeamInfo> teamsInfo,
			String selectedAgent, 
			Graphics2D g2d);

	/**
	 * Returns a Vector containing the list of relevant renderers (in the order
	 * they should be executed) for the visualization mode given as a parameter.
	 * @param visMode
	 * @return
	 */
	public static Vector<Renderer> getRenderersList(VisMode visMode){
		Vector<Renderer> renderers = new Vector<Renderer>();	
		if (visMode == VisMode.MODE_2011){
			renderers.add(new EdgesRenderer());
			renderers.add(new NodesRenderer());
		}
		else if (visMode == VisMode.MODE_2012){
			renderers.add(new ZonesRenderer());
			renderers.add(new NoNumbersProbedNodesTopologyRenderer());
			renderers.add(new NoNumbersAgentsRenderer());
		}
		else if (visMode == VisMode.MODE_2013){
			renderers.add(new ZonesRendererScore());
			renderers.add(new NoNumbersProbedNodesTopologyRenderer());
			renderers.add(new ActionTargetRenderer());
			renderers.add(new PolygonAgentsRenderer());
		}	
		return renderers;
	}
	
	
	public static String getRendererName(VisMode visMode){
		if (visMode == VisMode.MODE_2011){
			return "2011";
		}
		else if (visMode == VisMode.MODE_2012){
			return "2012";
		}
		else if (visMode == VisMode.MODE_2013){
			return "2013";
		}	
		return "";
	}
	
	/**
	 * Returns the next visualization mode from the one given as a parameter. It
	 * cycles to first one when the parameter is the last one. Useful for switching
	 * between modes. 
	 * @param visMode the current visualization mode
	 * @return the next visualization mode
	 */
	public static VisMode getNextMode(VisMode visMode) {
		return VisMode.values()[(visMode.ordinal()+1) % VisMode.values().length ];
	}
}
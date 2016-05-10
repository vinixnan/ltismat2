package massim.competition2012.monitor.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import massim.competition2012.monitor.Definitions;
import massim.competition2012.monitor.graph.AgentInfo;
import massim.competition2012.monitor.graph.EdgeInfo;
import massim.competition2012.monitor.graph.NodeInfo;
import massim.competition2012.monitor.graph.TeamInfo;
import massim.competition2012.monitor.graph.Util;

/**
 * Renders a game graph. Has a static factory method for instantiating
 * different renderer. Hides the renderers' implementation from the
 * client.
 * 
 * @author tristanbehrens
 *
 */
public abstract class Renderer {

	/**
	 * Creates a specific renderer.
	 * @param type
	 * @return
	 */
	public static Renderer createRenderer(String type) {
		
		if ( type.equals("nodes") ) {
			return new NodesRenderer();
		}
		else if ( type.equals("edges") ) {
			return new EdgesRenderer();
		}
		else if ( type.equals("zones") ) {
			return new ZonesRenderer();
		}
		else if ( type.equals("noNumbersTopology") ) {
			return new NoNumbersTopologyRenderer();
		}
		else if ( type.equals("noNumbersAgents") ) {
			return new NoNumbersAgentsRenderer();
		}
		else {
			assert false : "unknown renderer" + type;
		}
				
		return null;
	}
	
	abstract public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo, String selectedAgent, Graphics2D g2d);	

}


/**
 * Renders a set of nodes in the traditionam manner.
 * 
 * @author tristanbehrens
 *
 */
class NodesRenderer extends Renderer {

	public String selectedNode = "";
    public String selectedAgent = "";
	
	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo,
			String selectedAgent, Graphics2D g2d) {
		
		// draw all nodes
		for ( NodeInfo n : nodes) {
								
			// special render for selected node
			if ( n.name.equals(selectedNode) ) {
				
				g2d.setColor(new Color(255,255,255));
				g2d.setStroke(new BasicStroke(6));
				g2d.drawOval(n.x - Definitions.nodeRadius, n.y - Definitions.nodeRadius, 2*Definitions.nodeRadius, 2*Definitions.nodeRadius);

			}
			
			int domTeam = n.dominatorTeam;
			if( domTeam < -1 || domTeam >= Definitions.teamDomColors.length) {
				throw new AssertionError("Implement (not enough colors defined)");
			}
			g2d.setColor(new Color(0,0,0));
			g2d.setStroke(new BasicStroke(1));
			g2d.fillOval(n.x - (Definitions.nodeRadius + 0 ), n.y - (Definitions.nodeRadius + 0 ), 2*(Definitions.nodeRadius + 0 ), 2*(Definitions.nodeRadius + 0 ));
			
			g2d.setColor(domTeam == -1? new Color(155,155,155): Definitions.teamDomColors[domTeam]);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawOval(n.x - Definitions.nodeRadius, n.y - Definitions.nodeRadius, 2*Definitions.nodeRadius, 2*Definitions.nodeRadius);
			
			g2d.setColor(new Color(0,0,0));
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
				g2d.setColor(new Color(128,255,255));
			} else {
				g2d.setColor(new Color(255,255,255));
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
					g2d.setColor(new Color(255,255,255));
					g2d.setStroke(new BasicStroke(6));
					g2d.drawRoundRect(ag.x - Definitions.agentRadius, ag.y - Definitions.agentRadius, 2*Definitions.agentRadius, 2*Definitions.agentRadius, 3, 3);
				}
				
				// invariant
				if( ag.team < 0 || ag.team >= Definitions.agentColors.length) {
					throw new AssertionError("Implement (not enough colors defined)");
				}

				Color agentColor = Definitions.agentColors[ag.team];
				
				g2d.setStroke(new BasicStroke(1));
				g2d.setColor(ag.health > 0? new Color(0,0,0): new Color(120,120,120));
				g2d.fillRoundRect(ag.x - (Definitions.agentRadius+0), ag.y - (Definitions.agentRadius+0), 2*(Definitions.agentRadius+0), 2*(Definitions.agentRadius+0), 6, 6);
				
				g2d.setColor(agentColor);
				g2d.setStroke(new BasicStroke(2));
				g2d.drawRoundRect(ag.x - Definitions.agentRadius, ag.y - Definitions.agentRadius, 2*Definitions.agentRadius, 2*Definitions.agentRadius, 6, 6);
				
				g2d.setColor(new Color(0,0,0));
				g2d.setStroke(new BasicStroke(1));
				g2d.drawRoundRect(ag.x - (Definitions.agentRadius+1), ag.y -  (Definitions.agentRadius+1), 2* (Definitions.agentRadius+1), 2* (Definitions.agentRadius+1), 6, 6);
				
				g2d.setColor(new Color(255,255,255));

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
				
				g2d.setColor(new Color(000,000,000));
				g2d.setStroke(new BasicStroke(4));
				g2d.drawLine(e.node1.x, e.node1.y,
							 e.node2.x, e.node2.y);
			}			
			if( domTeam < -1 || domTeam >= Definitions.teamDomColors.length) {
				throw new AssertionError("Implement (not enough colors defined)");
			}
			
			// draw a white line
			g2d.setColor(domTeam == -1? new Color(200,200,200): Definitions.teamDomColors[domTeam]);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawLine(e.node1.x, e.node1.y,
						 e.node2.x, e.node2.y);

			// draw the weight
			int x = (int) (( e.node1.x + e.node2.x) / 2.0f);
			int y = (int) (( e.node1.y + e.node2.y) / 2.0f);
			g2d.setStroke(new BasicStroke(1));
			g2d.fillOval(x-7, y-7, 13, 13);
			g2d.setColor(new Color(0,0,0));
			String str = "" + e.weight;
			g2d.drawChars(str.toCharArray(), 0, str.length(), x-4, y+4);
			
		} // all edges drawn
		
					
	
	}
	
	
}

/**
 * Renders the zones using our excellent zone-polygon-rendering
 * algorithm.
 * 
 * @author tristanbehrens
 *
 */
class ZonesRenderer extends Renderer {
	
	private Graphics2D g2d = null;

	@Override
	public void render(Vector<NodeInfo> nodes, Vector<EdgeInfo> edges, Vector<AgentInfo> agents, Vector<TeamInfo> teamsInfo,
			String selectedAgent, Graphics2D g2d) {

		// 1. filter edges and nodes
		Map<Integer,Collection<NodeInfo>> filteredNodes = filterNodes(nodes);
		Map<Integer,Collection<EdgeInfo>> filteredEdges = filterEdges(edges, nodes);
		
		// 2. render
		for ( int domTeam : filteredEdges.keySet() ) {
			this.g2d = g2d;
			g2d.setColor(Definitions.teamDomColors[domTeam].darker().darker());
			renderZones(filteredNodes.get(domTeam), filteredEdges.get(domTeam));
		}

	}

	/**
	 * Yields a map that contains for each color the edges that have
	 * the same color.
	 * 
	 * @param edges
	 * @param nodes 
	 * @return
	 */
	private Map<Integer, Collection<EdgeInfo>> filterEdges(Vector<EdgeInfo> edges, Vector<NodeInfo> nodes) {
		
		Map<Integer, Collection<EdgeInfo>> ret = new HashMap<Integer, Collection<EdgeInfo>>();
		
		HashMap<NodeInfo, Vector<EdgeInfo>> incoming = new HashMap<NodeInfo, Vector<EdgeInfo>>();
		HashMap<NodeInfo, Vector<EdgeInfo>> outgoing = new HashMap<NodeInfo, Vector<EdgeInfo>>();
		for(NodeInfo n : nodes){
			incoming.put(n, new Vector<EdgeInfo>());
			outgoing.put(n, new Vector<EdgeInfo>());
		}
		
		for ( EdgeInfo e : edges ) {
			
			int domTeam = -1;
			if (e.node1.dominatorTeam != -1 && e.node1.dominatorTeam == e.node2.dominatorTeam) {
				domTeam = e.node1.dominatorTeam;
			}
			
			if ( domTeam >= 0 ) {
				
				// add to collection
				Collection<EdgeInfo> coll = ret.get(domTeam);
				if ( coll == null ) {
					coll = new Vector<EdgeInfo>();
					ret.put(domTeam, coll);
				}

				// add the edge
				EdgeInfo e1 = new EdgeInfo();
				e1.node1 = e.node1;
				e1.node2 = e.node2;
				coll.add(e1);
				
				incoming.get(e.node2).add(e1);
				outgoing.get(e.node1).add(e1);
				
				// generate the inverse edge and add
				EdgeInfo e2 = new EdgeInfo();
				e2.node1 = e.node2;
				e2.node2 = e.node1;
				coll.add(e2);
				
				incoming.get(e.node1).add(e2);
				outgoing.get(e.node2).add(e2);
			
			}	
			
		}
		
		for(NodeInfo n : nodes){
			for(EdgeInfo e1 : incoming.get(n)){
				for(EdgeInfo e2 : outgoing.get(n)){
					e1.succ.add(e2);
				}
			}
			for(EdgeInfo e1 : outgoing.get(n)){
				for(EdgeInfo e2 : incoming.get(n)){
					e1.pred.add(e2);
				}
			}
		}
		
		return ret;

	}

	/**
	 * Yields a map that contains for each color the nodes that have
	 * the same color.
	 * 
	 * @param nodes
	 * @return
	 */
	private Map<Integer, Collection<NodeInfo>> filterNodes(Vector<NodeInfo> nodes) {

		Map<Integer, Collection<NodeInfo>> ret = new HashMap<Integer, Collection<NodeInfo>>();
	
		for ( NodeInfo n : nodes ) {
			
			int domTeam = n.dominatorTeam;
			
			if ( domTeam >= 0 ) {
				Collection<NodeInfo> coll = ret.get(domTeam);
				if ( coll == null ) {
					coll = new Vector<NodeInfo>();
					ret.put(domTeam, coll);
				}
				coll.add(n);
			}	
			
		}
		
		return ret;
	}

	private static long maxTime = 0;
	private static int maxPath = 0;
	
	/**
	 * Renders a zone using a given set of nodes and a given set
	 * of edges.
	 * 
	 * @param nodesIn
	 * @param edgesIn
	 */
	private void renderZones(Collection<NodeInfo> nodesIn,
			Collection<EdgeInfo> edgesIn) {

		long startTime = System.nanoTime();

		// 1. render nodes
		for ( NodeInfo node : nodesIn ) {
			renderNode(node);
		}
		
		// 2. render edges
		for ( EdgeInfo edge :edgesIn ) {
			renderEdge(edge);
		}

		// 3.render polygons
		int depth = 3;
		int maxDepth = 8;
		LinkedList<EdgeInfo> edges = new LinkedList<EdgeInfo>(edgesIn);
		while ( !edges.isEmpty() && depth < maxDepth ) {
			
			LinkedList<EdgeInfo> remove = null;
			for ( EdgeInfo edge : edges) {

				LinkedList<EdgeInfo> pathIn = new LinkedList<EdgeInfo>();
				pathIn.add(edge);
				LinkedList<EdgeInfo> pathOut = findPolygon(nodesIn,edges,depth,pathIn);

				if ( pathOut != null ) {
					if ( pathOut.size() > 1 ) {
						renderPath(pathOut);
						if ( pathOut.size() > maxPath ) {
							maxPath = pathOut.size();
						}
					}
					remove = pathOut;
					break;
				}
				
			}
			if ( remove == null ) {
				depth ++;
			}
			else {
				edges.removeAll(remove);
			}
		}
	
		long time = System.nanoTime() - startTime;
		if ( time > maxTime ) {
			maxTime = time;
		}
		//System.out.println("time: " + time / 1000000.0 + "ms");
		//System.out.println("maxTime: " + maxTime / 1000000.0 + "ms");
		//System.out.println("maxPath: " + maxPath);
		
	}

	/**
	 * Renders a path.
	 * 
	 * @param pathOut
	 */
	private void renderPath(LinkedList<EdgeInfo> pathOut) {

		//System.out.println("Rendering " + pathOut);
		int[] x = new int[pathOut.size()];
		int[] y = new int[pathOut.size()]; 
		for ( int a = 0 ; a < pathOut.size() ; a++ ) {
			x[a] = pathOut.get(a).node1.x;
			y[a] = pathOut.get(a).node1.y;
		}
		g2d.fillPolygon(x, y, pathOut.size());
		g2d.setStroke(new BasicStroke(50,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
		g2d.drawPolygon(x, y, pathOut.size());

	}

	/**
	 * Renders an edge.
	 * 
	 * @param edge
	 */
	private void renderEdge(EdgeInfo edge) {

		g2d.setStroke(new BasicStroke(50,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
		g2d.drawLine(edge.node1.x, edge.node1.y, edge.node2.x, edge.node2.y);

	}

	/**
	 * Renders a node.
	 * 
	 * @param node
	 */
	private void renderNode(NodeInfo node) {

		g2d.setStroke(new BasicStroke(50,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
		g2d.drawLine(node.x, node.y, node.x, node.y);
		
	}

	/**
	 * Finds a polygon. This method is used recursively. It either returns
	 * null if there is no set of edges to be removed, it return a single
	 * edge if the edge has no predecessor or successor, or it returns a
	 * path describing a polygon of the desired length.
	 * 
	 * @param nodes the set of nodes of the graph.
	 * @param edges the set of edges of the graph.
	 * @param depth the desired size of the polygon.
	 * @param path the current path.
	 * @return
	 */
	private LinkedList<EdgeInfo> findPolygon(
			Collection<NodeInfo> nodes, LinkedList<EdgeInfo> edges,
			int depth, LinkedList<EdgeInfo> path) {

		//System.out.println("Path(" + depth + "):" + path);
		
		// should line be removed?
		// a line is supposed to be removed if it has no predecessor or successor
		if ( path.size() == 1 ) {
			EdgeInfo edge = path.getFirst();
			boolean hasPred = edge.hasPred();
//			for ( EdgeInfo cand : edges ) {
//				if ( edge.equals(cand) ) continue;
//				if ( cand.node1.equals(edge.node2) && cand.node2.equals(edge.node1) ) continue; // loop
//				if ( cand.node2.equals(edge.node1) ) {
//					hasPred = true;
//					break;
//				}
//			}
			boolean hasSucc = edge.hasSucc();
//			for ( EdgeInfo cand : edges ) {
//				if ( edge.equals(cand) ) continue;
//				if ( cand.node1.equals(edge.node2) && cand.node2.equals(edge.node1) ) continue; // loop
//				if ( cand.node1.equals(edge.node2) ) {
//					hasSucc = true;
//					break;
//				}
//			}
			if ( !( hasPred && hasSucc ) ) return path; // remove this line
		}
		
		// the path has the desired length. finished?
		if ( path.size() == depth) {
			EdgeInfo firstEdge = path.getFirst();
			EdgeInfo lastEdge = path.getLast();
			// required: the path is a circle and it is oriented clockwise
			// assumed: the path is convex
			if ( firstEdge.node1.equals(lastEdge.node2) && isPolygonClockwise(path) ) {
				return path;
			}
			else {
				return null;
			}
		}
		
		// not finished yet
		// consider the successors of the latest edge an see if a path can be found
		// search all candidates = all remaining edges
//		for ( EdgeInfo cand : edges ) {
//			
//			// reject all invalid candidates
//			if ( path.contains(cand) ) continue; // already in path -> ignore
//			EdgeInfo last = path.getLast();
//			if ( cand.equals(last) ) continue; // self -> ignore
//			if ( cand.node1.equals(last.node2) && cand.node2.equals(last.node1) ) continue; // loop -> ignore
//			if ( !last.node2.equals(cand.node1) ) continue; // not connected -> ignore
//			
//			// the canditate is valid -> make a new path and continue search
//			LinkedList<EdgeInfo> newPath = new LinkedList<EdgeInfo>();
//			newPath.addAll(path);
//			newPath.add(cand);
//			LinkedList<EdgeInfo> result = this.findPolygon(nodes, edges, depth, newPath);
//			if ( result != null ) return result; // fund something to remove -> done
//		
//		}
		
		for(EdgeInfo e : path.getLast().succ){
			LinkedList<EdgeInfo> newPath = new LinkedList<EdgeInfo>();
			newPath.addAll(path);
			newPath.add(e);
			LinkedList<EdgeInfo> result = this.findPolygon(nodes, edges, depth, newPath);
			if ( result != null ) return result; // found something to remove -> done
		}
		
		// return default value: nothing to remove
		return null;
	
	}

	/**
	 * Checks if a given convex polygon has a clockwise orientation.
	 * 
	 * @param path
	 * @return
	 */
	private boolean isPolygonClockwise(LinkedList<EdgeInfo> path) {

		Vector<Point> points = new Vector<Point>();
		for ( EdgeInfo edge : path ) {
			points.add(new Point(edge.node1.x,edge.node1.y));
		}
		
		Iterator<Point> it = points.iterator();
		Point pt1 = (Point)it.next();
		Point firstPt = pt1;
		Point lastPt = null;
		double area = 0.0;
		while(it.hasNext()){
			Point pt2 = (Point) it.next();
			area += (((pt2.getX() - pt1.getX()) * (pt2.getY() + pt1.getY())) / 2);
			pt1 = pt2;
			lastPt = pt1;
		}
		area += (((firstPt.getX() - lastPt.getX()) * (firstPt.getY() + lastPt.getY())) / 2);
		return area < 0;

	}

	
}

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

		Color color = new Color(150,150,150,150);
		
		g2d.setColor(color);
		for ( EdgeInfo edge: edges ) {
			int width = 2 + 1 * edge.weight;
			g2d.setStroke(new BasicStroke(width,
		            BasicStroke.CAP_ROUND,
		            BasicStroke.JOIN_ROUND));
			g2d.drawLine(edge.node1.x, edge.node1.y, edge.node2.x, edge.node2.y);
			
		}

		g2d.setColor(color.brighter());
		for ( NodeInfo node : nodes ) {
			
			int width = 4 + 6 * node.weight;
			g2d.setStroke(new BasicStroke(width,
		            BasicStroke.CAP_ROUND,
		            BasicStroke.JOIN_ROUND));
			g2d.drawLine(node.x, node.y, node.x, node.y);
		
		}

	}

}

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
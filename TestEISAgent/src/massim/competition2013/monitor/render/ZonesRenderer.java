package massim.competition2013.monitor.render;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import massim.competition2013.monitor.Definitions;
import massim.competition2013.monitor.graph.AgentInfo;
import massim.competition2013.monitor.graph.EdgeInfo;
import massim.competition2013.monitor.graph.NodeInfo;
import massim.competition2013.monitor.graph.TeamInfo;

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
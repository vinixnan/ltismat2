package massim.competition2014.scenario;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import massim.framework.util.DebugLog;

/**
 * This class is a graph generation that uses an imperfect triangulation algorithm over a grid, with an ad-hoc
 * Heuristic to improve performance when generating big maps.
 * 
 * This version introduces a new parameter to thin out the graph.
 *
 */
public class GraphGenerator2013 extends GraphGenerator implements Serializable{
	

	private static final long serialVersionUID = -4763499998252921628L;
	
	private HashMap<GraphNode, GraphNode> counterparts;
	
	private int thinning = -1;

	@Override
	public void generate(Vector<GraphNode> nodes, Vector<GraphEdge> edges,
			int nodesNum, int gridWidth, int gridHeight, int cellWidth,
			int minNodeWeight, int maxNodeWeight, int minEdgeCost, int maxEdgeCost, 
			Random random, int randomWeight, int gradientWeight, int optimaWeight, int blurIterations,
			int optimaPercentage) {
		
		if(this.thinning == -1){
			DebugLog.log(DebugLog.LOGLEVEL_NORMAL, "GraphGeneration: No thinning factor specified.");
			this.thinning = 0;
		}
		else if(this.thinning < 0 || this.thinning > 100){
			DebugLog.log(DebugLog.LOGLEVEL_NORMAL, "GraphGeneration: Thinning factor out of bounds. Must be 0 <= th <= 100.");
			this.thinning = 0;
		}
		
		//prepare for generating only half the required nodes in one half of the grid
		int oldGridWidth = gridWidth;
		nodesNum /= 2;
		gridWidth /= 2;
		
		//get the maximal distance to the grid's center
		double maxDistance = Math.sqrt(
				Math.pow(gridWidth*cellWidth/2, 2) +
				Math.pow(gridHeight*cellWidth/2, 2));
		int centerX = (oldGridWidth+1)*cellWidth/2;
		int centerY = (gridHeight+1)*cellWidth/2;
		
		int i = 0;
		//create nodes; their weight will be calculated after edge-generation
		while ( nodes.size() < nodesNum ) {
			
			int gridX = random.nextInt(gridWidth+1);
			int gridY = random.nextInt(gridHeight+1);			
			int x = gridX * cellWidth  + cellWidth/2;// + random.nextInt(10) - 5;
			int y = gridY * cellWidth + cellWidth/2;// + random.nextInt(10) - 5;
			
			GraphNode n = new GraphNode(i, 0, gridX, gridY,x,y);

			if ( !nodes.contains(n) ){
				nodes.add(n);
				i++;
			}
		}
		
		int c = 4;
		double limit = c *
				Math.sqrt(
					Math.pow( ((double)(cellWidth * gridHeight) / Math.sqrt(((double)gridHeight/(double)gridWidth)*(double)nodesNum)), 2) +
					Math.pow( ((double)(cellWidth * gridWidth) / Math.sqrt(((double)gridWidth/(double)gridHeight)*(double)nodesNum)), 2)
				);
		
		// create a full graph... the edges will be sorted by length
		Vector<GraphEdge> edgesFullSorted = new Vector<>();
		for ( int a = 0 ; a < nodes.size() ; a++ ) {
			
			for ( int b = a+1 ; b < nodes.size() ; b++ ) {
				
				GraphNode n1 = nodes.elementAt(a);
				GraphNode n2 = nodes.elementAt(b);
				
				int weight = minEdgeCost + random.nextInt(maxEdgeCost - minEdgeCost);
				GraphEdge e = new GraphEdge(weight,n1,n2);
				//edgesFull.add(e);
				
				
				
				//only add if shorter than some value
				if(e.getLength() < limit){	
					edgesFullSorted.add(e);
				}
			}
		}
		
		Collections.sort(edgesFullSorted,
						 new Comparator<GraphEdge>() {
							@Override
							public int compare(GraphEdge o1, GraphEdge o2) {
								if(o1.getLength() < o2.getLength()){
									return 1;
								} else if(o1.getLength() > o2.getLength()){
									return -1;
								} else {
									return 0;
								}
							}
						 });
		
		// filter remaining edges
		Vector<GraphEdge> remove = new Vector<>();
		for( int a=0 ; a < edgesFullSorted.size() ; a++ ) {
	
			// should be sorted descending
			if( a+1 != edgesFullSorted.size() )
			  assert edgesFullSorted.get(a).getLength() >= edgesFullSorted.get(a+1).getLength();

			// if intersects remove edge
			for( int b = a + 1 ; b < edgesFullSorted.size() ; b++ ) {
		
				// do not intersect adjacent edges
				GraphNode id1 = edgesFullSorted.get(a).node1;
				GraphNode id2 = edgesFullSorted.get(a).node2;
				GraphNode id3 = edgesFullSorted.get(b).node1;
				GraphNode id4 = edgesFullSorted.get(b).node2;
				
				if( id1.equals(id3)){
					if (checkSameLine(id1,id2,id4)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					}
					continue;
				}
				if( id1.equals(id4)){
					if (checkSameLine(id1,id2,id3)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					}
					continue;
				}
				if( id2.equals(id3)){
					if (checkSameLine(id2,id1,id4)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					}
					continue;
				}
				if( id2.equals(id4)){
					if (checkSameLine(id2,id1,id3)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					}
					continue;
				}

				// intersection algorithm
				double x1 = id1.x;
				double y1 = id1.y;
				double x2 = id2.x;
				double y2 = id2.y;					
				double x3 = id3.x;
				double y3 = id3.y;
				double x4 = id4.x;
				double y4 = id4.y;
				
				double ua = 
					((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) /
					((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
				double ub =
					((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) /
					((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
			
				boolean intersect = ua > 0.0f && ua < 1.0f && ub > 0.0f && ub < 1.0f; 
			
				if( intersect ) {
					
					remove.add(edgesFullSorted.get(a));
					break; // stop looking for intersections
					
				}
			}
		}
		
		// remaining edges are the triangulation
		for( GraphEdge e : edgesFullSorted) {
			
			if( !remove.contains(e) ){
				edges.add(e);
			}
		}
		
		//thin out the edges to get a more sparse graph 
		if(this.thinning != 0){
			//delete edges with a probability of 'thinning'%
			remove.clear();
			for (GraphEdge e: edges){
				if (this.thinning > random.nextInt(100)){
					remove.add(e);
				}
			}
		}
		
		Vector<GraphEdge> remainingEdges = new Vector<>();
		
		for( GraphEdge e : edges) {
			
			if( !remove.contains(e) ){
				remainingEdges.add(e);
			}
		}
		
		//determine connected components
		Map<GraphNode, Vector<GraphEdge>> incident = new HashMap<>();
		for(GraphNode n: nodes){
			incident.put(n, new Vector<GraphEdge>());
		}
		for(GraphEdge e: remainingEdges){
			incident.get(e.node1).add(e);
			incident.get(e.node2).add(e);
		}
		
		Vector<GraphNode> remainingNodes = new Vector<>(nodes);
		
		//perform a breadth-first search on all unused nodes
		Vector<ConnectedComponent> components = new Vector<>();
		while(!remainingNodes.isEmpty()){
//			Vector<GraphNode> connectedComponent = new Vector<GraphNode>();
			Set<GraphNode> usedNodes = new HashSet<>();
			GraphNode root = remainingNodes.remove(0);
			usedNodes.add(root);
//			connectedComponent.add(root);
			
			Vector<GraphNode> nodesToExpand = new Vector<>();
			nodesToExpand.add(root);
			boolean addedNew = true;
			
			while (addedNew){
				
				addedNew = false;
				Vector<GraphNode> nextNodes = new Vector<>();
				
				for(GraphNode n: nodesToExpand){
				
					for(GraphEdge e: incident.get(n)){
						GraphNode newNode;
						if(e.node1.equals(n)){
							newNode = e.node2;
						}
						else{
							newNode = e.node1;
						}
						
						if(!usedNodes.contains(newNode)){
							
							usedNodes.add(newNode);
//							connectedComponent.add(newNode);
							remainingNodes.remove(newNode); //not so nice
							nextNodes.add(newNode);
							addedNew = true;
						}
					}
				}
				
				nodesToExpand = nextNodes;
			}
			
			components.add(new ConnectedComponent(usedNodes));
		}
		
		while (components.size() > 1){
			DebugLog.log(DebugLog.LOGLEVEL_DEBUG, "GraphGeneration: connecting two components");
			//components have to be connected to a single graph again
			//each component can be connected with at least one other component -> find it
			
			ConnectedComponent cc1 = components.remove(0);
			
			for(GraphEdge e: remove){ //find an edge that can connect two components
				GraphNode cc2node;
				if(cc1.containsNode(e.node1) && !cc1.containsNode(e.node2)){
					cc2node = e.node2;
				}
				else if(cc1.containsNode(e.node2) && !cc1.containsNode(e.node1)){
					cc2node = e.node1;
				}
				else{
					continue;
				}
				boolean found = false;
				for(ConnectedComponent cc2: components){
					if(cc2.containsNode(cc2node)){
						remove.remove(e);
						remainingEdges.add(e);
						cc2.fuseWith(cc1);
						found = true;
						break;
					}
				}
				if(found){
					break;
				}
			}
		}
		
		/*
		 * remainingEdges includes all edges that either have not been 
		 * removed or restored to connect the components to a conneceted graph again
		 */
		edges.clear(); 
		edges.addAll(remainingEdges); 
		// <- end of thinning
		
		
		//determine node weights
		HashMap<String, Integer> randomWeights = new HashMap<>();
		HashMap<String, Integer> gradientWeights = new HashMap<>();
		HashMap<String, Integer> optimaWeights = new HashMap<>();
		
		if( randomWeight > 0){
			for (GraphNode n : nodes){
				randomWeights.put(n.name, minNodeWeight + random.nextInt(maxNodeWeight-minNodeWeight+1));
			}
		}
		if( gradientWeight > 0){
			for (GraphNode n : nodes){
				double distance = Math.sqrt(
						Math.pow(centerX - n.x, 2) +
						Math.pow(centerY - n.y, 2));
				double percentage = distance / (maxDistance/100);
				double d = 1 - percentage/100;
				double gradientVal = 1 + minNodeWeight + Math.round( (maxNodeWeight - minNodeWeight - 1) * d );
				gradientWeights.put(n.name, (int)gradientVal);
			}
		}
		if( optimaWeight > 0){
			//preparation
			HashMap<String,Vector<GraphEdge>> sources = new HashMap<>();
			HashMap<String,Vector<GraphEdge>> targets = new HashMap<>();
			for(GraphNode n : nodes){
				sources.put(n.name, new Vector<GraphEdge>());
				targets.put(n.name, new Vector<GraphEdge>());
			}
			for(GraphEdge e : edges){
				sources.get(e.node1.name).add(e);
				targets.get(e.node2.name).add(e);
			}
			//determine optima
			HashSet<String> optima = new HashSet<>();
			for (GraphNode n : nodes){
				if (random.nextInt(100)<optimaPercentage){
					optimaWeights.put(n.name, maxNodeWeight);
					optima.add(n.name);
				}
				else{
					optimaWeights.put(n.name, minNodeWeight);
				}
				
			}
			//blur
			double nodesWeight = 5;
			double per = (100-nodesWeight);
			for (int j = 0; j < blurIterations; j++){
				for (GraphNode n : nodes){
					if(!optima.contains(n.name)){
						double distanceSum = 0;
						Vector<double[]> weights = new Vector<>();
						for(GraphEdge e : sources.get(n.name)){
							double distance =
									Math.sqrt((Math.pow(Math.abs(e.node1.x-e.node2.x),2)
									+Math.pow(Math.abs(e.node1.y-e.node2.y),2)));
							distanceSum += distance;
							double[] arr ={distance, optimaWeights.get(e.node2.name)};
							weights.add(arr);
						}
						for(GraphEdge e : targets.get(n.name)){
							double distance =
									Math.sqrt((Math.pow(Math.abs(e.node1.x-e.node2.x),2)
									+Math.pow(Math.abs(e.node1.y-e.node2.y),2)));
							distanceSum += distance;
							double[] arr ={distance, optimaWeights.get(e.node1.name)};
							weights.add(arr);
						}
						
						double newWeight = nodesWeight*n.weight;
						for(double[] w : weights){
							newWeight += (w[0]/(distanceSum/per)) * w[1];
						}
//						System.out.println("newWeight "+newWeight);
						optimaWeights.put(n.name, (int)Math.round(newWeight/100));
					}
				}
			}
		}
		
		//compute final weight
		double o = optimaWeight + gradientWeight + randomWeight;
		assert o > 0;
		double optimaPart = optimaWeight/(o/100);
		double gradientPart = gradientWeight/(o/100);
		double randomPart = randomWeight/(o/100);
		
		for (GraphNode n: nodes){
			double finalWeight = 0;
			if(optimaWeight > 0){
				finalWeight += optimaPart * optimaWeights.get(n.name);
//				System.out.println("opt "+optimaPart  +" "+  optimaWeights.get(n.name));
			}
			if(gradientWeight > 0){
				finalWeight += gradientPart * gradientWeights.get(n.name);
//				System.out.println("grad "+gradientPart  +" "+  gradientWeights.get(n.name));
			}
			if(randomWeight > 0){
				finalWeight += randomPart * randomWeights.get(n.name);
//				System.out.println("rand "+randomPart +" "+ randomWeights.get(n.name));
			}
			
			int f = (int) Math.round(finalWeight/100);
			
			if (f > maxNodeWeight){
				f = maxNodeWeight;
			}
			else if(f < minNodeWeight){
				f = minNodeWeight;
			}
			
			n.weight = f;
		}
		
		
		// mirror everything at one vertical axis
		Vector<GraphNode> mirroredNodes = new Vector<>();
		// store every single node's counterpart to easily assign the edges later
		this.counterparts = new HashMap<>();
		// store the nodes at the right edge so that they can be linked to their counterparts
		HashMap<Integer, GraphNode> boundaryNodes = new HashMap<>();
		
		for (GraphNode n : nodes){
			
			GraphNode mirroredNode = new GraphNode(i, 
					n.weight, oldGridWidth-n.gridX, n.gridY, (oldGridWidth+1)*cellWidth-n.x, n.y);
			if(!nodes.contains(mirroredNode)){
				mirroredNodes.add(mirroredNode);
				this.counterparts.put(n, mirroredNode);
				i++;
				//update the boundary nodes
				if (!boundaryNodes.containsKey(n.gridY)){
					boundaryNodes.put(n.gridY, n);
				}
				else{
					if(boundaryNodes.get(n.gridY).gridX < n.gridX){
						boundaryNodes.put(n.gridY, n);
					}
				}
			}
			else{
				this.counterparts.put(n, n);
			}
			

		}
		
		nodes.addAll(mirroredNodes);
		
		Vector<GraphEdge> mirroredEdges = new Vector<>();
		for (GraphEdge e : edges){
			
			GraphEdge newEdge = new GraphEdge(
					e.weight, this.counterparts.get(e.node1), this.counterparts.get(e.node2));
			mirroredEdges.add(newEdge);
		}
		edges.addAll(mirroredEdges);
		
		Vector<GraphEdge> connectingEdges = new Vector<>();
		// connect both halves
		for (GraphNode n : boundaryNodes.values()){
			if( n != this.counterparts.get(n)){
				int weight = minEdgeCost + random.nextInt(maxEdgeCost - minEdgeCost);
				GraphEdge newEdge = new GraphEdge(weight, n, this.counterparts.get(n));
				connectingEdges.add(newEdge);
			}
		}
		
		// check the new edges for possible intersections
		for (GraphEdge e1 : connectingEdges){
			
			boolean add = true;
			
			for (GraphEdge e2 : edges){
				
				GraphNode id1 = e1.node1;
				GraphNode id2 = e1.node2;
				GraphNode id3 = e2.node1;
				GraphNode id4 = e2.node2;
				
				double x1 = id1.x;
				double y1 = id1.y;
				double x2 = id2.x;
				double y2 = id2.y;					
				double x3 = id3.x;
				double y3 = id3.y;
				double x4 = id4.x;
				double y4 = id4.y;
				
				double ua = 
					((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) /
					((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
				double ub =
					((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) /
					((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
			
				boolean intersect = ua > 0.0f && ua < 1.0f && ub > 0.0f && ub < 1.0f; 
				
				if(intersect){
					add = false;
					break;
				}
			}
			
			if(add){
				edges.add(e1);
			}
		}
		
		//shuffle nodes to assign new "names"
		Collections.shuffle(nodes, random);
		int j = 0;
		for (GraphNode n : nodes){
			n.name = GraphNode.NODE_NAME_PREFIX + j;
			j++;
		}
		
	}
	
	private boolean checkSameLine(GraphNode commonNode, GraphNode secondary1, GraphNode secondary2) {
		double x1 = commonNode.x;
		double y1 = commonNode.y;
		double x2 = secondary1.x;
		double y2 = secondary1.y;
		double x3 = secondary2.x;
		double y3 = secondary2.y;
		
		return (x1 == x2 && x1 == x3 && (y2-y1)*(y3-y1)>0)
		    || (y1 == y2 && y1 == y3 && (x2-x1)*(x3-x1)>0)
		    || ((x2-x1)/(y2-y1) == (x3-x1)/(y3-y1) && (x2-x1)*(x3-x1)>0);
	}

	@Override
	public String toString() {
		return "Sparse Balanced Triangulation";
	}
	
	public HashMap<GraphNode, GraphNode> getCounterparts(){
		
		return this.counterparts;
	}
	
	public void setThinning(int thinning){
		this.thinning = thinning;
	}
	
	/**
	 * Representing a connected component, i.e. each node is reachable from each other
	 * @author Tobias
	 *
	 */
	class ConnectedComponent{
		
		private Set<GraphNode> nodes;
		
		public ConnectedComponent(Set<GraphNode> nodes){
			this.nodes = new HashSet<>(nodes);
		}
		
		public boolean containsNode(GraphNode n){
			return this.nodes.contains(n);
		}
		
		public Set<GraphNode> getNodes(){
			return this.nodes;
		}
		
		/**
		 * Adds all nodes of the given cc to this cc.
		 * @param cc A connected component
		 */
		public void fuseWith(ConnectedComponent cc){
			this.nodes.addAll(cc.getNodes());
		}
	}
}

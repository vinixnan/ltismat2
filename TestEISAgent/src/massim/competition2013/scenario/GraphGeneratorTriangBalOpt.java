package massim.competition2013.scenario;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

/**
 * This class is a graph generation that uses an imperfect triangulation algorithm over a grid, with an ad-hoc
 * Heuristic to improve performance when generating big maps.
 *
 */
public class GraphGeneratorTriangBalOpt extends GraphGenerator implements Serializable{
	
	private HashMap<GraphNode, GraphNode> counterparts;

	@Override
	public void generate(Vector<GraphNode> nodes, Vector<GraphEdge> edges,
			int nodesNum, int gridWidth, int gridHeight, int cellWidth,
			int minNodeWeight, int maxNodeWeight, int minEdgeCost, int maxEdgeCost, 
			Random random, int randomWeight, int gradientWeight, int optimaWeight, int blurIterations,
			int optimaPercentage) {
		
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
		Vector<GraphEdge> edgesFullSorted = new Vector<GraphEdge>();
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
		Vector<GraphEdge> remove = new Vector<GraphEdge>();
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
					} else {
						continue;
					}
				}
				if( id1.equals(id4)){
					if (checkSameLine(id1,id2,id3)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					} else {
						continue;
					}
				}
				if( id2.equals(id3)){
					if (checkSameLine(id2,id1,id4)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					} else {
						continue;
					}
				}
				if( id2.equals(id4)){
					if (checkSameLine(id2,id1,id3)){
						remove.add(edgesFullSorted.get(a));
						break; // stop looking for intersections
					} else {
						continue;
					}
				}

				// intersection algorithm stolen from:
				// http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
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
		

		HashMap<String, Integer> randomWeights = new HashMap<String, Integer>();
		HashMap<String, Integer> gradientWeights = new HashMap<String, Integer>();
		HashMap<String, Integer> optimaWeights = new HashMap<String, Integer>();
		
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
			HashMap<String,Vector<GraphEdge>> sources = new HashMap<String, Vector<GraphEdge>>();
			HashMap<String,Vector<GraphEdge>> targets = new HashMap<String, Vector<GraphEdge>>();
			for(GraphNode n : nodes){
				sources.put(n.name, new Vector<GraphEdge>());
				targets.put(n.name, new Vector<GraphEdge>());
			}
			for(GraphEdge e : edges){
				sources.get(e.node1.name).add(e);
				targets.get(e.node2.name).add(e);
			}
			//determine optima
			HashSet<String> optima = new HashSet<String>();
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
						Vector<double[]> weights = new Vector<double[]>();
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
		Vector<GraphNode> mirroredNodes = new Vector<GraphNode>();
		// store every single node's counterpart to easily assign the edges later
		counterparts = new HashMap<GraphNode, GraphNode>();
		// store the nodes at the right edge so that they can be linked to their counterparts
		HashMap<Integer, GraphNode> boundaryNodes = new HashMap<Integer, GraphNode>();
		
		for (GraphNode n : nodes){
			
			GraphNode mirroredNode = new GraphNode(i, 
					n.weight, oldGridWidth-n.gridX, n.gridY, (oldGridWidth+1)*cellWidth-n.x, n.y);
			if(!nodes.contains(mirroredNode)){
				mirroredNodes.add(mirroredNode);
				counterparts.put(n, mirroredNode);
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
				counterparts.put(n, n);
			}
			

		}
		
		nodes.addAll(mirroredNodes);
		
		Vector<GraphEdge> mirroredEdges = new Vector<GraphEdge>();
		for (GraphEdge e : edges){
			
			GraphEdge newEdge = new GraphEdge(
					e.weight, counterparts.get(e.node1), counterparts.get(e.node2));
			mirroredEdges.add(newEdge);
		}
		edges.addAll(mirroredEdges);
		
	
		
		Vector<GraphEdge> connectingEdges = new Vector<GraphEdge>();
		// connect both halves
		for (GraphNode n : boundaryNodes.values()){
			if( n != counterparts.get(n)){
				int weight = minEdgeCost + random.nextInt(maxEdgeCost - minEdgeCost);
				GraphEdge newEdge = new GraphEdge(weight, n, counterparts.get(n));
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
		return "Balanced Triangulation";
	}
	
	public HashMap<GraphNode, GraphNode> getCounterparts(){
		
		return this.counterparts;
	}
	
}

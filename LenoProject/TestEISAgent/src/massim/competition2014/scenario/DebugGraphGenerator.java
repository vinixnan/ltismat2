package massim.competition2014.scenario;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

/**
 * This class is a graph generation that creates a graph that is essentially a grid. 
 * Number of nodes specified does not have any effect here.
 */
public class DebugGraphGenerator extends GraphGenerator implements Serializable{
	

	private static final long serialVersionUID = -4763499998252921628L;
	
	int nodeWeight = 2;
	int edgeWeight = 2;
	
	@Override
	public void generate(Vector<GraphNode> nodes, Vector<GraphEdge> edges,
			int nodesNum, int gridWidth, int gridHeight, int cellWidth,
			int minNodeWeight, int maxNodeWeight, int minEdgeCost, int maxEdgeCost, 
			Random random, int randomWeight, int gradientWeight, int optimaWeight, int blurIterations,
			int optimaPercentage) {
		
		int nodeCount = 0;
		GraphNode[][] nodesTMP = new GraphNode[gridWidth+1][gridHeight+1];
		for(int j = 0; j <= gridHeight; j++){
			for(int i = 0; i <= gridWidth; i++){
				int gridX = i;
				int gridY = j;
				int x = gridX * cellWidth  + cellWidth/2;// + random.nextInt(10) - 5;
				int y = gridY * cellWidth + cellWidth/2;// + random.nextInt(10) - 5;
				
				GraphNode n = new GraphNode(nodeCount, nodeWeight, gridX, gridY, x, y);
				nodes.add(n);
				nodesTMP[i][j] = n;
				nodeCount++;
				
				//connect node with the nodes above and left of it (if available)
				if (i > 0){
					GraphNode aboveNode = nodesTMP[i-1][j];
					GraphEdge e = new GraphEdge(edgeWeight, n, aboveNode);
					edges.add(e);
				}
				if (j > 0){
					GraphNode leftNode = nodesTMP[i][j-1];
					GraphEdge e = new GraphEdge(edgeWeight, n, leftNode);
					edges.add(e);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "Grid";
	}
}
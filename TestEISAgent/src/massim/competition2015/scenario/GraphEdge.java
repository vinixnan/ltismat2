package massim.competition2015.scenario;

import java.io.Serializable;

/**
 * This class represents and Edge in the graph that forms the map.
 */
public class GraphEdge implements Serializable {

	private static final long serialVersionUID = -2064294215950641200L;
	private static final int NUMBER_OF_CELLS = 8;
	
	private static final double ROUNDING = 0.01;
	
	private GraphNode node1;
	private GraphNode node2;
	private int type;
	// private Vector<Location> cells;

	public GraphEdge(GraphNode node1, GraphNode node2, int type) {
		
		this.type = type;
		this.node1 = node1;
		this.node2 = node2;
		// generateCells();
		
	}
	
	/*
	private void generateCells() {
		cells = new Vector<>(NUMBER_OF_CELLS);
		
		double n1lat = node1.loc.getLat();
		double n1lon = node1.loc.getLon();
		double latInc = (node2.loc.getLat() - n1lat) / (NUMBER_OF_CELLS + 1);
		double lonInc = (node2.loc.getLon() - n1lon) / (NUMBER_OF_CELLS + 1);
		for (int i = 1; i <= NUMBER_OF_CELLS; i++) {
			cells.add(new Location(n1lat+i*latInc, n1lon+i*lonInc));
		}
	}
	*/
	
	public GraphNode getNode1() {
		return node1;
	}



	public GraphNode getNode2() {
		return node2;
	}



	public int getType() {
		return type;
	}


/*
	public Vector<Location> getCells() {
		return cells;
	}
*/


	public float getLength() {
		
		return (float) Math.sqrt( (node1.x - node2.x) * (node1.x - node2.x) + (node1.y - node2.y) * (node1.y - node2.y) );
		
	}

}
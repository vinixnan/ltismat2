package massim.competition2015.scenario;

import java.io.Serializable;

/**
 * This class represents a node in the graph that forms the map.
 */
public class GraphNode implements Serializable {

	private static final long serialVersionUID = -6260869513436004934L;
	
	public static final String NODE_NAME_PREFIX = "v";
	
	public String name;
	public Location loc;
	public double x;
	public double y;
	
	public GraphNode(int nameNr, double lon, double lat) {
		
		this.name = NODE_NAME_PREFIX + nameNr;
		this.x = lon;
		this.y = lat;
		this.loc = new Location(lon, lat);
		
		//agents = new Vector<MapSimulationAgentState>();
			
	}
	
	
	/**
	 * Two nodes are considered equal if they have the same name or the same raw coordinates.
	 */
	public boolean equals(Object obj) {
		
		if ( obj == this )
			return true;
		
		if ( obj == null )
			return false;
		
		if( (obj instanceof massim.competition2015.scenario.GraphNode) == false )
			return false;
		
		massim.competition2015.scenario.GraphNode n = (massim.competition2015.scenario.GraphNode)obj;
		
		// Use raw coordinates here to avoid repeating nodes in the generation algorithms.
		return ( (this.name.equals(n.name)) || (this.x == n.x && this.y == n.y) );
		
	}


	
}

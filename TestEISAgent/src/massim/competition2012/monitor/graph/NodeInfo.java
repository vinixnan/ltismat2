package massim.competition2012.monitor.graph;

import java.util.Vector;

public class NodeInfo {
	public int x;
	public int y;
	public int gridY;
	public int gridX;
	public int weight;
	public int dominatorTeam;
	public String dominatorTeamName;
	public String name;
	public Vector<AgentInfo> agents = new Vector<AgentInfo>();
	public Vector<NodeInfo> neighbors = new Vector<NodeInfo>();
	
	public String toString() { 
		return name;
	}

}

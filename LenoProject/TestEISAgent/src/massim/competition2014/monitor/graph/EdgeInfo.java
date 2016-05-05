package massim.competition2014.monitor.graph;

import java.util.Vector;

public class EdgeInfo {
	
	public int weight;
	public NodeInfo node1;
	public NodeInfo node2;
	
	public Vector<EdgeInfo> pred = new Vector<EdgeInfo>();
	public Vector<EdgeInfo> succ = new Vector<EdgeInfo>();

	public String toString() { 
		return "(" + node1 + "," + node2 + ")";
	}
	
	public boolean hasSucc(){
		return this.succ.size() > 0;
	}
	public boolean hasPred(){
		return this.pred.size() > 0;
	}

}
package massim.competition2015.monitor.data;

import java.util.TreeSet;
import java.util.Vector;

public class WorldInfo {
	
	public String simStep;
	public String simId;
	public double minLon;
	public double minLat;
	public double maxLon;
	public double maxLat;
	public double proximity;
	public double cellSize;

	public Vector<TeamInfo> teamsInfo;

	public Vector<AgentInfo> agents;
	public TreeSet<String> agentNames;
	
	public Vector<FacilityInfo> facilities;
	public TreeSet<String> facilitiesNames;
	
	public Vector<JobInfo> jobs;
	public TreeSet<String> jobsNames;

	
}
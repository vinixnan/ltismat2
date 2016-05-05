package massim.competition2015.monitor.data;

public class AgentInfo {
	public String name;
	public String lastAction;
	public String lastActionParam;
	public String lastActionResult;
	public String role;
	public int team;
	public String teamName;
	public int charge;
	public int batteryCapacity;
	public int load;
	public int loadCapacity;
	public double lat;
	public double lon;

	public String products;

	public String toString(){return this.name;}
}
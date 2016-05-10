package massim.competition2015.configuration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represent a set of configuration options for one of the defined roles.
 */
public class RoleConfiguration implements Serializable {

	private static final long serialVersionUID = 3L;
	
	public String name;
	public int speed;
	public int loadCapacity;
	public int batteryCapacity;
	public Set<String> actions = new HashSet<>();
	public Set<String> roads = new HashSet<>();
	public Set<String> tools = new HashSet<>();

}

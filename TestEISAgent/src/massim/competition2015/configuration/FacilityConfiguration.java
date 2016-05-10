package massim.competition2015.configuration;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represent a set of configuration options for one of the defined roles.
 */
public class FacilityConfiguration implements Serializable {
	
	private static final long serialVersionUID = 3L;
	
	public String id;
	public String type;
	public int cost;
	public int capacity;
	public int rate;
	public int concurrent;
	
	public double lat;
	public double lon;
	
	public ArrayList<FacilityStock> stock;
	
	public static class FacilityStock implements Serializable {
		
		private static final long serialVersionUID = 3L;
		
		public String id;
		public int cost;
		public int amount;
		public int restock;
	}

}

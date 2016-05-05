package massim.competition2015.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represent a set of configuration options for one of the defined roles.
 */
public class ProductConfiguration implements Serializable {

	private static final long serialVersionUID = 3L;
	
	public String id;
	public int volume;
	public boolean userAssembled;
	
	// requirements to assemble if possible.
	public int assemblyTime = 0;
	public Map<String, Integer> itemsConsumed = null;
	public Map<String, Integer> toolsNeeded = null;
	
	public ProductConfiguration() {
		itemsConsumed = new HashMap<>();
		toolsNeeded = new HashMap<>();		
	}
		

}

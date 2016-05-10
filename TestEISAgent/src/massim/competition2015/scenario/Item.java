package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an item (product or tool).
 * @author fschlesinger
 *
 */
public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;
	public int volume;
	public boolean userAssembled;
	
	// requirements to assemble if usable.
	public Map<Item, Integer> itemsConsumed;
	public Map<Item, Integer> toolsNeeded;
	public int assemblyTime;	
	
	public Item() {
		this.itemsConsumed = new HashMap<>();
		this.toolsNeeded = new HashMap<>();
	}

}

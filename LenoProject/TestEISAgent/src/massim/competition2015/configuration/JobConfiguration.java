package massim.competition2015.configuration;

import java.io.Serializable;
import java.util.Map;

/**
 * This class represent a set of configuration options for one of the defined roles.
 */
public class JobConfiguration implements Serializable {

	private static final long serialVersionUID = 3L;
	
	public String id;
	public String type;
	public int firstStepActive;
	public int firstStepAuction;
	public int lastStepActive;
	public int fine;
	public int reward;
	public int maxReward;
	public String storageId;
	public Map<String, Integer> products;

}

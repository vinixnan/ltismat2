/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.List;

/**
 * @author fschlesinger
 *
 */
public abstract class Facility implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;
	public Location location;
	public List<String> allowedActions;
	
	/**
	 * 
	 */
	public Facility() {
		// TODO Auto-generated constructor stub
	}

}

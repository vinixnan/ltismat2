/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.Random;

/**
 * @author fschlesinger
 *
 */
public class PricedJob extends Job implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public int price;

	/**
	 * 
	 */
	public PricedJob(Random rand) {
		super(rand);
	}
	
	public PricedJob(Random rand, String id){
		super(rand, id);
	}
	
	public int getReward(){
		return price;
	}

}

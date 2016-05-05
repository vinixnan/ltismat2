/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * @author fschlesinger
 *
 */
public class AuctionJob extends Job implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * A fine to be paid if uncompleted.
	 */
	public int fine;

	private String auctionWinner;
	private int winningPrice;
	private LinkedHashMap<String,Integer> bids; 
	
	public int firstStepAuction;
	public int maxPrice;

	/**
	 * 
	 */
	public AuctionJob(Random rand) {
		super(rand);
		init();
	}
	
	public AuctionJob(Random rand, String id){
		super(rand, id);
		init();
	}
	
	private void init(){
		this.auctionWinner = null;
		this.bids = new LinkedHashMap<>();
	}
	
	public int getReward(){
		return winningPrice;
	}
	
	public int getFine(){
		return fine;
	}

	public String getAuctionWinner(){
		return auctionWinner;
	}
	
	public boolean isAssigned(){
		return auctionWinner != null;
	}
	
	public void addBid(String team, int price){
		if(bids.containsKey(team)){
			int minBid = Math.min(price, bids.get(team));
			bids.put(team, minBid);
		} else {
			bids.put(team, price);
		}
	}
	
	public boolean inAuction(){
		return this.status == AUCTION;
	}
	
	public void activate() {
		if (bids.isEmpty()){
			status = CANCELLED;
			return;
		}
		
		auctionWinner = null;
		winningPrice = maxPrice+1;
		for (Entry<String, Integer> entry : bids.entrySet()){
			if (entry.getValue() < winningPrice){
				winningPrice = entry.getValue();
				auctionWinner = entry.getKey();
			}
		}
		if (auctionWinner != null){
			status = ACTIVE;
		} else {
			status = CANCELLED;
		}
		
	}
}





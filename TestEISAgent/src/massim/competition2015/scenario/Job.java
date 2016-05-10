/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author fschlesinger
 *
 */
public abstract class Job implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// constats for defining state
	public static final int FUTURE = 0;
	public static final int AUCTION = 1;
	public static final int ACTIVE = 2;
	public static final int COMPLETED = 3;
	public static final int CANCELLED = 4;
	
	private static Set<Long> usedIDs = new HashSet<>();

	public Map <Item,Integer> itemsRequired;
	public Map <String, Map<Item, Integer>> itemsDelivered;
	
//	public Location location;
	public String storageId;
	public Storage storage;
	public final String id;
	
	protected int status;
	// private boolean active;
	// private boolean completed;
	
	
	public int firstStepActive;
	public int lastStepActive;
	
	public String poster;
	public String fulfiller;

	private String idFromConfig;
	
	/**
	 * 
	 */
	public Job(Random rand) {
		while(true){
			long randID = Math.abs(rand.nextLong());
			if(!usedIDs.contains(randID)){
				this.id = "job_id" + randID;
				this.usedIDs.add(randID);
				break;
			}
		}

		this.itemsRequired = new HashMap<Item, Integer>();
		this.itemsDelivered =  new HashMap<String, Map<Item, Integer>>();
		
	}

	/**
	 * Works like the normal constructor but stores the passed id for possbile future use
	 * @param id the id (probably read from config) to store in the object for future use
	 */
	public Job(Random rand, String id) {
		this(rand); //call main constructor
		this.idFromConfig = id;
	}
	
	public void addRequiredItem(Item item, Integer amount){
		this.itemsRequired.put(item, amount);
		
	}
	
	public int deliverItem(Item item, int amount, String team){
		Map<Item, Integer> deliveredMap = getTeamDelivered(team);
		if (!itemsRequired.containsKey(item)){
			return 0;
		}
		int required = itemsRequired.get(item);
		
		int delivered;
		if (!deliveredMap.containsKey(item)){
			delivered = Math.min(required, amount);
			deliveredMap.put(item, delivered);
			
		} else {
			int current = deliveredMap.get(item);
			delivered = Math.min(required, amount+current) - current;
			deliveredMap.put(item, current + delivered);
		}
		return delivered;
	}
	
	public boolean checkCompleted(String team){
		Map<Item, Integer> deliveredMap = getTeamDelivered(team);
		boolean completed = true;
		for (Entry<Item, Integer> entry: itemsRequired.entrySet()){
			Integer current = deliveredMap.get(entry.getKey());
			if (current == null) current = 0;
			if (entry.getValue() > current){
				completed = false;
				break;
			}
		}
		if (completed){
			status = COMPLETED;
			fulfiller = team;
			transferDelivered(true);
		}
		
		return completed;
	}

	protected void transferDelivered(boolean completed){
		
		for ( Entry<String,Map<Item, Integer>> teamEntry : itemsDelivered.entrySet()){
			if (completed && fulfiller.equals(teamEntry.getKey()) &&
					!poster.equals("system")){
				transferAll(teamEntry.getValue(), poster);
			} else {
				transferAll(teamEntry.getValue(), teamEntry.getKey());
			}
		}
	}
	
	protected void transferAll(Map<Item, Integer> items, String team){
		for ( Entry<Item, Integer> itemEntry : items.entrySet()){
			storage.addDeliveredItem(team, itemEntry.getKey(), itemEntry.getValue());
		}
		items.clear();
	}
	

	public void deactivate() {
		status = CANCELLED;
		transferDelivered(false);
	}
	
	public void activate() {
		status = ACTIVE;
	}
	
	public void setStatus(int s){
		status = s;
	}
	
    public int getStatus(){
        return this.status;
    }
	
	public boolean active(){
		return status == ACTIVE;
	}
	
	public boolean future(){
		return status == FUTURE;
	}
	
	public boolean completed(){
		return status == COMPLETED;
	}
	
	public boolean cancelled(){
		return status == CANCELLED;
	}
	
	abstract public int getReward();
	

	public Map<Item, Integer> getTeamDelivered(String team){
		Map<Item, Integer> teamDelivered;
		if (!itemsDelivered.containsKey(team)){
			teamDelivered = new HashMap<>(2);
			itemsDelivered.put(team, teamDelivered);
		} else {
			teamDelivered = itemsDelivered.get(team);
		}
		return teamDelivered;
	}

}

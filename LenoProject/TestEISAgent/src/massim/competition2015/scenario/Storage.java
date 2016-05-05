/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fschlesinger
 *
 */
public class Storage extends Facility implements Serializable {


	private static final long serialVersionUID = 1L;

	public int totalCapacity;
	public int usedCapacity;
	public int price;
	
	/**
	 * A map from team names to map of items + quantities. This items count
	 * towards filling the capacity of the storage.
	 */
	public Map<String, Map<Item, Integer>> stored;
	
	/**
	 * A map from team names to map of items + quantities. This items does
	 * not affect the capacity of the storage.
	 */
	public Map<String, Map<Item, Integer>> delivered;
	
	
	/**
	 * 
	 */
	public Storage() {
		usedCapacity = 0;
		stored = new HashMap<>(3);
		delivered = new HashMap<>(3);
	}
	
	public int getRemainingCapacity(){
		return totalCapacity - usedCapacity;
	}

	public Map<Item, Integer> getTeamStored(String team){
		Map<Item, Integer> teamStored;
		if (!stored.containsKey(team)){
			teamStored = new HashMap<>(3);
			stored.put(team, teamStored);
		} else {
			teamStored = stored.get(team);
		}
		return teamStored;
	}
	
	public int getItemCount(String team, Item item){
		Map<Item, Integer> storedTeam = getTeamStored(team);
		return getItemCount(team, item, storedTeam);
	}
	
	public boolean addItem(String team, Item item, int amount){
		if (amount*item.volume > getRemainingCapacity()) return false;
		Map<Item, Integer> storedTeam = getTeamStored(team);
		addItem(team, item, amount, storedTeam);
		usedCapacity += amount*item.volume;
		return true;
	}
	
	public boolean retrieveItem(String team, Item item, int amount){
		Map<Item, Integer> storedTeam = getTeamStored(team);
		if (retrieveItem(team, item, amount, storedTeam)){
			usedCapacity -= amount*item.volume;
			return true;
		}
		return false;
	}
	
	//////
	
	private int getItemCount(String team, Item item, 
			Map<Item, Integer> itemsMap){
		if (!itemsMap.containsKey(item)){
			return 0;
		}
		Integer c = itemsMap.get(item);
		if (c == null) return 0;
		return c;
	}
	
	private boolean addItem(String team, Item item, int amount, 
			Map<Item, Integer> itemsMap){
		//if (amount*item.volume > getRemainingCapacity()) return false;
		int current = 0;
		if (itemsMap.containsKey(item)){
			current = itemsMap.get(item);
		}
		itemsMap.put(item, current+amount);
		// usedCapacity += amount*item.volume;
		return true;
	}
	
	private boolean retrieveItem(String team, Item item, int amount, 
			Map<Item, Integer> itemsMap){
		if (!itemsMap.containsKey(item)){
			return false;
		}
		int current = itemsMap.get(item);
		if (current < amount){
			return false;
		}
		if (current == amount){
			itemsMap.remove(item);
		} else {
			itemsMap.put(item, current-amount);
		}
		// usedCapacity -= amount*item.volume;
		return true;
	}
	/////////
	
	
	
	public Map<Item, Integer> getTeamDelivered(String team){
		Map<Item, Integer> teamDelivered;
		if (!delivered.containsKey(team)){
			teamDelivered = new HashMap<>(3);
			delivered.put(team, teamDelivered);
		} else {
			teamDelivered = delivered.get(team);
		}
		return teamDelivered;
	}
	
	public int getDeliveredItemCount(String team, Item item){
		Map<Item, Integer> deliveredTeam = getTeamDelivered(team);
		return getItemCount(team, item, deliveredTeam);
	}
	
	public boolean addDeliveredItem(String team, Item item, int amount){
		Map<Item, Integer> deliveredTeam = getTeamDelivered(team);
		return addItem(team, item, amount, deliveredTeam);
	}
	
	public boolean retrieveDeliveredItem(String team, Item item, int amount){
		Map<Item, Integer> deliveredTeam = getTeamDelivered(team);
		return retrieveItem(team, item, amount, deliveredTeam);
	}
	
}

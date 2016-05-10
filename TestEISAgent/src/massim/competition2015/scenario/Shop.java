/**
 * 
 */
package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author fschlesinger
 *
 */
public class Shop extends Facility implements Serializable {


	private static final long serialVersionUID = 1L;

	public Map<Item, ShopStock> stock;
	
	
	/**
	 * 
	 */
	public Shop() {
		stock = new HashMap<>();
	}
	
	public static class ShopStock implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public Item item;
		public int cost;
		public int amount;
		public int restock;
	}
	
	public int getItemCount(Item item){
		if (!stock.containsKey(item)){
			return 0;
		}
		ShopStock stockItem = stock.get(item);
		return stockItem.amount;
	}
	
	public int getItemCost(Item item){
		if (!stock.containsKey(item)){
			return 0;
		}
		ShopStock stockItem = stock.get(item);
		return stockItem.cost;
	}
	

	public boolean retrieveItem(Item item, int amount){
		if (!stock.containsKey(item)){
			return false;
		}
		ShopStock stockItem = stock.get(item);
		if (stockItem.amount < amount){
			return false;
		}
		stockItem.amount -= amount;
		return true;
	}
	
	public void restock(int step){
		for (Entry<Item, ShopStock> stockEntry : stock.entrySet()){
			ShopStock stockItem = stockEntry.getValue();
			if (stockItem.restock != 0 && step % stockItem.restock == 0){
				stockItem.amount++;
			}
		}
	}
	
}

/**
 * 
 */
package massim.javaagents.agents.util.map;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a route that an agent is to traverse to reach some destination.
 * 
 * @author fschlesinger
 */
public class Route implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private LinkedList<Location> route;

	public Route() {
		route = new LinkedList<>();
	}
	
	public void addPoint(Location loc){
		route.add(loc);
	}
	
	public boolean completed(){
		return route.isEmpty();
	}
	
	public Location advance(int speed){
		Location loc =  null;
		while (speed > 0 && ! route.isEmpty()){
			loc = route.poll();
			speed--;
		}
		return loc;
	}
	
	public int getRouteLength(){
		return route == null? 0: route.size();
	}
	
	public int getRouteDuration(int speed){
		int length = getRouteLength();
		int steps = length / speed;
		steps += length % speed > 0 ? 1:0;
		return steps;
	}

	public List<Location> getWaypoints(){
		List<Location> ret = new LinkedList<>(route);
		return ret;
	}
}

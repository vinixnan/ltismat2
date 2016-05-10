/**
 * 
 */
package massim.javaagents.agents.util.map;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * The class that will implement the CityMap.
 * 
 * @author fschlesinger
 *
 */
public class CityMap implements Serializable {


	private static final long serialVersionUID = 2L;
	
	
	
	private double cellSize;
	private double proximity;
	
//	private Map<Location, List<GraphNode>> intersections;
//	private Map<Location, List<GraphEdge>> cells;
	
	
	/**
	 * 
	 */
	public CityMap(String mapName, double cellSize, double proximity) {
		this.cellSize = cellSize;
		this.proximity = proximity;
		Location.setProximity(proximity);
		GraphHopperManager.init(mapName);
	}
	
	public Route getNewRoute(Location from, Location to, Set<String> permissions){
		if(from == null || to == null) { return null; }
		Route route = null;
		if (permissions.contains("air")) { route = getNewAirRoute(from,to); }
		else if (permissions.contains("road")) { route = getNewCarRoute(from,to); }
		return route;
	}
	
	private Route getNewAirRoute(Location from, Location to){
		Route route = new Route();
		
		double fractions = getLength(from, to) / cellSize;
		
		// double fracSize = getLength(from, to) / fractions;
		
		Location loc = null;
		for (long i = 1; i <= fractions; i++) {
			loc = getIntermediateLoc(from, to, fractions, i);
			route.addPoint(loc);
		}
		if (!to.equals(loc)) { route.addPoint(to); }
		return route;
	}
	
	private Route getNewCarRoute(Location from, Location to){
		
		GHRequest req = new GHRequest(from.getLat(), from.getLon(), to.getLat(), to.getLon())
				.setWeighting("shortest")
				.setVehicle("car");
		GHResponse rsp = GraphHopperManager.getHopper().route(req);

		// first check for errors
		if(rsp.hasErrors()) {
		   // handle them!
		   // rsp.getErrors()
		   return null;
		}
		
		Route route = new Route();

		// points, distance in meters and time in millis of the full path
		PointList pointList = rsp.getPoints();
		// double distance = rsp.getDistance();
		// long millis = rsp.getMillis();
		Iterator<GHPoint3D> pIterator = pointList.iterator();
		if (!pIterator.hasNext()) return null;
		GHPoint prevPoint = pIterator.next();
		
		double remainder = 0;
		Location loc= null;
		while (pIterator.hasNext()){
			GHPoint nextPoint = pIterator.next();
			double length = getLength(prevPoint, nextPoint);
			if (length == 0){
				prevPoint = nextPoint;
				continue;
			}
			
			long i = 0;
			for (; i* cellSize + remainder < length ; i++) {
				loc = getIntermediateLoc(prevPoint, nextPoint, length, i*cellSize+remainder);
				if (!from.equals(loc)) {
					route.addPoint(loc);
				}
			}
			remainder = i* cellSize + remainder - length;
			prevPoint = nextPoint;
		}
		
		if (!to.equals(loc)) { route.addPoint(to); }
		
		return route;
	}
	
	
	public double getLength(Location loc1, Location loc2) {
		return Math.sqrt( Math.pow(loc1.getLon() - loc2.getLon(), 2)
						+ Math.pow(loc1.getLat() - loc2.getLat(), 2));
		
	}
	
	
	public Location getIntermediateLoc(Location loc1, Location loc2, double fractions, long i) {

		double lon = (loc2.getLon() - loc1.getLon())*i/fractions + loc1.getLon();
		double lat = (loc2.getLat() - loc1.getLat())*i/fractions + loc1.getLat();
		return new Location(lon,lat);
	}
	
	
	public double getLength(GHPoint loc1, GHPoint loc2) {
		return Math.sqrt( Math.pow(loc1.getLon() - loc2.getLon(), 2)
						+ Math.pow(loc1.getLat() - loc2.getLat(), 2));
		
	}
	
	public Location getIntermediateLoc(GHPoint loc1, GHPoint loc2, double length, double i) {
		
		double lon = (loc2.getLon() - loc1.getLon())*i/length + loc1.getLon();
		double lat = (loc2.getLat() - loc1.getLat())*i/length + loc1.getLat();
		return new Location(lon,lat);
	}
	
	public Location getNearestRoad(Location loc){

		GHPoint3D snap = GraphHopperManager.getHopper().getLocationIndex().findClosest(loc.getLat(),
				loc.getLon(), EdgeFilter.ALL_EDGES).getSnappedPoint();

		return new Location(snap.getLon(), snap.getLat());
	}
}

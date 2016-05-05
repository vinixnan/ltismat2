package massim.javaagents.agents.util.map;

import java.io.Serializable;

/**
 * Represents a point in the map.
 * @author fschlesinger
 *
 */
public class Location implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * To define when 2 locations can be considered the same.
	 */
	private static double proximity;
	
	/**
	 * Latitude
	 */
	private double lat;
	
	/**
	 * Longitude
	 */
	private double lon;

	public Location(double lon, double lat) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	/*
	public void setLat(double lat) {
		this.lat = lat;
	}
	*/

	public double getLon() {
		return lon;
	}

	/*
	public void setLon(double lon) {
		this.lon = lon;
	}
	*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		
		temp = Math.round(lat/proximity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Math.round(lon/proximity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (Math.round(this.lat/proximity) != Math.round(other.lat/proximity))
			return false;
		if (Math.round(this.lon/proximity) != Math.round(other.lon/proximity))
			return false;
		return true;
	}

	/**
	 * Set the proximity used for comparison.
	 * @param proximity
	 */
	public static void setProximity(double proximity){
		Location.proximity = proximity;
	}

}

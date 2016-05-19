/**
 * 
 */
package massimJacamoProject;

/**
 * Class to calculate distance between two points given in latitude and longitude
 * @author leno
 *
 */
public class DistanceCalculator {
	/**
	 * Calculates the distance in Km between two coodinates 
	 * @param lat1 latitude of point 1
	 * @param lon1 longitude of point 1
	 * @param lat2 latitude of point 2
	 * @param lon2 longitude of point 2
	 * @return the distance
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		String unit = "K"; //Km		
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
}

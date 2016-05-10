package massim.javaagents.agents.util.map;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import java.io.File;
import java.util.Set;

public class GraphHopperManager {

	static private String mapName = null;
	static private GraphHopper hopper = null;
	
	public static void init(String newMapName){
		
		if(newMapName.equals(mapName)){
			return;
		}
		mapName = newMapName;
		hopper = new GraphHopper().forDesktop();
		hopper.setOSMFile("osm" + File.separator + mapName + ".osm.pbf");
		
		// where to store graphhopper files?
		hopper.setGraphHopperLocation("graphs" + File.separator + mapName);
		hopper.setEncodingManager(new EncodingManager(EncodingManager.CAR));

		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		hopper.importOrLoad();	
	}
	
	public static GraphHopper getHopper(){
		return hopper;
	}
}

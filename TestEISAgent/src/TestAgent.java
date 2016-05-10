/**
 * 
 */
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.CityUtil;
/**
 * @author leno
 *
 */
public class TestAgent extends Agent {

	double latDest = -1;
	double lonDest = -1;
	double currLat = -1;
	double currLon = -1;
	
	double latMin = 51.4647;
	double lonMin = -0.1978;
	double latMax = 51.5223;
	double lonMax = -0.0354;
	
	
	public TestAgent(String name, String team) {
		super(name, team);
		// TODO Auto-generated constructor stub
	}

	//This method reads all the perceptions one at each time.
	@Override
	public void handlePercept(Percept arg0) {
		if (arg0.getName().equals("self")){
			System.out.println("FOUND : "+arg0.getParameters().toString()+"\n");
			LinkedList<Parameter> param = arg0.getParameters();
			boolean findLat = false,findLon = false;
			
			int i = 0;
			while(!findLat || !findLon){
				Parameter p = param.get(i);
				if(p.equals("lat")){
					currLat = Float.parseFloat(p.toString());
				}
				if(p.equals("lon")){
					currLon = Float.parseFloat(p.toString());
				}
			}
		}
		
	}

	//This method is executed each time an action is required
	@Override
	public Action step() {
		if(latDest==currLat && lonDest==currLon){
			latDest = -1;
		}
		if(latDest==-1){
			Random rnd= new Random();
			//Generates a new random destination
			latDest = latMin + (rnd.nextDouble()*(latMax-latMin));
			lonDest = lonMin + (rnd.nextDouble()*(latMax-latMin));
		}

		
		
		// Creates a new Action with its parameters. Action specifications are in the documentation.	
		Action action =  CityUtil.action("goto","lat="+latDest+" lon="+lonDest);

		
		return action;
		
		
	}

	
	

	

}

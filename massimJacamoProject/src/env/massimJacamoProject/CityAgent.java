package massimJacamoProject;
import java.util.Hashtable;

import eis.exceptions.AgentException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.eismassim.Entity;
import massim.javaagents.Agent;
import massim.javaagents.agents.CityUtil;

/**
 * 
 */

/**
 * @author leno
 * Class that receives notifications from the server (not related to Jacamo)
 */
public class CityAgent extends Agent {

	CityWorld artifact;
	
	//Should the agent execute next action?
	boolean processAction;
	
	//Variable to store initiated agents and posteriorly use them to link with Jacarta artifacts
	private static Hashtable<String,CityAgent> registeredAgents = new Hashtable<String,CityAgent>();
	
	public CityAgent(String name, String team) {
		super(name, team);
		registeredAgents.put(name,this);
		processAction = false;
	}
	
	/**
	 * Link a jacarta artifact to a CityAgent
	 * @param art the artifact
	 */
	public void registerArtifact(CityWorld art){
		this.artifact = art;
	}
	
	/**
	 * Returns an CityAgent instance related to a given environment name
	 * @param name name
	 * @return the agent
	 */
	public static CityAgent getAgent(String name){
		return registeredAgents.get(name);
	}

	@Override
	public void handlePercept(Percept percep) {
		//The "notifications" options must be ENABLED in the agents xml
		if(percep.getName().equals("requestAction")){
			processAction = true;
		}
		
	}

	@Override
	public Action step() {
		//Wait until the next action is requested
		while(!processAction){			
			try {
				//System.out.println("\n"+this.getAllBeliefs(getName())+"\n");
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Action schedule = artifact.getScheduledAction();
		if(schedule==null){
			return CityWorld.skipAction(this.getName());
		}
		
		artifact.clearSchedule();
		return schedule;
		//return CityUtil.action("goto","lat="+51.4847+" lon="+(-0.0454));
	}

}

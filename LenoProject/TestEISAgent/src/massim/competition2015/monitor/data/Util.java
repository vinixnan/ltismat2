package massim.competition2015.monitor.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides a set of useful utility functions.
 * 

 */
public class Util {


	/**
	 * Finds a team in a collection of teams.
	 *
	 * @param name
	 * @param teams
	 * @return
	 */
	public static TeamInfo searchTeam(String name, Vector<TeamInfo> teams) {
		if (name == null || teams == null) {
			return null;
		}
		for (TeamInfo team : teams) {
			if (team.name.equals(name)) {
				return team;
			}
		}
		return null;
	}



    public static FacilityInfo searchFacility(String selectedFacility, Collection<FacilityInfo> facilities) {
        if (selectedFacility == null || facilities == null) {
            return null;
        }
        for (FacilityInfo fac : facilities) {
            if (fac.name.equals(selectedFacility)) {
                return fac;
            }
        }
        return null;
    }
    
    public static JobInfo searchJob(String selectedJob, Collection<JobInfo> jobs) {
        if (selectedJob == null || jobs == null) {
            return null;
        }
        for (JobInfo job : jobs) {
            if (job.id.equals(selectedJob)) {
                return job;
            }
        }
        return null;
    }
    
    public static AgentInfo searchAgent(String selectedAgent, Collection<AgentInfo> agents) {
        if (selectedAgent == null || agents == null) {
            return null;
        }
        for (AgentInfo agent : agents) {
            if (agent.name.equals(selectedAgent)) {
                return agent;
            }
        }
        return null;
    }

	public static WorldInfo parseXML(Document doc) {
		
		NodeList nl = doc.getElementsByTagName("state");
		Element e = (Element) nl.item(0);
		if(e == null){ // nothing to parse
			return null;
		}
		
		

		WorldInfo wi = new WorldInfo();
		
		wi.simStep = e.getAttribute("step");
		wi.simId = e.getAttribute("simulation");
		wi.minLon = Double.parseDouble(e.getAttribute("min-lon"));
		wi.minLat = Double.parseDouble(e.getAttribute("min-lat"));
		wi.maxLon = Double.parseDouble(e.getAttribute("max-lon"));
		wi.maxLat = Double.parseDouble(e.getAttribute("max-lat"));
		wi.proximity = Double.parseDouble(e.getAttribute("proximity"));
		wi.cellSize = Double.parseDouble(e.getAttribute("cell-size"));

		//parse agents
		wi.agents = new Vector<AgentInfo>();
		wi.agentNames = new TreeSet<String>();
		NodeList agentsNl = doc.getElementsByTagName("agent");
		for (int j = 0; j < agentsNl.getLength(); j++) {
			Element agentEl = (Element) agentsNl.item(j);
			AgentInfo agent = new AgentInfo();

			agent.name = agentEl.getAttribute("name");
			agent.teamName = agentEl.getAttribute("team");
			// agent.team = getTeamNr(agentEl.getAttribute("team"));
			agent.role = agentEl.getAttribute("role");

			agent.lastAction = agentEl.getAttribute("lastAction");
			agent.lastActionParam = agentEl.getAttribute("lastActionParam");
			agent.lastActionResult = agentEl.getAttribute("lastActionResult");

			agent.charge = Integer.parseInt(agentEl.getAttribute("charge"));
			agent.batteryCapacity = Integer.parseInt(agentEl.getAttribute("batteryCapacity"));
			agent.load = Integer.parseInt(agentEl.getAttribute("load"));
			agent.loadCapacity = Integer.parseInt(agentEl.getAttribute("loadCapacity"));
			agent.lat = Double.parseDouble(agentEl.getAttribute("lat"));
			agent.lon = Double.parseDouble(agentEl.getAttribute("lon"));

			agent.products = agentEl.getAttribute("products");

			wi.agents.add(agent);
			wi.agentNames.add(agent.name);
		}

		//parse teams
		wi.teamsInfo = new Vector<TeamInfo>();
		nl = doc.getElementsByTagName("team");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e1 = (Element) nl.item(i);
			TeamInfo team = new TeamInfo();

			team.name = e1.getAttribute("name");
			team.money = Long.parseLong(e1.getAttribute("money"));
			team.jobs = e1.getAttribute("jobs");

			wi.teamsInfo.add(team);
		}

		//parse jobs
		wi.jobs = new Vector<JobInfo>();
		wi.jobsNames = new TreeSet<String>();
		nl = doc.getElementsByTagName("job");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e1 = (Element) nl.item(i);
			JobInfo job = new JobInfo();

			job.id = e1.getAttribute("id");
			job.poster = e1.getAttribute("poster");
			job.reward = Integer.parseInt(e1.getAttribute("reward"));
			job.requiredItems = e1.getAttribute("itemsReq");
			job.deliveredItems = e1.getAttribute("itemsDel");
            job.status = Integer.parseInt(e1.getAttribute("status"));

            wi.jobs.add(job);
            wi.jobsNames.add(job.id);
		}

		//parse facilities
		wi.facilities = new Vector<>();
		wi.facilitiesNames = new TreeSet<>();
		nl = doc.getElementsByTagName("facility");
		for(int i = 0; i < nl.getLength(); i++){
			Element e1 = (Element) nl.item(i);
			FacilityInfo fac = new FacilityInfo();

			fac.name = e1.getAttribute("name");
			fac.lat = Double.parseDouble(e1.getAttribute("lat"));
			fac.lon = Double.parseDouble(e1.getAttribute("lon"));
			fac.type = e1.getAttribute("type");

            switch(fac.type){
                case "CH":
                    FacilityInfo.ChargingStationInfo inf = new FacilityInfo.ChargingStationInfo();
                    inf.fuelPrice = Integer.parseInt(e1.getAttribute("fPrice"));
                    inf.maxConcurrent = Integer.parseInt(e1.getAttribute("concurrent"));
                    inf.qSize = Integer.parseInt(e1.getAttribute("q"));
                    inf.rate = Integer.parseInt(e1.getAttribute("rate"));

                    fac.extra = inf;
                    break;
                case "DU":
                    FacilityInfo.DumpLocationInfo du = new FacilityInfo.DumpLocationInfo();
                    du.price = Integer.parseInt(e1.getAttribute("price"));

                    fac.extra = du;
                    break;
                case "SH":
                    FacilityInfo.ShopInfo sh = new FacilityInfo.ShopInfo();
                    sh.stock = e1.getAttribute("stock");

                    fac.extra = sh;
                    break;
                case "ST":
                    FacilityInfo.StorageInfo st = new FacilityInfo.StorageInfo();
                    st.price = Integer.parseInt(e1.getAttribute("price"));
                    st.capacity = e1.getAttribute("capacity");
                    st.stored = e1.getAttribute("stored");

                    fac.extra = st;
                    break;
                case "WS":
                    FacilityInfo.WorkshopInfo ws = new FacilityInfo.WorkshopInfo();
                    ws.price = Integer.parseInt(e1.getAttribute("price"));

                    fac.extra = ws;
                    break;
                default:
            }
			
            wi.facilities.add(fac);
            wi.facilitiesNames.add(fac.name);
		}
		
		return wi;
	}
}
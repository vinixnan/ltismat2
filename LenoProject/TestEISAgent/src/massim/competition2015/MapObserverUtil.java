package massim.competition2015;

import massim.competition2015.scenario.*;
import massim.framework.SimulationState;
import massim.framework.simulation.SimulationStateImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Created by ta10 on 10.04.15.
 *
 * Used for RMIXML Observer (Monitor) and XML Observer (FileViewer)
 */
public class MapObserverUtil {

    /**
     * Appends the simulation "state" node to the supplied doc.
     * Intended for XML Observer and RMIXML Observer.
     *
     * @param doc the doc to append to
     * @param simstate the simulation state
     */
    static void appendState(Document doc, SimulationState simstate){

        Element el_root = doc.getDocumentElement();

        SimulationStateImpl simplestate = (SimulationStateImpl) simstate;
        MapSimulationWorldState worldState = (MapSimulationWorldState) simplestate.simulationState;

        Element elState = doc.createElement("state");
        elState.setAttribute("simulation", worldState.simulationName);

        elState.setAttribute("step", Integer.toString(simplestate.steps));

        elState.setAttribute("number-of-steps",""+worldState.maxNumberOfSteps);

        // map parameters
        elState.setAttribute("min-lat",""+worldState.minLat);
        elState.setAttribute("min-lon",""+worldState.minLon);
        elState.setAttribute("max-lat",""+worldState.maxLat);
        elState.setAttribute("max-lon",""+worldState.maxLon);
        elState.setAttribute("proximity",""+worldState.proximity);
        elState.setAttribute("cell-size",""+worldState.cellSize);


        //add Map information
        /*
        Element elNodes = doc.createElement("nodes");
        for(GraphNode n: worldState.cityMap.getNodes()){
            Element elNode = doc.createElement("node");
            elNode.setAttribute("name", n.name);
            elNode.setAttribute("x", String.valueOf(n.x));
            elNode.setAttribute("y", String.valueOf(n.y));
            elNodes.appendChild(elNode);
        }
        elState.appendChild(elNodes);

        Element elEdges = doc.createElement("edges");
        for(GraphEdge e: worldState.cityMap.getAllEdges()){
            Element elEdge = doc.createElement("edge");
            elEdge.setAttribute("type", String.valueOf(e.getType()));
            elEdge.setAttribute("x1", String.valueOf(e.getNode1().x));
            elEdge.setAttribute("y1", String.valueOf(e.getNode1().y));
            elEdge.setAttribute("x2", String.valueOf(e.getNode2().x));
            elEdge.setAttribute("y2", String.valueOf(e.getNode2().y));
            elEdges.appendChild(elEdge);
        }
        elState.appendChild(elEdges);
        */

        //add Facilities to the document
        Element elFacilities = doc.createElement("facilities");
        for (Facility f: worldState.getAllFacilities()){
            Element elFacility = doc.createElement("facility");
            elFacility.setAttribute("name", f.name);
            elFacility.setAttribute("lat", String.valueOf(f.location.getLat()));
            elFacility.setAttribute("lon", String.valueOf(f.location.getLon()));
            String type = "type";
            if(f instanceof ChargingStation){
                elFacility.setAttribute(type, "CH");
                ChargingStation ch = (ChargingStation)f;
                elFacility.setAttribute("fPrice", String.valueOf(ch.fuelPrice));
                elFacility.setAttribute("rate", String.valueOf(ch.chargingRate));
                elFacility.setAttribute("q", String.valueOf(ch.getQueueSize()));
                elFacility.setAttribute("concurrent", String.valueOf(ch.maxConcurrentCharging));
            }
            else if(f instanceof DumpLocation){
                elFacility.setAttribute(type, "DU");
                DumpLocation du = (DumpLocation)f;
                elFacility.setAttribute("price", String.valueOf(du.price));
            }
            else if(f instanceof Shop){
                elFacility.setAttribute(type, "SH");
                Shop sh = (Shop)f;
                elFacility.setAttribute("stock", formatShopItemList(sh.stock));
            }
            else if(f instanceof Storage){
                elFacility.setAttribute(type, "ST");
                Storage st = (Storage)f;
                elFacility.setAttribute("capacity", st.usedCapacity+"/"+st.totalCapacity);
                elFacility.setAttribute("price", String.valueOf(st.price));
                elFacility.setAttribute("stored", formatTeamStored(st.stored));
            }
            else if(f instanceof Workshop){
                elFacility.setAttribute(type, "WS");
                Workshop ws = (Workshop)f;
                elFacility.setAttribute("price", String.valueOf(ws.price));
            }
            else{
                elFacility.setAttribute(type, "??");
            }
            elFacilities.appendChild(elFacility);
        }
        elState.appendChild(elFacilities);

        //add Agents to the document
        Element elAgents = doc.createElement("agents");
        for(MapSimulationAgentState agent: worldState.agents){
            Element elAgent = doc.createElement("agent");
            elAgent.setAttribute("name", agent.name);
            elAgent.setAttribute("lastAction", agent.lastAction);
            elAgent.setAttribute("lastActionParam", agent.lastActionParam);
            elAgent.setAttribute("lastActionResult", agent.lastActionResult);
            elAgent.setAttribute("role", agent.roleName);
            elAgent.setAttribute("team", agent.team);
            elAgent.setAttribute("charge", String.valueOf(agent.getBatteryCharge()));
            elAgent.setAttribute("batteryCapacity", String.valueOf(agent.role.batteryCapacity));
            elAgent.setAttribute("load", String.valueOf(agent.load));
            elAgent.setAttribute("loadCapacity", String.valueOf(agent.role.loadCapacity));
            elAgent.setAttribute("lon", String.valueOf(agent.location.getLon()));
            elAgent.setAttribute("lat", String.valueOf(agent.location.getLat()));
            elAgent.setAttribute("products", formatItemList(agent.products));

            elAgents.appendChild(elAgent);
        }
        elState.appendChild(elAgents);

        //add Jobs to the document
        Element elJobs = doc.createElement("jobs");
        for (Job j: worldState.getAllJobs()){
            Element elJob = doc.createElement("job");
            elJob.setAttribute("id", j.id);
            elJob.setAttribute("poster", j.poster);
            elJob.setAttribute("reward", String.valueOf(j.getReward()));
            elJob.setAttribute("itemsReq", formatItemList(j.itemsRequired));
            elJob.setAttribute("itemsDel", formatTeamStored(j.itemsDelivered));
            elJob.setAttribute("status", String.valueOf(j.getStatus()));
            elJobs.appendChild(elJob);
        }
        elState.appendChild(elJobs);

        //add Teams info
        Element elTeams = doc.createElement("teams");
        for (TeamState team : worldState.teamsStates) {
            // Info about team
            Element elTeam = doc.createElement("team");
            elTeam.setAttribute("name", team.name);
            elTeam.setAttribute("money", String.valueOf(team.money));
            elTeam.setAttribute("jobs", formatJobsList(team.takenJobs));

            elTeams.appendChild(elTeam);
        }
        elState.appendChild(elTeams);

        el_root.appendChild(elState);
    }

    private static String formatTeamStored(Map<String, Map<Item, Integer>> stored) {
        String ret = "";

        for(Map.Entry<String, Map<Item, Integer>> e: stored.entrySet()){
            ret += "Team " + e.getKey() + ":\n";
            for(Map.Entry<Item, Integer> e2: e.getValue().entrySet()){
                ret += e2.getValue() + "x " + e2.getKey().name + "\n";
            }
        }

        return ret;
    }

    private static String formatShopItemList(Map<Item, Shop.ShopStock> stock) {
        String ret = "";

        for(Map.Entry<Item,Shop.ShopStock> e: stock.entrySet()){
            if(e.getKey() == null || e.getValue() == null) continue;
            ret += e.getValue().amount + "x " + e.getKey().name + " @ " + e.getValue().cost + "\n";
        }

        return ret;
    }

    private static String formatJobsList(Set<Job> jobs) {
        String ret = "";
        for (Job j: jobs){
            ret += j.id + " ";
        }
        return ret;
    }

    private static String formatItemList(Map<Item, Integer> items) {
        String ret = "";
        for(Map.Entry<Item, Integer> e: items.entrySet()){
            ret += e.getValue() + "x " + e.getKey().name + "\n ";
        }
        return ret;
    }
}

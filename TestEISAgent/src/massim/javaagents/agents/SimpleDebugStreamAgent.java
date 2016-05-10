package massim.javaagents.agents;

import com.graphhopper.storage.Graph;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.util.map.CityMap;
import massim.javaagents.agents.util.map.GraphHopperManager;
import massim.javaagents.agents.util.map.Location;
import massim.javaagents.agents.util.map.Route;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ta10 on 09.04.15.
 */
public class SimpleDebugStreamAgent extends Agent {

    private FileWriter out;
    private BufferedReader in;

    private int lastStep = -1;
    private Action lastAction;
    private int simCounter = 1;

    private String name = "";

    private File logpath;
    private File logfile;
    private File actionconf;


    private String map = "";
    static CityMap cityMap;
    private double lon = 0.0d;
    private double lat = 0.0d;

    private int routeLen = 0;

    public static long time = System.currentTimeMillis()/1000; //make sure all agents log to the same folder

    /**
     * Initializes an agent with a given name. Ensures that the name is unique.
     *
     * @param name is the name of the agent
     * @param team is the team of the agent
     */
    public SimpleDebugStreamAgent(String name, String team) {

        super(name, team);

        this.name = name;

        createIO();
    }

    private void createIO() {
        logpath = new File("perceptlog/"+time);
        logfile = new File(logpath+"/"+name+"-"+simCounter);
        actionconf = new File("actionconf/"+name+"-"+simCounter);

        logpath.mkdirs();

        if(!actionconf.exists()) try {
            actionconf.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!logfile.exists()) try {
            logfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            out = new FileWriter(logfile);
            out.write(name + " starting log at " + System.currentTimeMillis());

            in = new BufferedReader(new FileReader(actionconf));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action step() {

        boolean newStep = true;

        int pricedJobs = 0;
        int auctionJobs = 0;

        //log percepts
        if(out != null){

            try {
                out.write("Log: ");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Collection<Percept> percepts = getAllPercepts();

            for(Percept p: percepts){
                try {
                    out.write(p.toProlog());
//                	out.write(p.toXML());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(p.getName().equals("step")){
                    int step = new Integer(p.getParameters().get(0).toProlog()).intValue();

                    if(step < lastStep){
                        simCounter++;
                        createIO();
                    }
                    else if(step == lastStep){
                        newStep = false;
                    }
                    lastStep = step;
                }

                else if(p.getName().equalsIgnoreCase("map")){
                    map = p.getParameters().get(0).toString();
                }

                else if(p.getName().equalsIgnoreCase("lon")){
                    lon = Double.parseDouble(p.getParameters().get(0).toProlog());
                }
                else if(p.getName().equalsIgnoreCase("lat")){
                    lat = Double.parseDouble(p.getParameters().get(0).toProlog());
                }

                else if(p.getName().equalsIgnoreCase("routeLength")){
                    routeLen = Integer.parseInt(p.getParameters().get(0).toProlog());
                }
                else{
//                    println(p.getName());
                    if(p.getName().equalsIgnoreCase("pricedjob")){
                        pricedJobs++;
                    }
                    else if(p.getName().equalsIgnoreCase("auctionjob")){
                        auctionJobs++;
                    }
                }

            }
            try {
                out.write("\n \n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //launch graphhopper if possible
        if(lastStep==1 && !map.equals("") && this.name.equalsIgnoreCase("agentA1")){
            //FIXME change this if cellsize and/or proximity change
            cityMap = new CityMap(map, 0.001, 0.0002);
            Set<String> perms = new HashSet<>();
            perms.add("road");
            Route route = cityMap.getNewRoute(
                    new Location(lon, lat), new Location(-0.1368d,51.4872d), perms);

            println("Found route of length " + route.getRouteDuration(1));
        }

        //FIXME
        if(this.name.equalsIgnoreCase("agentA10")){
            println("Jobs: "+pricedJobs+" "+auctionJobs);
        }
        if(this.name.equals("agentB10")){
            println("My route: "+routeLen);
            return CityUtil.action("goto", "facility=dump5");
        }
        if(this.name.equals("agentA14")){
            println("My route: "+routeLen);
            return CityUtil.action("goto", "facility=dump2");
        }

        if(this.name.equals("agentA12")){
            if(lastStep < 30) {
                return CityUtil.action("call_breakdown_service");
            }
        }
        else{
            return CityUtil.action("skip");
        }

        //get action and execute it
        if(in != null && newStep){
            String action, name = "", param = "";
            try {

                if ((action = in.readLine()) == null) {
                    in.close();
                    in = null;
                    return lastAction=CityUtil.action("skip");
                }
                else{
                	int idx = action.indexOf(" ");
                	if(idx != -1){
                		name = action.substring(0, idx);
                		param = action.substring(idx+1);
                        println(getName()+": Executing: "+name+" "+param);
                        return lastAction=CityUtil.action(name, param);
                    }
                    else{
                    	name = action;
                        println(getName()+": Executing: "+name);
                        return lastAction=CityUtil.action(name);
                    }
                	
                	/*
                    String[] parts = action.split(" ");
                    name = parts[0];
                    if(parts.length > 1){
                        param = parts[1];
                        System.out.println(getName()+": Executing: "+name+" "+param);
                        return CityUtil.action(name, param);
                    }
                    else{
                        System.out.println(getName()+": Executing: "+name);
                        return CityUtil.action(name);
                    }
                    */
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{ //no new step or input closed, retry latest action
            if (lastAction != null) {
                println("retry last action");
                return lastAction;
            }
        }

        return lastAction=CityUtil.action("skip");
    }

    @Override
    public void handlePercept(Percept p) {}
}

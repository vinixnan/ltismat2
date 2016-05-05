package massim.server;


import static massim.framework.util.DebugLog.LOGLEVEL_CRITICAL;
import static massim.framework.util.DebugLog.LOGLEVEL_DEBUG;
import static massim.framework.util.DebugLog.LOGLEVEL_ERROR;
import static massim.framework.util.DebugLog.LOGLEVEL_NORMAL;
import static massim.framework.util.DebugLog.log;
import static massim.framework.util.DebugLog.setLogLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import massim.framework.AgentManager;
import massim.framework.AgentProviderAgentManager;
import massim.framework.ArrayAgentProvider;
import massim.framework.Component;
import massim.framework.TeamAgentFilter;
import massim.framework.connection.InetSocketListener;
import massim.framework.connection.UsernamePasswordSocketLoginManager;
import massim.framework.rmi.RMI_DefaultProperties;
import massim.framework.rmi.XMLDocumentServer;
import massim.framework.simulation.AbstractSimulation;
import massim.framework.util.DebugLog;
import massim.framework.util.XMLUtilities;
import massim.test.InvalidConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Server extends AbstractServer {
	public final int AGENT_PORT_DEFAULT=12300; 
	public final int AGENT_BACKLOG_DEFAULT=10;

	public int  score[];
	protected InetSocketListener socketlistener;
	protected ServerInetSocketListener serverinetsocketlistener;
	protected ServerSimulationAgents serversimulationagents;
	protected UsernamePasswordSocketLoginManager loginsocketmanager;
	protected ArrayAgentProvider arrayagentprovider;
	protected AgentManager agentmanager;
	protected TeamAgentFilter teamagentfilter;
	protected Registry rmiregistry;
	protected RMIServerStatus rmiinfoserver2;
	protected RMITournamentServer rmitournamentserver;
	protected Document xmlTournamentReport; 
	protected File xmlTournamentReportFile;
	protected List<String> manual;

	protected String tournamentname;
	
	protected LaunchSync launchSync;
	
	protected int tournamentmode;
	public HashMap<String, List<String>> team_member = new HashMap<>();
	
	private enum Serverstatus {
		NOTCONFIGURED, CONFIGURED, SIMSTART, SIMSTOP, SIMEND
	}
	
	private Serverstatus serverstatus;
	
	interface LaunchSync {
		void waitForStart();
	}
	
	class TimerLaunchSync implements LaunchSync {
		long time;
		public TimerLaunchSync(Element e) {
			this.time = Long.parseLong(e.getAttribute("time-to-launch"));
		}
		public void waitForStart() {
			try {
				log(LOGLEVEL_NORMAL,"The tournament will start in " + this.time + " milliseconds");
				Thread.sleep(this.time);
			} catch (InterruptedException e) {}
		}
	}
	
	class KeyLaunchSync implements LaunchSync {
		public void waitForStart() {
			try {
				log(LOGLEVEL_NORMAL,"Please press ENTER to start the tournament.");
				System.in.read();
			} catch (IOException e) {}
		}
	}
	
	class LaunchAtTimeSync implements LaunchSync {
		long time = 0;
		long diffTime = 0;
		public LaunchAtTimeSync(Element e) {
			DateFormat timeFormat;
			timeFormat = new SimpleDateFormat("HH:mm");
			Calendar cal = Calendar.getInstance();
			log(LOGLEVEL_NORMAL, "Current time is: " + cal.getTime().toString());
			try {				
				Calendar startDate = Calendar.getInstance();
				Date d = timeFormat.parse(e.getAttribute("time"));
				startDate.setTime(d);
				int hourOfDay = startDate.get(Calendar.HOUR_OF_DAY);
				int minute = startDate.get(Calendar.MINUTE);;
				startDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), hourOfDay, minute);
				log(LOGLEVEL_NORMAL,"Starting time: " + startDate.getTime().toString());
				this.time = startDate.getTimeInMillis();
				this.diffTime = this.time - cal.getTimeInMillis();
				if (this.diffTime < 0) {
					this.diffTime = 0;
				}
			} catch (ParseException e1) {
				log(LOGLEVEL_CRITICAL, "Error while parsing start time");
				System.exit(1);
			}
		}
		public void waitForStart() {
			try {
				log(LOGLEVEL_NORMAL, "The server will start in: " + this.diffTime + " milliseconds from now on");
				Thread.sleep(this.diffTime);
			} catch (InterruptedException e) {}
		}
		
	}
	
	private class RMITournamentServer extends UnicastRemoteObject implements XMLDocumentServer {
		private static final long serialVersionUID = 6468903192414320442L;

		public RMITournamentServer() throws RemoteException {
			super();
		}

		public Document getXMLDocument() {
			return Server.this.xmlTournamentReport;//FIXME: missing synchronization
		}
	}
	
	public Server(){
		this.serverstatus = Serverstatus.NOTCONFIGURED;
	}

	public Server(String[] args) throws InvalidConfigurationException {
		Element conf = this.parseCommandLineToConfig(args);
		this.config(conf);
		this.serverstatus = Serverstatus.CONFIGURED;
		log(LOGLEVEL_DEBUG, "Server configured.");
	}
	
public void config(Element xmlconfiguration) throws InvalidConfigurationException {
		
		this.setDebugLevel(xmlconfiguration);
		this.createTeamInfor(xmlconfiguration);
		
		log(LOGLEVEL_NORMAL,"Server launched.");
		this.tournamentname = xmlconfiguration.getAttribute("tournamentname");
		this.tournamentmode = Integer.parseInt(xmlconfiguration.getAttribute("tournamentmode"));

		//create socket listener
		this.serverinetsocketlistener = new ServerInetSocketListener((Element)XMLUtilities.getChildsByTagName(xmlconfiguration, "simulation-server").item(0));
		this.socketlistener = this.serverinetsocketlistener.object;
	
		//read rmiport and host
		this.readRMIinfor(xmlconfiguration);
		
		
		//read account list
		NodeList nl=XMLUtilities.getChildsByTagName(xmlconfiguration, "accounts");
		this.serversimulationagents = new ServerSimulationAgents((Element) nl.item(0)); 
		
		//TODO write a better parser
		NodeList nl1 = XMLUtilities.getChildsByTagName(xmlconfiguration,"match");
		if (nl1.getLength()!=1) {
			log(LOGLEVEL_CRITICAL,"simulation configuration invalid");
			System.exit(1);
		}
		this.el_match = (Element) nl1.item(0);
		
		//create UsernamePasswordSocketLoginManager 
		this.loginsocketmanager = new UsernamePasswordSocketLoginManager(this.serversimulationagents.accounts,this.serversimulationagents.accountSocketHandlerMap);

		//connect loginsocketmanager with socketlistener
		this.socketlistener.setSocketHandler(this.loginsocketmanager);
		
		//create arrayagentprovider
		this.arrayagentprovider = new ArrayAgentProvider(this.serversimulationagents.agents);

		//create team filter
		this.teamagentfilter = new TeamAgentFilter(this.serversimulationagents.accountTeamMap,this.arrayagentprovider);
		
		//create agent manager, based on teamagentfilter
		this.agentmanager = new AgentProviderAgentManager(this.teamagentfilter);
		
		//create launch sync
		String launchsynctype = xmlconfiguration.getAttribute("launch-sync-type");
		if (launchsynctype.equalsIgnoreCase("key")) {
			this.launchSync = new KeyLaunchSync();
		} else if (launchsynctype.equalsIgnoreCase("timer")) {
			this.launchSync = new TimerLaunchSync(xmlconfiguration);
		} else if (launchsynctype.equalsIgnoreCase("time")) {
			this.launchSync = new LaunchAtTimeSync(xmlconfiguration);
		}

		//2015: create dir if it doesn't exist
		File reportDir = new File(xmlconfiguration.getAttribute("reportpath"));
		if(!reportDir.exists()) reportDir.mkdirs();

		this.xmlTournamentReportFile = new File(xmlconfiguration.getAttribute("reportpath"), this.tournamentname + "_report.xml");
		//2015: create file if it doesn't exist
		if(this.xmlTournamentReportFile.exists()) try {
			this.xmlTournamentReportFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	Calendar cal = Calendar.getInstance();
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		try {
			DebugLog.setLogFile(new File(xmlconfiguration.getAttribute("reportpath"), df.format(cal.getTime())+"_"+
					InetAddress.getLocalHost().getHostName() + ".log"));
		} catch (UnknownHostException e1) {
//			e1.printStackTrace();
		}

		if (this.tournamentmode == 2) {

			this.manual = new LinkedList<>();

			try {

				NodeList nl2 = XMLUtilities.getChildsByTagName(xmlconfiguration,"manual-mode");
				Element el_match = (Element) nl2.item(0);
				NodeList matches = el_match.getElementsByTagName("match");

				for (int i = 0; i < matches.getLength(); i++) {
					Element match = (Element) matches.item(i);
					this.manual.add(match.getAttribute("team1") + "VS" + match.getAttribute("team2"));
				}
			}

			catch (Exception e) {
				log(LOGLEVEL_CRITICAL, "manual mode configuration invalid");
				System.exit(1);
			}
		}
		this.serverstatus = Serverstatus.CONFIGURED;
	}

/**
 * This method searches in the xml configuration file for the attribute "debug-level" and sets the verbosity of the debug shell accordingly.
 * If no attribute is found then debug-level="normal" is assumed.
 * @param xmlconfiguration The xml configuration document.
 */
private void setDebugLevel(Element xmlconfiguration) {
	String debugLevel = xmlconfiguration.getAttribute("debug-level");
	if (debugLevel.isEmpty()) {
		debugLevel = "normal";
	}
	setLogLevel(debugLevel);
	log(LOGLEVEL_NORMAL, "Debug-level is set to " + debugLevel);
}

private void createTeamInfor(Element xmlconfiguration) {
	
	 this.team_member = new HashMap<>();
	NodeList nl = xmlconfiguration.getElementsByTagName("account");

	for(int i = 0; i< nl.getLength(); i++){
		Element el = (Element) nl.item(i);
		String team = el.getAttribute("team");
		
		if(!this.team_member.containsKey(team)){
			List<String> member = new Vector<>();
			this.team_member.put(team, member);
		}
		List<String> member = this.team_member.get(team);
		
		member.add(el.getAttribute("username"));
	}
}

/**
 * This method reads information for rmi service (for the monitor) from conf element.    
 * @param conf in xml document. 
 */
	private void readRMIinfor(Element conf) {
		log(LOGLEVEL_DEBUG, "Read RMI properties.");
		NodeList nl = conf.getElementsByTagName("simulation");
		Element simulation=(Element) nl.item(0);
		RMI_DefaultProperties.RMI_HOST_DEFAULT = simulation.getAttribute("rmixmlobsserverhost");
		RMI_DefaultProperties.RMI_PORT_DEFAULT = Integer.parseInt(simulation.getAttribute("rmixmlobsserverport"));
	}

	protected ServerSimulationRun2 sr;
	protected Element el_match;
	private String[] team;
	private HashMap<String, Integer> team_score=null;
	
	public void runMatch(Element el_match, Map<String, String> teammap, String name, Node statmatchparent) throws InvalidConfigurationException {
		NodeList nl = XMLUtilities.getChildsByTagName(el_match,"simulation");

		Document statdoc = statmatchparent.getOwnerDocument();
		
		Element el_statmatch=statdoc.createElement("match");
		
		for (String a : teammap.keySet()) {
			el_statmatch.setAttribute(a, teammap.get(a));
		}
		
		statmatchparent.appendChild(el_statmatch);
		for (int i=0;i<nl.getLength();i++) {
			
			Element simuconfig = (Element) nl.item(i);
			String simname = this.tournamentname+"_";
			String[] teams=teammap.values().toArray(new String[teammap.size()]);
			for (int j=0;j<teams.length;j++) {
				simname += teams[j];
			}
			simname +="_"+simuconfig.getAttribute("id");
			this.sr=new ServerSimulationRun2(simuconfig,teammap,simname,this.tournamentname);
			this.sr.setAgentmanager(this.agentmanager);
			long starttime = System.currentTimeMillis();
			
			this.sr.runSimulation();
			long endtime = System.currentTimeMillis();
			Element n = this.sr.xmlstatisticsobserver.getDocument().getDocumentElement();
			Element el_statsim = statdoc.createElement("simulation");
			Element el_simresult = statdoc.createElement("result");
			
			el_statsim.setAttribute("starttime",Long.toString(starttime));
			el_statsim.setAttribute("endtime",Long.toString(endtime));
			el_statsim.setAttribute("name",simname);
			
			el_statsim.appendChild(el_simresult);
			
			el_statmatch.appendChild(el_statsim);
			el_statmatch.getOwnerDocument().adoptNode(n);
			
			for (int j=0;j<n.getAttributes().getLength();j++) {
				Attr a = (Attr) n.getAttributes().item(j);
				el_simresult.setAttribute(a.getName(),a.getValue());
			}
//			for (int j=0;j<n.getChildNodes().getLength();j++) {
//				el_simresult.appendChild(n.getChildNodes().item(j));
//			}
			int k = n.getChildNodes().getLength(); //the length changes during the loop
			for (int j = 0; j < k; j++) {
				el_simresult.appendChild(n.getChildNodes().item(0)); 
			}
		}
	}
	public int[] runMatch(Element el_match, Map<String, String> teammap,
			String name, Node statmatchparent, String team1Name,
			String team2Name) throws InvalidConfigurationException {

		NodeList nl = XMLUtilities.getChildsByTagName(el_match, "simulation");
		Document statdoc = statmatchparent.getOwnerDocument();
		Element el_statmatch = statdoc.createElement("match");

		int[] score = { 0, 0 };

		for (String a : teammap.keySet()) {
			el_statmatch.setAttribute(a, teammap.get(a));
		}

		statmatchparent.appendChild(el_statmatch);

		for (int i = 0; i < nl.getLength(); i++) {
			Element simuconfig = (Element) nl.item(i);
			String simname = this.tournamentname + "_";
			String[] teams = teammap.values().toArray(
					new String[teammap.size()]);

			for (int j = 0; j < teams.length; j++) {
				simname += teams[j];
			}

			simname += "_" + simuconfig.getAttribute("id");
			this.sr = new ServerSimulationRun2(simuconfig, teammap, simname,this.tournamentname);
			this.sr.setAgentmanager(this.agentmanager);

			long starttime = System.currentTimeMillis();
			String winner = this.sr.runSimulation();
			long endtime = System.currentTimeMillis();

			if (winner.equals(team1Name)) {
				score[0] += 3;
			}
			else if (winner.equals(team2Name)) {
				score[1] += 3;
			}
			else if (winner.equals("draw")) {
				score[0] += 1;
				score[1] += 1;
			}
			else{ //special case for map simulation (2015 and on)
				if (winner.startsWith("MAP127569")){
					String[] mapWinData = winner.split(":");
					if(mapWinData.length != 3){
						System.out.println("Something went terribly wrong. Make sure to add score manually!");
					}
					winner = mapWinData[1];
					boolean earnedMoney = Boolean.valueOf(mapWinData[2]);
					int points = earnedMoney? 3: 2;
					if (winner.equals(team1Name)) {
						score[0] += points;
					}
					else if (winner.equals(team2Name)) {
						score[1] += points;
					}
				}
			}

			log(LOGLEVEL_NORMAL,"######################### Results #########################");
			log(LOGLEVEL_NORMAL,"Winner of " + simname + " : " + winner);
			log(LOGLEVEL_NORMAL,team1Name + ": " + score[0]);
			log(LOGLEVEL_NORMAL,team2Name + ": " + score[1]);
		
			

			if (this.sr.xmlstatisticsobserver != null) {
				Element n = this.sr.xmlstatisticsobserver.getDocument().getDocumentElement();
				Element el_statsim = statdoc.createElement("simulation");
				Element el_simresult = statdoc.createElement("result");

				el_statsim.setAttribute("starttime", Long.toString(starttime));
				el_statsim.setAttribute("endtime", Long.toString(endtime));
				el_statsim.setAttribute("name", simname);

				el_statsim.appendChild(el_simresult);

				el_statmatch.appendChild(el_statsim);
				el_statmatch.getOwnerDocument().adoptNode(n);

				for (int j = 0; j < n.getAttributes().getLength(); j++) {
					Attr a = (Attr) n.getAttributes().item(j);
					el_simresult.setAttribute(a.getName(), a.getValue());
				}
//				for (int j=0;j<n.getChildNodes().getLength();j++) {
//					el_simresult.appendChild(n.getChildNodes().item(j));
//				}
				int k = n.getChildNodes().getLength(); //the length changes during the loop
				for (int j = 0; j < k; j++) {
					 // get item at position 0 is possible (and necessary) because the number 
					 // of childNodes decreases
					el_simresult.appendChild(n.getChildNodes().item(0)); 
				}
				
			}
			
		}

		return score;
	}

	
	public void run() throws InvalidConfigurationException {
		//initialize xml report
		this.createRMI();
		
		this.serverstatus = Serverstatus.SIMSTART;
		this.score = new int[this.serversimulationagents.teams.length];
		this.team = new String[this.serversimulationagents.teams.length];
		this.team_score = new HashMap<>();
		
		for (int i = 0; i < this.serversimulationagents.teams.length; i++) {
			this.team[i] = this.serversimulationagents.teams[i];
			this.team_score.put(this.team[i], this.score[i]);
		}
		
		try {
			this.xmlTournamentReport = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Element report_el_root = this.xmlTournamentReport.createElement("tournament");
		report_el_root.setAttribute("tournament-name", this.tournamentname);
		

		for (int i = 0; i < this.serversimulationagents.teams.length; i++) {	
			Element el_team = this.xmlTournamentReport.createElement("team");
			el_team.setAttribute("name",this.serversimulationagents.teams[i]);
			report_el_root.appendChild(el_team);
		}
		
		this.xmlTournamentReport.appendChild(report_el_root); 
		
		this.agentmanager.start();
		
		for (int i=0;i<this.serversimulationagents.agents.length;i++) 
			((Component)this.serversimulationagents.agents[i]).start();
		
		this.socketlistener.start();
		//start RMI server info component
		
		try {
			//create RMIInfoServer
			// This is needed for the tomcat connection page
			this.rmiinfoserver2 = new RMIServerStatus(this.serversimulationagents);
			log(LOGLEVEL_DEBUG, "RMI HOST: "+ RMI_DefaultProperties.RMI_HOST_DEFAULT);
			log(LOGLEVEL_DEBUG, "RMI PORT: "+ RMI_DefaultProperties.RMI_PORT_DEFAULT);
			Registry r = LocateRegistry.getRegistry(RMI_DefaultProperties.RMI_HOST_DEFAULT, RMI_DefaultProperties.RMI_PORT_DEFAULT);
			r.rebind("server2",this.rmiinfoserver2);
		
		} catch (RemoteException e) {
			log(LOGLEVEL_ERROR,"Error: couldn't bind RMIInfoServer");
			e.printStackTrace();
			System.exit(1);
		}

		try { 
			//create RMIXMLServer
			//This is needed for the tomcat results page
			this.rmitournamentserver = new RMITournamentServer();
			Registry r = LocateRegistry.getRegistry(RMI_DefaultProperties.RMI_HOST_DEFAULT, RMI_DefaultProperties.RMI_PORT_DEFAULT);
			r.rebind("statistics",this.rmitournamentserver);
		
		} catch (RemoteException e) {
			log(LOGLEVEL_ERROR,"Error: couldn't bind RMIInfoServer");
			e.printStackTrace();
			System.exit(1);
		} 

		// launch synchronization
		this.launchSync.waitForStart();
		switch (this.tournamentmode) {
		case 0:
			
			/*
			 * Tournament mode: each team plays against each other team
			 */
			for (int t1 = 0; t1 < this.serversimulationagents.teams.length; t1++) {
				for (int t2 = t1 + 1; t2 < this.serversimulationagents.teams.length; t2++) {

					log(LOGLEVEL_NORMAL, "now playing: "
							+ this.serversimulationagents.teams[t1] + " vs "
							+ this.serversimulationagents.teams[t2]);
					Map<String, String> m = new HashMap<>();
					m.put("red", this.serversimulationagents.teams[t1]);
					m.put("blue", this.serversimulationagents.teams[t2]);
					
					int[] a = runMatch(this.el_match, m,
							this.serversimulationagents.teams[t1] + "_VS_"
									+ this.serversimulationagents.teams[t2],
							report_el_root, this.serversimulationagents.teams[t1],
							this.serversimulationagents.teams[t2]);
					
					this.score[t1] += a[0];
					this.score[t2] += a[1];
				//	this.writeResult(t1,t2,a,score,"red","blue");
					this.team_score.put(this.serversimulationagents.teams[t1], this.score[t1]);
					this.team_score.put(this.serversimulationagents.teams[t2], this.score[t2]);

					printXMLTournamentReport();
					
				}
			}
			break;
		case 1: {
			/*
			 * Testing phase mode: each team against the Bot
			 */
			int t2 = 0;
			for (int i = 0; i < this.serversimulationagents.teams.length; i++)
				if (this.serversimulationagents.teams[i].equalsIgnoreCase("TUCBot"))
					t2 = i;
			for (int t1 = 0; t1 < this.serversimulationagents.teams.length; t1++) {
				// search bot team
				if (t1 == t2)
					continue;
				log(LOGLEVEL_NORMAL, "now playing: "
						+ this.serversimulationagents.teams[t1] + " vs "
						+ this.serversimulationagents.teams[t2]);
				Map<String, String> m = new HashMap<>();
				m.put("red", this.serversimulationagents.teams[t1]);
				m.put("blue", this.serversimulationagents.teams[t2]);

				int[] a = runMatch(this.el_match, m,
						this.serversimulationagents.teams[t1] + "_VS_"
								+ this.serversimulationagents.teams[t2],
						report_el_root, this.serversimulationagents.teams[t1],
						this.serversimulationagents.teams[t2]);
				this.score[t1] += a[0];
				this.score[t2] += a[1];

				this.team_score.put(this.serversimulationagents.teams[t1], this.score[t1]);
				this.team_score.put(this.serversimulationagents.teams[t2], this.score[t2]);
				
				printXMLTournamentReport();
			}
		}
		
		break;
	
		case 2: {

			for (int i = 0; i < this.manual.size(); i++) {

				String teamsTogether[] = this.manual.get(i).split("VS");
				String team1 = teamsTogether[0];
				String team2 = teamsTogether[1];

				int t1 = findTeam(team1);
				int t2 = findTeam(team2);

				log(LOGLEVEL_NORMAL, "now playing: "
						+ this.serversimulationagents.teams[t1] + " vs "
						+ this.serversimulationagents.teams[t2]);
				Map<String, String> m = new HashMap<>();
				m.put("red", this.serversimulationagents.teams[t1]);
				m.put("blue", this.serversimulationagents.teams[t2]);

				runMatch(this.el_match, m, this.serversimulationagents.teams[t1] + "_VS_"
						+ this.serversimulationagents.teams[t2], report_el_root);

				printXMLTournamentReport();

			}
		}
		
			break;
		}
	

		
		this.socketlistener.stop();
		for (int i=0;i<this.serversimulationagents.agents.length;i++) 
			((Component)this.serversimulationagents.agents[i]).stop();
		this.agentmanager.stop();
		
		//write result to file
		writeTournamentReportToFile();
		this.unbindRMI();

		this.serverstatus = Serverstatus.SIMEND;
	}
	
	private void printXMLTournamentReport() {
		try {
			StringWriter logmessage = new StringWriter(); 
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.xmlTournamentReport),new StreamResult(logmessage));
			log(LOGLEVEL_DEBUG, logmessage.toString());

		} catch (Exception e) {
		}
	}
	
	private void unbindRMI() {
		try {
			Registry r = LocateRegistry.getRegistry(RMI_DefaultProperties.RMI_PORT_DEFAULT);
			String[] rmiservices = r.list();
		for(int i=0 ; i< rmiservices.length;i++ ){
		//	if(!rmiservices[i].equalsIgnoreCase("SPSSERVER")){
				r.unbind(rmiservices[i]);
				log(LOGLEVEL_DEBUG,"Unbind rmiservice: "+rmiservices[i]);
				
		//	}
		}
		UnicastRemoteObject.unexportObject(this.rmitournamentserver, true);
		UnicastRemoteObject.unexportObject(this.rmiinfoserver2, true);
		
	
		//	r.unbind("statistics");
			this.rmitournamentserver = null;
			
	//		r.unbind("server2");
	//		rmiinfoserver2 = null;
		
		} catch (RemoteException e) {
			log(LOGLEVEL_ERROR,"Error: couldn't unbind RMIInfoServer");
			e.printStackTrace();
			System.exit(1);
		} catch (NotBoundException e) {
			log(LOGLEVEL_ERROR,"Error: couldn't unbind RMIInfoServer");
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void createRMI() {	
		
		try {
			LocateRegistry.createRegistry(RMI_DefaultProperties.RMI_PORT_DEFAULT);			
			log(LOGLEVEL_NORMAL,"Connected to rmiregistry at "+ RMI_DefaultProperties.RMI_HOST_DEFAULT + " on port "+RMI_DefaultProperties.RMI_PORT_DEFAULT);
		} catch (RemoteException e1) {
		//	e1.printStackTrace();
		//	System.exit(0);
			log(LOGLEVEL_NORMAL,"rmi existed on port:  "+RMI_DefaultProperties.RMI_PORT_DEFAULT);
		}
		
	}

	

	private int findTeam(String name) {
		int index = 0;
		for (int i = 0; i < this.serversimulationagents.teams.length; i++) {
			if (this.serversimulationagents.teams[i].equals(name)) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void writeTournamentReportToFile() throws InvalidConfigurationException {
		try {

			FileOutputStream out = new FileOutputStream(this.xmlTournamentReportFile);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			transformer.transform(new DOMSource(this.xmlTournamentReport), new StreamResult(out));
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new InvalidConfigurationException(e1);
		} catch (TransformerException e2) {
			e2.printStackTrace();
			throw new InvalidConfigurationException(e2);
		}
	}

	public static void main(String[] args) throws InvalidConfigurationException {
		Server server = new Server(args);	
		server.run();
		
	}

	int stoppedstep = 0;
	public boolean stopped = false;
	
	public void startSimulation() {
		
		if(this.serverstatus == Serverstatus.CONFIGURED)
			try {
				this.stopped = false;
				run();
			} catch (InvalidConfigurationException e) {	
				e.printStackTrace();
			}
			
		else log(LOGLEVEL_ERROR,"server needs configured before running ");
	}
	public synchronized void stopSimulation() {
		if(this.sr != null){
		AbstractSimulation sim = (AbstractSimulation) this.sr.getSimulation();
		this.stoppedstep = sim.getSteps();
		this.stopped = true;
		sim.setSteps(Integer.MAX_VALUE);
		}
	}
	
	public synchronized int getStep(){
		int step = this.stoppedstep;
		if(this.sr != null && !this.stopped){
			AbstractSimulation simulation = (AbstractSimulation) this.sr.getSimulation();
			step = simulation.getSteps();
		}
		return step;
	}
	public synchronized HashMap<String, Integer>getTeam_Score(){
	
		return this.team_score;
	}
}

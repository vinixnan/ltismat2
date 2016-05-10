package massim.competition2015.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * 
 */
public class ReportGenerator 
		//extends GraphMonitor 
		{

	private static final long serialVersionUID = -573692905809930002L;
	
	private class StepTeamInfo {
		public int step;
		public int zonesScore;
		public int score;
		public int achievementPoints;
		public int usedAchievementPoints;
		public int stepScore;		
		public int provedNodes;
	}
	
	private class TeamInfo {
		public Map<String,String> achievements = new HashMap<String, String>();
		public String name;
		public Vector<StepTeamInfo> stepsEvol = new Vector<StepTeamInfo>();
	}
	
	private int numberOfNodes = 1;
	
	private Map<String,TeamInfo> teamsInfoMap;
	
	private String simulationName;
	
	
	
	
	//File managment
	private String dirName;
	private Vector<Integer> fileNumbers;
	private int fileIdx;
	private File currFile;
	private File dir;
	// XML parsing
	DocumentBuilderFactory factory;
	

	// Constructor
	public ReportGenerator(String directory) throws FileNotFoundException {
		super();
		
		initFile(directory);
		fileIdx = 0;		
		if (fileNumbers.size() == 0){
			return;
		}
				
		factory = DocumentBuilderFactory.newInstance();
		updateFile();
		if (fileNumbers.size() > 1){
			beginPlay();
		}
		generateReport();
	}



	private void initFile(String directory) throws FileNotFoundException {
		dir = new File(directory);
		if (!dir.isDirectory()){
			throw new FileNotFoundException();
		}
		this.dirName = dir.getName();
		
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()){
					return false;
				}
				try {
					String name = f.getName();
					String firstPart = name.substring(0, dirName.length());
					String extension = name.substring(name.lastIndexOf("."));
					if (firstPart.equalsIgnoreCase(dirName)
							&& extension.equalsIgnoreCase(".xml")) {
						return true;
					}
					return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
			}
			
		};
		File [] files = dir.listFiles(filter);
		fileNumbers = new Vector<Integer>(files.length);
		for (int i = 0; i < files.length; i++){
			try {
				String name = files[i].getName();
				String nr = name.substring(dirName.length() + 1, name.lastIndexOf("."));
				fileNumbers.add(new Integer(nr));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(fileNumbers);		
	}
	
	private void beginPlay() {
		while (fileIdx < fileNumbers.size()-1){
				fileIdx ++;
				updateFile();
		}
	}

	private void updateFile() {
		try {
			currFile = new File(dir, dirName + "_" + fileNumbers.get(fileIdx).toString()+".xml");
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(currFile);
			parseXML(document);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public void parseXML(Document doc) {
		
		try {			
			String step;
			String simName;
			NodeList nl = doc.getElementsByTagName("state");
			Element e = (Element) nl.item(0);
			step = e.getAttribute("step");
			simName = e.getAttribute("simulation");
			
			if (simulationName == null || "".equals(simulationName)){
				simulationName = simName;
			}

			nl = doc.getElementsByTagName("vertex");
			if (numberOfNodes < nl.getLength()){
				numberOfNodes = nl.getLength();
			}/*
			for (int i = 0; i < nl.getLength(); i++) {
				Element e1 = (Element) nl.item(i);

				NodeList agentsNl = e1.getFirstChild().getChildNodes();
				for (int j = 0; j < agentsNl.getLength(); j++) {
					Element agentEl = (Element) agentsNl.item(j);

				}				

			}*/
			

			nl = doc.getElementsByTagName("team");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e1 = (Element) nl.item(i);

				TeamInfo team = getTeamInfo(e1.getAttribute("name"));
				StepTeamInfo stepTeamInfo = new StepTeamInfo();
				stepTeamInfo.step = Integer.parseInt(step);
				stepTeamInfo.score =  Integer.parseInt(e1.getAttribute("score"));
				stepTeamInfo.achievementPoints =  Integer.parseInt(e1.getAttribute("achievementPoints"));
				stepTeamInfo.usedAchievementPoints =  Integer.parseInt(e1.getAttribute("usedAchievementPoints"));
				stepTeamInfo.stepScore =  Integer.parseInt(e1.getAttribute("stepScore"));
				stepTeamInfo.zonesScore =  Integer.parseInt(e1.getAttribute("zonesScore"));
				
				NodeList nodeList = e1.getElementsByTagName("achievements");
				if (nodeList.getLength() > 0){
					nodeList = nodeList.item(0).getChildNodes();
					for (int j = 0; j < nodeList.getLength(); j++) {
						if (!(nodeList.item(j) instanceof Element)){
							continue;
						}
						Element agentEl = (Element) nodeList.item(j);
						String name = agentEl.getAttribute("name");
						if (!team.achievements.containsKey(name)){
							team.achievements.put(name, step);
						}
					}
				}
				
				nodeList = e1.getElementsByTagName("provedNodes");
				int pn = 0;
				if (nodeList.getLength() > 0){
					nodeList = nodeList.item(0).getChildNodes();
					for (int j = 0; j < nodeList.getLength(); j++) {
						if (!(nodeList.item(j) instanceof Element)){
							pn++;
						}
					}
				}
				stepTeamInfo.provedNodes = pn;
				
				team.stepsEvol.add(stepTeamInfo);
				
			}
			
			
		} catch (Exception e) {
			// TODO something?
		}
	}
	
	
	private TeamInfo getTeamInfo(String teamName){
		if (teamName == null || "".equals(teamName)){
			return null;
		}
		if (teamsInfoMap == null){
			teamsInfoMap = new HashMap<String, ReportGenerator.TeamInfo>(2);
		}
		if (!teamsInfoMap.containsKey(teamName)){
			TeamInfo team = new TeamInfo();
			team.name = teamName;
			teamsInfoMap.put(teamName, team);
			return team;
		}
		return teamsInfoMap.get(teamName);
	}
	
	private void generateReport() {
		// TODO Auto-generated method stub
		String fileName = "Report_" + simulationName + ".csv";		
		StringBuffer results = new StringBuffer("Step; Number of nodes");
		
		int steps = -1;
		for (TeamInfo ti: teamsInfoMap.values()){
			results.append("; Score ").append(ti.name)
					.append("; Step Score ").append(ti.name)
					.append("; Zone Score ").append(ti.name)
					.append("; Achievement Points ").append(ti.name)
					.append("; Used Achievement Points ").append(ti.name)
					.append("; Proved nodes ").append(ti.name);
			if (steps < 0 || steps > ti.stepsEvol.size()){
				steps = ti.stepsEvol.size();
			}
		}
		results.append("\n");
		for (int i = 0; i < steps; i++){
			boolean writeStep = true;
			for (TeamInfo ti: teamsInfoMap.values()){
				StepTeamInfo sti = ti.stepsEvol.get(i);
				if (writeStep){
					results.append(sti.step).append(";").append(numberOfNodes);
					writeStep = false;
				}
				results.append(";")
						.append(sti.score).append(";")
						.append(sti.stepScore).append(";")
						.append(sti.zonesScore).append(";")
						.append(sti.achievementPoints).append(";")
						.append(sti.usedAchievementPoints).append(";")
						.append(sti.provedNodes);
			}
			results.append("\n");
		}
		
		try {			
			FileWriter fw = new FileWriter(fileName, false);
			fw.append(results);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			
			String arg = null;
			for(int i = 0; i<args.length;i++){
				if(args[i].equalsIgnoreCase("-dir")){
					arg = args[i+1];
					break;
				}
					
			}
			if (arg != null){
				new ReportGenerator(arg);
			} else {
				System.out.println("ReportGenerator -dir <directrory containing the xmls>");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}

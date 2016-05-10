package massim.competition2011;

import java.util.Vector;

import massim.competition2011.scenario.TeamState;
import massim.framework.SimulationState;
import massim.framework.XMLOutputObserver;
import massim.framework.backup.BackupWriter;
import massim.framework.simulation.SimulationStateImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class provides all information needed for the tomcat results page.
 * Note: If you change the format of the document sent to the tomcat you have
 * to change the webapp WebClientTournamentServlet as well
 * Also, the webapp CreateResultsPage and the ResultsPage are affected.
 * So, be careful! 
 *
 */
public class GraphSimulationXMLStatisticsObserver extends XMLOutputObserver{

	private Element el_root;
	private Document doc;
	private String[] teamName;
	private long[] summedScore;
	public static String simulationName;
	private TeamState a;
	private TeamState b;
	private Vector<String> achsA;
	private Vector<String> achsB;
	public static String outputFolder;
	private String previewFile;
//	private String backgroundFile;

	public GraphSimulationXMLStatisticsObserver() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see massim.Observer#notifySimulationStart()
	 */
	public void notifySimulationStart() {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see massim.Observer#notifySimulationEnd()
	 */
	public void notifySimulationEnd() {
		
		synchronized (this) {
			
			applyChanges();
		}

		setChanged();
		notifyObservers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see massim.Observer#notifySimulationState(massim.SimulationState)
	 */
	public void notifySimulationState(SimulationState state) {
		
		SimulationStateImpl simplestate = (SimulationStateImpl) state;
		GraphSimulationWorldState worldState = (GraphSimulationWorldState) simplestate.simulationState;
		
		a = worldState.teamsStates.get(0);
		b = worldState.teamsStates.get(1);
		
		teamName = new String[2];
		teamName[0] = a.name;
		teamName[1] = b.name;
		
		summedScore = new long[2];
		summedScore[0] = a.summedScore;
		summedScore[1] = b.summedScore;
		
//		simulationName  = worldState.simulationName;
		
		achsA = worldState.getTeamState(a.name).getAchieved();
		achsB = worldState.getTeamState(b.name).getAchieved();
		
		previewFile = outputFolder + simulationName + System.getProperty("file.separator") + "SimulationPreview.svg";
//		backgroundFile= outputFolder+simulationName+BackupWriter.file_sep+"masSim-0.svg";
		
		synchronized (this) {
			applyChanges();
		}

		setChanged();
		notifyObservers();
	}
	
	/**
	 * Takes information from the fields and creates an xml-document based on that.
	 */
	private void applyChanges() {
		
		resetDocument();
		doc = getDocument();
		el_root = doc.createElement("statistics");
		el_root.setAttribute("simulation", simulationName);
		
		
//		Element team1 = doc.createElement("team");
//		Element team2 = doc.createElement("team");
//		
//		el_root.appendChild(team1);
//		el_root.appendChild(team2);
		
//		Element achs1 = doc.createElement("achievements");
//		Element achs2 = doc.createElement("achievements");
		
		el_root.setAttribute(teamName[0], Double.toString(summedScore[0]));
		el_root.setAttribute(teamName[1], Double.toString(summedScore[1]));
		el_root.setAttribute(teamName[0] + "cowsincorral", Double.toString(0));
		el_root.setAttribute(teamName[1] + "cowsincorral", Double.toString(0));
		el_root.setAttribute("simulation", simulationName);
		el_root.setAttribute("output",previewFile);
//		el_root.setAttribute("output2",backgroundFile);
		
//		team1.setAttribute("name", teamName[0]);
//		team1.setAttribute("summedScore", Double.toString(summedScore[0]));
//		
//		team2.setAttribute("name", teamName[1]);
//		team2.setAttribute("summedScore", Double.toString(summedScore[1]));
//		
//		Element achEl;
//		for(String ach : achsA){
//			achEl = doc.createElement("achievement");
//			achEl.appendChild(doc.createTextNode(ach));
//			achs1.appendChild(achEl);
//		}
//		for(String ach : achsB){
//			achEl = doc.createElement("achievement");
//			achEl.appendChild(doc.createTextNode(ach));
//			achs2.appendChild(achEl);
//		}
//		
//		team1.appendChild(achs1);
//		team2.appendChild(achs2);
//	
		doc.appendChild(el_root);
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

}

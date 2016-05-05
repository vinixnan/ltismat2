package massim.competition2011;

import massim.competition2011.scenario.TeamState;
import massim.framework.SimulationState;
import massim.framework.backup.BackupWriter;
import massim.framework.simulation.SimulationStateImpl;
import massim.gridsimulations.SimulationRMIXMLDocumentObserver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This RMIXMLDocumentObserverWebInterface provides the simulation statistics for the webserver (current simulation) (tomcat).
 * 
 */
public class GraphSimulationRMIXMLDocumentObserverWebInterface extends SimulationRMIXMLDocumentObserver{
	
	public static String outputFolder;

	public GraphSimulationRMIXMLDocumentObserverWebInterface() {
		super();
	}
	
	
	public void generateDocument(Document doc, SimulationState simstate) {
	
		SimulationStateImpl simplestate = (SimulationStateImpl) simstate;
		GraphSimulationWorldState worldState = (GraphSimulationWorldState) simplestate.simulationState;
		
		TeamState team1;
		TeamState team2;
				
		team1 = worldState.teamsStates.get(0);
		team2 = worldState.teamsStates.get(1);

		/*//To be used when adding achievements
		Vector<String> achsTeam1;
		Vector<String> achsTeam2;
		achsTeam1 = worldState.getTeamState(team1.name).getAchieved();
		achsTeam2 = worldState.getTeamState(team2.name).getAchieved();
		*/
		
		
		String[] teamName = new String[2];
		long[] averageScore = new long[2];
		int[] cowsInCorral = new int[2];
		teamName[0] = team1.name;
		teamName[1] = team2.name;
		averageScore[0] = team1.summedScore;
		averageScore[1] = team2.summedScore;
		cowsInCorral[0] = 0; //worldState.cowsInCorral[0];  //To be discarded in the future
		cowsInCorral[1] = 0; //worldState.cowsInCorral[1];
		
		String outputFile;
		String backgroundFile = "masSim-0.svg";
		outputFile = outputFolder+simulationName+BackupWriter.file_sep+GraphPolicy.svgFile;
		backgroundFile= outputFolder+simulationName+BackupWriter.file_sep+"masSim-0.svg";
		
			//doc = resetDocument();
			Element el_root = doc.getDocumentElement();
			//Element el_root = doc.createElement("statistics");
			//the plain teamname as attribute contains the score. If you change this, you have to change the Server.java and the webclient as well.
			
			/*el_root.setAttribute(teamName[0], Long.toString(averageScore[0]));
			el_root.setAttribute(teamName[1], Long.toString(averageScore[1]));
			el_root.setAttribute(teamName[0] + "cowsincorral", Integer.toString(cowsInCorral[0]));
			el_root.setAttribute(teamName[1] + "cowsincorral", Integer.toString(cowsInCorral[1]));
			el_root.setAttribute("simulation", simulationName);*/
			el_root.setAttribute("output",outputFile);
			el_root.setAttribute("output2",backgroundFile);
		//	doc.appendChild(el_root);
	}
}

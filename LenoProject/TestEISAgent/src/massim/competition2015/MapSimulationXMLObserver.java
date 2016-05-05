package massim.competition2015;

import static massim.framework.util.DebugLog.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import massim.framework.SimulationConfiguration;
import massim.framework.SimulationState;
import massim.framework.XMLOutputObserver;
import massim.framework.simulation.SimulationStateImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This RMIXMLDocumentObserver provides the simulation statistics for the webserver and the servermonitor.
 * 
 */
public class MapSimulationXMLObserver extends XMLOutputObserver {

	private static final String STATUS_KEY="status"; 
	private static final String STATUS_SIMULATIONRUNNING="running";
	private String baseFileName="";
	
	public MapSimulationXMLObserver() {
		super();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}
	
	public void notifySimulationConfiguration(SimulationConfiguration simconf) {
		massim.competition2015.configuration.MapSimulationConfiguration simconfig = (massim.competition2015.configuration.MapSimulationConfiguration) simconf;
		
		String simulationName = simconfig.simulationName;
		
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		baseFileName = simulationName + "_" + df.format(dt) + File.separator + simulationName + "_" + df.format(dt) + "_";
	}
	
	public void notifySimulationState(SimulationState state) {
		SimulationStateImpl simplestate = (SimulationStateImpl) state;
		String fileName = baseFileName + Integer.toString(simplestate.steps) + ".xml";
		resetDocument();
		generateDocument(getDocument(), simplestate);
		setChanged();
		notifyObservers(fileName);
	}
		
	/**
	 * Generates the XML representation of the simulation state to send to the observer. 
	 * @param doc The document to which the XML is generated.
	 * @param simplestate The current simulation state.
	 */
	public void generateDocument(Document doc, SimulationStateImpl simplestate) {

		Element el_root = doc.createElement("simulationstate");
		el_root.setAttribute(STATUS_KEY, STATUS_SIMULATIONRUNNING);

        doc.appendChild(el_root);

		//MapSimulationWorldState worldState = (MapSimulationWorldState) simplestate.simulationState;

	    MapObserverUtil.appendState(doc, simplestate);
	}


	
}

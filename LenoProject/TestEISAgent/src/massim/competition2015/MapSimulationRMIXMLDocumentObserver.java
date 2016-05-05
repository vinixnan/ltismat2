package massim.competition2015;

import massim.competition2015.MapSimulationAgentState;
import massim.competition2015.scenario.*;
import massim.framework.SimulationState;
import massim.framework.rmi.XMLDocumentObserver;
import massim.framework.simulation.SimulationStateImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This RMIXMLDocumentObserver provides the information for the servermonitor.
 * 
 */
public class MapSimulationRMIXMLDocumentObserver extends XMLDocumentObserver {
	
	/*
	 * (used by live desktop monitor and to save simulation for off-line analysis?)
	 */
	
	/**
	 * Generates the XML representation of the simulation state to send to the observer. 
	 * @param doc The document to which the XML is generated.
	 * @param simstate The current simulation state.
	 */
	public void generateDocument(Document doc, SimulationState simstate) {
		MapObserverUtil.appendState(doc, simstate);
	}

}
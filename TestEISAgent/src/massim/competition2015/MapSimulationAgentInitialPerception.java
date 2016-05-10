package massim.competition2015;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import massim.competition2015.scenario.Item;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import massim.framework.InitialStickyPerception;

/**
 * This class holds the information that is sent to the agent as initial perception when simulation begins.
 *
 * IMPORTANT: CHANGES TO THE STRUCTURE HAVE TO BE REFLECTED IN THE CORRESPONDING ENTITY IN eismassim
 */
public class MapSimulationAgentInitialPerception implements
		InitialStickyPerception, massim.framework.util.XMLCodec.XMLEncodable {

	private static final long serialVersionUID = 8293224111557780965L;
	
	/**
	 * The state of the agent to which this perception belongs.
	 */
	public massim.competition2015.MapSimulationAgentState self;

	/**
	 * The number of steps that this simulation will run.
	 */
	public int steps;

	/**
	 * The agent's role
	 */
	public String role;

	/**
	 * The name of the map to use for this simulation.
	 */
	public String map;

	/**
	 * The starting capital
	 */
	public long seedCapital;

	/**
	 * All items of the simulation
	 */
	public Set<Item> products = new HashSet<>();

	/**
	 * A collection of agent states, holding the states of all the team Members in the
	 * same team of the owner of this perception.
	 */
	public Collection<massim.competition2015.MapSimulationAgentState> teamMembers;

	/**
	 * Encodes the contents of this perception object in the right XML format,
	 * in concordance to the protocol description,
	 */
	@Override
	public void encodeToXML(Element target) {

		target.setAttribute("steps", String.valueOf(steps));

		target.setAttribute("team", self.team);

		target.setAttribute("map", this.map);

		target.setAttribute("seedCapital", String.valueOf(this.seedCapital));
		
		Document doc = target.getOwnerDocument();

		Element elRole = doc.createElement("role");
		elRole.setAttribute("name", self.roleName);
		elRole.setAttribute("speed", String.valueOf(self.role.speed));
		elRole.setAttribute("maxLoad", String.valueOf(self.role.loadCapacity));
		elRole.setAttribute("maxBattery", String.valueOf(self.role.batteryCapacity));
		for(String tool: self.role.tools){
			Element elTool = doc.createElement("tool");
			elTool.setAttribute("name", tool);
			elRole.appendChild(elTool);
		}
		target.appendChild(elRole);

		Element elProducts = doc.createElement("products");
		for(Item i: products){
			Element elProduct = doc.createElement("product");
			elProduct.setAttribute("name", i.name);
			elProduct.setAttribute("volume", String.valueOf(i.volume));
			elProduct.setAttribute("assembled", String.valueOf(i.userAssembled));
			if(i.userAssembled){
				Element elConsumed = doc.createElement("consumed");
				for(Map.Entry<Item,Integer> e : i.itemsConsumed.entrySet()){
					Element elItem = doc.createElement("item");
					elItem.setAttribute("name", e.getKey().name);
					elItem.setAttribute("amount", String.valueOf(e.getValue()));
					elConsumed.appendChild(elItem);
				}
				elProduct.appendChild(elConsumed);
				Element elTools = doc.createElement("tools");
				for(Map.Entry<Item,Integer> e : i.toolsNeeded.entrySet()){
					Element elItem = doc.createElement("item");
					elItem.setAttribute("name", e.getKey().name);
					elItem.setAttribute("amount", String.valueOf(e.getValue()));
					elTools.appendChild(elItem);
				}
				elProduct.appendChild(elTools);
			}
			elProducts.appendChild(elProduct);
		}

		target.appendChild(elProducts);
		
//		Element elSelf = target.getOwnerDocument()
//				.createElement("self");
//		elSelf.setAttribute("name", self.name);
//		elSelf.setAttribute("team", self.team);
//		elSelf.setAttribute("strength", String.valueOf(self.strength));
//		elSelf.setAttribute("maxEnergy", String.valueOf(self.maxEnergy));
//		elSelf.setAttribute("visRange", String.valueOf(self.visRange));	
//		target.appendChild(elSelf);
//		
//		Element elTeamMembers = target.getOwnerDocument()
//				.createElement("teamMembers");		
//		for (MapSimulationAgentState agent : teamMembers) {
//			Element elAgent = target.getOwnerDocument()
//					.createElement("teamMember");
//			elAgent.setAttribute("name", agent.name);
//			elAgent.setAttribute("team", agent.team);
//			elAgent.setAttribute("strength", String.valueOf(agent.strength));
//			elAgent.setAttribute("maxEnergy", String.valueOf(agent.maxEnergy));
//			elAgent.setAttribute("visRange", String.valueOf(agent.visRange));				
//			
//			elTeamMembers.appendChild(elAgent);
//		}
//		target.appendChild(elTeamMembers);		
	}

}

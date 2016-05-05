package massim.competition2014.monitor.graph;

import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Provides a set of useful graph utility functions.
 * 
 * @author tristanbehrens
 *
 */
public class Util {
	
	/**
	 * Finds a node in a collection of nodes.
	 * 
	 * @param name
	 * @param nodes
	 * @return
	 */
	public static NodeInfo searchNode(String name, Collection<NodeInfo> nodes) {
		for (NodeInfo node : nodes) {
			if (node.name.equals(name)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Finds an agent in a collection of nodes.
	 * 
	 * @param name
	 * @param nodes
	 * @return
	 */
	public static AgentInfo searchAgent(String name, Collection<NodeInfo> nodes) {
		for (NodeInfo node : nodes) {
			for (AgentInfo agent : node.agents) {
				if (agent.name.equals(name)) {
					return agent;
				}
			}
		}
		return null;
	}

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

	/**
	 * Computes the certificate of a triangle. That is 
	 * a unique string that represents the triangle.
	 * 
	 * @param n
	 * @param na
	 * @param nb
	 * @return
	 */
	private String triangleCertificate(NodeInfo n, NodeInfo na, NodeInfo nb) {
		
		// sort those names
		TreeSet<String> set = new TreeSet<String>();
		set.add(n.name);
		set.add(na.name);
		set.add(nb.name);
		
		// make a string
		String ret = "";
		for (String str : set) {
			ret += str;
		}
		return ret;

	}
	
}
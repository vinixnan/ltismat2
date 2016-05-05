package massim.competition2014.scenario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import massim.competition2014.GraphSimulationAgentState;


/**
 * This class holds information about the current state of a Team, including current score, zones built,
 * achievement points, achievements, etc.
 * 
 */
public class TeamState implements Serializable{

	private static final long serialVersionUID = -1800159344742627560L;
	
	public static final int ACHIEVEMENT_PONITS_SCALE = 1;
	
	public String name;
	public int teamIdx;
	public int ranking;
	public long summedScore;
	public long currAchievementPoints;
	public long usedAchievementPoints;
	public List<DominatedArea> areas;
	private int successfulAttacks;
	private int successfulParrys;
	private Set<GraphEdge> surveyedEdges;
	private Set<String> probedNodes;
	private Set<String> inspectedAgents;
	public Vector<Achievement> achievements;
	public Vector<Achievement> newAchievements;
	public HashMap<String,String> newBuyTransactions;
	public HashMap<String,String> buyTransactions;
	
	public TeamState(String name, int teamIdx) {
		super();
		this.name = name;
		this.teamIdx = teamIdx;
		this.ranking = -1;
		this.summedScore = 0;
		this.currAchievementPoints = 0;
		this.usedAchievementPoints = 0;
		this.successfulAttacks = 0;
		this.successfulParrys = 0;
		this.areas = new LinkedList<>();
		
		this.surveyedEdges = new HashSet<>();
		this.probedNodes = new HashSet<>();
		this.inspectedAgents = new HashSet<>();
		this.achievements = new Vector<>();
		this.newAchievements = new Vector<>();
		this.buyTransactions = new HashMap<>();
		this.newBuyTransactions = new HashMap<>();
	}
	
	/**
	 * Sums the step score to the total score. This method should be called
	 * once at the end of every simulation step.
	 */
	public void sumCurrent() { 
		this.summedScore += getCurrent();
	}
	
	/**
	 * Returns the current step-score (that is, the score that depends only on the current status of the world,
	 * and that should be added to the total team score in every step).
	 * @return
	 */
	public long getCurrent() { 
		return this.currAchievementPoints + getAreasValue();
	}
	
	/**
	 * Returns the summed score of all the dominated areas.
	 * @return
	 */
	public long getAreasValue() { 
		long score = 0;
		for (DominatedArea area: this.areas){
			score += getAreaValue(area);
		}
		return score;
	}
	
	/**
	 * Returns the value of the area given as parameter.
	 * @param area
	 * @return
	 */
	public long getAreaValue(DominatedArea area){
		// return area.totalScore;
		return area.calculateProbedScore(this.probedNodes);
	}
	
	
	/**
	 * Returns the value of the dominated area of which the agent given as parameter is part.
	 * (returns 0 if the agent is not dominating area)
	 * @param agent
	 * @return
	 */
	public long getAreaValue(GraphSimulationAgentState agent){
		for (DominatedArea area: this.areas){
			if (area.agents.contains(agent)){
				return getAreaValue(area);
			}
		}		
		return 0;
	}
	
	
	
	public boolean useAchievementPoints(long points){
		if (points >= this.currAchievementPoints) return false;
		this.currAchievementPoints -= points;
		this.usedAchievementPoints += points;
		return true;
	}
	
	
	/**
	 * Checks if the teams has reached new achievements, marks them as reached,
	 * and increases the current achievementPoints accordingly.
	 */
	public void calculateNewAchievements(){
		this.newAchievements.clear();
		for (Achievement ach : this.achievements) {
			if (!ach.achieved){
				if ("probedVertices".equals(ach.achievementClass)){
					if (this.probedNodes.size() >= ach.quantity){
						ach.achieved = true;
						this.currAchievementPoints += ach.points;
						this.newAchievements.add(ach);
					}
				}
				else if ("surveyedEdges".equals(ach.achievementClass)){
					if (this.surveyedEdges.size() >= ach.quantity){
						ach.achieved = true;
						this.currAchievementPoints += ach.points;
						this.newAchievements.add(ach);
					}
				}
				else if ("inspectedAgents".equals(ach.achievementClass)){
					if (this.inspectedAgents.size() >= ach.quantity){
						ach.achieved = true;
						this.currAchievementPoints += ach.points;
						this.newAchievements.add(ach);
					}
				}
				else if ("successfulAttacks".equals(ach.achievementClass)){
					if (this.successfulAttacks >= ach.quantity){
						ach.achieved = true;
						this.currAchievementPoints += ach.points;
						this.newAchievements.add(ach);
					}
				}
				else if ("successfulParries".equals(ach.achievementClass)){
					if (this.successfulParrys >= ach.quantity){
						ach.achieved = true;
						this.currAchievementPoints += ach.points;
						this.newAchievements.add(ach);
					}
				}
				else if ("areaValue".equals(ach.achievementClass)){
					for (DominatedArea area : this.areas) {
						if (area.calculateProbedScore(this.probedNodes)>= ach.quantity){
							ach.achieved = true;
							this.currAchievementPoints += ach.points;
							this.newAchievements.add(ach);
							break;
						}
					}
				}				
			}
		}	
	}
	
	/**
	 * Returns a vector with the name of the achievements that the team obtained
	 * during the <b>whole simulation</b>.
	 * @return a <code>Vector&lt;String&gt;</code> containing the names.
	 */
	public Vector<String> getAchieved(){
		Vector<String> v = new Vector<>();
		for (Achievement ach : this.achievements) {
			if (ach.achieved){
				v.add(ach.name);
			}
		}
		return v;
	}
	
	/**
	 * Returns a vector with the name of the achievements that the team obtained
	 * during the <b>last simulation step</b>.
	 * @return a <code>Vector&lt;String&gt;</code> containing the names.
	 */
	public Vector<String> getNewlyAchieved(){
		Vector<String> v = new Vector<>();
		for (Achievement ach : this.newAchievements) {
			v.add(ach.name);
		}
		return v;
	}
	/**
	 * Returns a HashMap that stores for each agent the buying transaction it did during the <b> last simulation step</b>.
	 * @return a <code>HashMap&lt;String,String&gt;</code> containing the agent names and the buying transactions per agent.
	 * The first String describes the agent, the second the item that was bought.
	 */
	public HashMap<String, String> getNewlyBought(){
		return this.newBuyTransactions;
	}
	
	public void successfullAttack(){
		this.successfulAttacks++;
	}
	
	public void successfullParry(){
		this.successfulParrys++;
	}
	
	public boolean addSurveyedEdge(GraphEdge e){
		return this.surveyedEdges.add(e);
	}
	
	public boolean addProbedNodes(String nodeName){
		return this.probedNodes.add(nodeName);
	}
	
	public boolean addInspectedAgent(String agentName){
		return this.inspectedAgents.add(agentName);
	}
	
	public boolean addProbedNodes(GraphNode node){
		return this.probedNodes.add(node.name);
	}
	
	public Set<String> getProbedNodes(){
		return this.probedNodes;
	}
	
	public boolean addInspectedAgent(GraphSimulationAgentState agent){
		return this.inspectedAgents.add(agent.name);
	}
	
	public void initAchievements(Vector<Achievement> configAchievements){
		
		for (Achievement achievement : configAchievements) {
			Achievement copy = new Achievement(achievement);
			this.achievements.add(copy);
		}
		
	}

	public void clearBuyingTransactions() {
		this.newBuyTransactions.clear();		
	}
}

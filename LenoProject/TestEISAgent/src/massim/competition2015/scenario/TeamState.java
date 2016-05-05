package massim.competition2015.scenario;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * This class holds information about the current state of a Team, including current score, zones built,
 * achievement points, achievements, etc.
 * 
 */
public class TeamState implements Serializable{

	private static final long serialVersionUID = 20151L;
	
	public static final int ACHIEVEMENT_PONITS_SCALE = 1;
	
	public String name;
	public int teamIdx;
	public int ranking;
	public long money;
	
	public Set<Job> takenJobs;
	public Set<Job> postedJobs;

	public TeamState(String name, int teamIdx, int seedCapital) {
		super();
		this.name = name;
		this.teamIdx = teamIdx;
		this.ranking = -1;
		this.money = seedCapital;
		this.takenJobs = new HashSet<>();
		this.postedJobs = new HashSet<>();
		
	}

	public void updateMoney(){
		// TODO 2016:: use to calculate interests.
	}

}

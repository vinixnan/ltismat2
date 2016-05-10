package massim.competition2013;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import massim.competition2013.scenario.Achievement;
import massim.competition2013.scenario.GraphNode;
import massim.competition2013.scenario.RoleConfiguration;
import massim.competition2013.scenario.TeamState;
import massim.framework.Observer;
import massim.framework.SimulationConfiguration;
import massim.framework.SimulationState;
import massim.framework.simulation.SimulationStateImpl;
import massim.framework.util.DebugLog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GraphSimulationStatisticsObserver implements Observer {

	private Color[] teamDomColors = { new Color(0, 158, 115), new Color(0, 114, 178) };

	private GraphSimulationWorldState worldState;
	private int steps = 0;
	private int agentsPerTeam;

	private SingleAgentResults agResults;

	// map for storing the buying transactions. team -> step -> vector of
	// (agent, item)
	private LinkedHashMap<Integer, HashMap<String, Vector<AgentBuyingTransaction>>> buyingTransactions;

	// map for storing the number of steps the nodes are dominated by the
	// current team
	private HashMap<GraphNode, NodeState> dominationCounter;

	// map for storing the sums of the 3 counters for each step
	private HashMap<String, Vector<Integer>> zoneStabilityValues;

	// map for storing the zonesScores
	private HashMap<String, Vector<String>> zonesScores;

	// teamname -> achievement-name -> step
	private HashMap<String, HashMap<String, Integer>> achievements;

	// teamname -> achievement-points (sorted by step)
	private HashMap<String, Vector<Long>> achievementPoints;

	// name of achievement -> achievement
	private HashMap<String, Achievement> achievementsConfMap;

	// teamname -> score/step
	private HashMap<String, Vector<Long>> scores;

	// failtype -> teamname -> failcount
	private HashMap<String, HashMap<String, Integer>> fails = new HashMap<>();

	// teams with failed actions (for safety)
	private Set<String> failedTeams = new HashSet<>();

	// whether the specific charts should be generated or not
	private boolean generateZoneStabilityChart = true;
	private boolean generateZonesScoresChart = true;
	private boolean generateAchievementPointsChart = true;
	private boolean generateAchievementCategoriesChart = true;
	private boolean generateActionsChart = true;
	private boolean achievementsTable = true;
	private boolean buyingTransactionsTable = true;
	private boolean assignLabelsToAchievementPoints = false; // this is only
																// advisable up
																// to 100 steps
																// or so
	private boolean generateScoreChart = true;

	// dimensions of each chart
	private int chartWidth = 1024;
	private int chartHeight = 768;

	private final BasicStroke dashedStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f);

	private String outputPath;
	public final static String fs = System.getProperty("file.separator");
	public final static String ls = System.getProperty("line.separator");

	/**
	 * This string will be used as header in the final output.
	 */
	private String header = "";

	private String lineSep = System.getProperty("line.separator");

	boolean nodeStatesInitialized = false;

	public GraphSimulationStatisticsObserver() {
		super();
	}

	public GraphSimulationStatisticsObserver(String outputpath) {

		// initialize maps
		this.dominationCounter = new HashMap<>();
		this.zonesScores = new HashMap<>();
		this.achievements = new HashMap<>();
		this.achievementPoints = new HashMap<>();
		this.achievementsConfMap = new HashMap<>();
		this.zoneStabilityValues = new HashMap<>();
		this.scores = new HashMap<>();
		this.buyingTransactions = new LinkedHashMap<>();

		this.outputPath = outputpath;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void notifySimulationStart() {
		checkFonts();
	}

	@Override
	public void notifySimulationEnd() {

		long time = System.currentTimeMillis();
		// final output
		DebugLog.log(DebugLog.LOGLEVEL_NORMAL, "Generating Statistics");

		writeStatisticsToFile();

		DebugLog.log(DebugLog.LOGLEVEL_DEBUG, "Took " + (System.currentTimeMillis() - time) / 1000 + " second(s)" + " to generate statistics.");
	}

	@Override
	public synchronized void notifySimulationState(SimulationState state) {

		this.steps++;

		SimulationStateImpl tcstate = (SimulationStateImpl) state;
		GraphSimulationWorldState wstate = (GraphSimulationWorldState) tcstate.simulationState;
		if (wstate.currentStep > -1) {
			if (this.worldState == null) {
				this.worldState = wstate;

				this.agentsPerTeam = wstate.agents.size() / 2;

				this.agResults = new SingleAgentResults(wstate.agentNamesMap.keySet());

				for (TeamState ts : wstate.teamsStates) {
					this.scores.put(ts.name, new Vector<Long>());
				}
			}

			// update team-related things
			updateScores(wstate);
			updateZoneStabilityValues(wstate);
			updateZonesScores(wstate);
			updateAchievements(wstate);

			// update agent-related things
			updateSingleAgentValues(wstate);

			// update team and agent-related things
			updateBuyingTransactions(wstate);
		}
	}

	/**
	 * checks whether system has fonts installed
	 * 
	 * @return true if fonts are installed
	 */
	private boolean checkFonts() {
		try {
			if (GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts().length == 0) {
				DebugLog.log(DebugLog.LOGLEVEL_CRITICAL, "No fonts installed! " + "Statistics won't be generated at the end of the simulation.");
				return false;
			}
		} catch (Exception e) {
			DebugLog.log(DebugLog.LOGLEVEL_CRITICAL, "No fonts installed! " + "Statistics won't be generated at the end of the simulation.");
			return false;
		}
		return true;
	}

	private void updateScores(GraphSimulationWorldState wstate) {

		for (TeamState ts : wstate.teamsStates) {
			this.scores.get(ts.name).add(ts.summedScore);
		}
	}

	@Override
	public void notifySimulationConfiguration(SimulationConfiguration simconf) {
		GraphSimulationConfiguration conf = ((GraphSimulationConfiguration) simconf);

		// prepare outputpath
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String simulationName = conf.simulationName;
		this.outputPath = this.outputPath + fs + simulationName + "_" + df.format(dt) + fs;

		// prepare the header of the textfile containing general info
		prepareHeader(conf);

		// prepare the map for fast access to achievements
		for (Achievement a : conf.achievements) {
			this.achievementsConfMap.put(a.name, a);
		}
	}

	private void prepareHeader(GraphSimulationConfiguration conf) {

		this.header += "Simulation " + conf.simulationName + " of teams: ";
		for (String tname : new HashSet<>(conf.getTeamNames())) {
			this.header += tname + ", ";
		}
		this.header = this.header.substring(0, this.header.length() - 2) + this.lineSep + "###" + this.lineSep;
		this.header += "RandomSeed: " + conf.randomSeed + " Steps: " + conf.maxNumberOfSteps + " NumberOfNodes: " + conf.numberOfNodes + this.lineSep + this.lineSep;
	}

	/**
	 * Writes all the accumulated data to file and automatically generates
	 * charts. Should only be called after all the sets of values and results
	 * are calculated and processed.
	 */
	private void writeStatisticsToFile() {

		File dir = new File(this.outputPath);
		dir.mkdirs();

		// if no fonts are installed, the graphics can't be created
		if (!checkFonts()) {
			this.generateScoreChart = false;
			this.generateZoneStabilityChart = false;
			this.generateZonesScoresChart = false;
			this.generateAchievementPointsChart = false;
			this.generateAchievementCategoriesChart = false;
			this.generateActionsChart = false;
		}

		drawCharts(this.outputPath, this.generateScoreChart, this.generateZoneStabilityChart, this.generateZonesScoresChart, this.generateAchievementPointsChart, this.generateAchievementCategoriesChart, this.generateActionsChart, this.achievementsTable, this.buyingTransactionsTable,
				this.assignLabelsToAchievementPoints, this.chartWidth, this.chartHeight);

		createTableOfFailedActions(this.outputPath);

		try {
			File f = new File(this.outputPath + "txtfile");
			f.createNewFile();
			FileWriter wr = new FileWriter(f);

			wr.write(this.header);
			writeTeamResults(wr);
			writeAgentResults(wr);

			wr.flush();
			wr.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createTableOfFailedActions(String basefilename) {

		File texFile = new File(basefilename + "failedActionsTable.tex");
		DecimalFormat df = new DecimalFormat("#.##");
		try {
			texFile.createNewFile();
			FileWriter out = new FileWriter(texFile);

			// write the header
			out.write("\\documentclass{article}" + ls + "\\begin{document}" + ls + "\\begin{tabular}{|c||c|c||c|c|}" + ls + "\\hline" + ls);

			// to guarantee fixed iteration order
			String[] failedT = this.failedTeams.toArray(new String[this.failedTeams.size()]);

			// write the table-body
			out.write("Reason");
			for (int j = 0; j < failedT.length; j++) {
				String teamName = failedT[j];
				out.write("&" + teamName + "& $\\%$");
			}
			out.write("\\\\" + ls + "\\hline");

			for (String failType : this.fails.keySet()) {
				HashMap<String, Integer> teams = this.fails.get(failType);
				out.write(ls + failType.replace("_", " "));
				for (int j = 0; j < failedT.length; j++) {
					String team = failedT[j];
					Integer count = teams.get(team);
					if (count != null) {
						out.write("&" + count.intValue() + "&" + df.format(((double) count) / (((double) (this.steps - 1) * this.agentsPerTeam) / 100.0)));
					} else {
						out.write("&&");
					}
				}
				out.write("\\\\" + ls); // next row
			}

			// write the footer
			out.write("\\hline" + ls + "\\end{tabular}" + ls + "\\end{document}");

			// cleanup
			out.flush();
			out.close();
		} catch (IOException e) {
			DebugLog.log(DebugLog.LOGLEVEL_ERROR, "Couldn't write table of failed actions.");
			e.printStackTrace();
		}
	}

	private void writeTeamResults(FileWriter wr) throws IOException {

		for (TeamState ts : this.worldState.teamsStates) {

			String team = ts.name;

			wr.write("Statistics for Team " + team + this.lineSep);

			writeOverallResult(wr, team);
			writeZoneStabilities(wr, team);
			writeAchievements(wr, team);
			writeZonesScores(wr, team);
		}

	}

	private void writeAgentResults(FileWriter wr) throws IOException {

		for (GraphSimulationAgentState ag : this.worldState.getAgents()) {
			String name = ag.name;
			wr.write("Statistics for Agent " + name + " of Team " + ag.team + ":" + this.lineSep);

			// write times of all actions performed along with results
			String s = "";
			HashMap<String, Vector<Integer>> acRes = this.agResults.actionResults.get(name);
			for (String ac : acRes.keySet()) {
				s += ac + ": total " + acRes.get(ac).get(0) + ", failed " + acRes.get(ac).get(1) + this.lineSep;
			}
			wr.write(s);

			// write zoneScores
			s = "";
			for (String zs : this.agResults.zoneScores.get(name)) {
				s += zs + " ";
			}
			wr.write("ZoneScores: " + s + this.lineSep);

		}
	}

	private void writeOverallResult(FileWriter wr, String team) throws IOException {

		TeamState ts = this.worldState.getTeamState(team);
		long score = ts.summedScore;
		int ranking = ts.ranking;
		wr.write("Score: " + score + " Ranking: " + ranking);
	}

	private void writeZonesScores(FileWriter wr, String team) throws IOException {

		Vector<String> zs = this.zonesScores.get(team);
		if (zs != null) {
			wr.write(this.lineSep + "ZonesScores:" + this.lineSep);
			for (String s : zs) {
				wr.write(s + " ");
			}
			wr.write(this.lineSep);
		}
	}

	private void writeAchievements(FileWriter wr, String team) throws IOException {

		Vector<String> ach = this.worldState.getTeamState(team).getAchieved();
		wr.write(this.lineSep + "Achievements:" + this.lineSep);
		for (String a : ach) {
			wr.write(a + this.lineSep);
		}
	}

	private void writeZoneStabilities(FileWriter wr, String tsname) throws IOException {
	}

	private void updateZonesScores(GraphSimulationWorldState wstate) {

		for (TeamState ts : wstate.teamsStates) {
			String tname = ts.name;
			Vector<String> zs = this.zonesScores.get(tname);
			if (zs == null) {
				zs = new Vector<>();
				this.zonesScores.put(tname, zs);
			}
			zs.add(String.valueOf(wstate.getTeamState(tname).getAreasValue()));
		}
	}

	private void updateAchievements(GraphSimulationWorldState wstate) {

		for (TeamState ts : wstate.teamsStates) {
			String tname = ts.name;

			// update the achievements
			Vector<String> newAchs = ts.getNewlyAchieved();
			for (String newAch : newAchs) {
				HashMap<String, Integer> achData = this.achievements.get(tname);
				if (achData == null) {
					achData = new HashMap<>();
					this.achievements.put(tname, achData);
				}
				achData.put(newAch, wstate.currentStep);
			}

			// update the current achievement-points
			Vector<Long> achPoints = this.achievementPoints.get(tname);
			if (achPoints == null) {
				achPoints = new Vector<>();
				this.achievementPoints.put(tname, achPoints);
			}
			achPoints.add(ts.currAchievementPoints);
		}
	}

	private void updateBuyingTransactions(GraphSimulationWorldState wstate) {

		for (TeamState ts : wstate.teamsStates) {
			String tname = ts.name;

			// update the buying transactions

			// get new buying transactions
			HashMap<String, String> newBuys = new HashMap<>(ts.getNewlyBought());
			ts.clearBuyingTransactions();
			
			// check whether we have something to do or if we can skip the adding part. 
			if (!newBuys.isEmpty()) {

				// get buying transactions for the current step
				HashMap<String, Vector<AgentBuyingTransaction>> buyData = this.buyingTransactions.get(wstate.currentStep);

				// add new buying transactions to the current step
				// Does a entry for the current step already exist? otherwise
				// create
				// a new one
				if (buyData == null) {
					buyData = new HashMap<>();
				}

				Vector<AgentBuyingTransaction> buyDataVector = buyData.get(tname);

				if (buyDataVector == null) {
					buyDataVector = new Vector<>();
					buyData.put(tname, buyDataVector);
				}

				// add new buying transactions
				for (Map.Entry<String, String> agent : newBuys.entrySet()) {
					AgentBuyingTransaction newBuy = new AgentBuyingTransaction(agent.getKey(), agent.getValue());
					buyDataVector.add(newBuy);
				}
				this.buyingTransactions.put(wstate.currentStep, buyData);
			}
		}

	}

	private void updateZoneStabilityValues(GraphSimulationWorldState wstate) {

		// initialize counters and stability values-map
		if (!this.nodeStatesInitialized) {
			for (GraphNode n : wstate.nodes) {
				this.dominationCounter.put(n, new NodeState());
			}
			this.nodeStatesInitialized = true;

			this.zoneStabilityValues.put(NodeState.NONE, new Vector<Integer>());
			for (TeamState ts : wstate.teamsStates) {
				this.zoneStabilityValues.put(ts.name, new Vector<Integer>());
			}
		}

		// update nodestates
		for (GraphNode n : wstate.nodes) {
			NodeState ns = this.dominationCounter.get(n);
			if (n.getDominatorTeam() == null) {
				if (ns.lastDominating.equals(NodeState.NONE)) {
					ns.counter++;
				} else {
					ns.counter = 1;
					ns.lastDominating = NodeState.NONE;
				}
			} else {
				if (ns.lastDominating.equals(n.getDominatorTeam())) {
					ns.counter++;
				} else {
					ns.counter = 1;
					ns.lastDominating = n.getDominatorTeam();
				}
			}
		}

		// calculate the sums of the respective counters
		Map<String, Integer> sums = new HashMap<>();
		sums.put(NodeState.NONE, new Integer(0));
		for (TeamState ts : wstate.teamsStates) {
			sums.put(ts.name, new Integer(0));
		}
		for (NodeState n : this.dominationCounter.values()) {
			int i = sums.get(n.lastDominating);
			sums.put(n.lastDominating, i + n.counter);
		}

		// add the new stability-values
		for (String s : sums.keySet()) {
			this.zoneStabilityValues.get(s).add(sums.get(s));
		}
	}

	private void updateSingleAgentValues(GraphSimulationWorldState wstate) {

		for (GraphSimulationAgentState ag : wstate.getAgents()) {

			String lastAction = ag.lastAction;
			String lastActionResult = ag.lastActionResult;
			String zoneScore = String.valueOf(wstate.getTeamState(ag.team).getAreaValue(ag));
			this.agResults.logResults(ag.name, lastAction, lastActionResult, zoneScore);
			this.agResults.logNoAction(ag.team);

			if (lastActionResult.startsWith("failed")) {
				this.failedTeams.add(ag.team);
				HashMap<String, Integer> teams = this.fails.get(lastActionResult);
				if (teams == null) {
					teams = new HashMap<>();
					this.fails.put(lastActionResult, teams);
				}
				Integer count = teams.get(ag.team);
				if (count == null) {
					count = new Integer(0);
					teams.put(ag.team, count);
				}
				teams.put(ag.team, count + 1);
				if (lastAction.equals("noAction")) {
					teams = this.fails.get("noAction");
					if (teams == null) {
						teams = new HashMap<>();
						this.fails.put("noAction", teams);
					}
					count = teams.get(ag.team);
					if (count == null) {
						count = new Integer(0);
						teams.put(ag.team, count);
					}
					teams.put(ag.team, count + 1);
				}
			}
		}
	}

	/**
	 * Uses JFreeChart to draw some awesome charts that contain statistics
	 */
	private void drawCharts(String basefilename, boolean scoreChart, boolean zoneStabilityChart, boolean zonesScoresChart, boolean achievementPointsChart, boolean achievementCategoriesChart, boolean actionsChart, boolean achievementsTable, boolean buyingTransactionsTable,
			boolean assignLabelsToAchievementPoints, int width, int height) {

		if (scoreChart) {
			XYSeriesCollection sers = new XYSeriesCollection();

			for (String tname : this.scores.keySet()) {
				double stepCount = .0d;
				XYSeries series = new XYSeries("Summed Score for Team " + tname);
				series.add(0, 0);
				for (Long score : this.scores.get(tname)) {
					series.add(stepCount, score);
					stepCount++;
				}
				sers.addSeries(series);
			}

			// create an XYLineChart based on the dataset
			JFreeChart chart = ChartFactory.createXYLineChart("Summed Scores", // title
					"Step", // X label
					"Summed Score", // Y label
					sers, PlotOrientation.VERTICAL, true, // legend
					false, // tooltips?
					false); // URLs?

			chart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);

			chart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
			chart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

			// write the chart to file
			try {
				ChartUtilities.saveChartAsPNG(new File(basefilename + "Scores.png"), chart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		XYSeriesCollection zscoreAndAchpts = new XYSeriesCollection();

		if (zoneStabilityChart) {

			Map<String, JFreeChart> charts = new HashMap<>();

			// this will hold every team's zoneStability-series
			XYSeriesCollection sers = new XYSeriesCollection();

			int teamCount = 0;
			// create a series for every team
			for (String tsname : this.zoneStabilityValues.keySet()) {

				if (!tsname.equals("NONE")) {
					double stepCount = .0d;
					XYSeries series = new XYSeries("Zone Stability for Team " + tsname);
					series.add(0, 0);
					for (Integer zsValue : this.zoneStabilityValues.get(tsname)) {
						series.add(stepCount, zsValue);
						stepCount++;
					}
					sers.addSeries(series);

					// create a chart for every team
					XYSeriesCollection teamSeries = new XYSeriesCollection(series);
					JFreeChart zschart = ChartFactory.createXYLineChart("ZoneStabilities for Team " + tsname, // title
							"Step", // X label
							"ZoneStability", // Y label
							teamSeries, PlotOrientation.VERTICAL, true, // legend
							false, // tooltips?
							false);
					zschart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[teamCount]);
					teamCount++;

					charts.put(tsname, zschart);
				}
			}
			// adapt the ranges to the highest range and save the charts to file
			double maxRange = .00;
			for (JFreeChart chart : charts.values()) {
				double range = chart.getXYPlot().getRangeAxis().getRange().getUpperBound();
				if (range > maxRange) {
					maxRange = range;
				}
			}
			for (String tsname : charts.keySet()) {
				JFreeChart chart = charts.get(tsname);
				chart.getXYPlot().getRangeAxis().setUpperBound(maxRange);
				try {
					ChartUtilities.saveChartAsPNG(new File(basefilename + "ZoneStabilities_" + tsname + ".png"), chart, width, height);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// create an XYLineChart based on the dataset for both teams
			JFreeChart zschart = ChartFactory.createXYLineChart("ZoneStabilities", // title
					"Step", // X label
					"ZoneStability", // Y label
					sers, PlotOrientation.VERTICAL, true, // legend
					false, // tooltips?
					false); // URLs?

			zschart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);

			zschart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
			zschart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

			// write the chart to file
			try {
				ChartUtilities.saveChartAsPNG(new File(basefilename + "ZoneStabilities.png"), zschart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (zonesScoresChart) {
			// this will hold every team's zoneScore-series
			XYSeriesCollection sers = new XYSeriesCollection();

			// create a series for every team
			for (String tsname : this.zonesScores.keySet()) {

				double stepCount = .0d;
				XYSeries series = new XYSeries("ZonesScores for Team " + tsname);
				series.add(0, 0);

				for (String zScore : this.zonesScores.get(tsname)) {
					series.add(stepCount, Double.parseDouble(zScore));
					stepCount++;
				}

				sers.addSeries(series);
			}

			XYSeriesCollection averages = new XYSeriesCollection();

			// compute the moving average from the zoneScores
			for (int i = 0; i < sers.getSeriesCount(); i++) {
				XYSeries avg = MovingAverage.createMovingAverage(sers, i, sers.getSeries(i).getKey() + "_avg", 25, 5);
				avg.add(0, 0);
				zscoreAndAchpts.addSeries(avg);
				averages.addSeries(avg);
			}

			// create an XYLineChart based on the dataset
			JFreeChart zschart = ChartFactory.createXYLineChart("ZonesScores", // title
					"Step", // X label
					"ZonesScore", // Y label
					averages, PlotOrientation.VERTICAL, true, // legend
					false, // tooltips?
					false); // URLs?

			zschart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);

			zschart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
			zschart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

			// write the chart to file
			try {
				ChartUtilities.saveChartAsPNG(new File(basefilename + "ZonesScores.png"), zschart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// this vector will hold the annotations and may be used in the combined
		// chart later on
		Vector<XYPointerAnnotation> annotations = new Vector<>();
		if (achievementPointsChart) {

			// this will hold every team's achievementPoints-series
			XYSeriesCollection sers = new XYSeriesCollection();

			// create a series for every team
			for (String tsname : this.achievementPoints.keySet()) {

				XYSeries series = new XYSeries("Achievement-Points for Team " + tsname);
				series.add(0, 0);
				Vector<Long> pts = this.achievementPoints.get(tsname);

				int stepCount = 1;
				for (Long l : pts) {
					series.add(stepCount, l);
					stepCount++;
				}

				sers.addSeries(series);
				zscoreAndAchpts.addSeries(series);
			}

			// create an XYLineChart based on the dataset
			JFreeChart chart = ChartFactory.createXYLineChart("Achievement-Points", // title
					"Step", // X label
					"Points", // Y label
					sers, PlotOrientation.VERTICAL, true, // legend
					false, // tooltips?
					false); // URLs?

			chart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);

			chart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
			chart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

			if (assignLabelsToAchievementPoints) {
				// add achievement-label-annotations
				for (String tname : this.achievements.keySet()) {
					HashMap<String, Integer> achs = this.achievements.get(tname);
					Vector<Long> teamAPoints = this.achievementPoints.get(tname);
					for (String ach : achs.keySet()) {
						int step = achs.get(ach);

						// shorten the identifier
						String label = trimAchievement(ach);

						XYPointerAnnotation pa = null;
						if (step < this.worldState.getConfig().maxNumberOfSteps / 10) {
							// right positioning
							pa = new XYPointerAnnotation(label, step, teamAPoints.get(step - 1), 6);
						} else {
							// normal (left) positioning
							pa = new XYPointerAnnotation(label, step, teamAPoints.get(step - 1), 4);
						}
						pa.setLabelOffset(5);
						annotations.add(pa);
					}
				}
				Collections.sort(annotations, new Comparator<XYPointerAnnotation>() {
					@Override
					public int compare(XYPointerAnnotation o1, XYPointerAnnotation o2) {
						if (o1.getX() < o2.getX()) {
							return -1;
						} else if (o1.getX() == o2.getX()) {
							return 0;
						} else {
							return 1;
						}
					}
				});

				double xDist = this.worldState.getConfig().maxNumberOfSteps / 20;
				double yDist = chart.getXYPlot().getRangeAxis().getRange().getUpperBound() / 50;

				for (int i = 0; i < annotations.size(); i++) {
					XYPointerAnnotation a1 = annotations.get(i);
					for (int j = i + 1; j < annotations.size(); j++) {
						XYPointerAnnotation a2 = annotations.get(j);
						// check for intersections
						if (Math.abs(a2.getX() - a1.getX()) < xDist) {
							// X's are near enough, check Y's
							if (Math.abs(a1.getY() - a2.getY()) < yDist) {
								if (a2.getX() < xDist) {
									a2.setAngle(a2.getAngle() - .4);
								} else {
									a2.setAngle(a2.getAngle() + .8);
								}
							}
						} else {
							break; // the following annotations are far away
						}
					}
				}

				// finally add annotations
				for (XYPointerAnnotation a : annotations) {
					chart.getXYPlot().addAnnotation(a);
				}
			}

			// write the chart to file
			try {
				ChartUtilities.saveChartAsPNG(new File(basefilename + "AchievementPoints.png"), chart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (zonesScoresChart && achievementPointsChart) {

			// create an XYLineChart based on the dataset
			JFreeChart chart = ChartFactory.createXYLineChart("ZonesScores and Achievement-Points", // title
					"Step", // X label
					"Points", // Y label
					zscoreAndAchpts, PlotOrientation.VERTICAL, true, // legend
					false, // tooltips?
					false); // URLs?

			chart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);
			chart.getXYPlot().getRenderer().setSeriesStroke(3, this.dashedStroke);

			chart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
			chart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

			chart.getXYPlot().getRenderer().setSeriesPaint(2, this.teamDomColors[0]);
			chart.getXYPlot().getRenderer().setSeriesPaint(3, this.teamDomColors[1]);

			if (assignLabelsToAchievementPoints) {
				// finally add annotations
				for (XYPointerAnnotation a : annotations) {
					chart.getXYPlot().addAnnotation(a);
				}
			}

			// write the chart to file
			try {
				ChartUtilities.saveChartAsPNG(new File(basefilename + "ZonesScoresAndAchievementPoints.png"), chart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (achievementCategoriesChart) {
			// create the XY-STEPchart per achievement-class, for both teams

			// achievementClass -> DataSet
			HashMap<String, XYSeriesCollection> catCollections = new HashMap<>();

			for (String tname : this.achievements.keySet()) {

				// achievementName -> step
				HashMap<String, Integer> achs = this.achievements.get(tname);

				// achievementClass -> Series
				HashMap<String, XYSeries> catSers = new HashMap<>();

				for (String aName : achs.keySet()) {
					Achievement a = this.achievementsConfMap.get(aName);
					XYSeries series = catSers.get(a.achievementClass);
					if (series == null) {
						series = new XYSeries(a.achievementClass + "-Achievements for Team " + tname);
						series.add(0, 0);
						catSers.put(a.achievementClass, series);
					}
					series.add(achs.get(aName), ((Number) a.quantity));
				}

				for (String cat : catSers.keySet()) {
					XYSeriesCollection serCol = catCollections.get(cat);
					if (serCol == null) {
						serCol = new XYSeriesCollection();
						catCollections.put(cat, serCol);
					}
					serCol.addSeries(catSers.get(cat));
				}
			}

			// finally create a chart for every collection of categorized
			// achievements
			for (String achievementClass : catCollections.keySet()) {

				XYSeriesCollection sc = catCollections.get(achievementClass);
				@SuppressWarnings("unchecked")
				List<XYSeries> series = sc.getSeries();
				for (XYSeries s : series) {
					s.add(this.worldState.getConfig().maxNumberOfSteps, ((XYDataItem) s.getItems().get(s.getItemCount() - 1)).getY());
				}

				// create an XYStepChart based on the dataset
				JFreeChart chart = ChartFactory.createXYStepChart(achievementClass + "-Achievements", // title
						"Step", // X label
						"Quantity", // Y label
						sc, PlotOrientation.VERTICAL, true, // legend
						false, // tooltips?
						false); // URLs?

				chart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);

				chart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
				chart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);

				chart.getXYPlot().setDomainAxis(new NumberAxis("Step"));

				// write the chart to file
				try {
					ChartUtilities.saveChartAsPNG(new File(basefilename + achievementClass + "Achievements.png"), chart, width, height);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (achievementsTable) {
			/*
			 * create a latex-file containing a table containing all
			 * achievements that were achieved in the course of the simulation
			 * the table looks sth like: | step | teamA | teamB |
			 */

			Vector<Vector<String>> rowData = new Vector<>();

			// temp map for storing achievements per step
			HashMap<Integer, Vector<String>> tempMap = new HashMap<>();

			Vector<String> teamNames = new Vector<>();
			teamNames.addAll(this.achievements.keySet());

			String tabFormat = "|c|";
			for (int i = 0; i < teamNames.size(); i++) {
				tabFormat += "c|";
			}

			// let the counter begin with 1, because position 0 contains the
			// step
			int teamCounter = 1;
			for (String teamName : teamNames) {
				HashMap<String, Integer> teamAchievements = this.achievements.get(teamName);

				for (String a : teamAchievements.keySet()) {
					int step = teamAchievements.get(a);
					// get the respective vector for the step
					Vector<String> stepVector = tempMap.get(step);
					if (stepVector == null) {
						stepVector = new Vector<>();
						// make sure the step is the first entry in the vector
						stepVector.add(String.valueOf(step));
						// make sure that the vector can be accessed anywhere
						for (int i = 0; i < teamNames.size(); i++) {
							stepVector.add(null);
						}
						tempMap.put(step, stepVector);
					}
					// get the maybe already existing entry
					String tableEntry = stepVector.get(teamCounter);
					if (tableEntry == null) {
						tableEntry = a;
					} else {
						tableEntry += ", " + a;
					}
					stepVector.remove(teamCounter);
					stepVector.add(teamCounter, tableEntry);
				}

				// increase teamcounter, so that following entries will be
				// placed in the next column
				teamCounter++;
			}

			rowData.addAll(tempMap.values());

			// sort by steps
			Collections.sort(rowData, new Comparator<Vector<String>>() {

				@Override
				public int compare(Vector<String> o1, Vector<String> o2) {
					int i = Integer.parseInt(o1.get(0));
					int j = Integer.parseInt(o2.get(0));
					if (i < j) {
						return -1;
					}
					if (i == j) {
						return 0;
					} else {
						return 1;
					}
				}
			});

			File texFile = new File(basefilename + "achievementsTable.tex");
			try {
				texFile.createNewFile();
				FileWriter out = new FileWriter(texFile);

				// write the header
				out.write("\\documentclass{article}" + ls + "\\begin{document}" + ls + "\\begin{tabular}{" + tabFormat + "}" + ls + "\\hline" + ls);

				out.write("Step");
				for (String teamName : teamNames) {
					out.write("&" + teamName);
				}
				out.write("\\\\" + ls + "\\hline");

				for (Vector<String> row : rowData) {
					out.write(ls + row.get(0));
					for (int i = 1; i < row.size(); i++) {
						out.write("&" + (row.get(i) == null ? "" : row.get(i)));
					}
					out.write("\\\\");
				}

				// write the footer
				out.write("\\hline" + ls + "\\end{tabular}" + ls + "\\end{document}");

				// cleanup
				out.flush();
				out.close();
			} catch (IOException e) {
				DebugLog.log(DebugLog.LOGLEVEL_ERROR, "Couldn't write table of achievements.");
				e.printStackTrace();
			}

		}

		if (buyingTransactionsTable) {
			/*
			 * create a LaTeX file containing a table containing all buying
			 * transactions that were achieved in the course of the simulation
			 * table1: | step | teamA | teamB | one cell: agent:item
			 */

			// create LaTeX document

			String tableOutput = new String();
			tableOutput = "\\documentclass{article}" + ls + "\\usepackage{tabularx}" + ls + "\\begin{document}" + ls + "\\begin{tabularx}{\\textwidth}{l|";
			Vector<String> teamNames = new Vector<>();

			for (TeamState ts : this.worldState.teamsStates) {
				teamNames.add(ts.name);
				// create header
				tableOutput += "X|";
			}

			tableOutput += "}" + ls + "\\hline" + ls + "Step";

			for (String teamName : teamNames) {
				tableOutput += "&" + teamName;
			}
			tableOutput += "\\\\" + ls + "\\hline";

			// write lines
			for (Map.Entry<Integer, HashMap<String, Vector<AgentBuyingTransaction>>> line : this.buyingTransactions.entrySet()) {
				tableOutput += line.getKey();
				for (String teamName : teamNames) {
					HashMap<String, Vector<AgentBuyingTransaction>> lineValue = line.getValue();

					tableOutput += "&";
					if (lineValue != null) {
						Vector<AgentBuyingTransaction> vEntries = lineValue.get(teamName);
						if (vEntries != null) {
							for (AgentBuyingTransaction vEntry : vEntries) {
								tableOutput += vEntry.agent + ":" + vEntry.item + " ";
							}
						}
					}
				}
				tableOutput += "\\\\" + "\\hline";
			}

			// write footer
			tableOutput += ls + "\\end{tabularx}" + ls + "\\end{document}";

			// write file to disk
			File texFile = new File(basefilename + "buyingTransactionTable.tex");
			try {
				texFile.createNewFile();
				FileWriter out = new FileWriter(texFile);

				out.write(tableOutput);
				// cleanup
				out.flush();
				out.close();
			} catch (IOException e) {
				DebugLog.log(DebugLog.LOGLEVEL_ERROR, "Couldn't write table of buying transactions.");
				e.printStackTrace();
			}
		}

		if (actionsChart) {

			Map<String, JFreeChart> charts = new HashMap<>();

			// create the bar-charts containing the frequency of actions per
			// role per team
			DecimalFormat df = new DecimalFormat("#.##");

			// calculate the total number of actions performed by each team
			HashMap<String, Integer> overallActionCounts = new HashMap<>();
			for (String agName : this.agResults.actionResults.keySet()) {
				String team = this.worldState.agentNamesMap.get(agName).team;
				int count = 0;
				for (Vector<Integer> v : this.agResults.actionResults.get(agName).values()) {
					count += v.get(0);
				}
				Integer i = overallActionCounts.get(team);
				if (i == null) {
					i = new Integer(0);
				}
				i += count;
				overallActionCounts.put(team, i);
			}

			// prepare the datastructure that will hold the values for the
			// charts
			// teamname -> rolename -> actionname -> sums
			HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> res = // I'm
																						// not
																						// proud
																						// of
																						// this
			new HashMap<>();
			for (TeamState ts : this.worldState.teamsStates) {

				HashMap<String, HashMap<String, ArrayList<Integer>>> tempmap = new HashMap<>();
				for (String rolename : this.worldState.config.rolesConfMap.keySet()) {

					HashMap<String, ArrayList<Integer>> map = new HashMap<>();

					Set<String> actions = new HashSet<>();
					actions.addAll(this.worldState.config.actionsConfMap.keySet());
					actions.add("skip");

					for (String actionname : actions) {
						ArrayList<Integer> al = new ArrayList<>();
						for (int k = 0; k < 5; k++) {
							al.add(0);
						}
						map.put(actionname, al);
					}

					tempmap.put(rolename, map);
				}
				res.put(ts.name, tempmap);
			}

			// fill the above prepared structure with values
			// agentname -> (actionname -> numbers)
			HashMap<String, HashMap<String, Vector<Integer>>> ar = this.agResults.actionResults;

			for (String agentName : ar.keySet()) {
				GraphSimulationAgentState agent = this.worldState.agentNamesMap.get(agentName);
				HashMap<String, Vector<Integer>> actionResults = ar.get(agentName);
				HashMap<String, ArrayList<Integer>> targetMap = res.get(agent.team).get(agent.roleName);
				for (String actionName : actionResults.keySet()) {
					ArrayList<Integer> actionSums = targetMap.get(actionName);
					if (actionSums == null) {
						actionSums = new ArrayList<>();
						for (int i = 0; i < 5; i++) {
							actionSums.add(0);
						}
						targetMap.put(actionName, actionSums);
					}
					Vector<Integer> sle = actionResults.get(actionName);
					for (int i = 0; i < 4; i++) {
						int c = actionSums.get(i);
						actionSums.set(i, c + sle.get(i));
					}
				}
			}

			for (String teamname : res.keySet()) {
				HashMap<String, HashMap<String, ArrayList<Integer>>> teamRes = res.get(teamname);
				// make a chart for every role
				for (String rolename : teamRes.keySet()) {

					HashMap<String, ArrayList<Integer>> roleRes = teamRes.get(rolename);
					RoleConfiguration role = this.worldState.config.rolesConfMap.get(rolename);

					// one dataset for each role
					DefaultCategoryDataset dataset = new DefaultCategoryDataset();

					double roleActionsFrequencySum = 0;

					// count all the actions, so that failed_role is respected
					// as well
					for (ArrayList<Integer> a : roleRes.values()) {
						roleActionsFrequencySum += a.get(0);
					}

					// list all actions that are available to this role (may be
					// 0 times)
					Vector<String> roleActions = new Vector<>();
					roleActions.addAll(role.actions);
					roleActions.add("skip");
					for (String availAction : roleActions) {

						ArrayList<Integer> v = roleRes.get(availAction);
						int frequency = v.get(0);
						int timesSucc = v.get(1);

						dataset.setValue(timesSucc, "Succeeded", availAction);
						// stack the remainder on top
						dataset.setValue(frequency - timesSucc, "Frequency", availAction);
					}

					JFreeChart chart = ChartFactory.createStackedBarChart(teamname + " " + rolename + " Actions", // Title
							"Actions", // X-Axis label
							"Performance", // Y-Axis label
							dataset, // Dataset
							PlotOrientation.VERTICAL, // Orientation
							true, // Show legend
							false, // Tooltips
							false // URL
							);

					CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
					renderer.setSeriesPaint(0, Color.GREEN);
					renderer.setSeriesOutlineStroke(1, this.dashedStroke);

					double range = chart.getCategoryPlot().getRangeAxis().getRange().getLength();
					chart.getCategoryPlot().getRangeAxis().resizeRange(1.2);

					// annotations for absolute and percentual values
					for (String availAction : roleActions) {

						ArrayList<Integer> v = roleRes.get(availAction);
						int frequency = v.get(0);
						double rolePercentage = frequency / (roleActionsFrequencySum / 100d);

						// annotation for rate and percentage of action
						CategoryTextAnnotation ann = new CategoryTextAnnotation(frequency + " / " + df.format(rolePercentage) + "%", availAction, frequency + (range / 20));
						ann.setPaint(Color.BLUE);
						chart.getCategoryPlot().addAnnotation(ann);

						// annotation for succeeded rate and percentage
						ann = new CategoryTextAnnotation(v.get(1) + " / " + df.format(v.get(1) / (roleActionsFrequencySum / 100d)) + "%", availAction, frequency + (range / 40));
						ann.setPaint(Color.green);
						chart.getCategoryPlot().addAnnotation(ann);

						// annotations for overall rate and percentage of
						// succeeded actions
						double overallPercentage = (double) frequency / ((double) overallActionCounts.get(teamname) / 100d);
						double overAllSucceededPercentage = rolePercentage / (overallActionCounts.get(teamname) / 100);

						ann = new CategoryTextAnnotation("ov.a: " + df.format(overallPercentage) + "%", availAction, 0 - (range / 40));
						ann.setPaint(Color.BLUE);
						chart.getCategoryPlot().addAnnotation(ann);

						ann = new CategoryTextAnnotation("ov.a(succ.): " + df.format(overAllSucceededPercentage) + "%", availAction, 0 - (range / 20));
						ann.setPaint(Color.BLUE);
						chart.getCategoryPlot().addAnnotation(ann);
					}

					// list failed_role actions in legend
					String failedRoleActions = "none";
					for (String action : roleRes.keySet()) {
						ArrayList<Integer> a = roleRes.get(action);
						if (a.get(3) > 0) {
							failedRoleActions = failedRoleActions.replace("none", "");
							failedRoleActions += " " + action;
						}
					}

					LegendItemCollection chartLegend = new LegendItemCollection();
					Shape shape = new Rectangle(10, 10);
					chartLegend.add(new LegendItem("Failed_Role: " + failedRoleActions, null, null, null, shape, Color.WHITE));

					chartLegend.addAll(chart.getCategoryPlot().getLegendItems());

					chart.getCategoryPlot().setFixedLegendItems(chartLegend);

					// try {
					// ChartUtilities.saveChartAsPNG(
					// new
					// File(basefilename+teamname+"_"+rolename+"_Actions_oldVersion.png"),
					// chart, width, height);
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					charts.put(basefilename + teamname + "_" + rolename + "_Actions.png", chart);
				}
			}

			double maxRange = .00;
			for (JFreeChart chart : charts.values()) {

				double range = chart.getCategoryPlot().getRangeAxis().getRange().getUpperBound();
				if (range > maxRange) {
					maxRange = range;
				}
			}
			for (JFreeChart chart : charts.values()) {
				chart.getCategoryPlot().getRangeAxis().setRange(-1. * maxRange / 12., maxRange);
			}
			for (String name : charts.keySet()) {
				try {
					ChartUtilities.saveChartAsPNG(new File(name), charts.get(name), width, height);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // end of actionschart
	}

	/**
	 * Shortens the name of an achievement (e.g. at40 for attacked 40)
	 */
	private String trimAchievement(String label) {

		if (label.startsWith("at")) {
			label = label.replace("attacked", "at");
		} else if (label.startsWith("i")) {
			label = label.replace("inspected", "i");
		} else if (label.startsWith("s")) {
			label = label.replace("surveyed", "s");
		} else if (label.startsWith("ar")) {
			label = label.replace("area", "ar");
		} else if (label.startsWith("pr")) {
			label = label.replace("proved", "pr");
		} else if (label.startsWith("pa")) {
			label = label.replace("parried", "pa");
		}
		return label;
	}

	/**
	 * Datastructure for storing values and results of all agents
	 * 
	 * @author tobi
	 */
	class SingleAgentResults {

		public HashMap<String, Vector<String>> zoneScores;
		public HashMap<String, HashMap<String, Vector<Integer>>> actionResults;
		public HashMap<String, Integer> noActions;

		public SingleAgentResults(Set<String> agNames) {

			// initialize maps
			this.zoneScores = new HashMap<>();
			this.actionResults = new HashMap<>();
			this.noActions = new HashMap<>();

			// setup dataset for every agent
			for (String agName : agNames) {
				this.zoneScores.put(agName, new Vector<String>());

				HashMap<String, Vector<Integer>> actRes = new HashMap<>();

				this.actionResults.put(agName, actRes);
			}
		}

		public void logNoAction(String team) {
			Integer noa = this.noActions.get(team);
			if (noa == null) {
				noa = 0;
			}
			this.noActions.put(team, noa + 1);
		}

		public void logResults(String agName, String lAct, String lActResult, String zoneScore) {

			this.zoneScores.get(agName).add(zoneScore);

			Vector<Integer> v = this.actionResults.get(agName).get(lAct);

			/*
			 * Structure of the Actions-Vector: 0: frequency 1: successful
			 * actions 2: failed 3: failed_role
			 */
			if (v == null) {
				v = new Vector<>();
				for (int i = 0; i < 4; i++) {
					v.add(0);
				}
				this.actionResults.get(agName).put(lAct, v);
			}

			int all = v.get(0);
			v.set(0, all + 1);

			if (lActResult.equals("successful")) {
				int successful = v.get(1);
				v.set(1, successful + 1);
			} else {
				if (lActResult.contains("failed")) {
					int failed = v.get(2);
					v.set(2, failed + 1);

					if (lActResult.equals("failed_role")) {
						int failedRole = v.get(3);
						v.set(3, failedRole + 1);
					}
				}
			}
		}
	}

	/**
	 * Data structure mainly for storing the domination counter
	 */
	class NodeState {
		public static final String NONE = "NONE";

		public int counter = 0;
		public String lastDominating = NodeState.NONE;
	}

	/**
	 * Data structure for the items bought by a particular agent.
	 * 
	 */
	private class AgentBuyingTransaction {
		
		public AgentBuyingTransaction(String ag, String item) {
			
			this.item = item;
			this.agent = ag;
		}

		public String item;
		public String agent;

		public String toString() {
			return this.agent + ": " + this.item;
		}
	}
}

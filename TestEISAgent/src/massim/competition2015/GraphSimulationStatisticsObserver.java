package massim.competition2015;

import massim.competition2015.configuration.MapSimulationConfiguration;
import massim.competition2015.scenario.TeamState;
import massim.framework.Observer;
import massim.framework.SimulationConfiguration;
import massim.framework.SimulationState;
import massim.framework.simulation.SimulationStateImpl;
import massim.framework.util.DebugLog;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class GraphSimulationStatisticsObserver implements Observer {
	
	/* 
	 * TODO 2015: Statistics/visualization. (move to statistics generator)
	 */

    private boolean works = true;

	private MapSimulationWorldState worldState;
	private int steps = 0;
	private int agentsPerTeam;



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
        super();
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
		this.works = checkFonts();
	}

	@Override
	public void notifySimulationEnd() {

        if(!this.works) return;

		long time = System.currentTimeMillis();
		// final output
		DebugLog.log(DebugLog.LOGLEVEL_NORMAL, "Generating Statistics");

		writeStatisticsToFile();

		DebugLog.log(DebugLog.LOGLEVEL_DEBUG, "Finished Statistics in " + (System.currentTimeMillis() - time) / 1000 + " second(s)");
	}

	@Override
	public synchronized void notifySimulationState(SimulationState state) {

        if(!this.works) return;

		this.steps++;

		SimulationStateImpl tcstate = (SimulationStateImpl) state;
		MapSimulationWorldState wstate = (MapSimulationWorldState) tcstate.simulationState;
		if (wstate.currentStep > -1) {
			if (this.worldState == null) {
				this.worldState = wstate;

				this.agentsPerTeam = wstate.agents.size() / 2;
			}

			//TODO 2015 log relevant wstate data
            List<MapSimulationAgentState> agents = wstate.getAgents();
            for(MapSimulationAgentState agent: agents){
                //TODO 2015: remove this and make it externally? (read from xmls)
            }
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

	@Override
	public void notifySimulationConfiguration(SimulationConfiguration simconf) {
		massim.competition2015.configuration.MapSimulationConfiguration conf = ((massim.competition2015.configuration.MapSimulationConfiguration) simconf);

		// prepare outputpath
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String simulationName = conf.simulationName;
		this.outputPath = this.outputPath + fs + simulationName + "_" + df.format(dt) + fs;

		// prepare the header of the textfile containing general info
		prepareHeader(conf);
	}

	private void prepareHeader(MapSimulationConfiguration conf) {

		this.header += "Simulation " + conf.simulationName + " of teams: ";
		for (String tname : new HashSet<>(conf.getTeamNames())) {
			this.header += tname + ", ";
		}
		this.header = this.header.substring(0, this.header.length() - 2) + this.lineSep + "###" + this.lineSep;
		this.header += "RandomSeed: " + conf.randomSeed + " Steps: " + conf.maxNumberOfSteps + this.lineSep + this.lineSep;
	}

	/**
	 * Writes all the accumulated data to file and automatically generates
	 * charts. Should only be called after all the sets of values and results
	 * are calculated and processed.
	 */
	private void writeStatisticsToFile() {

		File dir = new File(this.outputPath);
		dir.mkdirs();

		drawCharts(this.outputPath, this.chartWidth, this.chartHeight);

		createTableOfFailedActions(this.outputPath);

		try {
			File f = new File(this.outputPath + "txtfile");
			f.createNewFile();
			FileWriter wr = new FileWriter(f);

			wr.write(this.header);

            //TODO 2015: write results to file (move to statistics generator)

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

			//TODO 2015: write content (move to statistics generator)

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

	private void writeOverallResult(FileWriter wr, String team) throws IOException {

		TeamState ts = this.worldState.getTeamState(team);
		long score = ts.money;
		int ranking = ts.ranking;
		wr.write("Score: " + score + " Ranking: " + ranking);
	}

	/**
	 * Uses JFreeChart to draw some awesome charts that contain statistics
	 */
	private void drawCharts(String basefilename, int width, int height) {

        //FIXME commented for quick reference
//		if (scoreChart) {
//			XYSeriesCollection sers = new XYSeriesCollection();
//
//			for (String tname : this.scores.keySet()) {
//				double stepCount = .0d;
//				XYSeries series = new XYSeries("Summed Score for Team " + tname);
//				series.add(0, 0);
//				for (Long score : this.scores.get(tname)) {
//					series.add(stepCount, score);
//					stepCount++;
//				}
//				sers.addSeries(series);
//			}
//
//			// create an XYLineChart based on the dataset
//			JFreeChart chart = ChartFactory.createXYLineChart("Summed Scores", // title
//					"Step", // X label
//					"Summed Score", // Y label
//					sers, PlotOrientation.VERTICAL, true, // legend
//					false, // tooltips?
//					false); // URLs?
//
//			chart.getXYPlot().getRenderer().setSeriesStroke(1, this.dashedStroke);
//
//			chart.getXYPlot().getRenderer().setSeriesPaint(0, this.teamDomColors[0]);
//			chart.getXYPlot().getRenderer().setSeriesPaint(1, this.teamDomColors[1]);
//
//			// write the chart to file
//			try {
//				ChartUtilities.saveChartAsPNG(new File(basefilename + "Scores.png"), chart, width, height);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

        //TODO 2015: draw awesome charts (move to statistics generator)
	}
}

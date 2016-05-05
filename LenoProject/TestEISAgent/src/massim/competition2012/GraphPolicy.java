package massim.competition2012;

import java.awt.Color;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import massim.competition2012.monitor.Definitions;
import massim.competition2012.scenario.Achievement;
import massim.competition2012.scenario.GraphEdge;
import massim.competition2012.scenario.GraphNode;
import massim.competition2012.scenario.TeamState;
import massim.server.Server;
import massim.visualization.HandleFileFolder;
import massim.visualization.PreviewSvg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GraphPolicy extends massim.visualization.svg.SvgXmlFile{
	
	//some strings used for file creation
	public static String svgFile="";
	public static String previewFile="";
	private String namePreviewSvg = "SimulationPreview";
	
	private String svgImageHeight = "525";
	private String svgImageWidth = "1020";
	private double scaleFactor = 0;
	
	private String configPath = Server.configurationFilenamePath + System.getProperty("file.separator") + 
		"visualization" + System.getProperty("file.separator");
	
	//should be set later
	private String headInformationFirstLevel = "";
	private String headInformationSecondLevel = "";
	
	//most important parts
	public  Document doc;
	private int numberOfSvgFiles = -1;
	
	private String path = "";
	private String outputFolder = path;
	private String nameOutputFile = "masSim";
	
	//change as you like
	public static final int NODE_RADIUS = 10;
	public static final int AGENT_RADIUS = 10;
	
	public static final int LINE_WIDTH = 50;
	
	public static final Color GREEN = new Color(0,158,115);
	public static final Color BLUE = new Color(0,114,178);
	public static final Color GRAY = new Color(150,150,150,200);
	public static final Color DARKER_GRAY = GRAY.darker();
	public static final Color BRIGHTER_GRAY = GRAY.brighter();
	public static final Color COLOR_TEAM1_ZONE = new Color(0,158,115).darker().darker();
	public static final Color COLOR_TEAM2_ZONE = new Color(0,114,178).darker().darker();
	
	public static final String COLOR_GREEN = makeRGB(GREEN);
	public static final String COLOR_BLUE = makeRGB(BLUE);
	public static final String COLOR_GRAY = makeRGB(GRAY);
	public static final String COLOR_DARKER_GRAY = makeRGB(DARKER_GRAY);
	public static final String COLOR_BRIGHTER_GRAY = makeRGB(BRIGHTER_GRAY);
	public static final String TEAM1_ZONE = makeRGB(COLOR_TEAM1_ZONE);
	public static final String TEAM2_ZONE = makeRGB(COLOR_TEAM2_ZONE);
	
	
	private String[] teamNames;
	
	private static int maxPath = 0;
	
	//profiling
	public Vector<Double> intervals = new Vector<Double>();
	public long maxTime = 0;
	public long minTime = Long.MAX_VALUE;
	
	
	//#### methods ####
	
	
	public void create() {
		doc = createXML();
		
		//add stylesheet
		Node pi = doc.createProcessingInstruction
		         ("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");
		doc.insertBefore(pi, doc.getDocumentElement());
	}
	
	/**
	 * Draws the graph/topology
	 */
	public void drawGraph(String string, int sizeX, int sizeY,
			Vector<GraphNode> nodes, Vector<GraphEdge> edges) {
		
		setBackground(sizeX, sizeY);
		
		for (GraphEdge edge : edges){
			drawEdge(edge);
		}
		
		for (GraphNode node : nodes){
			drawNode(node);
		}
	}

	private void setBackground(long width, long height) {
		
		copy(configPath, outputFolder, "Surface.svg");
		String url = "Surface.svg#surface";
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		Element img = doc.createElement("use");
		img.setAttribute("x", "0");
		img.setAttribute("y", "0");
		img.setAttribute("transform", "scale("+String.valueOf(width/283.46)+")");
		img.setAttribute("z-index", "0");
		img.setAttribute("xlink:href", url);
		
		group.appendChild(img);
	}
	
	
	public void drawEdge(GraphEdge e) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		int width = 2 + 1 * e.weight;
		
		Element edge = doc.createElement("line");
		edge.setAttribute("x1", String.valueOf(e.node1.x));
		edge.setAttribute("y1", String.valueOf(e.node1.y));
		edge.setAttribute("x2", String.valueOf(e.node2.x));
		edge.setAttribute("y2", String.valueOf(e.node2.y));
		edge.setAttribute("style", "stroke-width:"+width+";");
		edge.setAttribute("class", "GrRd");
		
		group.appendChild(edge);	
	}
	
	public void drawEdge(EdgeDummy e) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		int width = 2 + 1 * e.weight;
		
		Element edge = doc.createElement("line");
		edge.setAttribute("x1", String.valueOf(e.node1.x));
		edge.setAttribute("y1", String.valueOf(e.node1.y));
		edge.setAttribute("x2", String.valueOf(e.node2.x));
		edge.setAttribute("y2", String.valueOf(e.node2.y));
		edge.setAttribute("style", "stroke-width:"+width+";");
		edge.setAttribute("class", "GrRd");
		
		group.appendChild(edge);	
	}

	public void drawNode(GraphNode n) {

		int width = 4 + 3 * n.weight;

		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element node = doc.createElement("circle");
		node.setAttribute("cx", String.valueOf(n.x));
		node.setAttribute("cy", String.valueOf(n.y));
		node.setAttribute("r", String.valueOf(width/2));
		node.setAttribute("class", "GrN");

		group.appendChild(node);
	}

	public void drawAgent(GraphSimulationAgentState ag, Color agentColor, int agx, int agy) {
		
		// rendering the action
		if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("attack")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;

			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (agx + radius * Math.sin(angle));
				int y1 = (int) (agy + radius * Math.cos(angle));
				int x2 = (int) (agx - radius * Math.sin(angle));
				int y2 = (int) (agy - radius * Math.cos(angle));
				drawLine(x1, y1, x2, y2, Color.YELLOW.brighter(), 6);
			}
		}
		else if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("parry")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;

			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (agx + radius * Math.sin(angle));
				int y1 = (int) (agy + radius * Math.cos(angle));
				int x2 = (int) (agx - radius * Math.sin(angle));
				int y2 = (int) (agy - radius * Math.cos(angle));
				drawLine(x1, y1, x2, y2, Color.CYAN.brighter(), 6);
			}
		}
		else if ( ag.lastActionResult.equals("successful") && ag.lastAction.equals("repair")  ) {
			int num = 4;
			float radius = Definitions.agentRadius + 8;

			for ( int a = 0 ; a < num ; a ++ ) {
				float angle = a * 3.14249f / num;
				int x1 = (int) (agx + radius * Math.sin(angle));
				int y1 = (int) (agy + radius * Math.cos(angle));
				int x2 = (int) (agx - radius * Math.sin(angle));
				int y2 = (int) (agy - radius * Math.cos(angle));
				drawLine(x1, y1, x2, y2, Color.PINK, 6);
			}
		}
		else {
			Color actionColor = null;
			if ( ag.lastActionResult.equals("successful") ) {
				if ( ag.lastAction.equals("attack") ) {
					actionColor = Color.YELLOW.brighter();
				}
				else if ( ag.lastAction.equals("parry") ) {
					actionColor = Color.CYAN.brighter();
				}
				else if ( ag.lastAction.equals("survey") ) {
					actionColor = Color.GREEN.brighter();
				}
				else if ( ag.lastAction.equals("probe") ) {
					actionColor = Color.GREEN.brighter();
				}
				else if ( ag.lastAction.equals("inspect") ) {
					actionColor = Color.GREEN.brighter();
				}
			}
			else if ( ag.lastActionResult.startsWith("failed") ) {
				actionColor = Color.RED;
			}
			if ( actionColor != null ) {
				int radius = Definitions.agentRadius + 8;
				drawCircle(agx, agy, radius, makeRGB(actionColor), 0, true, makeRGB(actionColor));
			}
		}
		
		// rendering the agent body
		int radius = Definitions.agentRadius;

		String darkerColor = makeRGB(agentColor.darker());
		drawCircle(agx, agy, radius, darkerColor, 5, true, makeRGB(agentColor));

		if ( ag.health == 0 ) {
			
			drawLine(agx+radius, agy+radius, agx-radius, agy-radius, agentColor.darker(), 6);
			drawLine(agx-radius, agy+radius, agx+radius, agy-radius, agentColor.darker(), 6);
		}
	}

	public void drawZones(Collection<GraphNode> nodesIn,
				Collection<EdgeDummy> edgesIn, Color domColor, String style) {
		
			// 1.render polygons
			int depth = 3;
			int maxDepth = 8;
			LinkedList<EdgeDummy> edges = new LinkedList<EdgeDummy>(edgesIn);
			while ( !edges.isEmpty() && depth < maxDepth ) {
				
				LinkedList<EdgeDummy> remove = null;
				for ( EdgeDummy edge : edges) {
	
					LinkedList<EdgeDummy> pathIn = new LinkedList<EdgeDummy>();
					pathIn.add(edge);
					LinkedList<EdgeDummy> pathOut = findPolygon(nodesIn,edges,depth,pathIn);
	
					if ( pathOut != null ) {
						if ( pathOut.size() > 1 ) {
							drawPath(pathOut, style);
							if ( pathOut.size() > maxPath ) {
								maxPath = pathOut.size();
							}
						}
						remove = pathOut;
						break;
					}
					
				}
				if ( remove == null ) {
					depth ++;
				}
				else {
					edges.removeAll(remove);
				}
				
			}
			
			// 2. render colored nodes
			for ( GraphNode node : nodesIn ) {
				drawZoneNode(node, style);
			}
			
			// 3. render colored edges
			boolean draw = true;
			for ( EdgeDummy edge :edgesIn ) {
				if(draw){ //only draw every 2nd edge
					drawZoneEdge(edge, style);
					draw = false;
				}
				else{
					draw = true;
				}
			}
			
			// 4. re-render original nodes
			for ( GraphNode node : nodesIn ) {
				drawNode(node);
			}
			
			// 5. re-render original edges
			draw = true;
			for ( EdgeDummy edge :edgesIn ) {
				if(draw){
					drawEdge(edge);
					draw = false;
				}
				else{
					draw = true;
				}
			}
		
		}

	private static String makeRGB(Color c) {
		return "rgba("+c.getRed()+","+c.getGreen()+","+c.getBlue()+","
				+(float)c.getAlpha()/255f+")";
	}

	private void drawLine(int x1, int y1, int x2, int y2, Color col, int width) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		String colVal = makeRGB(col);
		
		Element line = doc.createElement("line");
		line.setAttribute("x1", String.valueOf(x1));
		line.setAttribute("y1", String.valueOf(y1));
		line.setAttribute("x2", String.valueOf(x2));
		line.setAttribute("y2", String.valueOf(y2));
		line.setAttribute("style", "stroke:"+colVal+";stroke-width:"+width+";");
		
		group.appendChild(line);
	}

	/**
	 * Method to draw a circle with the specified attributes
	 */
	private void drawCircle(int x, int y, int radius, String frameColor, int frameWidth, boolean fill, String fillColor) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element node = doc.createElement("circle");
		node.setAttribute("cx", String.valueOf(x));
		node.setAttribute("cy", String.valueOf(y));
		node.setAttribute("r", String.valueOf(radius));
		node.setAttribute("stroke", frameColor);
		node.setAttribute("stroke-width", String.valueOf(frameWidth));
		if (fill){
			node.setAttribute("fill", fillColor);
		}
		
		group.appendChild(node);
	}
	
	/**
	 * Draws a table containing the following information in the top right corner:
	 * TeamName, Total Score, Step Score, Current Zone Value, Current Achievement Points, Achievements
	 * @param teamsStates - the teams' states
	 * @return the current y-position after drawing the table
	 */
	public int drawFirstTable(Vector<TeamState> teamsStates, long width, int currStep) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element g = doc.createElement("g");
		g.setAttribute("id", "teamTable");
		g.setAttribute("transform", "translate(-100, 150)");
		
		// > headline drawn in static table
		
		int y = 140;
		
		//append current number of steps
		Element text2 = doc.createElement("text");
		text2.setAttribute("x", String.valueOf(width + 250));
		text2.setAttribute("y", String.valueOf(y-65));
		text2.setAttribute("class", "TxtNrml");
		text2.appendChild(doc.createTextNode("Step: "+currStep));
		
		g.appendChild(text2);
		
		// > headlines drawn in static table
		
		y += 40;
		
		for (TeamState ts : teamsStates){
			g.appendChild(createRow(false, y, width, ts.name, String.valueOf(ts.summedScore), 
					String.valueOf(ts.getCurrent()), String.valueOf(ts.getAreasValue()), String.valueOf(ts.currAchievementPoints)));
			y += 40;
		}
		
		group.appendChild(g);
		
		return y;
	}

	/**
	 * Creates a row for the teamTable using the parameters as values
	 */
	private Element createRow(boolean headline, int y, long width, String string0, String string1,
			String string2, String string3, String string4) {
		
		Element text;
		Element tspan;
		
		String anchor = headline? "middle" : "start";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("class", "Tb");
		text.setAttribute("text-anchor", anchor);
		
		if (!headline){
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(width + 250));
			tspan.setAttribute("fill", teamNames[0].equals(string0)? COLOR_GREEN : COLOR_BLUE);
			tspan.appendChild(doc.createTextNode(string0));
			text.appendChild(tspan);
		}
		
		tspan = doc.createElement("tspan");	
		tspan.setAttribute("x", String.valueOf(width + 650));
		tspan.appendChild(doc.createTextNode(string1));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 850));
		tspan.appendChild(doc.createTextNode(string2));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 1050));
		tspan.appendChild(doc.createTextNode(string3));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 1250));
		tspan.appendChild(doc.createTextNode(string4));
		text.appendChild(tspan);
		
		return text;
	}
	
	/**
	 * Draws name, role, current energy, max energy,current health, 
	 * max health, strength and vis range of agent in output "table"
	 * @return current y-position
	 */
	public int drawSecondTable(Vector<GraphSimulationAgentState> agents, long width, int y) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element g = doc.createElement("g");
		g.setAttribute("id", "agentTable");
		g.setAttribute("transform", "translate(-100, 150)");
		
		// > headlines drawn static
		
		y += 30;
		
		// draw the dynamic information
		
		for (GraphSimulationAgentState ag : agents){

			y = createDynamicRow(g, y, width, new String[]{
					String.valueOf(ag.energy) +"/"+ String.valueOf(ag.maxEnergy), 
					String.valueOf(ag.health) +"/"+ String.valueOf(ag.maxHealth), 
					String.valueOf(ag.strength), 
					String.valueOf(ag.visRange),
					String.valueOf(ag.lastAction +" " + ag.lastActionParam +":" + ag.lastActionResult)}, 
					ag,
					ag.lastActionResult.startsWith("failed"));
		}
		
		group.appendChild(g);
		
		return y;
	}

	/**
	 * Renders an edge.
	 * 
	 * @param edge
	 */
	private void drawZoneEdge(EdgeDummy edge, String style) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element line = doc.createElement("line");
		line.setAttribute("x1", String.valueOf(edge.node1.x));
		line.setAttribute("y1", String.valueOf(edge.node1.y));
		line.setAttribute("x2", String.valueOf(edge.node2.x));
		line.setAttribute("y2", String.valueOf(edge.node2.y));
		line.setAttribute("class", style+"e");
		
		group.appendChild(line);
	}

	/**
	 * Renders a node.
	 * 
	 * @param node
	 */
	private void drawZoneNode(GraphNode node, String style) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element elnode = doc.createElement("circle");
		elnode.setAttribute("cx", String.valueOf(node.x));
		elnode.setAttribute("cy", String.valueOf(node.y));
		elnode.setAttribute("r", String.valueOf(LINE_WIDTH/2));
		elnode.setAttribute("class", style+"n");

		group.appendChild(elnode);
	}

	/**
	 * Renders a path.
	 * 
	 * @param pathOut
	 */
	private void drawPath(LinkedList<EdgeDummy> pathOut, String style) {
	
		int[] x = new int[pathOut.size()];
		int[] y = new int[pathOut.size()]; 
		for ( int a = 0 ; a < pathOut.size() ; a++ ) {
			x[a] = pathOut.get(a).node1.x;
			y[a] = pathOut.get(a).node1.y;
		}
		drawPolygon(x, y, style);
	
	}

	private void drawPolygon(int[] x, int[] y, String style) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		String points="";
		
		for(int i = 0; i < x.length; i++){
			points += x[i]+","+y[i]+" ";
		}
		
		Element p = doc.createElement("polygon");
		p.setAttribute("points", points);
		p.setAttribute("class", style);
		
		group.appendChild(p);
		
	}

	private int createRow(Element g, int y, long width, String[] entries, 
			GraphSimulationAgentState ag) {
		
		Element text;
		Element tspan;
		
		String anchor = "middle";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("class", "Tb2");
		text.setAttribute("text-anchor", anchor);
		
		long x = width + 250;
		
		x += 100;
		
		for (String s : entries){
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(x));
			tspan.appendChild(doc.createTextNode(s));
			text.appendChild(tspan);
			x += 115;
		}
		
		g.appendChild(text);
		
		return y+30;
	}
	
	/**
	 * append static information (ag-name, role, strength, vis-range) to 2nd table
	 */
	private int createStaticRow(Element g, int y, long width, String[] entries, 
			GraphSimulationAgentState ag) {
		
		Element text;
		Element tspan;
		
		String anchor = "start";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("class", "Tb2");
		text.setAttribute("text-anchor", anchor);
		
		long x = width + 250;
		
		//append agentnumber in team-color
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(x));
		tspan.setAttribute("fill", teamNames[0].equals(ag.team)? COLOR_GREEN : COLOR_BLUE);
		
		String agentNumber;
		try {
			Integer.parseInt(ag.name.substring(ag.name.length()-2));
			agentNumber = ag.name.substring(ag.name.length()-2);
		} catch(NumberFormatException e){
			agentNumber = ag.name.substring(ag.name.length()-1);
		}
		
		tspan.appendChild(doc.createTextNode(agentNumber));
		text.appendChild(tspan);
			
		x += 100;
		
		for (String s : entries){
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(x));
			tspan.appendChild(doc.createTextNode(s));
			text.appendChild(tspan);
			x += 115;
		}
		
		g.appendChild(text);
		
		return y+30;
	}
	
	private int createDynamicRow(Element g, int y, long width, String[] entries, 
			GraphSimulationAgentState ag, boolean failed) {
		
		Element text;
		Element tspan;
		
		String anchor = "start";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("class", "Tb2");
		text.setAttribute("text-anchor", anchor);
		
		long x = width + 250;
		
		x += 215; // width of static part (100 + 1*115)
		
		for (int i = 0; i < entries.length; i++){
			String s = entries[i];
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(x));
			if(i == 4 && failed){
				tspan.setAttribute("fill", "red");
			}
			tspan.appendChild(doc.createTextNode(s));
			text.appendChild(tspan);
			x += 115;
		}
		
		g.appendChild(text);
		
		return y+30;
	}

	public void listAchievements(int y, long width, Vector<TeamState> teamsStates) {
		
		y += 15;
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element g = doc.createElement("g");
		g.setAttribute("id", "achievementPart");
		g.setAttribute("transform", "translate(-100, 150)");
		
		Element text;
		Element tspan;
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("class", "TxtAch");
		
		//achievements caption
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 250));
		tspan.appendChild(doc.createTextNode("Achievements"));
		text.appendChild(tspan);
		
		long x = width + 250;
		
		for (TeamState ts : teamsStates){
			
			int newY = y+30;
			
			//teamname caption
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(x));
			tspan.setAttribute("y", String.valueOf(newY));
			tspan.setAttribute("fill", ts.name.equals(this.teamNames[0])? COLOR_GREEN : COLOR_BLUE);
			tspan.appendChild(doc.createTextNode(ts.name));
			text.appendChild(tspan);
			
			//achievements
			for (Achievement ach : ts.achievements){
				
				if(ach.achieved){
					newY+=20;
					tspan = doc.createElement("tspan");
					tspan.setAttribute("x", String.valueOf(x));
					tspan.setAttribute("y", String.valueOf(newY));
					tspan.appendChild(doc.createTextNode(ach.name));
					text.appendChild(tspan);
				}
			}
			
			x += 500;
		}
		
		g.appendChild(text);
		group.appendChild(g);
	}

	public void save(double internalWidth, double internalHeight) {
		
		String currentFile = outputFolder + nameOutputFile + "-" + numberOfSvgFiles + svgEnding;
		
		Element rootDoc = doc.getDocumentElement();
		
		Element scaleElement = doc.getElementById("scaleSvg");
		scaleElement.setAttribute("transform", "scale(" + Double.toString(this.getScaleFactor(svgImageHeight, svgImageWidth, internalHeight, internalWidth)) + ")");
		rootDoc.appendChild(scaleElement);
		
		saveXML(doc, currentFile);
		
		//svgFile=currentFile;
		svgFile= nameOutputFile + "-" + numberOfSvgFiles + svgEnding;
		previewFile = outputFolder+namePreviewSvg+svgEnding;

	}
	
	private double getScaleFactor(String imageHeight, String imageWidth, double internalHeight, double internalWidth) {
		if (this.scaleFactor == 0) { 
			double scaleHeight = Double.parseDouble(imageHeight) / internalHeight;
			double scaleWidth = Double.parseDouble(imageWidth) / internalWidth;
			if (scaleHeight < scaleWidth) {
				this.scaleFactor = scaleHeight;
				return scaleHeight;
			} else {
				this.scaleFactor = scaleWidth;
				return scaleWidth;
			}
		} else {
			return this.scaleFactor;
		}
	}


	
	public Document createXML() {
		/* call svg.SvgXmlFile.generateXML */
		Document document = generateXML();
		numberOfSvgFiles  = numberOfSvgFiles + 1;
		return document;
	}

	/**
	 * Method creates preview-SVG when called; should be called at the end of the match
	 * because it needs the number of SVGs created
	 */
	public void createPreviewSvg() {
	
		PreviewSvg pre = new PreviewSvg();
	
		pre.setImageHeight(svgImageHeight);
		pre.setImageWidth(svgImageWidth);
		pre.createPreviewSvg(outputFolder, configPath, numberOfSvgFiles,
				headInformationFirstLevel, headInformationSecondLevel);
	}

	public void createFolder(String name) {
		
		HandleFileFolder folder = new HandleFileFolder();
		folder.createFolder(path + name);
		this.setOutputFolder(path + name + System.getProperty("file.separator"));
	}

	private void setOutputFolder(String newPath) {
		this.outputFolder = newPath;		
	}

	public void setTeamNames(Vector<String> teamNames2) {
		this.teamNames = teamNames2.toArray(new String[teamNames2.size()]);
		
		//set header information
		this.headInformationFirstLevel = "Graph Simulation 2012";
		this.headInformationSecondLevel = GraphSimulationVisualizationObserver.simulationName;
	}
	
	public String getTeamOne(){
		return this.teamNames[0];
	}

	private void copy(String inputPath, String outputPath, String name) {
	    try {
			FileReader in = new FileReader(inputPath + name);
			FileWriter out = new FileWriter(outputPath + name);
			int c;
			while ((c = in.read()) != -1)
			  out.write(c);
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
	 		e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
														
	}

	/**
	 * Finds a polygon. This method is used recursively. It either returns
	 * null if there is no set of edges to be removed, it return a single
	 * edge if the edge has no predecessor or successor, or it returns a
	 * path describing a polygon of the desired length.
	 * 
	 * @param nodes the set of nodes of the graph.
	 * @param edges the set of edges of the graph.
	 * @param depth the desired size of the polygon.
	 * @param path the current path.
	 * @return
	 */
	private LinkedList<EdgeDummy> findPolygon(
			Collection<GraphNode> nodes, LinkedList<EdgeDummy> edges,
			int depth, LinkedList<EdgeDummy> path) {

		//System.out.println("Path(" + depth + "):" + path);
		
		// should line be removed?
		// a line is supposed to be removed if it has no predecessor or successor
		if ( path.size() == 1 ) {
			EdgeDummy edge = path.getFirst();
			boolean hasPred = edge.hasPred();
//			for ( EdgeDummy cand : edges ) {
//				if ( edge.equals(cand) ) continue;	//same edge
//				if ( cand.node1.equals(edge.node2) && cand.node2.equals(edge.node1) ) continue; // reverse edge
//				if ( cand.node2.equals(edge.node1) ) {
//					hasPred = true;
//					break;
//				}
//			}
			boolean hasSucc = edge.hasSucc();
//			for ( EdgeDummy cand : edges ) {
//				if ( edge.equals(cand) ) continue;	//same edge
//				if ( cand.node1.equals(edge.node2) && cand.node2.equals(edge.node1) ) continue; // reverse edge
//				if ( cand.node1.equals(edge.node2) ) {
//					hasSucc = true;
//					break;
//				}
//			}
			if ( !( hasPred && hasSucc ) ) return path; // remove this line
		}
		
		// the path has the desired length. finished?
		if ( path.size() == depth) {
			EdgeDummy firstEdge = path.getFirst();
			EdgeDummy lastEdge = path.getLast();
			// required: the path is a circle and it is oriented clockwise
			// assumed: the path is convex
			if ( firstEdge.node1.equals(lastEdge.node2) && isPolygonClockwise(path) ) {
				return path;
			}
			else {
				return null;
			}
		}
		
		// not finished yet
		// consider the successors of the latest edge an see if a path can be found
		// search all candidates = all remaining edges
//		for ( EdgeDummy cand : edges ) { 
//			
//			// reject all invalid candidates
//			if ( path.contains(cand) ) continue; // already in path -> ignore
//			EdgeDummy last = path.getLast();
//			if ( cand.equals(last) ) continue; // self -> ignore
//			if ( cand.node1.equals(last.node2) && cand.node2.equals(last.node1) ) continue; // loop -> ignore
//			if ( !last.node2.equals(cand.node1) ) continue; // not connected -> ignore
//			
//			// the canditate is valid -> make a new path and continue search
//			LinkedList<EdgeDummy> newPath = new LinkedList<EdgeDummy>();
//			newPath.addAll(path);
//			newPath.add(cand);
//			LinkedList<EdgeDummy> result = this.findPolygon(nodes, edges, depth, newPath);
//			if ( result != null ) return result; // found something to remove -> done
//		
//		}
		
		for(EdgeDummy e : path.getLast().succ){
			LinkedList<EdgeDummy> newPath = new LinkedList<EdgeDummy>();
			newPath.addAll(path);
			newPath.add(e);
			LinkedList<EdgeDummy> result = this.findPolygon(nodes, edges, depth, newPath);
			if ( result != null ) return result; // found something to remove -> done
		}
		
		// return default value: nothing to remove
		return null;
	
	}
	
	
	/**
	 * Checks if a given convex polygon has a clockwise orientation.
	 * 
	 * @param path
	 * @return
	 */
	private boolean isPolygonClockwise(LinkedList<EdgeDummy> path) {

		Vector<Point> points = new Vector<Point>();
		for ( EdgeDummy edge : path ) {
			points.add(new Point(edge.node1.x,edge.node1.y));
		}
		
		Iterator<Point> it = points.iterator();
		Point pt1 = (Point)it.next();
		Point firstPt = pt1;
		Point lastPt = null;
		double area = 0.0;
		while(it.hasNext()){
			Point pt2 = (Point) it.next();
			area += (((pt2.getX() - pt1.getX()) * (pt2.getY() + pt1.getY())) / 2);
			pt1 = pt2;
			lastPt = pt1;
		}
		area += (((firstPt.getX() - lastPt.getX()) * (firstPt.getY() + lastPt.getY())) / 2);
		return area < 0;

	}
	
	/**
	 * Used here as equivalent of EdgeInfo
	 */
	class EdgeDummy{
		public GraphNode node1;
		public GraphNode node2;
		public int weight;
		public Vector<EdgeDummy> succ = new Vector<EdgeDummy>();
		public Vector<EdgeDummy> pred = new Vector<EdgeDummy>();
		
		public String toString() { 
			return "(" + node1 + "," + node2 + ")";
		}
		public boolean hasSucc(){
			return this.succ.size() > 0;
		}
		public boolean hasPred(){
			return this.pred.size() > 0;
		}
	}
	
	/**
	 * Draws the non-changing info regarding the right part (tables etc.)
	 */
	public void drawFixedInformation(GraphSimulationWorldState state, long width) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element g = doc.createElement("g");
		g.setAttribute("id", "fixedTbl");
		g.setAttribute("transform", "translate(-100, 150)");
		
		int y = 80;
		
		//append headline
		Element text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y-65));
		text.setAttribute("class", "TxtNrml");
		text.appendChild(doc.createTextNode(headInformationSecondLevel));
		g.appendChild(text);
		
		y += 60;
		
		//append headlines of the table
		g.appendChild(createRow(true, y, width, "", "Total Score", "Step Score", "Zone Value", "Ach. Pts"));
		
		y += 150; // to get it even with the dynamic information
		
		group.appendChild(g);
		
		// fixed part of the second table
		
		g = doc.createElement("g");
		g.setAttribute("id", "fixedTbl2");
		g.setAttribute("transform", "translate(-100, 150)");
		
		String[] headers = new String[]{"Role", "Energy", "Health", "Strength", "V.-range", "Last Act."};
		
		y = createRow(g, y, width, headers, null);
		
		//append static information (ag-name, role)
		
		Vector<GraphSimulationAgentState> agents = state.agents;
		
		for (GraphSimulationAgentState ag : agents){
			String role;
			try {
				role = ag.roleName.substring(0, 2);
			} catch (IndexOutOfBoundsException e) {
				role = ag.roleName;
			}
			
			
			y = createStaticRow(g, y, width, new String[]{
					String.valueOf(role) }, ag);
		}
		
		group.appendChild(g);
	}
	
}

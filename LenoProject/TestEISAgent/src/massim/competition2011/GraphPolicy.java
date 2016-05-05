package massim.competition2011;

import java.util.Vector;

import massim.competition2011.monitor.GraphMonitor;
import massim.competition2011.scenario.Achievement;
import massim.competition2011.scenario.GraphEdge;
import massim.competition2011.scenario.GraphNode;
import massim.competition2011.scenario.TeamState;
import massim.server.Server;
import massim.visualization.HandleFileFolder;
import massim.visualization.PreviewSvg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
	
	public static final String COLOR_GREEN = "rgb(0,158,115)";
	public static final String COLOR_BLUE = "rgb(0,114,178)";
	public static final String COLOR_GRAY = "rgb(155,155,155)";
	
	private String[] teamNames;
	

	public GraphPolicy(){}
	
	public void create() {
		doc = createXML();
	}

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
		
		//get the right teamcolor
		String colVal;
		String domTeam = e.node1.getDominatorTeam();
		if (domTeam == null){
			colVal = COLOR_GRAY;
		}
		else if(domTeam.equals(teamNames[0])){
			colVal = COLOR_GREEN;
		}
		else{
			colVal = COLOR_BLUE;
		}
		
		Element edge = doc.createElement("line");
		edge.setAttribute("x1", String.valueOf(e.node1.x));
		edge.setAttribute("y1", String.valueOf(e.node1.y));
		edge.setAttribute("x2", String.valueOf(e.node2.x));
		edge.setAttribute("y2", String.valueOf(e.node2.y));
		edge.setAttribute("style", "stroke:"+colVal+";stroke-width:3");
		
		group.appendChild(edge);
		
		//draw weight
		int x = (int) ((e.node1.x+e.node2.x)/2.0f);
		int y = (int) ((e.node1.y+e.node2.y)/2.0f);
		
		Element node = doc.createElement("circle");
		node.setAttribute("cx", String.valueOf(x));
		node.setAttribute("cy", String.valueOf(y));
		node.setAttribute("r", "6.5");
		node.setAttribute("stroke", "rgb"+colVal);
		node.setAttribute("stroke-width", "1");
		node.setAttribute("fill", "white");
		
		group.appendChild(node);
		
		Element text = doc.createElement("text");
		Node content = doc.createTextNode(String.valueOf(e.weight));
		text.appendChild(content);
		text.setAttribute("x", String.valueOf(x-4));
		text.setAttribute("y", String.valueOf(y+4));
		text.setAttribute("font-family", "Arial");
		text.setAttribute("font-size", "10");
		text.setAttribute("text-align", "center");
		text.setAttribute("fill", "black");
		
		group.appendChild(text);
	}

	public void drawNode(GraphNode n) {
		
		String domColor;
		String str  = n.getDominatorTeam();
	
		if (str == null){
			domColor = "rgb(255,255,255)";
		}
		else if(str.equals(teamNames[0])){
			domColor = COLOR_GREEN;
		}
		else{
			domColor = COLOR_BLUE;
		}
		
		//outer black border
		drawCircle(n.x, n.y, NODE_RADIUS+3, "rgb(0,0,0)", 1, false, "");
		//2px frame in teamColor or white
		drawCircle(n.x, n.y, NODE_RADIUS+2, domColor, 2, false, "");
//		//inner black border
//		drawCircle(n.x, n.y, NODE_RADIUS, "rgb(0,0,0)", 1, false, "");
		//inner node in teamColor or white
		drawCircle(n.x, n.y, NODE_RADIUS, "rgb(0,0,0)", 1, true, domColor);
		
		drawText(n.x-3, n.y+3, String.valueOf(n.weight), "black");
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
	

	public void drawText(int x, int y, String t, String color) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element text = doc.createElement("text");
		Node content = doc.createTextNode(t);
		text.appendChild(content);
		text.setAttribute("x", String.valueOf(x));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("font-family", "Arial");
		text.setAttribute("font-size", "14");
		text.setAttribute("text-align", "center");
		text.setAttribute("fill", color);
		
		group.appendChild(text);
	}

	public void drawRoundRect(int x, int y, int width, int height, boolean fill, String fillColor, String frameColor, int frameWidth) {
		
		Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
		
		Element rect = doc.createElement("rect");
		rect.setAttribute("x", String.valueOf(x));
		rect.setAttribute("y", String.valueOf(y));
		rect.setAttribute("rx", "6");
		rect.setAttribute("ry", "6");
		rect.setAttribute("width", String.valueOf(width));
		rect.setAttribute("height", String.valueOf(height));
		
		String style ="stroke:"+frameColor+";stroke-width:"+frameWidth;
		if (fill){
			style += ";fill:"+fillColor;
		}
		rect.setAttribute("style", style);
	
		group.appendChild(rect);
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
		
		int y = 80;
		
		//append current number of steps
		Element text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y-65));
		text.setAttribute("font-size", "32px");
		text.setAttribute("font-weight", "bold");
		text.setAttribute("fill", "black");
		
		text.appendChild(doc.createTextNode(headInformationSecondLevel));
		g.appendChild(text);
		y += 60;

		Element text2 = doc.createElement("text");
		text2.setAttribute("x", String.valueOf(width + 250));
		text2.setAttribute("y", String.valueOf(y-65));
		text2.setAttribute("font-size", "32px");
		text2.setAttribute("font-weight", "bold");
		text2.setAttribute("fill", "black");
		text2.appendChild(doc.createTextNode("Step: "+currStep));
		
		g.appendChild(text2);
		
		g.appendChild(createRow(true, y, width, "", "Total Score", "Step Score", "Zone Value", "Ach. Pts"));
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
	 * Creates a row for the teamTable using parameters as values

	 */
	private Element createRow(boolean headline, int y, long width, String string0, String string1,
			String string2, String string3, String string4) {
		
		Element text;
		Element tspan;
		
		String anchor = headline? "middle" : "start";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("font-size", "24px");
		text.setAttribute("font-weight", "bold");
		text.setAttribute("fill", "black");
		text.setAttribute("text-anchor", anchor);
		
		if (!headline){
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(width + 250));
			tspan.setAttribute("fill", teamNames[0].equals(string0)? COLOR_GREEN : COLOR_BLUE);
			tspan.appendChild(doc.createTextNode(string0));
			text.appendChild(tspan);
		}
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 400));
		tspan.appendChild(doc.createTextNode(string1));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 600));
		tspan.appendChild(doc.createTextNode(string2));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 800));
		tspan.appendChild(doc.createTextNode(string3));
		text.appendChild(tspan);
		
		tspan = doc.createElement("tspan");
		tspan.setAttribute("x", String.valueOf(width + 1000));
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
		
		String[] headers = new String[]{"Role", "Energy", "Health", "Strength", "Vis Range", "Last Action"};
		
		y = createRow(g, true, y, width, headers, null);
		
		for (GraphSimulationAgentState ag : agents){
			String role;
			try {
				role = ag.roleName.substring(0, 2);
			} catch (IndexOutOfBoundsException e) {
				role = ag.roleName;
			}
			
			
			y = createRow(g, false, y, width, new String[]{
					String.valueOf(role), String.valueOf(ag.energy) +"/"+ String.valueOf(ag.maxEnergy), 
					String.valueOf(ag.health) +"/"+ String.valueOf(ag.maxHealth), String.valueOf(ag.strength), 
					String.valueOf(ag.visRange), String.valueOf(ag.lastAction +" " + ag.lastActionParam +":" + ag.lastActionResult)}, ag);
		}
		
		group.appendChild(g);
		
		return y;
	}

	private int createRow(Element g, boolean headline, int y, long width, String[] entries, 
			GraphSimulationAgentState ag) {
		
		Element text;
		Element tspan;
		
		String anchor = headline? "middle" : "start";
		
		text = doc.createElement("text");
		text.setAttribute("x", String.valueOf(width + 250));
		text.setAttribute("y", String.valueOf(y));
		text.setAttribute("font-size", "18px");
		text.setAttribute("font-weight", "bold");
		text.setAttribute("fill", "black");
		text.setAttribute("text-anchor", anchor);
		
		long x = width + 250;
		
		if (!headline){
			tspan = doc.createElement("tspan");
			tspan.setAttribute("x", String.valueOf(x));
			tspan.setAttribute("fill", teamNames[0].equals(ag.team)? COLOR_GREEN : COLOR_BLUE);
			tspan.appendChild(doc.createTextNode(ag.name));
			text.appendChild(tspan);
		}
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
		text.setAttribute("font-size", "18px");
		text.setAttribute("font-weight", "bold");
		text.setAttribute("fill", "black");
		text.setAttribute("text-anchor", "start");
		
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
		this.headInformationFirstLevel = "Graph Simulation 2011";
		this.headInformationSecondLevel = GraphSimulationVisualizationObserver.simulationName;
		//teamNames[0] +" vs. "+teamNames[10]; //FIXME this relies on having 10 agents per team
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
			// TODO Auto-generated catch block
	 		e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
														
	}														
	
}

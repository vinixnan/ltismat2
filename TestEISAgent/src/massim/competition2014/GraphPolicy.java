package massim.competition2014;

import java.awt.Color;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import massim.competition2014.scenario.Achievement;
import massim.competition2014.scenario.GraphEdge;
import massim.competition2014.scenario.GraphNode;
import massim.competition2014.scenario.TeamState;
import massim.server.AbstractServer;
import massim.visualization.HandleFileFolder;
import massim.visualization.PreviewSvg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GraphPolicy extends massim.visualization.svg.SvgXmlFile {

    // some strings used for file creation
    public static String svgFile = "";
    public static String previewFile = "";
    private String namePreviewSvg = "SimulationPreview";

    private String svgImageHeight = "525";
    private String svgImageWidth = "1020";
    private double scaleFactor = 0;

    private String configPath = AbstractServer.configurationFilenamePath + System.getProperty("file.separator") + "visualization" + System.getProperty("file.separator");

    // should be set later
    private String headInformationFirstLevel = "";
    private String headInformationSecondLevel = "";

    // most important parts
    public Document doc;
    private int numberOfSvgFiles = -1;

    private String path = "";
    private String outputFolder = this.path;
    private String nameOutputFile = "masSim";

    // change as you like
    public static final int NODE_RADIUS = 10;
    public static final int AGENT_RADIUS = 10;

    public static final int LINE_WIDTH = 50;

    public static final Color GREEN = new Color(0, 158, 115);
    public static final Color BLUE = new Color(0, 114, 178);
    public static final Color COLOR_TEAM1_ZONE = new Color(0, 158, 115).darker().darker();
    public static final Color COLOR_TEAM2_ZONE = new Color(0, 114, 178).darker().darker();

    public static final String COLOR_GREEN = makeRGB(GREEN);
    public static final String COLOR_BLUE = makeRGB(BLUE);

    private String[] teamNames;

    private static int maxPath = 0;

    private GraphSimulationVisualizationObserver vo;

    // profiling
    public Vector<Double> intervals = new Vector<>();
    public long maxTime = 0;
    public long minTime = Long.MAX_VALUE;

    // #### methods ####

    public GraphPolicy(GraphSimulationVisualizationObserver graphSimulationVisualizationObserver) {
        this.vo = graphSimulationVisualizationObserver;
    }

    public void create() {
        this.doc = createXML();

        // add stylesheet
        Node pi = this.doc.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");
        this.doc.insertBefore(pi, this.doc.getDocumentElement());
    }

    /**
     * Draws the graph/topology
     */
    public void drawGraph(String string, int sizeX, int sizeY, Vector<GraphNode> nodes, Vector<GraphEdge> edges) {

        setBackground(sizeX, sizeY);

        for (GraphEdge edge : edges) {
            drawEdge(edge);
        }

        for (GraphNode node : nodes) {
            drawNode(node);
        }
    }

    private void setBackground(long width, long height) {

        copy(this.configPath, this.outputFolder, "Surface.svg");
        String url = "Surface.svg#surface";

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);
        Element img = this.doc.createElement("use");
        img.setAttribute("x", "0");
        img.setAttribute("y", "0");
        img.setAttribute("transform", "scale(" + String.valueOf(width / 283.46) + ")");
        img.setAttribute("z-index", "0");
        img.setAttribute("xlink:href", url);

        group.appendChild(img);
    }

    public void drawEdge(GraphEdge e) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        int width = 2 + 1 * e.weight;

        String classValue = "E";

        Element edge = this.doc.createElement("line");
        edge.setAttribute("x1", String.valueOf(e.node1.x));
        edge.setAttribute("y1", String.valueOf(e.node1.y));
        edge.setAttribute("x2", String.valueOf(e.node2.x));
        edge.setAttribute("y2", String.valueOf(e.node2.y));

        String cl = this.vo.strokeWidthClasses.get(width);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            edge.setAttribute("style", "stroke-width:" + width + ";");
        }

        edge.setAttribute("class", classValue);

        group.appendChild(edge);
    }

    public void drawEdge(EdgeDummy e) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        int width = 2 + 1 * e.weight;

        String classValue = "E";

        Element edge = this.doc.createElement("line");
        edge.setAttribute("x1", String.valueOf(e.node1.x));
        edge.setAttribute("y1", String.valueOf(e.node1.y));
        edge.setAttribute("x2", String.valueOf(e.node2.x));
        edge.setAttribute("y2", String.valueOf(e.node2.y));

        String cl = this.vo.strokeWidthClasses.get(width);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            edge.setAttribute("style", "stroke-width:" + width + ";");
        }

        edge.setAttribute("class", classValue);

        group.appendChild(edge);
    }

    public void drawNode(GraphNode n) {

        int width = 7 + 3 * n.weight;

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element node = this.doc.createElement("circle");
        node.setAttribute("cx", String.valueOf(n.x));
        node.setAttribute("cy", String.valueOf(n.y));
        node.setAttribute("r", String.valueOf(width / 2));
        node.setAttribute("class", "N");

        group.appendChild(node);
    }

    public void drawAgent(GraphSimulationAgentState ag, Color agentColor, int agx, int agy) {

        // rendering the agent body

        int boxWidth = 54;

        // draw agent-polygon
        if (ag.roleName.equalsIgnoreCase("explorer")) {
            // circle
            drawCircle(agx - boxWidth / 3, agy - boxWidth / 6, (boxWidth / 3), makeRGB(Color.black), 2, true, makeRGB(agentColor));
        } else if (ag.roleName.equalsIgnoreCase("repairer")) {
            // octagon
            String o = "";
            o += (agx - boxWidth / 6) + "," + (agy - boxWidth / 6);
            o += " " + (agx + boxWidth / 6) + "," + (agy - boxWidth / 6);
            o += " " + (agx + boxWidth / 3) + "," + (agy);
            o += " " + (agx + boxWidth / 3) + "," + (agy + boxWidth / 3);
            o += " " + (agx + boxWidth / 6) + "," + (agy + boxWidth / 2);
            o += " " + (agx - boxWidth / 6) + "," + (agy + boxWidth / 2);
            o += " " + (agx - boxWidth / 3) + "," + (agy + boxWidth / 3);
            o += " " + (agx - boxWidth / 3) + "," + (agy);

            drawPolygon(o, makeRGB(Color.BLACK), 2, true, makeRGB(agentColor));
        } else if (ag.roleName.equalsIgnoreCase("saboteur")) {
            // diamond
            String o = "";
            o += (agx) + "," + (agy - boxWidth / 6);
            o += " " + (agx + boxWidth / 3) + "," + (agy + boxWidth / 6);
            o += " " + (agx) + "," + (agy + boxWidth / 2);
            o += " " + (agx - boxWidth / 3) + "," + (agy + boxWidth / 6);

            drawPolygon(o, makeRGB(Color.BLACK), 2, true, makeRGB(agentColor));

        } else if (ag.roleName.equalsIgnoreCase("sentinel")) {
            // square
            drawRect(agx - boxWidth / 3, agy - boxWidth / 6, (boxWidth / 3) * 2, (boxWidth / 3) * 2, makeRGB(Color.black), 2, true, makeRGB(agentColor));
        } else if (ag.roleName.equalsIgnoreCase("inspector")) {
            // downwards triangle
            String o = "";
            o += (agx - boxWidth / 2) + "," + (agy - boxWidth / 6);
            o += " " + (agx + boxWidth / 2) + "," + (agy - boxWidth / 6);
            o += " " + (agx) + "," + (agy + boxWidth / 2);

            drawPolygon(o, makeRGB(Color.black), 2, true, makeRGB(agentColor));
        }

        // draw red X if agent is disabled
        if (ag.health == 0) {
            drawLine(agx - boxWidth / 3, agy - boxWidth / 6, agx + boxWidth / 3, agy + boxWidth / 2, Color.RED.darker(), 6);
            drawLine(agx + boxWidth / 3, agy - boxWidth / 6, agx - boxWidth / 3, agy + boxWidth / 2, Color.RED.darker(), 6);
        }

        // draw status-box
        Color resultCol;
        String result = ag.lastActionResult;
        if (result.equalsIgnoreCase("successful")) {
            resultCol = Color.GREEN.darker().darker();
        } else if (result.startsWith("failed")) {
            if (result.equalsIgnoreCase("failed_in_range")) {
                resultCol = Color.YELLOW.darker();
            } else {
                resultCol = Color.RED.darker();
            }
        } else {
            if (result.equalsIgnoreCase("useless")) {
                resultCol = Color.GREEN.darker().darker();
            } else {
                resultCol = Color.BLACK;
            }
        }
        drawRect(agx - boxWidth / 2, agy - boxWidth / 2, boxWidth, boxWidth / 3, makeRGB(agentColor), 2, true, makeRGB(resultCol));

        // draw agent-id
        // Font font = new Font("Arial", Font.PLAIN, 14);

        String agID = parseID(ag.name);

        // int idWidth = g2d.getFontMetrics().stringWidth(agID);
        // int idHeight = g2d.getFontMetrics().getHeight();

        int idX = agx - 7;
        int idY = agy + (boxWidth / 6) + 3;

        drawText(agID, idX, idY, makeRGB(Color.white));

        // draw the last action and its result
        String action = ag.lastAction;

        if (action.equalsIgnoreCase("attack")) {
            drawSword(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("parry")) {
            drawShield(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("inspect")) {
            drawMagnifyingGlass(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("survey")) {
            drawGlasses(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth, resultCol);
        } else if (action.equalsIgnoreCase("probe")) {
            drawDrill(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("recharge")) {
            drawLightning(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("skip")) {
            // nothing to do
        } else if (action.equalsIgnoreCase("repair")) {
            drawWrench(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("goto")) {
            drawCompass(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
        } else if (action.equalsIgnoreCase("buy")) {
            String param = ag.lastActionParam;

            if (param != null && !param.equals("")) {
                drawText("+1", agx - boxWidth / 2, agy - boxWidth / 6, makeRGB(Color.white));
            }

            if (param.equalsIgnoreCase("sensor")) {
                drawGlasses(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth, resultCol);
            } else if (param.equalsIgnoreCase("battery")) {
                drawLightning(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
            } else if (param.equalsIgnoreCase("shield")) {
                drawHeart(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
            } else if (param.equalsIgnoreCase("sabotageDevice")) {
                drawSword(agx - boxWidth / 6, agy - boxWidth / 2, boxWidth);
            }
        }
    }

    private void drawRect(int x, int y, int width, int height, String frameCol, int frameWidth, boolean fill, String fillCol) {
		/*
		 * <rect width="300" height="100"
		 * style="fill:rgb(0,0,255);stroke-width:1;stroke:rgb(0,0,0)"/>
		 */

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element node = this.doc.createElement("rect");
        node.setAttribute("height", String.valueOf(height));
        node.setAttribute("width", String.valueOf(width));
        node.setAttribute("x", String.valueOf(x));
        node.setAttribute("y", String.valueOf(y));

        String classValue = "";
        String cl = this.vo.strokeClasses.get(frameCol);
        if (cl != null) {
            classValue += cl;
        } else {
            node.setAttribute("stroke", frameCol);
        }

        cl = this.vo.strokeWidthClasses.get(frameWidth);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            node.setAttribute("stroke-width", String.valueOf(frameWidth));
        }

        if (fill) {
            cl = this.vo.fillClasses.get(fillCol);
            if (cl != null) {
                classValue += " " + cl;
            } else {
                node.setAttribute("style", "fill:" + fillCol);
            }
        }

        if (!classValue.equals("")) {
            node.setAttribute("class", classValue);
        }

        group.appendChild(node);
    }

    /**
     * Draws a polygon
     *
     * @param points
     *            the points given as a string of the form
     *            "x1,y1 x2,y2 ... xn,yn"
     */
    private void drawPolygon(String points, String frameCol, int frameWidth, boolean fill, String fillCol) {
		/*
		 * e.g. <polygon points="200,10 250,190 160,210"
		 * style="fill:lime;stroke:purple;stroke-width:1"/>
		 */

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element node = this.doc.createElement("polygon");
        node.setAttribute("points", points);

        String classValue = "";

        String cl = this.vo.strokeClasses.get(frameCol);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            node.setAttribute("stroke", frameCol);
        }

        cl = this.vo.strokeWidthClasses.get(frameWidth);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            node.setAttribute("stroke-width", String.valueOf(frameWidth));
        }

        if (fill) {

            cl = this.vo.fillClasses.get(fillCol);
            if (cl != null) {
                classValue += " " + cl;
            } else {
                node.setAttribute("fill", fillCol);
            }
        }

        if (!classValue.equals("")) {
            node.setAttribute("class", classValue);
        }

        group.appendChild(node);
    }

    private void drawText(String text, int x, int y, String col) {
        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element node = this.doc.createElement("text");
        node.setAttribute("x", String.valueOf(x));
        node.setAttribute("y", String.valueOf(y));

        String cl = this.vo.fillClasses.get(col);
        if (cl != null) {
            node.setAttribute("class", cl);
        } else {
            node.setAttribute("fill", col);
        }

        node.appendChild(this.doc.createTextNode(text));

        group.appendChild(node);
    }

    /**
     * @return false iff zones were not drawn completely due to exceeded timeout
     */
    public boolean drawZones(Collection<GraphNode> nodesIn, Collection<EdgeDummy> edgesIn, long timeout, String style) {

        // 1.render polygons
        int depth = 3;
        int maxDepth = 8;
        LinkedList<EdgeDummy> edges = new LinkedList<>(edgesIn);
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;
        while (!edges.isEmpty() && depth < maxDepth) {

            LinkedList<EdgeDummy> remove = null;
            for (EdgeDummy edge : edges) {

                LinkedList<EdgeDummy> pathIn = new LinkedList<>();
                pathIn.add(edge);
                LinkedList<EdgeDummy> pathOut = findPolygon(nodesIn, edges, depth, pathIn);

                timeElapsed = System.currentTimeMillis() - startTime;
                if(timeElapsed > timeout){
                    return false;
                }

                if (pathOut != null) {
                    if (pathOut.size() > 1) {
                        drawPath(pathOut, style);
                        if (pathOut.size() > maxPath) {
                            maxPath = pathOut.size();
                        }
                    }
                    remove = pathOut;
                    break;
                }

            }
            if (remove == null) {
                depth++;
            } else {
                edges.removeAll(remove);
            }

        }

        // 2. render colored nodes
        for (GraphNode node : nodesIn) {
            drawZoneNode(node, style);
        }

        // 3. render colored edges
        boolean draw = true;
        for (EdgeDummy edge : edgesIn) {
            if (draw) { // only draw every 2nd edge
                drawZoneEdge(edge, style);
                draw = false;
            } else {
                draw = true;
            }
        }

        // 4. re-render original nodes
        for (GraphNode node : nodesIn) {
            drawNode(node);
        }

        // 5. re-render original edges
        draw = true;
        for (EdgeDummy edge : edgesIn) {
            if (draw) {
                drawEdge(edge);
                draw = false;
            } else {
                draw = true;
            }
        }
        return true;
    }

    public void drawZonesFastMode(Collection<GraphNode> nodesIn, Collection<EdgeDummy> edgesIn, String style) {
        //Draw zones very fast (do not draw them at all, edges are already colored)

        // 2. render colored nodes
        for (GraphNode node : nodesIn) {
            drawZoneNode(node, style);
        }

        // 3. render colored edges
        boolean draw = true;
        for (EdgeDummy edge : edgesIn) {
            if (draw) { // only draw every 2nd edge
                drawZoneEdge(edge, style);
                draw = false;
            } else {
                draw = true;
            }
        }

        // 4. re-render original nodes
        for (GraphNode node : nodesIn) {
            drawNode(node);
        }

        // 5. re-render original edges
        draw = true;
        for (EdgeDummy edge : edgesIn) {
            if (draw) {
                drawEdge(edge);
                draw = false;
            } else {
                draw = true;
            }
        }
    }

    /**
     * Creates an rgb-string (for SVGs) from a Color object
     */
    public static String makeRGB(Color c) {
        return "rgba(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + (float) c.getAlpha() / 255f + ")";
    }

    private void drawLine(int x1, int y1, int x2, int y2, Color col, int width) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        String colVal = makeRGB(col);

        Element line = this.doc.createElement("line");
        line.setAttribute("x1", String.valueOf(x1));
        line.setAttribute("y1", String.valueOf(y1));
        line.setAttribute("x2", String.valueOf(x2));
        line.setAttribute("y2", String.valueOf(y2));

        String classValue = "";
        String cl = this.vo.strokeClasses.get(colVal);
        if (cl != null) {
            classValue += cl;
        } else {
            line.setAttribute("stroke", colVal);
        }

        cl = this.vo.strokeWidthClasses.get(width);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            line.setAttribute("stroke-width", String.valueOf(width));
        }

        if (!classValue.equals("")) {
            line.setAttribute("class", classValue);
        }

        group.appendChild(line);
    }

    /**
     * @param dasharray
     *            e.g. "5,5,10"
     * @param dashArrayOffset
     */
    private void drawDashLine(int x1, int y1, int x2, int y2, String dasharray, int dashArrayOffset, Color col, int width) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        String colVal = makeRGB(col);

        Element line = this.doc.createElement("line");
        line.setAttribute("x1", String.valueOf(x1));
        line.setAttribute("y1", String.valueOf(y1));
        line.setAttribute("x2", String.valueOf(x2));
        line.setAttribute("y2", String.valueOf(y2));

        String classValue = "";
        String cl = this.vo.strokeClasses.get(colVal);
        if (cl != null) {
            classValue += cl;
        } else {
            line.setAttribute("stroke", colVal);
        }

        cl = this.vo.strokeWidthClasses.get(width);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            line.setAttribute("stroke-width", String.valueOf(width));
        }

        //create arrow definition
        // check whether a node defs already exists else create one
        Node defs = this.doc.getDocumentElement().getElementsByTagName("defs").item(0);
        if ( defs == null ) {
            defs = this.doc.createElement("defs");
            this.doc.getDocumentElement().appendChild(defs);
        }

        //check whether marker exists and get arrow url otherwise create marker
        String arrow = this.vo.arrowDefinitions.get(colVal);
        if (arrow == null) {
            arrow = "ar" + (this.vo.arrowDefinitions.size() + 1);
            this.vo.arrowDefinitions.put(colVal, arrow);

            Element marker = this.doc.createElement("marker");
            marker.setAttribute("id", arrow);
            marker.setAttribute("viewBox", "0 0 20 20");
            marker.setAttribute("refX", "100");
            marker.setAttribute("refY", "10");
            marker.setAttribute("markerUnits", "strokeWidth");
            marker.setAttribute("markerWidth", "8");
            marker.setAttribute("markerHeight", "6");
            marker.setAttribute("orient", "auto");
            cl = this.vo.fillClasses.get(colVal);
            if (cl != null) {
                marker.setAttribute("class", this.vo.fillClasses.get(colVal));
            }
            defs.appendChild(marker);

            Element markerPath = this.doc.createElement("path");
            markerPath.setAttribute("d", "M 0 0 L 20 10 L 0 20 z");
            marker.appendChild(markerPath);
        }
        line.setAttribute("marker-end", "url(#" + arrow + ")");

        // ugly workaround. Actually, we should change the calling method to add a new css-class.
        if (dasharray.equalsIgnoreCase("30,30") && (dashArrayOffset == 30) ) {
            classValue += " " + "sd";
        } else {
            line.setAttribute("style", "stroke-dasharray:" + dasharray + ";" + "stroke-dashoffset:" + dashArrayOffset + ";");
        }

        if (!classValue.equals("")) {
            line.setAttribute("class", classValue);
        }

        group.appendChild(line);
    }

    /**
     * Method to draw a circle with the specified attributes
     */
    private void drawCircle(int x, int y, int radius, String frameColor, int frameWidth, boolean fill, String fillColor) {

        int cx = x + radius;
        int cy = y + radius;

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element node = this.doc.createElement("circle");
        node.setAttribute("cx", String.valueOf(cx));
        node.setAttribute("cy", String.valueOf(cy));
        node.setAttribute("r", String.valueOf(radius));

        String classValue = "";
        String cl = this.vo.strokeClasses.get(frameColor);
        if (cl != null) {
            classValue += cl;
        } else {
            node.setAttribute("stroke", frameColor);
        }
        cl = this.vo.strokeWidthClasses.get(frameWidth);
        if (cl != null) {
            classValue += " " + cl;
        } else {
            node.setAttribute("stroke-width", String.valueOf(frameWidth));
        }

        if (fill) {
            cl = this.vo.fillClasses.get(fillColor);
            if (cl != null) {
                classValue += " " + cl;
            } else {
                node.setAttribute("fill", fillColor);
            }
        }

        if (!classValue.equals("")) {
            node.setAttribute("class", classValue);
        }

        group.appendChild(node);
    }

    /**
     * Draws a table containing the following information in the top right
     * corner: TeamName, Total Score, Step Score, Current Zone Value, Current
     * Achievement Points, Achievements
     *
     * @param teamsStates
     *            - the teams' states
     * @return the current y-position after drawing the table
     */
    public int drawFirstTable(Vector<TeamState> teamsStates, long width, int currStep) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element g = this.doc.createElement("g");
        g.setAttribute("id", "teamTable");
        g.setAttribute("transform", "translate(-100, 150)");

        // > headline drawn in static table

        int y = 140;

        // append current number of steps
        Element text2 = this.doc.createElement("text");
        text2.setAttribute("x", String.valueOf(width + 250));
        text2.setAttribute("y", String.valueOf(y - 65));
        text2.setAttribute("class", "TxtNrml");
        text2.appendChild(this.doc.createTextNode("Step: " + currStep));

        g.appendChild(text2);

        // > headlines drawn in static table

        y += 40;

        for (TeamState ts : teamsStates) {
            g.appendChild(createRow(false, y, width, ts.name, String.valueOf(ts.summedScore), String.valueOf(ts.getCurrent()), String.valueOf(ts.getAreasValue()), String.valueOf(ts.currAchievementPoints)));
            y += 40;
        }

        group.appendChild(g);

        return y;
    }

    /**
     * Creates a row for the teamTable using the parameters as values
     */
    private Element createRow(boolean headline, int y, long width, String string0, String string1, String string2, String string3, String string4) {

        Element text;
        Element tspan;

        String anchor = headline ? "middle" : "start";

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "Tb");
        text.setAttribute("text-anchor", anchor);

        if (!headline) {
            tspan = this.doc.createElement("tspan");
            tspan.setAttribute("x", String.valueOf(width + 250));
            tspan.setAttribute("fill", this.teamNames[0].equals(string0) ? COLOR_GREEN : COLOR_BLUE);
            tspan.appendChild(this.doc.createTextNode(string0));
            text.appendChild(tspan);
        }

        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(width + 650));
        tspan.appendChild(this.doc.createTextNode(string1));
        text.appendChild(tspan);

        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(width + 850));
        tspan.appendChild(this.doc.createTextNode(string2));
        text.appendChild(tspan);

        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(width + 1050));
        tspan.appendChild(this.doc.createTextNode(string3));
        text.appendChild(tspan);

        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(width + 1250));
        tspan.appendChild(this.doc.createTextNode(string4));
        text.appendChild(tspan);

        return text;
    }

    /**
     * Draws name, role, current energy, max energy,current health, max health,
     * strength and vis range of agent in output "table"
     *
     * @return current y-position
     */
    public int drawSecondTable(Vector<GraphSimulationAgentState> agents, long width, int yValue) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        int y = yValue;
        int startY = y;

        Element g = this.doc.createElement("g");
        g.setAttribute("id", "agentTable");
        g.setAttribute("transform", "translate(-100, 150)");

        // > headlines drawn static

        y += 30;

        // draw the dynamic information

        for (GraphSimulationAgentState ag : agents) {
            y = createDynamicRowColors(g, y, width, new Vector<>(Arrays.asList(ag.energy,ag.maxEnergy)), new Vector<>(Arrays.asList(ag.health,ag.maxHealth)), ag.strength, ag.visRange, new Vector<>(Arrays.asList(ag.lastAction,ag.lastActionParam,ag.lastActionResult)), ag, ag.lastActionResult.startsWith("failed"));
        }

        group.appendChild(g);

        drawLine((int) width + 1190, y, (int) width + 1190, startY + 156, Color.BLACK, 1);

        return y;
    }

    /**
     * Renders an edge.
     *
     * @param edge
     */
    private void drawZoneEdge(EdgeDummy edge, String style) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element line = this.doc.createElement("line");
        line.setAttribute("x1", String.valueOf(edge.node1.x));
        line.setAttribute("y1", String.valueOf(edge.node1.y));
        line.setAttribute("x2", String.valueOf(edge.node2.x));
        line.setAttribute("y2", String.valueOf(edge.node2.y));
        line.setAttribute("class", style + "e");

        group.appendChild(line);
    }

    /**
     * Renders a node.
     *
     * @param node
     */
    private void drawZoneNode(GraphNode node, String style) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element elnode = this.doc.createElement("circle");
        elnode.setAttribute("cx", String.valueOf(node.x));
        elnode.setAttribute("cy", String.valueOf(node.y));
        elnode.setAttribute("r", String.valueOf(LINE_WIDTH / 2));
        elnode.setAttribute("class", style + "n");

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
        for (int a = 0; a < pathOut.size(); a++) {
            x[a] = pathOut.get(a).node1.x;
            y[a] = pathOut.get(a).node1.y;
        }
        drawPolygon(x, y, style);

    }

    private void drawPolygon(int[] x, int[] y, String style) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        String points = "";

        for (int i = 0; i < x.length; i++) {
            points += x[i] + "," + y[i] + " ";
        }

        Element p = this.doc.createElement("polygon");
        p.setAttribute("points", points);
        p.setAttribute("class", style);

        group.appendChild(p);

    }

    private int createRow(Element g, int y, long width, String[] entries, GraphSimulationAgentState ag) {

        Element text;
        Element tspan;

        String anchor = "middle";

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "Tb2");
        text.setAttribute("text-anchor", anchor);

        long x = width + 350;

        for (String s : entries) {
            tspan = this.doc.createElement("tspan");
            tspan.setAttribute("x", String.valueOf(x));
            tspan.appendChild(this.doc.createTextNode(s));
            text.appendChild(tspan);
            x += 115;
        }

        g.appendChild(text);

        return y + 30;
    }

    /**
     * append static information (ag-name, role, strength, vis-range) to 2nd
     * table
     */
    private int createStaticRow(Element g, int y, long width, String[] entries, GraphSimulationAgentState ag) {

        Element text;
        Element tspan;

        String anchor = "start";

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "Tb2");
        text.setAttribute("text-anchor", anchor);

        long x = width + 240;

        // append agentnumber in team-color
        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(x));
        tspan.setAttribute("fill", this.teamNames[0].equals(ag.team) ? COLOR_GREEN : COLOR_BLUE);

        String agentNumber;
        try {
            Integer.parseInt(ag.name.substring(ag.name.length() - 2));
            agentNumber = ag.name.substring(ag.name.length() - 2);
        } catch (NumberFormatException e1) {
            try {
                agentNumber = ag.name.substring(ag.name.length() - 1);
            } catch (NumberFormatException e2) {
                agentNumber = "??";
            }
        } catch (Exception e) {
            agentNumber = "??";
        }

        tspan.appendChild(this.doc.createTextNode(agentNumber));
        text.appendChild(tspan);

        x += 100;

        for (String s : entries) {
            tspan = this.doc.createElement("tspan");
            tspan.setAttribute("x", String.valueOf(x));
            tspan.appendChild(this.doc.createTextNode(s));
            text.appendChild(tspan);
            x += 115;
        }

        g.appendChild(text);

        return y + 30;
    }

    private int createDynamicRowColors(Element g, int y, long width, Vector<Integer> energy, Vector<Integer> health, Integer strength, Integer visibility, Vector<String> Action, GraphSimulationAgentState ag, boolean failed) {
        Element text;
        String anchor = "start";

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "Tb2");
        text.setAttribute("text-anchor", anchor);

        long x = width + 250;

        x += 185; // width of static part (100 + 1*115)

        //Energy, health, strength, visibility
        x = createDynamicCellColor(energy, text, x);
        x = createDynamicCellColor(health, text, x);
        x = createDynamicCellColor(strength, text, x);
        x = createDynamicCellColor(visibility, text, x);

        //Action columns
        Element tspan;
        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(x));

        if (Action.size() == 3) {
            if (failed) {
                if (Action.get(1).contains("NoAction") ||
                        Action.lastElement().equalsIgnoreCase("failed_wrong_param") ||
                        Action.lastElement().equalsIgnoreCase("failed_role") ||
                        Action.lastElement().equalsIgnoreCase("failed") ||
                        Action.lastElement().equalsIgnoreCase("unknownAction")
                        ){
                    tspan.setAttribute("fill", "red");
                }
                else {
                    tspan.setAttribute("fill", "orange");
                }
            } else {
                tspan.setAttribute("fill", "green");
            }
            String textnode = Action.get(0) +": ";
            textnode += Action.get(1) +" ";
            textnode += Action.get(2);
            tspan.appendChild(this.doc.createTextNode(textnode));
            text.appendChild(tspan);
        }

        g.appendChild(text);
        return y + 30;

    }

    /**
     * This method creates the text for a cell with just one integer
     * @param value The integer
     * @param text The text of the whole row
     * @param x the column
     * @return the new x value (i.e., the new column)
     */
    private long createDynamicCellColor(Integer value, Element text, long x) {
        Element tspan;
        //cell entry
        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(x));
        //cell color
        if (value != 0) {
            tspan.appendChild(this.doc.createTextNode(value.toString()));
            text.appendChild(tspan);
        }
        return x + 115;
    }

    /**
     * This method computes the energy and health ratio and colors the text in a certain color
     * @param values The integer pair
     * @param text The text of the whole row
     * @param x the column
     * @return the new x value (i.e., the new column)
     */
    private long createDynamicCellColor(Vector<Integer> values, Element text, long x) {
        Element tspan;
        //cell entry
        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(x));
        //cell color
        if (values.size() == 2) {
            double ratio = (double) values.get(0)/values.get(1);
            if (ratio == 0) {
                tspan.setAttribute("fill", "red");
            } else if (ratio < (0.5) ) {
                tspan.setAttribute("fill", "orange");
            } else {
                tspan.setAttribute("fill", "green");
            }
            tspan.appendChild(this.doc.createTextNode(values.firstElement().toString()+" | "+values.lastElement().toString()));
            text.appendChild(tspan);
        }
        return x + 115;
    }

    private int createDynamicRow(Element g, int y, long width, String[] entries, GraphSimulationAgentState ag, boolean failed) {

        Element text;
        Element tspan;

        String anchor = "start";

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "Tb2");
        text.setAttribute("text-anchor", anchor);

        long x = width + 250;

        x += 185; // width of static part (100 + 1*115)

        for (int i = 0; i < entries.length; i++) {
            String s = entries[i];
            tspan = this.doc.createElement("tspan");
            tspan.setAttribute("x", String.valueOf(x));
            if (i == 4 && failed) {
                tspan.setAttribute("fill", "red");
            }
            tspan.appendChild(this.doc.createTextNode(s));
            text.appendChild(tspan);
            x += 115;
        }

        g.appendChild(text);

        return y + 30;
    }

    public void listAchievements(int yValue, long width, Vector<TeamState> teamsStates) {
        int y = yValue;
        y += 15;

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element g = this.doc.createElement("g");
        g.setAttribute("id", "achievementPart");
        g.setAttribute("transform", "translate(-100, 150)");

        Element text;
        Element tspan;

        text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("class", "TxtAch");

        // achievements caption
        tspan = this.doc.createElement("tspan");
        tspan.setAttribute("x", String.valueOf(width + 250));
        tspan.appendChild(this.doc.createTextNode("Achievements"));
        text.appendChild(tspan);

        long x = width + 250;

        int newY = y + 30;

        for (TeamState ts : teamsStates) {

            // teamname caption
            tspan = this.doc.createElement("tspan");
            tspan.setAttribute("x", String.valueOf(x));
            tspan.setAttribute("y", String.valueOf(newY));
            tspan.setAttribute("fill", ts.name.equals(this.teamNames[0]) ? COLOR_GREEN : COLOR_BLUE);
            tspan.appendChild(this.doc.createTextNode(ts.name));
            text.appendChild(tspan);

            // achievements
            for (Achievement ach : ts.achievements) {

                if (ach.achieved) {
                    newY += 20;
                    tspan = this.doc.createElement("tspan");
                    tspan.setAttribute("x", String.valueOf(x));
                    tspan.setAttribute("y", String.valueOf(newY));
                    tspan.appendChild(this.doc.createTextNode(ach.name));
                    text.appendChild(tspan);
                }
            }

            // x += 500;
            newY += 25;
        }

        g.appendChild(text);
        group.appendChild(g);
    }

    public void save(double internalWidth, double internalHeight) {

        String currentFile = this.outputFolder + this.nameOutputFile + "-" + this.numberOfSvgFiles + this.svgEnding;

        Element rootDoc = this.doc.getDocumentElement();

        Element scaleElement = this.doc.getElementById("scaleSvg");
        scaleElement.setAttribute("transform", "scale(" + Double.toString(this.getScaleFactor(this.svgImageHeight, this.svgImageWidth, internalHeight, internalWidth)) + ")");
        rootDoc.appendChild(scaleElement);

        saveXML(this.doc, currentFile);

        // svgFile=currentFile;
        svgFile = this.nameOutputFile + "-" + this.numberOfSvgFiles + this.svgEnding;
        previewFile = this.outputFolder + this.namePreviewSvg + this.svgEnding;

    }

    private double getScaleFactor(String imageHeight, String imageWidth, double internalHeight, double internalWidth) {
        if (this.scaleFactor == 0) {
            double scaleHeight = Double.parseDouble(imageHeight) / internalHeight;
            double scaleWidth = Double.parseDouble(imageWidth) / internalWidth;
            if (scaleHeight < scaleWidth) {
                this.scaleFactor = scaleHeight;
                return scaleHeight;
            }
            this.scaleFactor = scaleWidth;
            return scaleWidth;
        }
        return this.scaleFactor;
    }

    public Document createXML() {
		/* call svg.SvgXmlFile.generateXML */
        Document document = generateXML();
        this.numberOfSvgFiles = this.numberOfSvgFiles + 1;
        return document;
    }

    /**
     * Method creates preview-SVG when called; should be called at the end of
     * the match because it needs the number of SVGs created
     */
    public void createPreviewSvg() {

        PreviewSvg pre = new PreviewSvg();

        pre.setImageHeight(this.svgImageHeight);
        pre.setImageWidth(this.svgImageWidth);
        pre.createPreviewSvg(this.outputFolder, this.configPath, this.numberOfSvgFiles, this.headInformationFirstLevel, this.headInformationSecondLevel);
    }

    public void createFolder(String name) {

        HandleFileFolder folder = new HandleFileFolder();
        folder.createFolder(this.path + name);
        this.setOutputFolder(this.path + name + System.getProperty("file.separator"));
    }

    private void setOutputFolder(String newPath) {
        this.outputFolder = newPath;
    }

    public void setTeamNames(Vector<String> teamNames2) {
        this.teamNames = teamNames2.toArray(new String[teamNames2.size()]);

        // set header information
        this.headInformationFirstLevel = "Graph Simulation 2014";
        this.headInformationSecondLevel = GraphSimulationVisualizationObserver.simulationName;
    }

    public String getTeamOne() {
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
     * Finds a polygon. This method is used recursively. It either returns null
     * if there is no set of edges to be removed, it return a single edge if the
     * edge has no predecessor or successor, or it returns a path describing a
     * polygon of the desired length.
     *
     * @param nodes
     *            the set of nodes of the graph.
     * @param edges
     *            the set of edges of the graph.
     * @param depth
     *            the desired size of the polygon.
     * @param path
     *            the current path.
     * @return
     */
    private LinkedList<EdgeDummy> findPolygon(Collection<GraphNode> nodes, LinkedList<EdgeDummy> edges, int depth, LinkedList<EdgeDummy> path) {

        // System.out.println("Path(" + depth + "):" + path);

        // should line be removed?
        // a line is supposed to be removed if it has no predecessor or
        // successor
        if (path.size() == 1) {
            EdgeDummy edge = path.getFirst();
            boolean hasPred = edge.hasPred();
            // for ( EdgeDummy cand : edges ) {
            // if ( edge.equals(cand) ) continue; //same edge
            // if ( cand.node1.equals(edge.node2) &&
            // cand.node2.equals(edge.node1) ) continue; // reverse edge
            // if ( cand.node2.equals(edge.node1) ) {
            // hasPred = true;
            // break;
            // }
            // }
            boolean hasSucc = edge.hasSucc();
            // for ( EdgeDummy cand : edges ) {
            // if ( edge.equals(cand) ) continue; //same edge
            // if ( cand.node1.equals(edge.node2) &&
            // cand.node2.equals(edge.node1) ) continue; // reverse edge
            // if ( cand.node1.equals(edge.node2) ) {
            // hasSucc = true;
            // break;
            // }
            // }
            if (!(hasPred && hasSucc))
                return path; // remove this line
        }

        // the path has the desired length. finished?
        if (path.size() == depth) {
            EdgeDummy firstEdge = path.getFirst();
            EdgeDummy lastEdge = path.getLast();
            // required: the path is a circle and it is oriented clockwise
            // assumed: the path is convex
            if (firstEdge.node1.equals(lastEdge.node2) && isPolygonClockwise(path)) {
                return path;
            }
            return null;
        }

        // not finished yet
        // consider the successors of the latest edge an see if a path can be
        // found
        // search all candidates = all remaining edges
        // for ( EdgeDummy cand : edges ) {
        //
        // // reject all invalid candidates
        // if ( path.contains(cand) ) continue; // already in path -> ignore
        // EdgeDummy last = path.getLast();
        // if ( cand.equals(last) ) continue; // self -> ignore
        // if ( cand.node1.equals(last.node2) && cand.node2.equals(last.node1) )
        // continue; // loop -> ignore
        // if ( !last.node2.equals(cand.node1) ) continue; // not connected ->
        // ignore
        //
        // // the canditate is valid -> make a new path and continue search
        // LinkedList<EdgeDummy> newPath = new LinkedList<EdgeDummy>();
        // newPath.addAll(path);
        // newPath.add(cand);
        // LinkedList<EdgeDummy> result = this.findPolygon(nodes, edges, depth,
        // newPath);
        // if ( result != null ) return result; // found something to remove ->
        // done
        //
        // }

        for (EdgeDummy e : path.getLast().succ) {
            LinkedList<EdgeDummy> newPath = new LinkedList<>();
            newPath.addAll(path);
            newPath.add(e);
            LinkedList<EdgeDummy> result = this.findPolygon(nodes, edges, depth, newPath);
            if (result != null)
                return result; // found something to remove -> done
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

        Vector<Point> points = new Vector<>();
        for (EdgeDummy edge : path) {
            points.add(new Point(edge.node1.x, edge.node1.y));
        }

        Iterator<Point> it = points.iterator();
        Point pt1 = (Point) it.next();
        Point firstPt = pt1;
        Point lastPt = null;
        double area = 0.0;
        while (it.hasNext()) {
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
    class EdgeDummy {
        public GraphNode node1;
        public GraphNode node2;
        public int weight;
        public Vector<EdgeDummy> succ = new Vector<>();
        public Vector<EdgeDummy> pred = new Vector<>();

        public String toString() {
            return "(" + this.node1 + "," + this.node2 + ")";
        }

        public boolean hasSucc() {
            return this.succ.size() > 0;
        }

        public boolean hasPred() {
            return this.pred.size() > 0;
        }
    }

    /**
     * Draws the non-changing info regarding the right part (tables etc.)
     */
    public void drawFixedInformation(GraphSimulationWorldState state, long width) {

        Node group = this.doc.getDocumentElement().getElementsByTagName("g").item(0);

        Element g = this.doc.createElement("g");
        g.setAttribute("id", "fixedTbl");
        g.setAttribute("transform", "translate(-100, 150)");

        int y = 80;

        // append headline
        Element text = this.doc.createElement("text");
        text.setAttribute("x", String.valueOf(width + 250));
        text.setAttribute("y", String.valueOf(y - 65));
        text.setAttribute("class", "TxtNrml");
        text.appendChild(this.doc.createTextNode(this.headInformationSecondLevel));
        g.appendChild(text);

        y += 60;

        // append headlines of the table
        g.appendChild(createRow(true, y, width, "", "Total Score", "Step Score", "Zone Value", "Ach. Pts"));

        y += 150; // to get it even with the dynamic information

        group.appendChild(g);

        // fixed part of the second table

        g = this.doc.createElement("g");
        g.setAttribute("id", "fixedTbl2");
        g.setAttribute("transform", "translate(-100, 150)");

        String[] headers = new String[] { "Role", "Energy", "Health", "Strength", "V.-range", "Last Act." };

        y = createRow(g, y, width, headers, null);

        // append static information (ag-name, role)

        Vector<GraphSimulationAgentState> agents = state.agents;

        for (GraphSimulationAgentState ag : agents) {
            String role;
            try {
                role = ag.roleName.substring(0, 2);
            } catch (IndexOutOfBoundsException e) {
                role = ag.roleName;
            }

            y = createStaticRow(g, y, width, new String[] { String.valueOf(role) }, ag);
        }

        group.appendChild(g);
    }

    /**
     * It is assumed, that the 1 to 2 last characters of the agent's name
     * represent its ID (and are a number)
     */
    private String parseID(String name) {

        try {
            Integer.parseInt(name.substring(name.length() - 2));
            return name.substring(name.length() - 2);
        } catch (NumberFormatException n) {
            return name.substring(name.length() - 1);
        }
    }

    private void drawCompass(int agx, int agy, int boxWidth) {
        drawCircle(agx + (boxWidth / 18) * 5, agy, boxWidth / 18, makeRGB(Color.white), 0, true, makeRGB(Color.white));
        drawCircle(agx + (boxWidth / 18) * 5, agy + (boxWidth / 9) * 2, boxWidth / 18, makeRGB(Color.white), 0, true, makeRGB(Color.white));
        drawCircle(agx + boxWidth / 9, agy + boxWidth / 9, boxWidth / 18, makeRGB(Color.white), 0, true, makeRGB(Color.white));
        drawCircle(agx + (boxWidth / 9) * 4, agy + boxWidth / 9, boxWidth / 18, makeRGB(Color.white), 0, true, makeRGB(Color.white));

        drawLine(agx + (boxWidth / 9) * 2, agy + boxWidth / 6, agx + (boxWidth / 9) * 4, agy + boxWidth / 6, (Color.white), 1);
        drawLine(agx + (boxWidth / 3), agy + boxWidth / 9, agx + (boxWidth / 3), agy + (boxWidth / 9) * 2, (Color.white), 1);
    }

    private void drawHeart(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx + (boxWidth / 9) * 2) + "," + (agy);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 9) * 4) + "," + (agy);
        p += " " + (agx + (boxWidth / 9) * 5) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 9) * 5) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy + (boxWidth / 3));
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 9));
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    private void drawWrench(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx + (boxWidth / 9)) + "," + (agy);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 3) * 2) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 3) * 2) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 3)) + "," + (agy + (boxWidth / 3));
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 3));
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 9) * 2) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 9) * 2) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 9));
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    private void drawLightning(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx + boxWidth / 3) + "," + (agy);
        p += " " + (agx + boxWidth / 3) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 9) * 4) + "," + (agy + (boxWidth / 9));
        p += " " + (agx + (boxWidth / 9) * 2) + "," + (agy + (boxWidth / 3));
        p += " " + (agx + (boxWidth / 9) * 2) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 9)) + "," + (agy + (boxWidth / 9) * 2);
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    private void drawDrill(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx + boxWidth / 6) + "," + (agy);
        p += " " + (agx + boxWidth / 2) + "," + (agy);
        p += " " + (agx + boxWidth / 3) + "," + (agy + boxWidth / 3);
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    private void drawGlasses(int agx, int agy, int boxWidth, Color innerCol) {

        drawCircle(agx, agy + (boxWidth / 9), (boxWidth / 9), makeRGB(Color.white), 1, true, makeRGB(innerCol));
        drawCircle(agx + boxWidth / 3, agy + boxWidth / 9, (boxWidth / 9), makeRGB(Color.white), 1, true, makeRGB(innerCol));

        drawLine(agx + (boxWidth / 9) * 2, agy + (boxWidth / 9) * 2, agx + (boxWidth / 3), agy + (boxWidth / 9) * 2, Color.white, 1);

        drawLine(agx, agy + (boxWidth / 9) * 2, agx + (boxWidth / 9) * 2, agy, Color.white, 1);
        drawLine(agx + (boxWidth / 9) * 5, agy + (boxWidth / 9) * 2, agx + (boxWidth / 3) * 2, agy, Color.white, 1);
    }

    private void drawMagnifyingGlass(int agx, int agy, int boxWidth) {

        drawLine(agx + (boxWidth / 9) * 2, agy + (boxWidth / 9), agx + (boxWidth / 9) * 4, agy + (boxWidth / 9) * 2, Color.white, 3);
        drawCircle(agx + boxWidth / 9, agy, (boxWidth / 9), makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    private void drawShield(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx + boxWidth / 6) + "," + (agy);
        p += " " + (agx + boxWidth / 2) + "," + (agy);
        p += " " + (agx + boxWidth / 2) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + boxWidth / 3) + "," + (agy + boxWidth / 3);
        p += " " + (agx + boxWidth / 6) + "," + (agy + (boxWidth / 9) * 2);
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    /**
     * Draws a sword. As in draw with a pen.
     */
    private void drawSword(int agx, int agy, int boxWidth) {
        String p = "";
        p += (agx) + "," + (agy);
        p += " " + (agx + boxWidth / 9) + "," + (agy);
        p += " " + (agx + (boxWidth / 9) * 4) + "," + (agy + boxWidth / 6);
        p += " " + (agx + (boxWidth / 9) * 5) + "," + (agy);
        p += " " + (agx + (boxWidth / 9) * 5) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 3) * 2) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx + (boxWidth / 9) * 5) + "," + (agy + boxWidth / 3);
        p += " " + (agx + boxWidth / 2) + "," + (agy + (boxWidth / 18) * 5);
        p += " " + (agx + (boxWidth / 9) * 4) + "," + (agy + boxWidth / 3);
        p += " " + (agx + (boxWidth / 9) * 2) + "," + (agy + boxWidth / 3);
        p += " " + (agx + (boxWidth / 9) * 4) + "," + (agy + (boxWidth / 9) * 2);
        p += " " + (agx) + "," + (agy + boxWidth / 9);
        drawPolygon(p, makeRGB(Color.white), 0, true, makeRGB(Color.white));
    }

    public void drawTargetLine(int x, int y, int x2, int y2, GraphSimulationAgentState ag, String lastAction, String lastActionResult, Color agentColor) {

        // draw dashed line in team-color
        String dashPattern = "30,30";

        drawLine(x, y, x2, y2, agentColor, 4);

        Color actionCol = agentColor;

        // draw dashed line according to action
        if (lastAction.equalsIgnoreCase("attack")) {
            actionCol = Color.ORANGE;
        } else if (lastAction.equalsIgnoreCase("inspect")) {
            actionCol = Color.MAGENTA;
        } else if (lastAction.equalsIgnoreCase("probe")) {
            actionCol = Color.CYAN;
        } else if (lastAction.equalsIgnoreCase("repair")) {
            actionCol = Color.PINK;
        }

        drawDashLine(x, y, x2, y2, dashPattern, 30, actionCol, 4);
    }

    // public void drawProbing(GraphNode n, Vector<TeamState> probeTeams) {
    //
    // int radius = (5 + 2 * n.weight)/2;
    //
    // //draw base circle containing the indicators
    // drawCircle(n.x, n.y, (5 + 2 * n.weight)/2, makeRGB(Color.GRAY), 1, true,
    // makeRGB(Color.GRAY));
    //
    // for ( TeamState team: probeTeams){
    //
    // Color teamColor = team.name.equals(teamNames[0])? Color.GREEN:
    // Color.BLUE;
    // // Set new color with alpha channel.
    // String domColor = makeRGB(
    // new Color(teamColor.getRed(),
    // teamColor.getGreen(),
    // teamColor.getBlue(),
    // 170));
    //
    // Node group = doc.getDocumentElement().getElementsByTagName("g").item(0);
    //
    // Element node = doc.createElement("path");
    //
    // int mx = n.x - radius;
    // int my = n.y;
    //
    // int lx = n.x + radius;
    // int ly = n.y;
    //
    // int ang = team.name.equals(teamNames[0])? 45: 135;
    //
    // node.setAttribute("d",
    // "M "+mx+" "+my+" "+"L "+lx+" "+ly+"a "+radius+" "+radius+" "+ang+
    // "0 0 "+mx+" "+my);
    //
    // String classValue = "";
    // if(vo.strokeClasses.containsKey(domColor)){
    // classValue += vo.strokeClasses.get(domColor);
    // }
    // else{
    // node.setAttribute("stroke", domColor);
    // }
    // classValue += " "+vo.strokeWidthClasses.get(1);
    //
    // if(vo.fillClasses.containsKey(domColor)){
    // classValue += " "+vo.fillClasses.get(domColor);
    // }
    // else{
    // node.setAttribute("fill", domColor);
    // }
    //
    // node.setAttribute("class", classValue);
    //
    // group.appendChild(node);
    // }
    // }

}

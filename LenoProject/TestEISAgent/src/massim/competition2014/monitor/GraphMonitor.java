package massim.competition2014.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import massim.competition2014.monitor.graph.AgentInfo;
import massim.competition2014.monitor.graph.EdgeInfo;
import massim.competition2014.monitor.graph.NodeInfo;
import massim.competition2014.monitor.graph.TeamInfo;
import massim.competition2014.monitor.graph.Util;
import massim.competition2014.monitor.render.Renderer;
import massim.competition2014.monitor.render.Renderer.VisMode;
import massim.framework.rmi.XMLDocumentServer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the RMI XML monitor application for 2014 Mars Scenario. Start with these options:<br/>
 * <br/>
 * <code>GraphMonitor [-rmihost &lt;host&gt;] [-rmiport &lt;port&gt;] [-savexmls [&lt;folder&gt;]]</code><br/>
 * <br/>
 * You can specify the host and the port of the RMI server 
 * (by default <code>localhost</code> and <code>1099</code>).<br/>
 * <br/>
 * By activating the <code>-savexmls</code> flag, the monitor stores all the well-formed XMLs received in a
 * properly named sub-folder of the specified <code>folder</code>, for later visualization using the
 * file-based viewer. If the <code>-savexmls</code> flag is active and no folder is specified, the current
 * working folder is used.
 */
public class GraphMonitor extends JFrame implements ActionListener, Runnable, ComponentListener {

	private static final long serialVersionUID = 4099856653707665324L;
	
	private Preferences prefs;
	
	// Config Info.
	private boolean saveXMLs = false;
	private String xmlsFolder = null;
	private String simId = null;
	private String fileBaseName = null;
	
	// 
	private Object xmlWriteSync = new Object();
	private Document xmlDoc1 = null;
	private Document xmlDoc2 = null;
	
	private String prevSimName;
	private String prevStep;
	
	private boolean drawBackground = true;
	private boolean autoZoom = false;
	
	private boolean biggerAgents = false;	

	private VisMode visMode;
	
	// RMI info
//	public boolean monitorStarted = false;
	public String rmihost="localhost";
	public int rmiport = 1099;
	public String service;
	
	@SuppressWarnings("unused")
	private boolean simulationRunning;
	
	// Window/panels
	WorldView worldView;
	JScrollPane scrollPane;
	InfoPanel infoPanel;
	JButton pauseButton;
	JToggleButton fitToWindowbutton;
	JToggleButton backgroundButton;
	JButton modeButton;
	JComboBox agentsNamesCombo;
	
	// Status
	private boolean parsedDoc = false;
	private Boolean paused = false;
	
	// World
	private Vector<String> teams;
	private Vector<NodeInfo> nodes;
	private TreeSet<String> agentNames = new TreeSet<String>();
	boolean agentNamesChanged = false;
	private Vector<EdgeInfo> edges;
	private Vector<TeamInfo> teamsInfo;
	private Vector<AgentInfo> agents;
	private String simStep;
	
	// For sync purposes
	Object syncObject = new Object();
	
	String selectedNode = null;
	String selectedAgent = null;
	Point selectedPosition = new Point(0,0);
	Point maxCoords = new Point(0,0);
	
	
	private final class AgentNameComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			if (o1 == null){
				if (o2 == null){
					return 0;
				}
				return -1;
			} else if (o2 == null){
				return 1;
			} 					
			if (Character.isDigit(o1.charAt(o1.length()-2))){
				if (Character.isDigit(o2.charAt(o2.length()-2))){
					return o1.compareToIgnoreCase(o2);
				}
				else {
					if (o1.substring(0, o1.length()-2)
							.equalsIgnoreCase(
							o2.substring(0, o2.length()-1))){
						return 1;
					}							
					return o1.compareToIgnoreCase(o2);
				}
			} else {
				if (Character.isDigit(o2.charAt(o2.length()-2))){
					if (o1.substring(0, o1.length()-1)
							.equalsIgnoreCase(
							o2.substring(0, o2.length()-2))){
						return -1;
					}							
					return o1.compareToIgnoreCase(o2);
				}
				else {
					return o1.compareToIgnoreCase(o2);
					
				}
			}
			

		}
	}
	
	private class WorldView extends JPanel implements MouseListener, ActionListener {
		
		private static final long serialVersionUID = 3959575661850468381L;

		private int sizeX;
		private int sizeY;
		private int margin = 50;
		private Image bgImage = null;
		
		private double[] zooms = {0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0};
		private int zoomIdx = 4;
		private double scale = 1.0;
		

		
		public WorldView(){
			this.addMouseListener(this);
			this.setDoubleBuffered(true);	
			
			String filesep = "/";
			java.net.URL url =    GraphMonitor.class.getResource("img"+filesep +"Surface.png");
			bgImage = new ImageIcon(url).getImage();
			
			autoZoom = prefs.getBoolean("autoZoom", autoZoom);
			zoomIdx = prefs.getInt("zoomIdx", zoomIdx);
			scale = zooms[zoomIdx];
			
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(bgImage, 0);
			try {
				mt.waitForAll();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		private void calculateSize(){
			NodeInfo agentNode = nodes.firstElement();
			int maxX = agentNode.x;
			int maxY = agentNode.y;
			for(NodeInfo node: nodes){
				if (node.x > maxX){
					maxX = node.x;
				}
				if (node.y > maxY){
					maxY = node.y;
				}
			}
			maxCoords = new Point(maxX,maxY);

			if (autoZoom){
				// If autoZoom (fit to screen), define the scale based on the width and height of the graph, and the size of the viewport.
				scale = Math.min(((double)scrollPane.getVisibleRect().width) / (maxX + margin), ((double)scrollPane.getVisibleRect().height) / (maxY + margin));
			}
			sizeX = (int)Math.round((maxX + margin) * scale);
			sizeY = (int)Math.round((maxY + margin) * scale);
			
			if (autoZoom){
				int viewX = scrollPane.getVisibleRect().width - scrollPane.getVerticalScrollBar().getWidth();
				int viewY = scrollPane.getVisibleRect().height - scrollPane.getHorizontalScrollBar().getHeight();
				this.setPreferredSize(new Dimension(viewX, viewY));
			} else {
				this.setPreferredSize(new Dimension(sizeX, sizeY));
			}
			//*/

			this.revalidate();
			
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			
			Graphics2D g2d = (Graphics2D)g;
			
			g2d.scale(1, 1);
			
			// enable anti aliasing
			RenderingHints renderingHints = new RenderingHints(
					RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.addRenderingHints(renderingHints);
			
			// clear
			g2d.setColor(Definitions.backgroundColor);
			g2d.fillRect(0, 0, this.getSize().width, this.getSize().height);
			
			// do not paint if the doc is not parsed
			synchronized (syncObject) {
				if (!parsedDoc){
					return;
				}
			}
					
			// go ahead... paint!
			synchronized (syncObject) {
				
				calculateSize();
				
				// draw the background
				if ( drawBackground ){
					g2d.drawImage(bgImage, 0, 0, sizeX, sizeY, null);
					Color prevColor = g2d.getColor();
					// draw a mask in front of the background image to reduce its contrast.
					g2d.setColor(Definitions.backgroundMaskColor);
					g2d.fillRect(0, 0, sizeX, sizeY);
					g2d.setColor(prevColor);
				}
				
				// take zoom into account
				g2d.scale(scale, scale);				
				
				// set up renderers... different modes = different sets of renderers
				Vector<Renderer> renderers = Renderer.getRenderersList(visMode);
				
				// render
				for ( Renderer renderer : renderers ) {
					renderer.render(nodes, edges, agents, teamsInfo, selectedAgent, g2d);
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {		
			if ( e.getClickCount() == 1 && parsedDoc) {			
				clickAt(e.getPoint().x,e.getPoint().y, e.getButton());			
			}		
			this.repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}
		
		public void clickAt(int x, int y, int button) {
			
			x = (int)Math.round(x/scale);
			y = (int)Math.round(y/scale);
			
			synchronized (syncObject) {
				for (NodeInfo n : nodes) {
					if ((n.x - x) * (n.x - x) + (n.y - y) * (n.y - y) <= Definitions.nodeRadius
							* Definitions.nodeRadius) {
						String oldNode = selectedNode;
						selectedNode = n.name;
						selectedAgent = null;
						firePropertyChange("nodeSelected", oldNode,
								selectedNode);
						selectedPosition = new Point(x,y);
						return;
					}
					
					int agRadius = Definitions.agentRadius;
					if(biggerAgents){
						agRadius = 26;
					}
					
					for (AgentInfo ag : n.agents) {
						//if( (ag.x - x)*(ag.x - x) + (ag.y - y)*(ag.y - y) <= agentRadius*agentRadius ) {
						if (Math.abs(ag.x - x) <= agRadius
								&& Math.abs(ag.y - y) <= agRadius) {
							String oldAgent = selectedAgent;
							selectedAgent = ag.name;
							selectedNode = null;
							firePropertyChange("agentSelected", oldAgent,
									selectedAgent);
							selectedPosition = new Point(x,y);
						}
					}
				}
			}
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if ("zoomIn".equals(e.getActionCommand())){
				autoZoom = false;
				fitToWindowbutton.setSelected(autoZoom);
				if ( zoomIdx < zooms.length-1){
					zoomIdx++;
				}
				scale = zooms[zoomIdx];
				prefs.putInt("zoomIdx", zoomIdx);
				prefs.putBoolean("autoZoom", autoZoom);
			}
			else if ("zoomOut".equals(e.getActionCommand())){
				autoZoom = false;
				fitToWindowbutton.setSelected(autoZoom);
				if ( zoomIdx > 0){
					zoomIdx--;
				}
				scale = zooms[zoomIdx];
				prefs.putInt("zoomIdx", zoomIdx);
				prefs.putBoolean("autoZoom", autoZoom);
				
			}
			else if ("fitWindow".equals(e.getActionCommand())){
				if (autoZoom){
					scale = zooms[zoomIdx];
				}
				autoZoom = !autoZoom;
				fitToWindowbutton.setSelected(autoZoom);
				prefs.putBoolean("autoZoom", autoZoom);
			}
			else if ("toggleBackground".equals(e.getActionCommand())){
				drawBackground = !drawBackground;
				prefs.putBoolean("drawBackground", drawBackground);
			}
			else if ("toggleMode".equals(e.getActionCommand())){
				visMode = Renderer.getNextMode(visMode);
				modeButton.setText("Mode " + Renderer.getRendererName(visMode));
				if(visMode == VisMode.MODE_2013){ // TODO this is a Hack!
					biggerAgents = true;
				}
				else{
					biggerAgents = false;
				}
				prefs.putInt("visMode", visMode.ordinal());
			}
			else if ("selectAgent".equals(e.getActionCommand())){
				String agentName = (String)agentsNamesCombo.getSelectedItem();
				if(!(agentName == null && selectedAgent == null)){
					String oldAgent = selectedAgent;
			    	selectedAgent = agentName;		        
					selectedNode = null;
					firePropertyChange("agentSelected", oldAgent,
							selectedAgent);
				}
			}
			else {
				System.out.println("unknown action " + e.getActionCommand());
			}
			this.repaint();
			
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if(("zoomIn".equals(e.getActionCommand()) || "zoomOut".equals(e.getActionCommand())) && !selectedPosition.equals(new Point(0,0))){
						Rectangle visRect = scrollPane.getViewport().getVisibleRect();
						
						Point corner = new Point((int)(scale*(double)selectedPosition.x - visRect.width/2), (int)(scale*(double)selectedPosition.y-visRect.height/2) );
						scrollPane.getViewport().setViewPosition(corner);
					}
				}
			});
			
		}
	
	}
	
	private class InfoPanel extends JPanel implements PropertyChangeListener{

		private static final long serialVersionUID = 1663621252127221096L;
		
		
		private Vector<JLabel> labels;
		private int lastLabel = -1;
		
		public InfoPanel() {
			super(true);
			this.setPreferredSize(new Dimension(170, 0));
			this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			labels = new Vector<JLabel>(25);
			
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("agentSelected".equals(evt.getPropertyName()) ||
					"nodeSelected".equals(evt.getPropertyName())) {
				String newValue = (String)evt.getNewValue();
				String oldValue = (String)evt.getOldValue();
				if (newValue == null){
					if (oldValue == null){
						return;
					}
				}
				else if (newValue.equals(oldValue)){
					return;
				}
				update();
			}
		}
		
		public void update(){
			NodeInfo node = null;
			AgentInfo agent = null;
			TeamInfo team = null;
			String step = null;
			String simName = null;
			int i = 0;
			
			synchronized (syncObject) {
				if (!parsedDoc){
					return;
				}
				step = simStep;
				simName = simId;
				if (selectedNode != null){
					node = Util.searchNode(selectedNode, nodes);
					if (node != null){
						team = Util.searchTeam(node.dominatorTeamName, teamsInfo);
					}
				} else if (selectedAgent != null){
					agent = Util.searchAgent(selectedAgent, nodes);
					if (agent != null){
						team = Util.searchTeam(agent.teamName, teamsInfo);
					}
				}
				agentsNamesCombo.setSelectedItem(selectedAgent);
			}
			
			getLabel(i).setText("     ----SIMULATION----   ");
			getLabel(++i).setText("  " + simName);
			getLabel(++i).setText("  Step:  " + step);
			getLabel(++i).setText("  Current ranking:");
			for (TeamInfo rkTeam: teamsInfo){
				if (team != null){
					if (team.equals(rkTeam)){
						getTeamColoredLabel(++i,rkTeam).setText("    " + rkTeam.name + ":     " + rkTeam.score);
					} else {
						getLabel(++i).setText("    " + rkTeam.name + ":     " + rkTeam.score);
					}
				} else {
					getTeamColoredLabel(++i,rkTeam).setText("    " + rkTeam.name + ":     " + rkTeam.score);
				}
			}
			if (node != null){
				getLabel(++i).setText("    ");
				getLabel(++i).setText("     ----VERTEX----   ");
				getLabel(++i).setText("  Node name:   " + node.name);
				getLabel(++i).setText("  Weight:   " + node.weight);
				getLabel(++i).setText("  Domintad by:   " + (node.dominatorTeamName!= null ? node.dominatorTeamName:"-"));
			} else if (agent != null){
				getLabel(++i).setText("    ");
				getLabel(++i).setText("     ----AGENT----   ");
				getLabel(++i).setText("  Agent name:   " + agent.name);
				getLabel(++i).setText("  Team:   " + agent.teamName);
				getLabel(++i).setText("  Role:   " + agent.role);
				getLabel(++i).setText("  Status:   " + (agent.health == 0? "Disabled" : "Normal"));
				getLabel(++i).setText("  Energy:   " + agent.energy + "/" + agent.maxEnergy);
				getLabel(++i).setText("  Health:   " + agent.health + "/" + agent.maxHealth);
				getLabel(++i).setText("  Strength:   " + agent.strength);
				getLabel(++i).setText("  Visibility range:   " + agent.visRange);
				getLabel(++i).setText("  Last action:   " + agent.lastAction);
				getLabel(++i).setText("  - Parameter:   " + agent.lastActionParam);
				getLabel(++i).setText("  - Result:   " + agent.lastActionResult);
			}
			if (team != null) {
				getLabel(++i).setText("   ");
				getLabel(++i).setText("     ----TEAM----   ");
				getLabel(++i).setText("  Team name:   " + team.name);
				getLabel(++i).setText("  Total score:   " + team.score);
				getLabel(++i).setText("  Step score:   " + team.stepScore);
				getLabel(++i).setText("  Zones score:   " + team.zonesScore);
				getLabel(++i).setText("  Current ach. pts:   " + team.achievementPoints);
				getLabel(++i).setText("  Used ach. pts:   " + team.usedAchievementPoints);
				if (team.achievements.size() > 0) {
					getLabel(++i).setText("  ");
					getLabel(++i).setText("  Achievements:");
					for (String ach : team.achievements) {
						getLabel(++i).setText("   * " + ach);
					}
				}
			}
			
			lastLabel = i;			
			clearLabels(++i);			
			this.repaint();
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			int newHeight = this.calculateNewHeight(lastLabel);
			this.setPreferredSize(new Dimension(170, newHeight));
			this.revalidate();
		}
			
		private JLabel getTeamColoredLabel(int i, TeamInfo team){
			return getColoredLabel(i,Definitions.agentColors[getTeamNr(team.name)]);
		}
		
		private JLabel getColoredLabel(int i, Color color){
			JLabel l = getLabel(i);
			l.setForeground(color);
			return l;
		}
		
		private JLabel getLabel(int i){
			if (i < this.labels.size()){
				JLabel l = this.labels.get(i);
				l.setForeground(Color.BLACK); 
				return l;
			}
			JLabel l = null;
			while (i >= this.labels.size()){
				l =  new JLabel("");
				l.setForeground(Color.BLACK);
				l.setAlignmentX(LEFT_ALIGNMENT); 
				this.add(l);
				this.labels.add(l);
			}
			return l;
		}
		
		private int calculateNewHeight(int labelIdx){
			if (lastLabel < 0) { 
				return 0; 
			}
			JLabel l = this.labels.get(labelIdx);
			return l.getBounds().y + l.getBounds().height;
		}
		
		private void clearLabels(int i){
			while (i < this.labels.size()){
				this.labels.get(i).setText("");
				i++;
			}
		}
		
	}
	
	
	
	// Beginning of main class

	public GraphMonitor(String[] args){
		
		this();
		
		System.out.println("GraphMonitor [-rmihost <host>] [-rmiport <port>] [-savexmls [<folder>]]");
		
		for(int i = 0; i<args.length;i++){
			if(args[i].equalsIgnoreCase("-rmihost"))
				this.rmihost = args[i+1];
			if(args[i].equalsIgnoreCase("-rmiport"))
				this.rmiport = Integer.parseInt(args[i+1]);
			if(args[i].equalsIgnoreCase("-savexmls")){
				saveXMLs = true;
				if (i+1 < args.length && !args[i+1].startsWith("-")){
					xmlsFolder = args[i+1];
				}
			}
		}
		
	}
	
	public GraphMonitor(){
		
		try  {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing, use default.
		}
		prefs = Preferences.userRoot().node(this.getClass().getName());
		drawBackground = prefs.getBoolean("drawBackground", true);
		try {
			visMode = VisMode.values()[prefs.getInt("visMode", 0)];
		} catch (Exception e) {
			visMode = VisMode.values()[0];
		}
		
		// Set size for window
		int sizeX = prefs.getInt("windowSizeX", 800);
		int sizeY = prefs.getInt("windowSizeY", 700);
		if (sizeX < 400 || sizeY < 300){ // Make sure is is not too small.
			sizeX = 800;
			sizeY = 700;
		}		
		this.setSize(sizeX, sizeY);
		this.addComponentListener(this);
		
		this.setTitle("Agent Contest 2014");
		worldView = new WorldView();

		scrollPane = new JScrollPane(worldView);
		
		// New panel
		JPanel mainPanel = new JPanel(new BorderLayout(), true);
		
		// Upper Menu
		JPanel menuPanel = createUpperMenu();
		
		mainPanel.add(menuPanel, BorderLayout.NORTH);
		
		// Right menu
		infoPanel = new InfoPanel();
		worldView.addPropertyChangeListener("nodeSelected", infoPanel);
		worldView.addPropertyChangeListener("agentSelected", infoPanel);
		JScrollPane infoScrollPane = new JScrollPane(infoPanel); //Just modified
		
		// Create SplitPanel and add it to main
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPane, infoScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(this.getWidth()-200);
		splitPane.setResizeWeight(1.0);
		mainPanel.add(splitPane, BorderLayout.CENTER);
		
		this.add(mainPanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	/**
	 * Creates the upper menu.
	 * 
	 * @return
	 */
	protected JPanel createUpperMenu() {
		
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel,BoxLayout.X_AXIS));
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		menuPanel.add(pauseButton);
		
		menuPanel.add(Box.createRigidArea(new Dimension(25, 0)));
		
		addButtons(menuPanel);
	
		return menuPanel;
	
	}

	/**
	 * Adds the buttons to the panel.
	 * @param menuPanel
	 */
	protected void addButtons(JPanel menuPanel) {
		JButton button = new JButton("Zoom in");
		button.addActionListener(worldView);
		button.setActionCommand("zoomIn");
		menuPanel.add(button);
		
		button = new JButton("Zoom out");
		button.addActionListener(worldView);
		button.setActionCommand("zoomOut");
		menuPanel.add(button);
		
		menuPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		
		fitToWindowbutton = new JToggleButton("Fit to Window");
		fitToWindowbutton.setSelected(autoZoom);
		fitToWindowbutton.addActionListener(worldView);
		fitToWindowbutton.setActionCommand("fitWindow");
		menuPanel.add(fitToWindowbutton);

		backgroundButton = new JToggleButton("Background");
		backgroundButton.setSelected(drawBackground);
		backgroundButton.addActionListener(worldView);
		backgroundButton.setActionCommand("toggleBackground");
		menuPanel.add(backgroundButton);

		modeButton = new JButton("Mode " + Renderer.getRendererName(visMode));
		modeButton.addActionListener(worldView);
		modeButton.setActionCommand("toggleMode");
		menuPanel.add(modeButton);
		
		agentsNamesCombo = new JComboBox();
		agentsNamesCombo.addActionListener (worldView);
		agentsNamesCombo.setActionCommand("selectAgent");
		menuPanel.add(agentsNamesCombo);

	}
	
	/**
	 * Runs the monitor.
	 */
	private void runMonitor() {
		
		Thread guiThread = new Thread(this);
		guiThread.setDaemon(true);
		
		boolean parsingThreadStarted = false;
		
		while (true) {
			try {
				this.searchService();
				while (true) {
					Document xmlDoc = getRMIObject(this.rmihost, this.rmiport,
							this.service);
					if (xmlDoc != null) {

						synchronized (xmlWriteSync) {
							xmlDoc1 = xmlDoc;
							xmlWriteSync.notifyAll();
							//guiThread.notify();
						}
						
						if (!parsingThreadStarted){
							guiThread.start();
							parsingThreadStarted = true;
						}

					    this.saveXML(xmlDoc1);

					}

				}
			} catch (Exception e) {
//				e.printStackTrace(); // TODO remove
				try {
					Thread.sleep(500);
				} catch (InterruptedException e2) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Updates the view
	 */
	protected void updateView() {
		
		// update the info panel
		infoPanel.update();
		
		// update the combo box if the agent names have changed
		if ( agentNamesChanged ) {
			agentsNamesCombo.removeAllItems();
			agentsNamesCombo.addItem(null);
			ArrayList<String> orderedNames = new ArrayList<String>(agentNames);
			Collections.sort(orderedNames, new AgentNameComparator());
			for ( String agentName : orderedNames ) {
				agentsNamesCombo.addItem(agentName);
			}
			agentNamesChanged = false;
		}
		
		worldView.repaint();
	}

	/**
	 * Searches for an RMI service.
	 */
	public void searchService() {
		
		boolean serviceRunning = false;
		while(!serviceRunning){
			try {
				Registry r = LocateRegistry.getRegistry(rmihost,rmiport);
				String[] s = r.list();
				for(int i = 0 ; i< s.length; i++){
					System.out.println("RMI Service found: "+s[i]);
					if(s[i].contains("xmlsimulationmonitor")){
						serviceRunning = true;
						service = s[i];
						System.out.println("take RMI Service: "+s[i]);
						break;
					}					
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {				
					//e.printStackTrace();
				}				
			} catch (RemoteException e1) {
				System.out.println("There is no running server on: "+this.rmihost+":"+this.rmiport);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {				
					//e.printStackTrace();
				}
				//e1.printStackTrace();			
			}
		}		
	
	}
	
	/**
	 * Parses an XML-document.
	 * 
	 * @param doc
	 */
	public void parseXML(Document doc) {
		
		try {			
			
			TreeSet<String> newAgentNames = new TreeSet<String>();
			Vector<AgentInfo> newAgents = new Vector<AgentInfo>();
			
			String step;
			String simName;
			NodeList nl = doc.getElementsByTagName("state");
			Element e = (Element) nl.item(0);
			if(e != null){ //else nothing to parse
				step = e.getAttribute("step");
				simName = e.getAttribute("simulation");
				
				Vector<NodeInfo> newNodes = new Vector<NodeInfo>();
				HashMap<String,NodeInfo> newNodesMap = new HashMap<String, NodeInfo>();
				nl = doc.getElementsByTagName("vertex");
				for (int i = 0; i < nl.getLength(); i++) {
					Element e1 = (Element) nl.item(i);
					NodeInfo node = new NodeInfo();
					
					node.name = e1.getAttribute("name");
					node.gridX =  Integer.parseInt(e1.getAttribute("gridX"));
					node.gridY =  Integer.parseInt(e1.getAttribute("gridY"));
					node.x =  Integer.parseInt(e1.getAttribute("x"));
					node.y =  Integer.parseInt(e1.getAttribute("y"));
					node.weight =  Integer.parseInt(e1.getAttribute("weight"));
					if(e1.hasAttribute("dominatorTeam")){
						node.dominatorTeamName = e1.getAttribute("dominatorTeam");
					} else {
						node.dominatorTeamName = null;
					}
					node.dominatorTeam = getTeamNr(node.dominatorTeamName);
					
					//node.agents = new Vector<AgentInfo>();
					NodeList agentsNl = e1.getElementsByTagName("entity");
					for (int j = 0; j < agentsNl.getLength(); j++) {
						Element agentEl = (Element) agentsNl.item(j);
						AgentInfo agent = new AgentInfo();
						
						agent.name = agentEl.getAttribute("name");
						agent.teamName = agentEl.getAttribute("team");
						agent.team = getTeamNr(agentEl.getAttribute("team"));
						//agent.node = agentEl.getAttribute("node");
						agent.role = agentEl.getAttribute("roleName");
						agent.strength = Integer.parseInt(agentEl.getAttribute("strength"));
						agent.maxEnergy = Integer.parseInt(agentEl.getAttribute("maxEnergy"));
						agent.energy = Integer.parseInt(agentEl.getAttribute("energy"));
						agent.health = Integer.parseInt(agentEl.getAttribute("health"));
						agent.maxHealth = Integer.parseInt(agentEl.getAttribute("maxHealth"));
						agent.visRange = Integer.parseInt(agentEl.getAttribute("visRange"));
						agent.lastAction = agentEl.getAttribute("lastAction");
						agent.lastActionParam = agentEl.getAttribute("lastActionParam");
						agent.lastActionResult = agentEl.getAttribute("lastActionResult");
						agent.node = node;
						node.agents.add(agent);
						newAgents.add(agent);
						newAgentNames.add(agent.name);
					}				
					newNodes.add(node);
					newNodesMap.put(node.name, node);
				}
				
				// agent names
				if ( !agentNames.equals(newAgentNames) ) {
					agentNames = newAgentNames;
					agentNamesChanged = true;
				}
				
				Vector<EdgeInfo> newEdges = new Vector<EdgeInfo>();
				nl = doc.getElementsByTagName("edge");
				for (int i = 0; i < nl.getLength(); i++) {
					Element e1 = (Element) nl.item(i);
					EdgeInfo edge = new EdgeInfo();
					
					edge.weight =  Integer.parseInt(e1.getAttribute("weight"));
					edge.node1 = newNodesMap.get(e1.getAttribute("node1"));
					edge.node2 = newNodesMap.get(e1.getAttribute("node2"));
					
					if ( !edge.node1.neighbors.contains(edge.node2) ) edge.node1.neighbors.add(edge.node2);
					if ( !edge.node2.neighbors.contains(edge.node1) ) edge.node2.neighbors.add(edge.node1);
	
					newEdges.add(edge);
				}
				
				Vector<TeamInfo> currentTeams = new Vector<TeamInfo>();
				nl = doc.getElementsByTagName("team");
				for (int i = 0; i < nl.getLength(); i++) {
					Element e1 = (Element) nl.item(i);
					TeamInfo team = new TeamInfo();
					
					team.name = e1.getAttribute("name");
					team.number = getTeamNr(team.name);
					team.score =  Integer.parseInt(e1.getAttribute("score"));
					team.achievementPoints =  Integer.parseInt(e1.getAttribute("achievementPoints"));
					team.usedAchievementPoints =  Integer.parseInt(e1.getAttribute("usedAchievementPoints"));
					team.stepScore =  Integer.parseInt(e1.getAttribute("stepScore"));
					team.zonesScore =  Integer.parseInt(e1.getAttribute("zonesScore"));
					
					team.achievements = new Vector<String>();
					NodeList nodeList = e1.getElementsByTagName("achievements");
					if (nodeList.getLength() > 0){
						nodeList = ((Element)nodeList.item(0)).getElementsByTagName("achievement");
						for (int j = 0; j < nodeList.getLength(); j++) {
							Element agentEl = (Element) nodeList.item(j);
							String name = agentEl.getAttribute("name");					
							team.achievements.add(name);
						}
					}
					
					////////
					team.provedNodes = new Vector<String>();
					nodeList = e1.getElementsByTagName("provedNodes");
					if (nodeList.getLength() > 0){
						nodeList = ((Element)nodeList.item(0)).getElementsByTagName("node");
						for (int j = 0; j < nodeList.getLength(); j++) {
							Element agentEl = (Element) nodeList.item(j);
							String name = agentEl.getAttribute("name");					
							team.provedNodes.add(name);
						}
					}
					////////
					
					currentTeams.add(team);
				}
				
				Collections.sort(currentTeams, new Comparator<TeamInfo>(){
	
					@Override
					public int compare(TeamInfo o1, TeamInfo o2) {
						if (o1.score > o2.score){
							return -1;
						} else if (o1.score < o2.score){
							return +1;
						} else {
							return o1.name.compareTo(o2.name);
						}
					}
					
				});
				
				if (!paused.booleanValue()) {
					synchronized (syncObject) {
						nodes = new Vector<NodeInfo>(newNodesMap.values());
						edges = newEdges;
						agents = newAgents;
						teamsInfo = currentTeams;
						simStep = step;
						parsedDoc = true;
						simId = simName;
					}
				}
			}
			
		} catch (Exception e) {
			synchronized (syncObject) {
				parsedDoc = false;
			}
			e.printStackTrace(); // TODO remove
		}
	}

	
	/**
	 * Saves an XML file.
	 * 
	 * @param doc
	 */
	public void saveXML(Document doc) {
		
		if (saveXMLs) {
			try {
				String step;
				String simName;
				NodeList nl = doc.getElementsByTagName("state");
				Element e = (Element) nl.item(0);
				if(e != null){ //else nothing to save
					step = e.getAttribute("step");
					simName = e.getAttribute("simulation");
	
					if (step != null && simName != null
							&& !(simName.equals(prevSimName) && step.equals(prevStep))) {
						try {
							TransformerFactory tFactory = TransformerFactory
									.newInstance();
							Transformer transformer = tFactory.newTransformer();
							DOMSource source = new DOMSource(doc);
							File f = new File(xmlsFolder, simName + File.separator);
							if (!f.exists()) {
								f.mkdirs();
							}
							f = new File(xmlsFolder, simName + File.separator
									+ simName + "_" + step + ".xml");
							if (!f.exists()) {
								f.createNewFile();
							}
							StreamResult result = new StreamResult(f);
							transformer.transform(source, result);
	
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}

			} catch (Exception e) {
				//parsedDoc = false;
				e.printStackTrace(); // TODO remove
			}
		}
	}
	
	
	/**
	 * Yields the number of a team.
	 * 
	 * @param name
	 * @return
	 */
	public int getTeamNr(String name) {

		if (name == null || "".equals(name)) { return -1; }
		if (teams == null ) { teams = new Vector<String>(); }
		if (teams.indexOf(name) == -1) {
			teams.add(name); 
		}
		
		return teams.indexOf(name);

	}
	
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param service
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 */
	protected Document getRMIObject(String host, int port, String service) throws RemoteException, NotBoundException {
		Document	xmlDoc = null;
		try {
			simulationRunning = true;
			Registry registry = LocateRegistry.getRegistry(host,port); 
			XMLDocumentServer serverState = (XMLDocumentServer) registry.lookup(service);
			try {
				xmlDoc = serverState.getXMLDocument();
			} catch (NullPointerException e) {
				simulationRunning = false;
				throw new RemoteException(
						"NullPointerException while trying to get XMLDocument",
						e);

			}
		} catch (RemoteException e) {
			System.err.println("Currently no simulation running on " + e + " " + host
					+ " " + port + "...\n");
			throw e;

		} catch (NotBoundException e) {
			System.err.println("Currently no simulation running on " + e + " " + host
					+ " " + port + "...\n");
			throw e;
		}
		return xmlDoc;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		GraphMonitor graph = new GraphMonitor(args);
		graph.runMonitor();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("pause".equals(e.getActionCommand())){			
			synchronized (paused) {
				if (paused.booleanValue()){
					pauseButton.setText("pause");
					paused = false;
				} else {
					pauseButton.setText("resume");
					paused = true;
				}
			}
		}
	}

	@Override
	public void run() {

		while (true) {
			
			try {
				if (xmlDoc1 != null && xmlDoc2 != xmlDoc1 ) {
					synchronized (xmlWriteSync) {
						xmlDoc2 = xmlDoc1;
					}
					synchronized (paused) {
						    this.parseXML(xmlDoc2);
					}
					updateView();
				} else {
					try {
						xmlWriteSync.wait();
					} catch (InterruptedException e) {
						// reasume.
					}
				}
				
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e2) {
					e.printStackTrace();
				}
			}

			
		}

		
	}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		// Save preferences for new size.
		prefs.putInt("windowSizeX", this.getWidth());
		prefs.putInt("windowSizeY", this.getHeight());
	}


	
}



package massim.competition2015.monitor;

import massim.competition2015.monitor.data.*;
import massim.competition2015.monitor.render.CopyrightNotePainter;
import massim.competition2015.monitor.render.DimMapPainter;
import massim.competition2015.monitor.render.Renderer;
import massim.competition2015.monitor.render.RenderersManager;
import massim.competition2015.monitor.render.RenderersManager.VisMode;
import massim.competition2015.scenario.Job;
import massim.framework.rmi.XMLDocumentServer;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.JXMapKit.DefaultProviders;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * This is the RMI XML monitor application for 2015 Mars Scenario. Start with these options:<br/>
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
	//private String fileBaseName = null;

	private Object xmlWriteSync = new Object();
	private Document xmlDoc1 = null;
	private Document xmlDoc2 = null;

	private String prevSimName;
	private String prevStep;
	
	private boolean drawBackground = false;
	// private boolean autoZoom = false;

	private VisMode visMode;
	
	// RMI info
	public String rmihost="localhost";
	public int rmiport = 1099;
	public String service;

	@SuppressWarnings("unused")
	private boolean simulationRunning;
	
	// Window/panels
	private WorldView worldView;
	private JScrollPane scrollPane;
	private InfoPanel infoPanel;
	private DebugPanel debugPanel;
	private JButton pauseButton;
	private JButton fitToWindowbutton;
	private JToggleButton backgroundButton;
	private JButton modeButton;
	private JComboBox<String> agentsNamesCombo;
	
	// Status
	private boolean parsedDoc = false;
	private Boolean paused = false;
	
	
	// For sync purposes
	Object syncObject = new Object();
	
	// For update purposes
	private String selectedAgent = null;
	private String selectedFacility = null;
	
    private boolean facilitiesNamesChanged = false;
	private boolean jobsNamesChanged = false;
	private boolean agentNamesChanged = false;
	private boolean mapDimentionsChanged = false;
	
	private Vector<String> teamsNames;
	
	// World
	
	private WorldInfo world;
	

	
	// begin of methods.


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
	
	private class WorldView extends JXMapKit implements MouseListener, ActionListener {
		
		private static final long serialVersionUID = 3959575661850468381L;
				
		private CompoundPainter<JXMapViewer> cPainter;

		public WorldView(){
			this.getMainMap().addMouseListener(this);
			
			this.setDoubleBuffered(true);	
			
			// create a local cache for tiles
			TileFactoryInfo l_info = new OSMTileFactoryInfo();
	        DefaultTileFactory l_tileFactory = new DefaultTileFactory(l_info);
	        LocalResponseCache.installResponseCache(l_info.getBaseURL(), new File("cache" + File.separator + "tiles"), false);
	        this.setTileFactory(l_tileFactory);
			
			
			// this.setMiniMapVisible(false);
			// this.setZoomSliderVisible(false);
			// this.setZoomButtonsVisible(false);
			// this.setDataProviderCreditShown(true);
			GeoPosition center = new GeoPosition(51.805, 10.345);
			this.setAddressLocation(center);
			//this.setAddressLocationShown(true);
			this.setZoom(5);
			
			
			this.cPainter = new CompoundPainter<>();
			//this.cPainter.addPainter(painter);
			
			this.getMainMap().setOverlayPainter(cPainter);
			
		}
		
		public void update() {
			
			if (mapDimentionsChanged) {
				zoomToBestFit();
				mapDimentionsChanged = false;
			}
			
			ArrayList<Painter<JXMapViewer>> painters =  new ArrayList<>();
			
			if (drawBackground){
				painters.add(DimMapPainter.getInstance());
			}
			
			painters.add(CopyrightNotePainter.getInstance());
			
			// set up renderers... different modes = different sets of renderers
			Vector<Renderer> renderers = RenderersManager.getRenderersList(visMode);
			
			// render
			for ( Renderer renderer : renderers ) {
				renderer.setData( world, selectedAgent, selectedFacility);
				painters.add(renderer);
			}
			
			cPainter.setPainters(painters);
			this.repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if ( e.getClickCount() == 1 && parsedDoc && e.getButton() == MouseEvent.BUTTON1) {
				clickAt(e.getPoint());
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

		public void clickAt(Point clickPoint) {
			
			JXMapViewer map = this.getMainMap();
	       
	        
	        //convert to screen
	        Rectangle rect = map.getViewportBounds();
			
	        boolean selected = false;
	        
	        synchronized (syncObject) {
				for (AgentInfo ag : world.agents) {
					GeoPosition gp = new GeoPosition(ag.lat, ag.lon);
					Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
					Point converted_gp_pt = new Point((int)gp_pt.getX()-rect.x,
	                        (int)gp_pt.getY()-rect.y);
					
					if (converted_gp_pt.distance(clickPoint) < 20) {
						String oldAgent = selectedAgent;
						selectedAgent = ag.name;
						selectedFacility = null;
						firePropertyChange("agentSelected", oldAgent,
								selectedAgent);
						selected = true;
						return;
					}
					
				}
				
				for (FacilityInfo fac : world.facilities) {
					GeoPosition gp = new GeoPosition(fac.lat, fac.lon);
					Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
					Point converted_gp_pt = new Point((int)gp_pt.getX()-rect.x,
	                        (int)gp_pt.getY()-rect.y);
					
					if (converted_gp_pt.distance(clickPoint) < 20) {
						String oldFacility = selectedFacility;
						selectedFacility = fac.name;
						selectedAgent = null;
						firePropertyChange("facilitySelected", oldFacility,
								selectedFacility);
						selected = true;
						return;
					}
					
				}
	        }
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if ("zoomIn".equals(e.getActionCommand())){
				this.setZoom(this.getMainMap().getZoom() - 1);
			}
			else if ("zoomOut".equals(e.getActionCommand())){
				this.setZoom(this.getMainMap().getZoom() + 1);
			}
			else if ("fitWindow".equals(e.getActionCommand())){
				if ( parsedDoc ){
					zoomToBestFit();
				}
			}
			else if ("toggleBackground".equals(e.getActionCommand())){
				drawBackground = !drawBackground;
				prefs.putBoolean("drawBackground", drawBackground);
				this.update();
			}
			else if ("toggleMode".equals(e.getActionCommand())){
				visMode = RenderersManager.getNextMode(visMode);
				modeButton.setText("Mode " + RenderersManager.getRendererName(visMode));

				prefs.putInt("visMode", visMode.ordinal());
				this.update();
			}
			else if ("selectAgent".equals(e.getActionCommand())){
				String agentName = (String)agentsNamesCombo.getSelectedItem();
				if(!(agentName == null && selectedAgent == null)){
					String oldAgent = selectedAgent;
			    	selectedAgent = agentName;
					selectedFacility = null;
					firePropertyChange("agentSelected", oldAgent,
							selectedAgent);
				}
				this.update();
			}
			else {
				System.out.println("unknown action " + e.getActionCommand());
			}
/*
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
			*/

		}

		private void zoomToBestFit() {
			GeoPosition max = new GeoPosition(world.maxLat, world.maxLon);
			GeoPosition min = new GeoPosition(world.minLat, world.minLon);
			GeoPosition avg = new GeoPosition((world.maxLat+world.minLat)/2, (world.maxLon+world.minLon)/2);
			Set<GeoPosition> positions = new HashSet<>(2, 2);
			positions.add(max);
			positions.add(min);
			this.getMainMap().zoomToBestFit(positions, 0.98);
			this.setAddressLocation(avg);
			this.getMainMap().recenterToAddressLocation();
		}


		

	}

	private class DebugPanel extends JPanel implements ActionListener {

		private static final long serialVersionUID = 658811200030399869L;

		private JTextArea agentInfo = new JTextArea();
		private JTextArea facilityInfo = new JTextArea();
		private JTextArea jobInfo = new JTextArea();
		JComboBox<String> cboxAgents;
		JComboBox<String> cboxFacilities;
		JComboBox<String> cboxJobs;

		public DebugPanel(){
			super(true);
			this.setLayout(new BorderLayout());

			cboxAgents = new JComboBox<>();
			cboxFacilities = new JComboBox<>();
			cboxJobs = new JComboBox<>();

			JPanel combosPanel = new JPanel(new GridLayout(1,3), true);
			combosPanel.add(cboxAgents); combosPanel.add(cboxFacilities); combosPanel.add(cboxJobs);
			this.add(combosPanel, BorderLayout.NORTH);
			
			JPanel textPanel = new JPanel(new GridLayout(1,3), true);
			textPanel.add(agentInfo); textPanel.add(facilityInfo); textPanel.add(jobInfo);
			this.add(new JScrollPane(textPanel), BorderLayout.CENTER);

			cboxAgents.addActionListener(this);
			cboxFacilities.addActionListener(this);
			cboxJobs.addActionListener(this);
		}
		
		public void updateAgentsCombo(){
			String selectedAgentName = (String)cboxAgents.getSelectedItem();
			cboxAgents.removeAllItems();
			//cboxAgents.addItem(null);
			for ( String agentName : world.agentNames ) {
				cboxAgents.addItem(agentName);
				
			}
			if (selectedAgentName != null && world.agentNames.contains(selectedAgentName)){
				cboxAgents.setSelectedItem(selectedAgentName);
			}
		}
		
		public void updateFacilitiesCombo(){
			String selectedFacilityName = (String)cboxFacilities.getSelectedItem();
			cboxFacilities.removeAllItems();
			//cboxFacilities.addItem(null);
			for ( String facilityName : world.facilitiesNames ) {
				cboxFacilities.addItem(facilityName);
				
			}
			if (selectedFacilityName != null && world.facilitiesNames.contains(selectedFacilityName)){
				cboxFacilities.setSelectedItem(selectedFacilityName);
			}
		}
		
		public void updateJobsCombo(){
			String selectedJobName = (String)cboxJobs.getSelectedItem();
			cboxJobs.removeAllItems();
			//cboxJobs.addItem(null);
			for ( String jobName : world.jobsNames ) {
				cboxJobs.addItem(jobName);
				
			}
			if (selectedJobName != null && world.jobsNames.contains(selectedJobName)){
				cboxJobs.setSelectedItem(selectedJobName);
			}
		}
		
		

		public void update(){
			
			String agentName = (String) cboxAgents.getSelectedItem();
			AgentInfo ag = Util.searchAgent(agentName, world.agents);
			if (ag != null){
				agentInfo.setText(
						 "Agent: "+ag.name+"\n"
						 +"Load: "+ag.load+"/"+ag.loadCapacity+"\n"
						 +"Charge: "+ag.charge+"/"+ag.batteryCapacity+"\n"
						 +"Last Action: "+ag.lastAction+"\n"
						 +"- Param: "+ag.lastActionParam+"\n"
						 +"- Result: "+ag.lastActionResult+"\n"
						 +"Role: "+ag.role+"\n"
						 +"Team: "+ag.teamName+"\n"
						 +"Position: "+ag.lon+","+ag.lat+"\n"
						 +"Products:\n"+ag.products
				);
			} else {
				agentInfo.setText("");
			}
			
			String facilityName = (String)cboxFacilities.getSelectedItem();
			FacilityInfo fac = Util.searchFacility(facilityName, world.facilities);
			if (fac != null){

                String extra = "";
                if(fac.extra != null){
                    if(fac.extra instanceof FacilityInfo.ShopInfo){
                        FacilityInfo.ShopInfo inf = (FacilityInfo.ShopInfo)fac.extra;
                        extra += inf.stock;
                    }
                    else if(fac.extra instanceof FacilityInfo.ChargingStationInfo){
                        FacilityInfo.ChargingStationInfo inf = (FacilityInfo.ChargingStationInfo)fac.extra;
                        extra += "Fuel price: "+inf.fuelPrice+"\n"
                                    +"Queued: "+inf.qSize+"\n"
								    +"Slots: "+inf.maxConcurrent+"\n"
                                    +"Charging rate: "+inf.rate;
                    }
                    else if(fac.extra instanceof FacilityInfo.DumpLocationInfo){
                        FacilityInfo.DumpLocationInfo inf = (FacilityInfo.DumpLocationInfo)fac.extra;
                        extra += "Price: "+inf.price;
                    }
                    else if(fac.extra instanceof FacilityInfo.WorkshopInfo){
                        FacilityInfo.WorkshopInfo inf = (FacilityInfo.WorkshopInfo)fac.extra;
                        extra += "Price: "+inf.price;
                    }
                    else if(fac.extra instanceof FacilityInfo.StorageInfo){
                        FacilityInfo.StorageInfo inf = (FacilityInfo.StorageInfo)fac.extra;
                        extra += "Price: "+inf.price+"\n"
                                    +"Capacity: "+inf.capacity+"\n"
                                    +"Stored:"+"\n"
                                    +inf.stored;
                    }
                }
				
				facilityInfo.setText(
						"Facility: "+fac.name+"\n"
						+"Position: "+fac.lat+","+fac.lon+"\n"
                        +extra
				);
			} else {
				facilityInfo.setText("");
			}
			
			String jobName = (String)cboxJobs.getSelectedItem();
			JobInfo job = Util.searchJob(jobName, world.jobs);
			if (job!=null){
                String status;
                switch(job.status){
                    case Job.ACTIVE: status = "active"; break;
                    case Job.AUCTION: status = "auction"; break;
                    case Job.CANCELLED: status = "cancelled"; break;
                    case Job.COMPLETED: status = "completed"; break;
                    case Job.FUTURE: status = "future"; break;
                    default: status = "??";
                }
				jobInfo.setText(
						"Job: "+job.id+"\n"
						+"Posted by: "+job.poster+"\n"
						+"Reward: "+job.reward+"\n"
						+"Required: "+job.requiredItems+"\n"
						+"Delivered: "+job.deliveredItems+"\n"
                        +"Status: "+status+"\n"
				);
			} else {
				jobInfo.setText("");
			}
			
		}

		public void actionPerformed(ActionEvent e) {
			update();
			// JComboBox<String> source = (JComboBox<String>)e.getSource();
			/*
			synchronized (syncObject) {
				if (!parsedDoc){
					return;
				}
				this.cboxAgents.removeAllItems();
				for(AgentInfo a: agents){
					cboxAgents.addItem(a);
				}
				this.cboxFacilities.removeAllItems();
				for(FacilityInfo f: facilities){
					cboxFacilities.addItem(f);
				}
				this.cboxJobs.removeAllItems();
				for(JobInfo j: jobs){
					cboxJobs.addItem(j);
				}
			}
			*/
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
					"facilitySelected".equals(evt.getPropertyName())) {
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
			AgentInfo agent = null;
            FacilityInfo facility = null;
			TeamInfo team = null;
			JobInfo job = null;

			String step = null;
			String simName = null;
			int i = 0;

			synchronized (syncObject) {
				if (!parsedDoc){
					return;
				}
				step = world.simStep;
				simName = world.simId;
				if (selectedAgent != null){
					agent = Util.searchAgent(selectedAgent, world.agents);
					if (agent != null){
						team = Util.searchTeam(agent.teamName, world.teamsInfo);
					}
				} else if (selectedFacility != null){
                    facility = Util.searchFacility(selectedFacility, world.facilities);
                }
				agentsNamesCombo.setSelectedItem(selectedAgent);
			}

			getLabel(i).setText("     ----SIMULATION----   ");
			getLabel(++i).setText("  " + simName);
			getLabel(++i).setText("  Step:  " + step);
			getLabel(++i).setText("  Current ranking:");
			for (TeamInfo rkTeam: world.teamsInfo){
				if (team != null){
					if (team.equals(rkTeam)){
						getTeamColoredLabel(++i,rkTeam).setText("    " + rkTeam.name + ":     " + rkTeam.money);
					} else {
						getLabel(++i).setText("    " + rkTeam.name + ":     " + rkTeam.money);
					}
				} else {
					getTeamColoredLabel(++i,rkTeam).setText("    " + rkTeam.name + ":     " + rkTeam.money);
				}
			}
			if (facility != null){
				getLabel(++i).setText("    ");
				getLabel(++i).setText("     ----Facility----   ");
				getLabel(++i).setText("  Facility name:   " + facility.name);
				// getLabel(++i).setText("  info:   " + facility.extra);
			} else if (agent != null){
				getLabel(++i).setText("    ");
				getLabel(++i).setText("     ----AGENT----   ");
				getLabel(++i).setText("  Agent name:   " + agent.name);
				getLabel(++i).setText("  Team:   " + agent.teamName);
				getLabel(++i).setText("  Role:   " + agent.role);
				getLabel(++i).setText("  Charge:   " + agent.charge);
				getLabel(++i).setText("  Load:   " + agent.load + "/" + agent.loadCapacity);
				getLabel(++i).setText("  Last action:   " + agent.lastAction);
				getLabel(++i).setText("  - Parameter:   " + agent.lastActionParam);
				getLabel(++i).setText("  - Result:   " + agent.lastActionResult);
			}
			if (team != null) {
				getLabel(++i).setText("   ");
				getLabel(++i).setText("     ----TEAM----   ");
				getLabel(++i).setText("  Team name:   " + team.name);
				getLabel(++i).setText("  Total score:   " + team.money);
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
			return getColoredLabel(i, Definitions.agentColors[getTeamNr(team.name)]);
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
		if (sizeX < 400 || sizeY < 300){ // Make sure it is not too small.
			sizeX = 800;
			sizeY = 700;
		}
		this.setSize(sizeX, sizeY);
		this.addComponentListener(this);

		this.setTitle("Agent Contest 2015");
		worldView = new WorldView();
		

		scrollPane = new JScrollPane(worldView);
		// scrollPane = new JScrollPane(jxmap);

		// New panel
		JPanel mainPanel = new JPanel(new BorderLayout(), true);

		// Upper Menu
		JPanel menuPanel = createUpperMenu();

		mainPanel.add(menuPanel, BorderLayout.NORTH);

		// Right menu
		infoPanel = new InfoPanel();
		worldView.addPropertyChangeListener("facilitySelected", infoPanel);
		worldView.addPropertyChangeListener("agentSelected", infoPanel);
		JScrollPane infoScrollPane = new JScrollPane(infoPanel); //Just modified

		// Create SplitPanel
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPane, infoScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(this.getWidth() - 200);
		splitPane.setResizeWeight(1.0);

		//create debugPanel
		debugPanel = new DebugPanel();

		//create new (Maybe temporary) higher-level splitpanel
		JSplitPane splitSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				splitPane, debugPanel);
		splitSplitPane.setOneTouchExpandable(true);
		splitSplitPane.setDividerLocation(this.getHeight()/2);
		splitSplitPane.setResizeWeight(1.0);

		mainPanel.add(splitSplitPane, BorderLayout.CENTER);

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

		fitToWindowbutton = new JButton("Recenter map");
	//	fitToWindowbutton.setSelected(autoZoom);
		fitToWindowbutton.addActionListener(worldView);
		fitToWindowbutton.setActionCommand("fitWindow");
		menuPanel.add(fitToWindowbutton);

		backgroundButton = new JToggleButton("Background");
		backgroundButton.setSelected(drawBackground);
		backgroundButton.addActionListener(worldView);
		backgroundButton.setActionCommand("toggleBackground");
		menuPanel.add(backgroundButton);

		modeButton = new JButton("Mode " + RenderersManager.getRendererName(visMode));
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
	//	infoPanel.update();

		// update the combo box if the agent names have changed
		if ( agentNamesChanged ) {
			agentsNamesCombo.removeAllItems();
			agentsNamesCombo.addItem(null);
			ArrayList<String> orderedNames = new ArrayList<String>(world.agentNames);
			Collections.sort(orderedNames, new AgentNameComparator());
			for ( String agentName : orderedNames ) {
				agentsNamesCombo.addItem(agentName);
			}
			debugPanel.updateAgentsCombo();
			agentNamesChanged = false;
		}
		
		infoPanel.update();
		
		if ( jobsNamesChanged ) {
			debugPanel.updateJobsCombo();
			jobsNamesChanged = false;
		}
		if (facilitiesNamesChanged) {
			debugPanel.updateFacilitiesCombo();
			facilitiesNamesChanged = false;
		}
		
		debugPanel.update();

		worldView.update();
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
			
			WorldInfo newWorldInfo = Util.parseXML(doc);

			if (!paused.booleanValue()) {
				synchronized (syncObject) {
					
					parsedDoc = true;
					
					if (world == null){
						facilitiesNamesChanged = true;
						mapDimentionsChanged = true;
						jobsNamesChanged = true;
						agentNamesChanged = true;
						
					} else {
						// check for relevant changes to be displayed
						if (	world.minLon != newWorldInfo.minLon || 
								world.minLat != newWorldInfo.minLat ||
								world.maxLon != newWorldInfo.maxLon || 
								world.maxLat != newWorldInfo.maxLat) {
							mapDimentionsChanged = true;
						}
						if ( world.facilitiesNames== null || 
								!world.facilitiesNames.equals(newWorldInfo.facilitiesNames) ) {
							facilitiesNamesChanged = true;
						}
						if ( world.jobsNames== null || 
								!world.jobsNames.equals(newWorldInfo.jobsNames) ) {
							jobsNamesChanged = true;
						}
						if ( world.agentNames== null || !world.agentNames.equals(newWorldInfo.agentNames) ) {
							agentNamesChanged = true;
						}
					}
					
					world = newWorldInfo;
					
					for (AgentInfo agent : world.agents){
						agent.team = getTeamNr(agent.teamName);
					}
					Collections.sort(world.teamsInfo, new Comparator<TeamInfo>(){
						@Override
						public int compare(TeamInfo o1, TeamInfo o2) {
							if (o1.money > o2.money){
								return -1;
							} else if (o1.money < o2.money){
								return +1;
							} else {
								return o1.name.compareTo(o2.name);
							}
						}

					});
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
		if (teamsNames == null ) { teamsNames = new Vector<String>(); }
		if (teamsNames.indexOf(name) == -1) {
			teamsNames.add(name);
		}

		return teamsNames.indexOf(name);
	}


	/**
	 *
	 * @param host
	 * @param port
	 * @param service
	 * @throws java.rmi.RemoteException
	 * @throws java.rmi.NotBoundException
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
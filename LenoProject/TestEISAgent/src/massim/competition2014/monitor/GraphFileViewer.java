package massim.competition2014.monitor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is a file-based version of the 2014 Mars Scenario's monitor application for off-line
 * review of matches. To start this application you must specify the directory containing the XMLs files
 * of a match:<br/>
 * <br/>
 * <code>GraphFileViewer -dir &lt;directrory&gt;</code><br/>
 * <br/>
 * The required XMLs can be obtained running the RMI XML monitor application with the <code>-savexmls</code>
 * flag during a simulation.
 * (Note that The folder to use here should be one of the automatically-created sub-folders of the folder that
 * you gave as parameter to the RMI XML monitor application) 
 */
public class GraphFileViewer extends GraphMonitor {

	private static final long serialVersionUID = -573692905809930002L;
	
	//File managment
	private String dirName;
	private Vector<Integer> fileNumbers;
	private int fileIdx;
	private File currFile;
	private File dir;
	// XML parsing
	DocumentBuilderFactory factory;
	
	// Buttons
	private JButton buttonFirst;	
	private JButton buttonPrev;
	private JButton buttonNext;
	private JButton buttonLast;	

	private JButton buttonPlay;
	private JButton buttonPlayFs;
	private JButton buttonPlaySl;
	
	private JButton buttonChoose;

	
	private JFileChooser fileChooser;

	// Play-related
	private Object playSync = new Object();
	private boolean playing;	
	private long[] playSpeeds = {10000, 5000, 2500, 1000, 500, 400, 300, 200, 100, 50, 10};
	private int playSpeedIdx = 4;
	private boolean restart = false;
	
	// Constructor
	public GraphFileViewer(String directory) throws FileNotFoundException {
		this (false, directory);	
	}
	
	public GraphFileViewer(boolean showdialog, String dir) throws FileNotFoundException {
		super();
		factory = DocumentBuilderFactory.newInstance();
		if (showdialog){
			start(selectFile(dir));
		} else {
			start(dir);
		}
	}
	
	private String selectFile(String dir) throws FileNotFoundException{
		String directory = null;
		JFileChooser fc = getFileChooser(dir);
		
		int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
            if (file.isDirectory()){
            	directory = file.getPath();
            } else {
            	if (file.isFile()){
            		directory = file.getParent();
            	} else {
            		throw new FileNotFoundException();
            	}
            }
           
        } else {
        	// Exit.
        	throw new FileNotFoundException();
        }
		
        return directory;
	}
	
	
	private JFileChooser getFileChooser(String dir){
		if (fileChooser == null){
			fileChooser = new JFileChooser(dir);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		return fileChooser;
	}
	
	private void start(String directory) throws FileNotFoundException{
		initFile(directory);
		fileIdx = 0;		
		if (fileNumbers.size() == 0){
			return;
		}
				
		
		updateFile();
		if (fileNumbers.size() > 1){
			buttonNext.setEnabled(true);
			buttonLast.setEnabled(true);
			beginPlay();
		}	
	}
	
	private void restart(String directory) throws FileNotFoundException{
		initFile(directory);
		fileIdx = 0;		
		if (fileNumbers.size() == 0){
			return;
		}				
		
		updateFile();
		if (fileNumbers.size() > 1){
			buttonNext.setEnabled(true);
			buttonLast.setEnabled(true);
		}	
	}
	
	

	private void initFile(String directory) throws FileNotFoundException {
		dir = new File(directory);
		if (!dir.isDirectory()){
			throw new FileNotFoundException();
		}
		this.dirName = dir.getName();
		
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()){
					return false;
				}
				try {
					String name = f.getName();
					String firstPart = name.substring(0, dirName.length());
					String extension = name.substring(name.lastIndexOf("."));
					if (firstPart.equalsIgnoreCase(dirName)
							&& extension.equalsIgnoreCase(".xml")) {
						return true;
					}
					return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
			}
			
		};
		File [] files = dir.listFiles(filter);
		fileNumbers = new Vector<Integer>(files.length);
		for (int i = 0; i < files.length; i++){
			try {
				String name = files[i].getName();
				String nr = name.substring(dirName.length() + 1, name.lastIndexOf("."));
				fileNumbers.add(new Integer(nr));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(fileNumbers);		
	}
	
	private void beginPlay() {
		synchronized (playSync) {
			while (true){
				playing = false;
				restart = false;
				buttonPlay.setVisible(true);
				if(buttonPlay.getFont().canDisplay('\u25BA')){
					buttonPlay.setText("\u25BA");
				} else {
					buttonPlay.setText("Play");
				}
				buttonPlayFs.setVisible(true);
				buttonPlaySl.setVisible(true);
				buttonPlayFs.setEnabled(true);
				buttonPlaySl.setEnabled(true);
				if (fileNumbers.size() > 1){
					buttonPlay.setEnabled(true);
				}
				while (!restart){
					
					try {
						playSync.wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					while (playing && !restart){
						if (fileIdx < fileNumbers.size()-1){
							fileIdx ++;
							buttonPrev.setEnabled(true);
							buttonFirst.setEnabled(true);
							updateFile();
						}
						if (fileIdx >= fileNumbers.size()-1){
							fileIdx = fileNumbers.size()-1;
							buttonPlay.setEnabled(false);
							if(buttonPlay.getFont().canDisplay('\u25BA')){
								buttonPlay.setText("\u25BA");
							} else {
								buttonPlay.setText("Play");
							}
							buttonNext.setEnabled(false);
							buttonLast.setEnabled(false);
							playing = false;
						}	
						try {
							long speed;
							synchronized (playSpeeds) {
								speed = playSpeeds[playSpeedIdx];
							}						
							playSync.wait(speed);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		
	}

	private void updateFile() {
		try {
			currFile = new File(dir, dirName + "_" + fileNumbers.get(fileIdx).toString()+".xml");
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(currFile);
			parseXML(document);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateView();		
	}

	public void actionPerformed(ActionEvent e) {
		if ("last".equals(e.getActionCommand())){
			synchronized (playSync) {
				buttonPrev.setEnabled(true);
				buttonFirst.setEnabled(true);
				fileIdx++;
				fileIdx = fileNumbers.size() - 1;
				buttonNext.setEnabled(false);
				buttonPlay.setEnabled(false);
				buttonLast.setEnabled(false);
				updateFile();
			}
		}
		else if ("next".equals(e.getActionCommand())){
			synchronized (playSync) {
				buttonPrev.setEnabled(true);
				buttonFirst.setEnabled(true);
				fileIdx++;
				if (fileIdx == fileNumbers.size() - 1) {
					buttonNext.setEnabled(false);
					buttonPlay.setEnabled(false);
					buttonLast.setEnabled(false);
				}
				updateFile();
			}
		}
		else if ("previous".equals(e.getActionCommand())){
			synchronized (playSync) {
				buttonNext.setEnabled(true);
				buttonPlay.setEnabled(true);
				buttonLast.setEnabled(true);
				fileIdx--;
				if (fileIdx == 0) {
					buttonPrev.setEnabled(false);
					buttonFirst.setEnabled(false);
				}
				updateFile();
			}
		}
		else if ("first".equals(e.getActionCommand())){
			synchronized (playSync) {
				buttonNext.setEnabled(true);
				buttonPlay.setEnabled(true);
				buttonLast.setEnabled(true);
				fileIdx = 0;
				buttonPrev.setEnabled(false);
				buttonFirst.setEnabled(false);
				updateFile();
			}
		}
		else if ("play".equals(e.getActionCommand())){
			synchronized (playSync) {
				playing = !playing;
				if (playing) {
					if(buttonPlay.getFont().canDisplay('\u2759')){
						buttonPlay.setText("\u2759\u2759");
					} else {
						buttonPlay.setText("Pause");
					}
					playSync.notifyAll();
				} else {
					if(buttonPlay.getFont().canDisplay('\u25BA')){
						buttonPlay.setText("\u25BA");
					} else {
						buttonPlay.setText("Play");
					}
				}				
			}			
		}
		else if ("faster".equals(e.getActionCommand())){
			synchronized (playSpeeds) {
				buttonPlaySl.setEnabled(true);
				playSpeedIdx++;
				if (playSpeedIdx == playSpeeds.length - 1){
					buttonPlayFs.setEnabled(false);
				}
			}			
		}
		else if ("slower".equals(e.getActionCommand())){
			synchronized (playSpeeds) {
				buttonPlayFs.setEnabled(true);
				playSpeedIdx--;
				if (playSpeedIdx == 0){
					buttonPlaySl.setEnabled(false);
				}
			}			
		}
		else if ("choose".equals(e.getActionCommand())){
			synchronized (playSync) {
				try {
					restart(selectFile(dir.getPath()));
					restart  = true;
					playSync.notifyAll();
				} catch (FileNotFoundException e1) {
					// Do nothing
				}
				
				
			}			
		}
	}
	
	@Override
	protected JPanel createUpperMenu() {
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel,BoxLayout.X_AXIS));
		menuPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		
		buttonFirst = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonFirst.getFont().canDisplay('\u25C4') && buttonFirst.getFont().canDisplay('\u2759')){
			buttonFirst.setText("\u2759\u25C4\u25C4");
		} else {
			buttonFirst.setText("|<<");
		}
		buttonFirst.addActionListener(this);
		buttonFirst.setActionCommand("first");
		buttonFirst.setEnabled(false);
		menuPanel.add(buttonFirst);
		
		buttonPrev = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonPrev.getFont().canDisplay('\u25C4') && buttonFirst.getFont().canDisplay('\u2759')){
			buttonPrev.setText("\u2759\u25C4");
		} else {
			buttonPrev.setText("|<");
		}
		buttonPrev.addActionListener(this);
		buttonPrev.setActionCommand("previous");
		buttonPrev.setEnabled(false);
		menuPanel.add(buttonPrev);
		
		buttonPlay = new JButton();
		if(buttonPlay.getFont().canDisplay('\u25BA')){
			buttonPlay.setText("\u25BA");
		} else {
			buttonPlay.setText("Play");
		}
		buttonPlay.addActionListener(this);
		buttonPlay.setEnabled(false);
		buttonPlay.setVisible(false);
		buttonPlay.setActionCommand("play");		
		menuPanel.add(buttonPlay);
		
		
		buttonNext = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonNext.getFont().canDisplay('\u25BA') && buttonFirst.getFont().canDisplay('\u2759')){
			buttonNext.setText("\u25BA\u2759");
		} else {
			buttonNext.setText(">|");
		}
		buttonNext.addActionListener(this);
		buttonNext.setEnabled(false);
		buttonNext.setActionCommand("next");
		menuPanel.add(buttonNext);
		
		buttonLast = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonLast.getFont().canDisplay('\u25BA') && buttonFirst.getFont().canDisplay('\u2759')){
			buttonLast.setText("\u25BA\u25BA\u2759");
		} else {
			buttonLast.setText(">>|");
		}
		buttonLast.addActionListener(this);
		buttonLast.setActionCommand("last");
		buttonLast.setEnabled(false);
		menuPanel.add(buttonLast);

		
		
		menuPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		

		buttonPlaySl = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonPlaySl.getFont().canDisplay('\u25B9')){
			buttonPlaySl.setText("\u25B9");
		} else {
			buttonPlaySl.setText("-");
		}
		buttonPlaySl.addActionListener(this);
		buttonPlaySl.setEnabled(false);
		buttonPlaySl.setVisible(false);
		buttonPlaySl.setActionCommand("slower");		
		menuPanel.add(buttonPlaySl);
		
		
		
		buttonPlayFs = new JButton();
		// Set unicode simbol when possible, otherwise default text
		if(buttonPlayFs.getFont().canDisplay('\u25B9')){
			buttonPlayFs.setText("\u25B9\u25B9\u25B9");
		} else {
			buttonPlayFs.setText("+");
		}
		buttonPlayFs.addActionListener(this);
		buttonPlayFs.setEnabled(false);
		buttonPlayFs.setVisible(false);
		buttonPlayFs.setActionCommand("faster");		
		menuPanel.add(buttonPlayFs);
		

		
		menuPanel.add(Box.createRigidArea(new Dimension(25, 0)));
		
		addButtons(menuPanel);
		
		menuPanel.add(Box.createRigidArea(new Dimension(25, 0)));
		
		buttonChoose = new JButton("Change Match");
		buttonChoose.addActionListener(this);
		buttonChoose.setEnabled(true);
		buttonChoose.setVisible(true);
		buttonChoose.setActionCommand("choose");		
		menuPanel.add(buttonChoose);
		
		return menuPanel;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			
			String dir = null;
			boolean dialog = false;
			for(int i = 0; i<args.length;i++){
				if(args[i].equalsIgnoreCase("-dir")){
					dir = args[++i];
				}
				if(args[i].equalsIgnoreCase("-dialog")){
					dialog = true;
				}					
			}
			if (!dialog && dir != null){
				new GraphFileViewer(dir);
			} else {
				new GraphFileViewer(true, dir);
				//System.out.println("GraphMonitor -dir <directrory containing the xmls>");
			}
		} catch (FileNotFoundException e) {
			System.out.println("GraphMonitor -dir <directrory containing the xmls>");
			// e.printStackTrace();
		}
	}

}

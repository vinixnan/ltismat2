package massim.competition2015.monitor.render;

import massim.competition2015.monitor.data.*;

import java.util.Vector;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;


public interface Renderer extends Painter<JXMapViewer> {

	void setData(WorldInfo world,
				 String selectedAgent,
				 String selectedFacility);
	

	
}
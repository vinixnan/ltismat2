package massim.competition2015.monitor.render;

import java.awt.Graphics2D;
import massim.competition2015.monitor.Definitions;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

public class DimMapPainter implements  Painter<JXMapViewer> {

	protected DimMapPainter() {
	}

	static public DimMapPainter instance;
	
	static public DimMapPainter getInstance(){
		if (instance == null){
			instance = new DimMapPainter();
		}
		return instance;
	}
	
	@Override
	public void paint(Graphics2D g2d, JXMapViewer object, int width, int height) {
		g2d.setColor(Definitions.backgroundMaskColor);
		g2d.fillRect(0, 0, width, height);

	}


}

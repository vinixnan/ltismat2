package massim.competition2015.monitor.render;

import java.awt.Color;
import java.awt.Graphics2D;

import massim.competition2015.monitor.Definitions;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

public class CopyrightNotePainter implements  Painter<JXMapViewer> {

	protected CopyrightNotePainter() {
	}
	
	static public CopyrightNotePainter instance;
	
	static public CopyrightNotePainter getInstance(){
		if (instance == null){
			instance = new CopyrightNotePainter();
		}
		return instance;
	}

	@Override
	public void paint(Graphics2D g2d, JXMapViewer object, int width, int height) {
		// TODO Auto-generated method stub

		g2d.setPaint(Definitions.backgroundMaskColor);
        g2d.fillRoundRect(width-310, height-20, 210 , 20, 10, 10);
        g2d.setPaint(Color.WHITE);
        g2d.drawString("\u00a9 OpenStreetMap contributors", width-310+10, height-20+15);
	}


}

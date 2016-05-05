package massim.competition2015.monitor;

import java.awt.Color;

public class Definitions {
	
	public static Color [] teamDomColors = {new Color(0,158,115),new Color(0,114,178),new Color(240,228,66),
			new Color(204,121,167),new Color(86,180,233),new Color(230,159,0),new Color(213,94,0),
			new Color(128,0,0),new Color(0,0,128)};
	public static Color [] agentColors = {new Color(0,158,115),new Color(0,114,178),new Color(240,228,66),
			new Color(204,121,167),new Color(86,180,233),new Color(230,159,0),new Color(213,94,0),
			new Color(128,0,0),new Color(0,0,128)};

	
	
	public static final int nodeRadius = 10;
	public static final int agentRadius = 10;

	//*
	public static final Color edgesColor = new Color(150,150,150,150);
		public static final Color nodesColor = new Color(
			edgesColor.brighter().getRed(),
			edgesColor.brighter().getGreen(),
			edgesColor.brighter().getBlue(),
			200);
	public static final Color backgroundColor = Color.BLACK;
	public static final Color backgroundMaskColor = new Color(0,0,0,180);
	// public static final Color backgroundMaskColor = new Color(255,255,255,50);
	/*/
	public static final Color edgesColor = new Color(50,50,50,150);
	public static final Color nodesColor = new Color(
			edgesColor.darker().getRed(),
			edgesColor.darker().getGreen(),
			edgesColor.darker().getBlue(),
			200);
	public static final Color backgroundColor = Color.WHITE;
	public static final Color backgroundMaskColor = new Color(255,235,235,200); 
	//*/
	
}
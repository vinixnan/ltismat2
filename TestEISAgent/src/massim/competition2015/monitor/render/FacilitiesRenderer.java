package massim.competition2015.monitor.render;

import massim.competition2015.monitor.Definitions;
import massim.competition2015.monitor.data.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * Created by ta10 on 18.13.15.
 */
public class FacilitiesRenderer extends WaypointPainter<Waypoint> implements Renderer {


	public FacilitiesRenderer(){
		this.setRenderer(new WaypointRenderer<Waypoint>() {
			@Override
		    public void paintWaypoint(Graphics2D g2d, JXMapViewer map, Waypoint wp) {
				
				if (!(wp instanceof FacilityWaypoint)){
					return;
				}
				
				FacilityInfo fac = ((FacilityWaypoint)wp).getFacilityInfo();
				boolean selected = ((FacilityWaypoint)wp).selected;
				Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
				int x = (int)point.getX();
				int y = (int)point.getY();
				
				Color color = Color.BLUE;
		        
		        
		        if("CH".equalsIgnoreCase(fac.type)){
		        	color = Color.GREEN;
		        	
		        } else if("DU".equalsIgnoreCase(fac.type)){
		        	color = Color.RED;
		        	
		        } else if("SH".equalsIgnoreCase(fac.type)){
		        	color = Color.MAGENTA;
		        	
		        } else if("ST".equalsIgnoreCase(fac.type)){
		        	color = Color.CYAN;
		        	
		        } else if("WS".equalsIgnoreCase(fac.type)){
		        	color = Color.ORANGE;
		        } else {
		        	
				}
		        
		        
		        Polygon triangle = new Polygon();
		        triangle.addPoint(x-5, y-17);
		        triangle.addPoint(x, y);
		        triangle.addPoint(x+5, y-17);
		        
		        
	        	Polygon bTriangle = new Polygon();
	        	bTriangle.addPoint(x-8, y-17);
	        	bTriangle.addPoint(x, y + 4);
	        	bTriangle.addPoint(x + 8, y - 17);
	        	
	        	g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
	        	
	        	if (selected){
	        		g2d.setColor(Color.WHITE);
	        	} else {
	        		g2d.setColor(Color.BLACK);
	        	}
	        	
		        g2d.fillPolygon(bTriangle);
		        g2d.fillOval(x-11, y-29, 22, 22);
		        

		        
		        
		        
		        g2d.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
		        g2d.setColor(color);
		        g2d.fillPolygon(triangle);
		        g2d.setColor(Color.BLACK);
		       // g2d.drawPolygon(triangle);
		       // g2d.drawOval(x-9, y-27, 18, 18);
		        
		        g2d.setColor(color);
		        g2d.fillOval(x-9, y-27, 18, 18);
		        g2d.fillPolygon(triangle);
		        g2d.setColor(Color.BLACK);
		       // g2d.drawOval(x-9, y-26, 18, 18);


				//draw facility icons
				int mx = x;
				int my = y-18;
				g2d.setColor(Color.BLACK);
				g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				if("CH".equalsIgnoreCase(fac.type)){
					Polygon icon = new Polygon();
					icon.addPoint(mx, my-7);
					icon.addPoint(mx, my-2);
					icon.addPoint(mx+7, my-2);
					icon.addPoint(mx, my+7);
					icon.addPoint(mx,my+2);
					icon.addPoint(mx-7,my+2);
					icon.addPoint(mx, my-7);
					g2d.drawPolygon(icon);
				} else if("DU".equalsIgnoreCase(fac.type)){
					Polygon bottom = new Polygon();
					bottom.addPoint(mx-4, my+6);
					bottom.addPoint(mx+4, my+6);
					bottom.addPoint(mx+5, my+5);
					bottom.addPoint(mx+5, my-2);
					bottom.addPoint(mx-5, my-2);
					bottom.addPoint(mx-5, my+5);
					bottom.addPoint(mx-4, my+6);
					g2d.drawPolygon(bottom);

					g2d.drawLine(mx - 6, my - 5, mx + 6, my - 5);

					g2d.drawArc(mx - 2, my - 8, 4, 4, 0, 180);

					g2d.drawLine(mx - 2, my, mx - 2, my + 7);
					g2d.drawLine(mx+2, my, mx+2, my+7);
				} else if("SH".equalsIgnoreCase(fac.type)){
					Polygon cart = new Polygon();
					cart.addPoint(mx-7, my-4);
					cart.addPoint(mx-4, my-4);
					cart.addPoint(mx+5, my-4);
					cart.addPoint(mx+3, my+3);
					cart.addPoint(mx-2, my+3);
					cart.addPoint(mx - 4, my - 4);
					g2d.drawPolygon(cart);

					g2d.drawOval(mx - 3, my + 4, 2, 2);
					g2d.drawOval(mx+2, my+4, 2, 2);
				} else if("ST".equalsIgnoreCase(fac.type)){
					Polygon arr = new Polygon();
					arr.addPoint(mx-3, my-6);
					arr.addPoint(mx+3, my-6);
					arr.addPoint(mx+3, my+2);
					arr.addPoint(mx+5, my+2);
					arr.addPoint(mx, my+7);
					arr.addPoint(mx-5, my+2);
					arr.addPoint(mx-3, my+2);
					arr.addPoint(mx-3, my-6);
					g2d.drawPolygon(arr);
				} else if("WS".equalsIgnoreCase(fac.type)){
					Polygon ha = new Polygon();
					ha.addPoint(mx-5, my-5);
					ha.addPoint(mx+5, my-5);
					ha.addPoint(mx+6, my-1);
					ha.addPoint(mx+5, my-3);
					ha.addPoint(mx+1, my-3);
					ha.addPoint(mx+1, my+6);
					ha.addPoint(mx-1, my+6);
					ha.addPoint(mx-1, my-3);
					ha.addPoint(mx-5, my-3);
					ha.addPoint(mx-5, my-5);

					g2d.drawPolygon(ha);
				}
		    }
		});
	}
	
	
	@Override
	public void setData(WorldInfo world, String selectedAgent, String selectedFacility) {
		
		Set<Waypoint> waypoints = new HashSet<>();
		
		for ( FacilityInfo f : world.facilities) {
			waypoints.add(new FacilityWaypoint(f, f.name.equals(selectedFacility)));
		}
		this.setWaypoints(waypoints);
	}

	
	private static class FacilityWaypoint extends DefaultWaypoint {
		
		private FacilityInfo facilityInfo;
		public boolean selected = false;
		
		public FacilityInfo getFacilityInfo(){
			return facilityInfo;
		}
		
		public FacilityWaypoint(FacilityInfo facility, boolean selected){
			super(facility.lat, facility.lon);
			this.facilityInfo = facility;
			this.selected = selected;
		}
	}
	
/*
        // draw all facilities
        for ( FacilityInfo f : facilities) {

        	
        	
        	Color mainColor = Color.BLUE;
        	Color secondaryColor = Color.CYAN;
        	/* if ("charging".equals(f.type)){
        		mainColor = Color.BLUE;
        		secondaryColor = Color.CYAN;
        	} else if ("charging".equals(f.type)) {
        		mainColor = Color.BLUE;
        		secondaryColor = Color.CYAN;
        	}
        	
        
        } // all facilities drawn
    }
    */
}

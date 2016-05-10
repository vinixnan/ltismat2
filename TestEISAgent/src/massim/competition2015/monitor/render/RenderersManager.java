package massim.competition2015.monitor.render;

import java.util.Vector;

/**
 * Renders a game graph. Has a static factory method for instantiating
 * different renderer. Hides the renderers' implementation from the
 * client.
 * 
 * @author (tristanbehrens)
 *
 */
public abstract class RenderersManager {
	
	
	public enum VisMode {
		MODE_2015a
	}

	/**
	 * Returns a Vector containing the list of relevant renderers (in the order
	 * they should be executed) for the visualization mode given as a parameter.
	 * @param visMode
	 * @return
	 */
	public static Vector<Renderer> getRenderersList(VisMode visMode){

		Vector<Renderer> renderers = new Vector<Renderer>();
		// renderers.add(new NodesRenderer());
		// renderers.add(new EdgesRenderer());
		renderers.add(new FacilitiesRenderer());
		renderers.add(new AgentsRenderer());
		return renderers;
	}
	
	public static String getRendererName(VisMode visMode){
		if (visMode == VisMode.MODE_2015a){
			return "2015a";
		}
		return "";
	}
	
	/**
	 * Returns the next visualization mode from the one given as a parameter. It
	 * cycles to first one when the parameter is the last one. Useful for switching
	 * between modes. 
	 * @param visMode the current visualization mode
	 * @return the next visualization mode
	 */
	public static VisMode getNextMode(VisMode visMode) {
		return VisMode.values()[(visMode.ordinal()+1) % VisMode.values().length ];
	}
	

	
}
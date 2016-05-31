// Internal action code for project massimJacamoProject

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.ChargingStation;
/**
 * Defines the nearest charging station of a given coordinate given as latitude/longitude
 * @author leno
 *
 */
public class nearestChargingSt extends DefaultInternalAction {
	
	/**
	 * The first parameter is the latitude, and the second is the longitude, the third is the return, that is the ID
	 * of the charging station.
	 */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        
    	double lat = ((NumberTermImpl) args[0]).solve();
    	double lon = ((NumberTermImpl) args[1]).solve();
    	ChargingStation cgst = GlobalPercepts.getNearestChargingStation(lat,lon);
    	
    	return un.unifies(args[2], new StringTermImpl(cgst.getID()));
    }
}

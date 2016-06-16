// Internal action code for project massimJacamoProject

package jia;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.Shop;
import massimJacamoProject.GlobalPercepts.Storage;

public class planDelivery extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	double lat = ((NumberTermImpl) args[0]).solve();
    	double lon = ((NumberTermImpl) args[1]).solve();
    	Shop shp=GlobalPercepts.getNearestShop(lat, lon);//find better controller have to give
    	String idShop="none";
    	if(shp!=null)
    		idShop=shp.getID();
		return un.unifies(args[2], new StringTermImpl(idShop));
    }
}

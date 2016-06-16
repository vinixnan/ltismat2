// Internal action code for project massimJacamoProject

package jia;

import java.util.Arrays;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.ChargingStation;
import massimJacamoProject.GlobalPercepts.DumpStation;
import massimJacamoProject.GlobalPercepts.Shop;
import massimJacamoProject.GlobalPercepts.Storage;
import massimJacamoProject.GlobalPercepts.Workshop;

public class tempShop extends DefaultInternalAction {
	
	
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		double lat = ((NumberTermImpl) args[0]).solve();
    	double lon = ((NumberTermImpl) args[1]).solve();
		
		String idShop="none"; 
		if(lat!=0 && lon!=0){
			idShop=GlobalPercepts.getCurrentFacilityToGo();
		}
		return un.unifies(args[2], new StringTermImpl(idShop));
        //return un.unifies(new StringTermImpl(idShop), terms[terms.length-1]);
	}
}

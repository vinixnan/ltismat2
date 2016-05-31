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
	
	public static Integer[] lastVisited=null;
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		double lat = ((NumberTermImpl) args[0]).solve();
    	double lon = ((NumberTermImpl) args[1]).solve();
    	
		if(tempShop.lastVisited==null){
			tempShop.lastVisited=new Integer[2];
			Arrays.fill(tempShop.lastVisited, 0);
		}
		
		String idShop="none"; 
		if(lat!=0 && lon!=0){
			switch(tempShop.lastVisited[0]){
				case 0:
					ChargingStation cgst = GlobalPercepts.getNearestChargingStation(lat,lon);
					if(cgst!=null)
						idShop=cgst.getID();
					break;
				case 1:
					DumpStation dmp=GlobalPercepts.getNearestDumpStation(lat, lon);
					if(dmp!=null)
						idShop=dmp.getID();
					break;
				case 2:
					Shop shp=GlobalPercepts.getNearestShop(lat, lon);
					if(shp!=null)
						idShop=shp.getID();
					break;
				case 3:
					Storage stg=GlobalPercepts.getNearestStorage(lat, lon);
					if(stg!=null)
						idShop=stg.getID();
					break;
				default:
					Workshop wrks=GlobalPercepts.getNearestWorkshop(lat, lon);
					if(wrks!=null)
						idShop=wrks.getID();
					break;
			}
			tempShop.lastVisited[0]++;
			if(tempShop.lastVisited[0] > 4){
				tempShop.lastVisited[0]=0;
				tempShop.lastVisited[1]=0;
			}
		}
		return un.unifies(args[2], new StringTermImpl(idShop));
        //return un.unifies(new StringTermImpl(idShop), terms[terms.length-1]);
	}
}

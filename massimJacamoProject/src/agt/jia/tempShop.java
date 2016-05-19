// Internal action code for project massimJacamoProject

package jia;

import java.util.Arrays;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

public class tempShop extends DefaultInternalAction {
	
	public static Integer[] lastVisited=null;
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		int qtdMaxAllPlaces=2;
		if(tempShop.lastVisited==null){
			tempShop.lastVisited=new Integer[2];
			Arrays.fill(tempShop.lastVisited, 0);
		}
		else{
			tempShop.lastVisited[1]++;
			if(tempShop.lastVisited[1] > qtdMaxAllPlaces){
				tempShop.lastVisited[1]=0;
				tempShop.lastVisited[0]++;
			}
			if(tempShop.lastVisited[0] > 4){
				tempShop.lastVisited[0]=0;
				tempShop.lastVisited[1]=0;
			}
		}
		
		String type="";
		switch(tempShop.lastVisited[0]){
			case 0:
				type="chargingStation";
				break;
			case 1:
				type="dumpLocation";
				break;
			case 2:
				type="shop";
				break;
			case 3:
				type="storage";
				break;
			default:
				type="workshop";
				break;
		}
        String idShop=type+tempShop.lastVisited[1];
        return un.unifies(new StringTermImpl(idShop), terms[terms.length-1]);
	}
}

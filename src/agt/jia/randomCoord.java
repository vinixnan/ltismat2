// Internal action code for project massimJacamoProject

package jia;

import java.util.Random;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

public class randomCoord extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        
    	//Test maximum lat and lon, only valid for the test xml
    	double latMin = 51.4647;
    	double lonMin = -0.1978;
    	double latMax = 51.5223;
    	double lonMax = -0.0354;
    	
    	Random rnd = new Random();
    	double desLat = latMin + (rnd.nextDouble()*(latMax-latMin));
		double desLon = lonMin + (rnd.nextDouble()*(lonMax-lonMin));
        
    	un.unifies(terms[0],  new NumberTermImpl(desLat));
    	return un.unifies(terms[1], new NumberTermImpl(desLon));
        
       
    }
}

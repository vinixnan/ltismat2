// Internal action code for project massimJacamoProject

package jia;

import java.util.Random;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class tempShop extends DefaultInternalAction {

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        
		Random rnd = new Random();
        int numShop=rnd.nextInt(2)+1;
        String idShop="shop"+numShop;
        return un.unifies(new StringTermImpl(idShop), terms[terms.length-1]);
	}
}

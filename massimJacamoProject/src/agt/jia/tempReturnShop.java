// Internal action code for project massimJacamoProject

package jia;

import java.util.Random;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class tempReturnShop extends DefaultInternalAction {

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        
        Random rdn=new Random();
        int numShop=rdn.nextInt(3);
        String idShop="shop"+numShop;
        ts.getAg().getLogger().info(idShop);
        return un.unifies(terms[0],  new StringTermImpl(idShop));
	}
}

// Internal action code for project massimJacamoProject

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

public class waitMilli extends DefaultInternalAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
       // ts.getAg().getLogger().info("executing internal action 'jia.waitMilli'");
        
        NumberTermImpl  param = (NumberTermImpl) args[0];
        Thread.sleep((long) param.solve());
        
        // everything ok, so returns true
        return true;
    }
}

// Internal action code for project massimJacamoProject

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.Role;

public class is_time_to_recharge extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	double currentcharge = ((NumberTermImpl) args[0]).solve();
    	//return currentcharge<=10000;
    	
    	/*
    	 * In development
    	 */
    	
    	String agentName=ts.getUserAgArch().getAgName();
    	agentName="a"+agentName.replace("vehicle", "");
    	Role agRole=GlobalPercepts.getRoleByAgentName(agentName);
    	String ret="";
    	if(agRole!=null){
    		ret=String.valueOf(agRole.getBatteryCapacity());
    	}
        return un.unifies(new StringTermImpl(agentName), args[args.length-1]);
        
    }
}

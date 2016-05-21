// Internal action code for project massimJacamoProject

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.Role;

public class is_time_to_recharge extends DefaultInternalAction {

	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	double currentCharge = ((NumberTermImpl) args[0]).solve();
    	String agentName=ts.getUserAgArch().getAgName();
    	//TODO melhorar forma de obter nome de agente
    	agentName="a"+agentName.replace("vehicle", "");
    	Role agRole=GlobalPercepts.getRoleByAgentName(agentName);
    	if(agRole!=null){
    		double percent=currentCharge/agRole.getBatteryCapacity();
    		return (percent <= 0.7);
    	}
        return false;
    }
}

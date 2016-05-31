// Internal action code for project massimJacamoProject

package jia;

import jason.asSemantics.*;
import jason.asSyntax.*;
import massimJacamoProject.GlobalPercepts;
import massimJacamoProject.GlobalPercepts.Role;

public class get_charging_decision extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	//F, GT, A, L, R)
    	String facilityName = ((StringTermImpl) args[0]).toString();
    	String goingToFacility = ((StringTermImpl) args[1]).toString();
    	double avaliableCharge = ((NumberTermImpl) args[2]).solve();
    	String lastAction = ((StringTermImpl) args[3]).toString();
    	String resultLastAction = ((StringTermImpl) args[4]).toString();
    	String agentName=ts.getUserAgArch().getAgName();
    	//TODO melhorar forma de obter nome de agente
    	agentName="a"+agentName.replace("vehicle", "");
    	Role agRole=GlobalPercepts.getRoleByAgentName(agentName);
    	int answer=0;//!work
    	
    	if(agRole!=null){
    		double percent=avaliableCharge/agRole.getBatteryCapacity();
    		if(lastAction.equals("charge")){
    			if(agRole.getBatteryCapacity()==avaliableCharge ){
    				//estava carregando e terminou, trabalhar
    				answer=1;//!work;
    			}
    			else{
    				//ainda esta carregando
    				answer=2;//charge;
    			}
    		}
    		else if(percent <= 0.2){
    			//não esta carregando e o percentual de carga está baixo
    			if(facilityName.contains("charging")){
    				//necessita de carga e esta na estação
    				answer=3;//charge; .goto("none");
    			}
    			else if(goingToFacility.contains("charging")){
    				//necessita de carga, não esta na estação, mas esta se dirigindo a ela
    				answer=4;// goto(GT);
    			}
    			else{
    				//necessita de carga, não esta na estação e não esta se dirigindo a ela
    				answer=5;// tlat(LA); tlon(LO); jia.nearestChargingSt(LA, LO, IDSHOP); goTo(IDSHOP).
    			}
    		}
    	}
    	return un.unifies(args[5], new NumberTermImpl(answer));
    }
}

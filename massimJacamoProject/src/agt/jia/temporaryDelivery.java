// Internal action code for project massimJacamoProject

package jia;

import java.util.Random;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class temporaryDelivery extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
    	// ID do destino e o item/quantidade
    	//destino pode ser um veiculo
    	Random rdn=new Random();
    	int volume=rdn.nextInt(3)+1;
    	String id="material"+rdn.nextInt(3);
    	String deliveryPlace="";
    	switch(rdn.nextInt(2)+1){
    		case 1:
    			deliveryPlace="vehicle"+rdn.nextInt(16);
    			break;
    		default:
    			deliveryPlace="storage"+rdn.nextInt(1);
        		break;
    	}
    	deliveryPlace="storage"+rdn.nextInt(1);//modelo1
    	volume=3;//modelo1
    	
    	un.unifies(args[0],  new StringTermImpl(id));
    	un.unifies(args[1],  new NumberTermImpl(volume));
    	return un.unifies(args[2], new StringTermImpl(deliveryPlace));
    }
}

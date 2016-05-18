// Agent sample_agent in project massimJacamoProject

/* Initial beliefs and rules */

/* Initial goals */

start.


/* Plans */

+start : true <- jia.randomCoord(LAT,LON);
			goTo(LAT,LON);
			-+start.



{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

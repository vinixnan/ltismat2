// Agent explorer in project massimJacamoProject

/* Initial beliefs and rules */

/* Initial goals */

explore.

/* Plans */

+explore : true <- jia.tempShop(IDSHOP);
			goTo(IDSHOP);
			-+explore.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

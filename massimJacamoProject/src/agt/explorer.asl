// Agent explorer in project massimJacamoProject

/* Initial beliefs and rules */

/* Initial goals */

explore.

+inFacility(V)
  <- println("observed new value: ",V).
  
+lastAction(V)
  <- println("observed new value: ",V).
  
+lastActionResult(V)
  <- println("observed new value: ",V).

/* Plans */
//se estiver em facility, vai pra outra inFacility
//se nao tiver plano atual vai pra facility  lastAction lastActionResult
//se estiver no meio do caminho nao faz nada continue_a()
//se estiver em qualquer lugar com pouca energia, ir carregar charge
//moise working
+explore : true <- jia.tempShop(IDSHOP);
			goTo(IDSHOP);
			-+explore.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

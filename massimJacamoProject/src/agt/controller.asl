// Agent controller in project massimJacamoProject

/* Initial beliefs and rules */

newJob("none").
availableAgent("vehicle2").

its_time_to_get_new_job :- newJob(NJ) & NJ == "none".

/* Initial goals */

!start.

/* Plans */

//+!start : true <- .send(vehicle2, tell, hello).
+!start : its_time_to_get_new_job <- .send(vehicle2, tell, hello).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

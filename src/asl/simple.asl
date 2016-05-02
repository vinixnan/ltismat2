//{ include("mod.common.asl") }

 // Agent sample_agent in project jasrt

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- .print("hello world. ").

{ include("lib/jacamo/templates/common-cartago.asl") }
{ include("lib/jacamo/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }


// Agent sample_agent in project massimJacamoProject

/* Initial beliefs and rules */

/* Initial goals */

start.
chargetrigger.

/* Plans */

+start : true <- jia.randomCoord(LAT,LON);
			goTo(LAT,LON);
			-+start.
			
+start : lat(A) & lon(B) <- .print("Latitude: " , A , " Long:" , B);
			-lat(A);
			-lon(B).


+chargetrigger : tcharge(A) & jia.is_time_to_recharge(A)  <-  
			tinfacility(F);
			tlastaction(L); 
			tlastactionresult(R);
			tgoingto(F, GT);
			!gocharge(F,GT,A);
			!keepcharging(L,R);
-+chargetrigger.
			
+!leprinter(P) : P\=="" <- .print("ruim ",P).
+!leprinter(P) : P=="" <- tlat(LA).

/* Methods to go to a charging station (if necessary) and then start to charge */
//if charge < 100 go to the nearest charging station
+!gocharge(F, GT,A) : A < 100  <- tlat(LA); tlon(LO); jia.nearestChargingSt(LA, LO, IDSHOP); goTo(IDSHOP).
//method to keep going until arrive. Its a copy of the used in explorer agent
+!gocharge(F, GT,A) : GT\=="none" <- goTo(GT).
//if arrive in the charging station then charge
+!gocharge(F, GT,A) : (GT==F & A < 100)   <- charge.

/* Methods to keep charging */
//if the charging is not complete then charge
+!keepcharging(L,R) : L=="charge" & R\=="complete" <- charge.
//if the charging is complete then go back work
+!keepcharging(L,R) : L=="charge" & R=="complete" <- !work.



{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

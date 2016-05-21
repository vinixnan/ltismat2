// Agent sample_agent in project massimJacamoProject

/* Initial beliefs and rules */

/* Initial goals */


chargetrigger.

/* Plans */


+chargetrigger : true  <-  
			tcharge(A);
			tinfacility(F);
			tlastaction(L); 
			tlastactionresult(R);
			tgoingto(F, GT);
			!gocharge(F,GT,A,L,R);
-+chargetrigger.
			
/* Methods to go to a charging station (if necessary) and then start to charge */
//if charge < 100 go to the nearest charging station
+!gocharge(F, GT, A, L, R) : jia.is_time_to_recharge(A)  <- tlat(LA); tlon(LO); jia.nearestChargingSt(LA, LO, IDSHOP); goTo(IDSHOP).
//method to keep going until arrive. Its a copy of the used in explorer agent
+!gocharge(F, GT, A, L, R) : jia.is_time_to_recharge(A) & GT\=="none" <- goTo(GT).
//if arrive in the charging station then charge and if the charging is not complete then charge
+!gocharge(F, GT, A, L, R) : (jia.is_time_to_recharge(A) & GT==F) | (L=="charge" & R\=="complete")  <- .print("charge"); charge.
+!gocharge(F, GT, A, L, R) : not jia.is_time_to_recharge(A) | (L=="charge" & R=="complete") <- !work.

+!work : true <- jia.randomCoord(LAT,LON); goTo(LAT,LON).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

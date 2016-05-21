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

+!work : true <- jia.randomCoord(LAT,LON); goTo(LAT,LON).


/* Methods to go to a charging station (if necessary) and then start to charge */
+!gocharge(F, GT, A, L, R) :  jia.get_charging_decision(F, GT, A, L, R, Resp) & (Resp==1 | Resp==0) <-  !work.
//if the charging is not complete then charge
+!gocharge(F, GT, A, L, R) :  jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==2 <- charge.
//if arrive in the charging station then charge 
+!gocharge(F, GT, A, L, R) :  jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==3 <- tnogoingto; charge.
//method to keep going until arrive
+!gocharge(F, GT, A, L, R) :  jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==4 <- goTo(GT).	
//if charge < 100 go to the nearest charging station		
+!gocharge(F, GT, A, L, R) :  jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==5 <- tlat(LA); tlon(LO); jia.nearestChargingSt(LA, LO, IDSHOP); goTo(IDSHOP).
			
			
			






{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

// Agent sample_agent in project massimJacamoProject

/* Initial beliefs and rules */

placeGoingTo("none").
lastActionPerformed("none").
lastResultPerformed("none").
facilityLocation("none").
chargeBelief(0).

is_already_charged :- lastActionPerformed(L) & lastResultPerformed(R) & facilityLocation(F) 
& chargeBelief(A) & placeGoingTo(GT) & jia.get_charging_decision(F, GT, A, L, R, Resp) & (Resp==0 | Resp==1).

is_still_not_charged :- lastActionPerformed(L) & lastResultPerformed(R) & facilityLocation(F) 
& chargeBelief(A) & placeGoingTo(GT) & jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==2.

its_time_to_charge :- lastActionPerformed(L) & lastResultPerformed(R) & facilityLocation(F) 
& chargeBelief(A) & placeGoingTo(GT) & jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==3.

its_time_to_going :- lastActionPerformed(L) & lastResultPerformed(R) & facilityLocation(F) 
& chargeBelief(A) & placeGoingTo(GT) & jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==4.

its_time_to_find_a_charging_station :- lastActionPerformed(L) & lastResultPerformed(R) & facilityLocation(F) 
& chargeBelief(A) & placeGoingTo(GT) & jia.get_charging_decision(F, GT, A, L, R, Resp) & Resp==5.

/* Initial goals */

updateBeliefs.
chargetrigger.

/* Plans */
+updateBeliefs : true <- tcharge(A); tinfacility(F); tlastaction(L);  tlastactionresult(R);
-+lastActionPerformed(L); -+lastResultPerformed(R); -+facilityLocation(F); -+chargeBelief(A);
?placeGoingTo(GT); -+placeGoingTo(GT);
-+updateBeliefs.

+chargetrigger : true  <-  !gocharge; -+chargetrigger.




/* Methods to go to a charging station (if necessary) and then start to charge */
+!gocharge:  is_already_charged <-  !work.
//if the charging is not complete then charge
+!gocharge:  is_still_not_charged <- charge.
//if arrive in the charging station then charge 
+!gocharge:  its_time_to_charge <- tnogoingto; charge.
//method to keep going until arrive
+!gocharge:  its_time_to_going <- ?placeGoingTo(GT); goTo(GT).	
//if charge < 100 go to the nearest charging station		
+!gocharge: its_time_to_find_a_charging_station <- tlat(LA); tlon(LO); jia.nearestChargingSt(LA, LO, IDSHOP); -+placeGoingTo(IDSHOP); goTo(IDSHOP).
			
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

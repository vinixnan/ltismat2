// Agent deliverer in project massimJacamoProject

/* Initial beliefs and rules */
productDesired("none").
volumeDesired(0).
volumeOwned(0).
supplierDestination("none").
deliveryDestination("none").
placeGoingTo("none").

lastActionPerformed("none").
lastResultPerformed("none").
facilityLocation("none").
chargeBelief(0).

is_the_simulation_not_started :- chargeBelief(CB) & CB==0.
its_time_to_find_delivery :- ~is_the_simulation_not_started & productDesired(PD) & PD=="none" & chargeBelief(CB) & CB > 0.
its_time_to_keep_going :- placeGoingTo(PGT) & PGT\=="none" & facilityLocation(FL) & FL\==PGT & chargeBelief(CB) & CB > 0.
its_time_to_buy :- placeGoingTo(PGT) & facilityLocation(FL) & FL==PGT & PGT\=="none". //add jia para shop
its_time_to_assemble :- placeGoingTo(PGT) & facilityLocation(FL) & FL==PGT.//add jia para assemble
its_time_to_deliver :- productDesired(PD)\=="none" & volumeDesired(VD) & volumeOwned(VO) & VD==VO & chargeBelief(CB) & CB > 0.
its_time_to_store :- placeGoingTo(PGT) & deliveryDestination(DD) & DD==PGT & PGT\=="none".

/* Initial goals */

start.
updateBeliefs.

/* Plans */

+updateBeliefs : true <- tcharge(A); tinfacility(F); tlastaction(L);  tlastactionresult(R);
-+lastActionPerformed(L); -+lastResultPerformed(R); -+facilityLocation(F); -+chargeBelief(A);
-+updateBeliefs.


+start : true <- !deliver;
-+start.


+!deliver : is_the_simulation_not_started <- skip.

+!deliver : its_time_to_find_delivery <- tlat(LAT); tlon(LON); jia.temporaryDelivery(Product, Volume, DeliveryDestination); jia.planDelivery(LAT, LON, SupplierDestination);
-+productDesired(Product); -+volumeDesired(Volume); -+volumeOwned(0); -+supplierDestination(SupplierDestination);  -+deliveryDestination(DeliveryDestination);
-+placeGoingTo(SupplierDestination);.print("find").
//keep going
+!deliver : its_time_to_keep_going <- ?placeGoingTo(GoingToPlace); goTo(GoingToPlace);.print("going").
//assemble item if in facility
//+!deliver : its_time_to_assemble <- ?productDesired(Item); assemble(Item); ?volumeOwned(VO); -+volumeOwned(VO + 1).
//buy item if in facility
+!deliver : its_time_to_buy <- ?productDesired(Item); ?volumeDesired(Volume); buy(Item, Volume); ?volumeOwned(VO); -+volumeOwned(Volume);.print("buy").
//change place to go to delivery place
+!deliver : its_time_to_deliver <- ?deliveryDestination(DeliveryDestination); -+placeGoingTo(DeliveryDestination);.print("goingtodeliver").
//store and restart beliefs
+!deliver : its_time_to_store <- ?productDesired(Item); ?volumeDesired(Volume); store(Item, Volume); 
-+placeGoingTo("none"); -+productDesired("none"); -+volumeDesired(0); -+volumeOwned(0); -+supplierDestination("none"); -+deliveryDestination("none"); -+placeGoingTo("none");.print("stored").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

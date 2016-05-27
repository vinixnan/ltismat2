{ include("vehicle_base.asl") }
// Agent deliverer in project massimJacamoProject

/* Initial beliefs and rules */
productDesired("none").
volumeDesired(0).
volumeOwned(0).
supplierDestination("none").
deliveryDestination("none").


is_the_simulation_not_started :- chargeBelief(CB) & CB==0.
its_time_to_find_delivery :-  productDesired(PD) & PD=="none" & chargeBelief(CB) & CB > 0.
its_time_to_keep_going :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL\==PGT & chargeBelief(CB) & CB > 0.
its_time_to_buy :-        placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & volumeDesired(VD) & volumeOwned(VO) & VO < VD . //add jia para shop
its_time_to_deliver :- productDesired(PD)\=="none" & volumeDesired(VD) & volumeOwned(VO) & VD==VO & chargeBelief(CB) & CB > 0.
its_time_to_store :- placeGoingTo(PGT) & deliveryDestination(DD) & DD==PGT & PGT\=="none".

/* Initial goals */
+!work <- !deliver.


/* Plans */
+!deliver : is_the_simulation_not_started <- skip.

+!deliver : its_time_to_find_delivery  <- tlat(LAT); tlon(LON); jia.temporaryDelivery(Product, Volume, DeliveryDestination); jia.planDelivery(LAT, LON, SupplierDestination);
-+productDesired(Product); -+volumeDesired(Volume); -+volumeOwned(0); -+supplierDestination(SupplierDestination);  -+deliveryDestination(DeliveryDestination);
-+placeGoingTo(SupplierDestination); goTo(SupplierDestination); .print("find").

//keep going
+!deliver : its_time_to_keep_going <- ?placeGoingTo(GoingToPlace); goTo(GoingToPlace);.
//assemble item if in facility
//+!deliver : its_time_to_assemble <- ?productDesired(Item); assemble(Item); ?volumeOwned(VO); -+volumeOwned(VO + 1).
//buy item if in facility
+!deliver : its_time_to_buy <- ?productDesired(Item); ?volumeDesired(Volume); buy(Item, Volume); ?volumeOwned(VO); -+volumeOwned(Volume);.print("buy").
//change place to go to delivery place
+!deliver : its_time_to_deliver <- ?deliveryDestination(DeliveryDestination); -+placeGoingTo(DeliveryDestination).
//store and restart beliefs
+!deliver : its_time_to_store <- ?productDesired(Item); ?volumeDesired(Volume); store(Item, Volume); 
-+placeGoingTo("none"); -+productDesired("none"); -+volumeDesired(0); -+volumeOwned(0); -+supplierDestination("none"); -+deliveryDestination("none"); -+placeGoingTo("none");.print("stored").
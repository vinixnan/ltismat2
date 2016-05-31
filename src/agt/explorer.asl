{ include("vehicle_base.asl") }

/* Initial beliefs and rules */
its_time_to_see_where_go :- facilityLocation(F) & chargeBelief(A) & placeGoingTo(GT) & (GT==F | GT=="none").
its_time_to_go :- facilityLocation(F) & chargeBelief(A) & placeGoingTo(GT) & GT\=="none".

/* Initial goals */
+!work <- !explore.

/* Plans */
+!explore: its_time_to_see_where_go  <- tlat(LA); tlon(LO); jia.tempShop(LA, LO, IDSHOP); goTo(IDSHOP).
+!explore: its_time_to_go  <- goTo(GT).


{ include("vehicle_base.asl") }
/* Initial beliefs and rules */
/* Initial goals */
/* Plans */
+!work : true <- jia.randomCoord(LAT,LON); goTo(LAT,LON).

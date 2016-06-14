{ include("vehicle_base.asl") }
// Agent conveyor in project massimJacamoProject

/* Agents to transportation tasks. They receive messages from the controller agent
 * changing their beliefs in order to assign a new task. Conveyor agents basically 
 * wait for the controller's commands and execute exactly what is asked
 */

/* Initial beliefs and rules */
updateAllBeliefs.
job("none").
task(0).
toDoTask("none").
destination("none").
item("none").
itemVolume(0).
itemUnits(0).
availableVolume(0).

is_the_simulation_not_started :- chargeBelief(CB) & CB==0.
time_to_act :- destination(D) & D\=="none" & placeGoingTo(GT) & D\==GT & chargeBelief(CB) & CB > 0.
time_to_keep_going :- placeGoingTo(GT) & facilityLocation(FL) & GT\=="none" & FL\==GT & chargeBelief(CB) & CB > 0.
time_to_buy :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="buy".
time_to_deliver :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="deliver_job".
time_to_store :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="store".

/* Initial goals */

+newtask : true <- ?job(J); -+job(J); ?task(T); -+task(T); ?toDoTask(TD) -+toDoTask(TD);
					?destination(D); -+destination(D); ?item(I) -+item(I);
					?itemVolume(IV); -+itemVolume(IV); ?itemUnits(IU); -+itemUnits(IU);
					?availableVolume(AV); -+availableVolume(AV).

+!work <- !transport.

/* Plans */

+!transport : is_the_simulation_not_started <- skip.
+!transport : time_to_act <- ?destination(D); -+placeGoingTo(D); goTo(D); .print("act").
+!transport : time_to_keep_going <- ?placeGoingTo(GT); goTo(GT).
+!transport : time_to_buy <- ?item(I); ?itemUnits(IU); buy(I, IU); .print("bought").
+!transport : time_to_deliver <- ?job(J); deliver_job(J); .print("delivered").
+!transport : time_to_store <- ?item(I); ?itemUnits(IU); store(I, IU); .print("stored").


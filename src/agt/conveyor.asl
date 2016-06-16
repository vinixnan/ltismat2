{ include("vehicle_base.asl") }
// Agent conveyor in project massimJacamoProject

/* Agents to transportation tasks. They receive messages from the controller agent
 * changing their beliefs in order to assign a new task. Conveyor agents basically 
 * wait for the controller's commands and execute exactly what is asked
 */

/* Initial beliefs and rules */
updateAllBeliefs.
job("none").
task(-1).
toDoTask("none").
destination("none").
finalDestiny("none").
item("none").
itemVolume(0).
itemUnits(0).
availableVolume(0).

is_the_simulation_not_started :- chargeBelief(CB) & CB==0.
no_tasks :- task(T) & T == -1.
time_to_act :- destination(D) & D\=="none" & placeGoingTo(GT) & D\==GT & chargeBelief(CB) & CB > 0.
time_to_keep_going :- placeGoingTo(GT) & facilityLocation(FL) & GT\=="none" & FL\==GT & chargeBelief(CB) & CB > 0.
time_to_buy :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="buy".
time_to_deliver :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="deliver_job".
time_to_store :- placeGoingTo(PGT) & facilityLocation(FL) & PGT\=="none" & FL==PGT & toDoTask(TD) & TD=="store".

/* Initial goals */

+newtask : true <- ?job(J); -+job(J); ?task(T); -+task(T); ?toDoTask(TD); -+toDoTask(TD);
					?destination(D); -+destination(D); ?finalDestiny(FD); -+finalDestiny(FD); ?item(I); -+item(I);
					?itemVolume(IV); -+itemVolume(IV); ?itemUnits(IU); -+itemUnits(IU);
					?availableVolume(AV); -+availableVolume(AV); .print("Task received: ", T).

+!work <- !transport.

/* Plans */

+!transport : is_the_simulation_not_started <- skip.
+!transport : no_tasks <- skip.
+!transport : time_to_act <- ?destination(D); -+placeGoingTo(D); goTo(D); tinfacility(F); -+facilityLocation(F).//; .print("act").
+!transport : time_to_keep_going <- ?placeGoingTo(GT); goTo(GT); tinfacility(F); -+facilityLocation(F); ?placeGoingTo(GT); -+placeGoingTo(GT); ?toDoTask(TD); -+toDoTask(TD).
+!transport : time_to_buy <- ?item(I); ?itemUnits(IU); buy(I, IU); .print("bought"); -+toDoTask("deliver_job"); ?toDoTask(TD); .print("Task: ", TD); ?finalDestiny(FD); -+destination(FD); ?destination(DEST); .print("New destination: ", DEST).
+!transport : time_to_deliver <- .print("trying to deliver"); ?job(J); deliver_job(J); .print("delivered").
+!transport : time_to_store <- ?item(I); ?itemUnits(IU); store(I, IU); .print("stored").


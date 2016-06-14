//{ include("vehicle_base.asl") }
// Agent controller in project massimJacamoProject

/* The controller agent is the brain, it checks the available tasks, the jobs, 
 * decides when to bid for an auction job and assigns pending tasks to conveyor 
 * agents sending them messages to change their beliefs
 */

/* Initial beliefs and rules */

updateAllBeliefs.
controlTrigger.
vehicle2("available").
existingTask(0). //0: not checked, 1: checked, but no task, 2: checked, new task
jobId("none").
taskId(0).
operation("none").
destinyId("none").
itemName("none").
quantity(0).
existingAuction(0). //0: not checked, 1: checked, but no auction, 2: checked, new auction
auctionJobId("none").
maximumBid(0.0).
chargeBelief(0).

time_to_wait :- (vehicle2(V2) & V2 \== "available") | (chargeBelief(A) & A == 0).

/* Priced Jobs */
time_to_check_priced_jobs :- vehicle2(V2) & V2 == "available" & existingTask(ET) & ET == 0 & chargeBelief(A) & A > 0.
/* Priced Jobs */

/*
time_to_check_tasks :- vehicle2(V2) & V2 == "available" & existingTask(ET) & ET == 0 & existingAuction(EA) & EA \== 2 & chargeBelief(A) & A > 0.
time_to_check_auction :- existingAuction(EA) & EA == 0 & existingTask(ET) & ET == 1 & chargeBelief(A) & A > 0.
time_to_bid :- existingAuction(EA) & EA == 2 & existingTask(ET) & ET == 0.
*/

time_to_delegate :- vehicle2(V2) & V2 == "available" & existingTask(ET) & ET == 2 & chargeBelief(A) & A > 0.

/* Initial goals */

//+!work <- !control.

/* Plans */

+updateAllBeliefs : true <- tcharge(A); -+chargeBelief(A);
-+updateAllBeliefs.

+controlTrigger : true <- !control; -+controlTrigger.

+!control : time_to_wait <- skip.

/* Priced Jobs */
+!control : time_to_check_priced_jobs <- jia.controller(JobId, TaskId, ToDoTask, DestinyId, ItemName, Quantity, ExistingTask);
											-+existingTask(ExistingTask);
											-+jobId(JobId);
											-+taskId(TaskId);
											-+operation(ToDoTask);
											-+destinyId(DestinyId);
											-+itemName(ItemName);
											-+quantity(Quantity);
											-+existingAuction(0).
/* Priced Jobs */

/*
+!control : time_to_check_tasks <- jia.getPendingTask(JobId, TaskId, ToDoTask, DestinyId, ItemName, Quantity, ExistingTask);
										-+existingTask(ExistingTask);
										-+jobId(JobId);
										-+taskId(TaskId);
										-+operation(ToDoTask);
										-+destinyId(DestinyId);
										-+itemName(ItemName);
										-+quantity(Quantity);
										-+existingAuction(0).
+!control : time_to_check_auction <- jia.getAuctionJob(AuctionJobId, MaximumBid, ExistingAuction);
										-+auctionJobId(AuctionJobId); -+maximumBid(MaximumBid);
										-+existingAuction(ExistingAuction);
										-+existingTask(0).
+!control : time_to_bid <- ?auctionJobId(AJ); ?maximumBid(MB); bid_for_job(AJ, MB);
							.print("Bidding: ", AJ, MB);
							-+existingAuction(0); -+existingTask(0).
*/
+!control : time_to_delegate <- ?jobId(JI); ?taskId(TI); ?operation(O); ?destinyId(DI);
								?itemName(IN); ?quantity(Q);
								.send(vehicle2, tell, job(JI));
								.send(vehicle2, tell, task(TI));
								.send(vehicle2, tell, toDoTask(O));
								.send(vehicle2, tell, destination(DI));
								.send(vehicle2, tell, item(IN));
								.send(vehicle2, tell, itemUnits(Q));
								.send(vehicle2, tell, newtask);
								.print("Sending task: ", TI);
								-+vehicle2("unavailable"); -+existingTask(0).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }


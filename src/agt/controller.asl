//{ include("vehicle_base.asl") }
// Agent controller in project massimJacamoProject

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
existingAuction(0). //0: does not exist, 1: exists
auctionJobId("none").
maximumBid(0.0).
chargeBelief(0).

time_to_wait :- (vehicle2(V2) & V2 \== "available") | (chargeBelief(A) & A == 0).
time_to_check_controller :- vehicle2(V2) & V2 == "available" & existingTask(ET) & ET == 0 & chargeBelief(A) & A > 0.
time_to_check_auction :- existingTask(ET) & ET == 1 & chargeBelief(A) & A > 0.
time_to_bid :- existingAuction(EA) & EA == 1 & existingTask(ET) & ET == 1.
time_to_delegate :- vehicle2(V2) & V2 == "available" & existingTask(ET) & ET == 2 & chargeBelief(A) & A > 0.

/* Initial goals */

//+!work <- !control.

/* Plans */

+updateAllBeliefs : true <- ?vehicle2(V2); -+vehicle2(V2); ?existingTask(ET); -+existingTask(ET);
							?jobId(JI); -+jobId(JI); ?taskId(TI); -+taskId(TI);
							?operation(O); -+operation(O); ?destinyId(DI); -+destinyId(DI);
							?itemName(IN); -+itemName(IN); ?quantity(Q); -+quantity(Q);
							tcharge(A); -+chargeBelief(A);
-+updateAllBeliefs.

+controlTrigger : true <- !control; -+controlTrigger.

//+testBla[source(A)] <- .print("I received a 'testBla' from ", A).//; ?testVehicle(V); .print("belief: ", V); -+testVehicle(V).
//+!start : true <- .send(vehicle2, tell, hello).
+!control : time_to_wait <- skip.
+!control : time_to_check_controller <- jia.controller(JobId, TaskId, ToDoTask, DestinyId, ItemName, Quantity, ExistingTask);
										-+existingTask(ExistingTask);
										-+jobId(JobId);
										-+taskId(TaskId);
										-+operation(ToDoTask);
										-+destinyId(DestinyId);
										-+itemName(ItemName);
										-+quantity(Quantity);
										.print("JobId: ", JobId).
+!control : time_to_check_auction <- jia.getAuctionJob(AuctionJobId, MaximumBid, ExistingAuction);
										-+auctionJobId(AuctionJobId); -+maximumBid(MaximumBid);
										-+existingAuction(ExistingAuction);
										.print("Existing Auction: ", ExistingAuction).
+!control : time_to_bid <- ?auctionJobId(AJ); ?maximumBid(MB); bid_for_job(AJ, MB);
							.print("Bidding: ", AJ, MB);
							-+existingAuction(0); -+existingTask(0).
+!control : time_to_delegate <- ?jobId(JI); ?taskId(TI); ?operation(O); ?destinyId(DI);
								?itemName(IN); ?quantity(Q);
								.send(vehicle2, tell, job(JI));
								.send(vehicle2, tell, task(TI));
								.send(vehicle2, tell, toDoTask(O));
								.send(vehicle2, tell, destination(DI));
								.send(vehicle2, tell, item(IN));
								.send(vehicle2, tell, itemUnits(Q));
								.print("sending task ", TI);
								-+vehicle2("unavailable"); -+existingTask(0).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

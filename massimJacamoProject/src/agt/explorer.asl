// Agent explorer in project massimJacamoProject

/* Initial beliefs and rules */
!explore_goal.

/* Initial goals */



+!explore_goal
	<-	.print("Starting explore goal");
	!explore.

+!explore : true <- 
			tcharge(A);
			tinfacility(F);
			tlastaction(L); 
			tlastactionresult(R);
			tgoingto(F, GT);
			!goexplore(F,GT);
			//!gorecharge(A); 
			!!explore.	
			
+!goexplore(F, GT) : GT==F | GT=="none" <- tlat(LA); tlon(LO); jia.tempShop(LA, LO, IDSHOP); goTo(IDSHOP).
+!goexplore(F, GT) : GT\=="none"  <- goTo(GT).
//+!gorecharge(A) :  true <- .print("").
//+!gorecharge(A) :  A < 5000 <- .print("charge ", A).


//+!explore : tinfacility(FAC) & FAC="none"  <- jia.tempShop(IDSHOP);
//		goTo(IDSHOP);
//			!!explore.
			

//+!explore : tlastaction(A)="skip" <- jia.tempShop(IDSHOP);
	//		goTo(IDSHOP);
		//	!!explore.




{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

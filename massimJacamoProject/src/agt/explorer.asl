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
			!goexplore(F,L,R);
			//!gorecharge(A); 
			!!explore.	
			
+!goexplore(F, L, R) : F\=="none" | L\=="goto" <- jia.tempShop(IDSHOP); goTo(IDSHOP).
+!goexplore(F, L, R) :  L=="goto"  <- continue_a.
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

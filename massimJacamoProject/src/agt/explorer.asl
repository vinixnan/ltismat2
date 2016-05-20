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
			!goexplore(F,GT,A);
			!!explore.	
			
+!goexplore(F, GT,A) : GT==F | GT=="none" <- tlat(LA); tlon(LO); jia.tempShop(LA, LO, IDSHOP); goTo(IDSHOP).
+!goexplore(F, GT,A) : GT\=="none"  <- goTo(GT).




{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }

import java.util.Random;



public class Application_ABL implements Runnable{

	private static String nodeId;
	private static String server;
	private static int port ;
	private static String resource_nodeId;
	private static String resource_server;
	private static int resource_port ;
	public static void main(String[] args) throws Exception {
		int noOfCriticalSectionrequests = Integer.valueOf(args[0].trim());
		int startingDelay = Integer.valueOf(args[1].trim());
		int criticalSectionDelays = Integer.valueOf(args[2].trim());
		nodeId = args[3].trim();
		server = args[4].trim();
		port = Integer.valueOf(args[5].trim());

		resource_nodeId = args[6].trim();
		resource_server = args[7].trim();
		resource_port = Integer.valueOf(args[8].trim());

		String applog = nodeId + "AppLog";
		//LogToFile.CreateWriteFile(applog, "In Application Module----------");
		//LogToFile.CreateWriteFile(applog, "noOfCriticalSectionrequests-->"+noOfCriticalSectionrequests);
		//LogToFile.CreateWriteFile(applog, "startingDelay-->"+startingDelay);
		//LogToFile.CreateWriteFile(applog, "criticalSectionDelays-->"+criticalSectionDelays);

		//Start Mututal Exculison Algo Threads 
		Application_ABL app = new Application_ABL();
		Thread applicationThread = new Thread(app);
		applicationThread.start();

		//Thread.currentThread().sleep(100);

		Node node = null;
		Random randomno = new Random();
		if (noOfCriticalSectionrequests != 0) {
			//Delay to Start a node
			Thread.currentThread().sleep( 5000 + startingDelay);

			//Get the node Instance
			node = Node.getInstance();



			randomno = new Random();
			for (int i=0;i<noOfCriticalSectionrequests;i++) {
				int sleepInterval = randomno.nextInt(60);   //100
				//if(sleepInterval<50) {
					//sleepInterval = sleepInterval + 50;
				//}
				Thread.currentThread().sleep(sleepInterval+10);  //10

				//System.out.println("Entering CS at.... "+nodeId);
				int retVal = node.csEnter();			//System.out.println("Entering CS at.... "+nodeId);
				System.out.println(nodeId+"----->"+i);
				if(retVal == 0) {
					ProcessThread clientThread = ProcessThread.getProcessThread();
					System.out.println(nodeId+" Use Resource");
					clientThread.useResource(nodeId, resource_server, resource_port);
				}
				Thread.currentThread().sleep(10);

				System.out.println("-- Now going to Execute CS Leave "+nodeId);			
				//Release Resource
				ProcessThread clientThread = ProcessThread.getProcessThread();
				System.out.println(nodeId+"Leave Resource");
				clientThread.leaveResource(nodeId, resource_server, resource_port);

				int retVal_new = node.csLeave();
				if(retVal_new != 0) {
					System.out.println("---------------------------------Exception----------------------------------------");
				}
			}

		}
		System.out.println("-----------Process Completed-----------------------------------------------------------------at ->"+nodeId);

	}

	public void run() {
		try {
			Starter.start(nodeId, server, port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

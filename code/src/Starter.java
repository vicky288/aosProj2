import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;


public class Starter {
	public static void start(String node_id, String server_url, int port_no) throws InterruptedException {
		long lStartTime = new Date().getTime();

		String nodeId = node_id;
		String server = server_url;
		int port = port_no;
		LogToFile.CreateWriteFile(nodeId, "Start Of Node Initialization");
		LogToFile.CreateWriteFile(nodeId, "Node Id is->" + nodeId + " Node Server Is ->" +server+ " Node port is ->"+port);		

		Node node = Node.getNodeInstance(nodeId, server, port);
		Thread nodeThread = new Thread(node);
		node.nodeInitialize();
		try {
			Thread.currentThread().sleep(40);
			nodeThread.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	LogToFile.CreateWriteFile(node.getNodeId(), "No of nodes->"+node.getTotalNumOfNodes());
		System.out.println("No of nodes->"+node.getTotalNumOfNodes());
		
	    
	    System.out.println("######################Printing Initial Keys##########################");
	    LogToFile.CreateWriteFile(node.getNodeId(), "######################Printing Initial Keys##########################");
	    ArrayList<String> keyList = node.getKeyList();
	    for(String key:keyList) {
	    	LogToFile.CreateWriteFile(node.getNodeId(), "key->"+key);

	    }
	    	    
	    // End Of Initialization Section
	    	    
	    
	    
	    //Intitialize the Receive Thread
	    ProcessThread server_thread = ProcessThread.getProcessThread(node);
	    server_thread.initializeServerToReceive(node.getPort());
		Thread serverThread = new Thread(server_thread);
		try {
			Thread.currentThread().sleep(50);
			serverThread.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Thread.currentThread().sleep(50);
		


		lStartTime = new Date().getTime();
		
		//Process Received Messages
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		while(true) {
			//LogToFile.CreateWriteFile(node.getNodeId(), "$$$");
			server_thread.processRequestKeyQueue();
			Thread.currentThread().sleep(25);				//50
			lEndTime = new Date().getTime();
			difference = lEndTime - lStartTime;
			//LogToFile.CreateWriteFile(node.getNodeId(), "Time since start up(in milliSecs)...."+difference);
			if (difference < 0) {		//222200000
				break;
			}
		}
		
	    System.out.println("######################Printing Final Keys##########################");
	    LogToFile.CreateWriteFile(node.getNodeId(), "######################Printing Final Keys##########################");
	    node = Node.getNodeInstance();
	    ArrayList<String> keyListFinal = node.getKeyList();
	    for(String key:keyListFinal) {
	    	System.out.print(key+" ");
	    	LogToFile.CreateWriteFile(node.getNodeId(), "key->"+key);

	    }	    
		server_thread.setReceiveThreadTerminationCondition(true);
		//Send A dummy Message To take the thread out of waiting state and close the connection
		MessageFormat dummyMessage = new MessageFormat();
		try {
			server_thread.sendToNode(node.getServer(), node.getPort(), dummyMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server_thread.closeServerConnection();
		
		System.out.println("-----------Process Completed-------------!!!");
	    LogToFile.CreateWriteFile(node.getNodeId(), "---------------------------Process Completed--------------------------------");
		
	}

}

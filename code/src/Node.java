import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;


public class Node implements Runnable{
	private String nodeId;
	private String server;
	private int port;
	private int totalNumOfNodes;
	private TreeMap<String, String> nodesKnowledge = new TreeMap<String, String>();
	private ArrayList<String> keyList = new ArrayList<String>();
	private boolean isExecutingCriticalSection = false;
	private boolean isCSStopInvoked = false;

	private ArrayList<Long> CSRequestTimeStampQueue = new ArrayList<Long>(); 


	//used to keep track of keys those have been requested in a critical section
	private ArrayList<String> keyRequestedList = new ArrayList<String>();
	private static Node instance = null;


	private Node() {

	}
	//Singleton Class // Easy to use the same object throughout
	public static Node getNodeInstance(String nodeId, String server, int port) {
		if(instance == null) {
			System.out.println("Node Instance is null");
			instance = new Node();
			instance.nodeId = nodeId;
			instance.server = server;
			instance.port = port;
		}
		return instance;
	}

	public static Node getNodeInstance() {
		return instance;
	}
	public void nodeInitialize() {

		LogToFile.CreateWriteFile(this.nodeId, "Start");

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("config.txt"));
			String line = "";
			int nodeInfoEntry = 0;
			int keyInfoEntry = 0;
			int noOfNodesEntry = 0;
			while((line=br.readLine())!=null)
			{
				if(line.equals("#Nodes")) {
					noOfNodesEntry = 0;
				}
				if(noOfNodesEntry == 1) {
					this.totalNumOfNodes =  Integer.valueOf(line);
				}
				if(line.equals("#Number of nodes")) {
					noOfNodesEntry = 1;
				}

				if(line.equals("#Parameters")) {
					keyInfoEntry = 0;
				}
				if(keyInfoEntry == 1) {
					String keysInfo[] = line.split(":");
					if (keysInfo[0].equals(this.nodeId)) {
						String allKeys[] = keysInfo[1].split(",");
						for (String key:allKeys) {
							keyList.add(key);
						}
					}
				}
				if(line.equals("#Keys")) {
					nodeInfoEntry = 0;
					keyInfoEntry = 1;
				}
				if(nodeInfoEntry == 1) {
					String nodesInfo[] = line.split(":");
					//System.out.println(nodesInfo[0]+"----"+nodesInfo[1]);
					String nodeId = nodesInfo[0];
					this.nodesKnowledge.put(nodeId, nodesInfo[1]);
				}
				if(line.equals("#Nodes")) {
					nodeInfoEntry = 1;
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int  csEnter() {
		long CSEnterTimeStamp = new Date().getTime();
		CSRequestTimeStampQueue.add(CSEnterTimeStamp);
		Collections.sort(CSRequestTimeStampQueue);
		//LogToFile.CreateWriteFile(this.nodeId, "----------CSRequestTimeStampQueue size is ->--------------"+CSRequestTimeStampQueue.size());

		//		int retVal = 1;
		//		boolean exitCondition = isExecutingCriticalSection();
		//		while (!exitCondition) {
		//			exitCondition = isExecutingCriticalSection();
		//			retVal = 0;
		//		}
		//		return retVal; 
		//LogToFile.CreateWriteFile(this.nodeId, "@@@@@@@@@@@@----------Inside Run ....CSRequestTimeStampQueue size is ->--------------"+CSRequestTimeStampQueue.size());
		int listSize = CSRequestTimeStampQueue.size();
		if(listSize > 0) {
			CSExecution();
		}
		if(isExecutingCriticalSection()) {
			return 0;

		} else {
			return 1;
		}
	}

	//Node Critical Section Execution Thread-NCS
	public void run() {
		LogToFile.CreateWriteFile(this.nodeId, "----------Node Thread for Critical Section Started--------------");
		while(true) {
//			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
//			Date date = new Date();
//			LogToFile.CreateWriteFile(this.nodeId+"NCS", dateFormat.format(date)+"----------Node Critical Section Execution Thread Running--------------outer------");
			
			//LogToFile.CreateWriteFile(this.nodeId, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

			boolean exitCondition = isExecutingCriticalSection();
			//LogToFile.CreateWriteFile(this.nodeId, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			try {
				Thread.currentThread().sleep(20);		//30
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long timeStamp = new Date().getTime();
			while (exitCondition) {
//				LogToFile.CreateWriteFile(this.nodeId+"NCS", dateFormat.format(date)+"----------Node Critical Section Execution Thread Running--------------Inner------");

				timeStamp = new Date().getTime();
				LogToFile.CreateWriteFile(nodeId, "CS Execution Started at"+timeStamp);
				System.out.println("CS Execution Started on process "+ nodeId + " at "+timeStamp);
				boolean exitCondition_stop = isCSStopInvoked();

				while(!exitCondition_stop) {
					try {
						Thread.currentThread().sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					//LogToFile.CreateWriteFile(this.nodeId, "????????????????????????????????????????????????????????????????????");
					exitCondition_stop = isCSStopInvoked();
				}
				timeStamp = new Date().getTime();
				LogToFile.CreateWriteFile(nodeId, "CS Execution Finished at"+timeStamp);
				System.out.println("CS Execution Finished on process "+ nodeId + " at "+timeStamp);
				//set isExecutingCriticalSection to false
				setExecutingCriticalSection(false);
				exitCondition = isExecutingCriticalSection();
			}
		}
	}

	public void sendMessagesForKeys(long earliestCSEnterTimeStamp) {
		ArrayList<String> keysRequired = new ArrayList<String>();
		boolean needKeys = false;
		//Find out keys which are required to start Executing 
		if (keyList.size() < totalNumOfNodes-1) {
			needKeys = true;
			ArrayList<String> allKeys = getListOfKeysforCSExecution();

			for (String key:allKeys) {
				//Printing all keys needed for CS Execution
				//System.out.print(key);
				//LogToFile.CreateWriteFile(this.nodeId, key);

				if(!this.keyList.contains(key)) {
					keysRequired.add(key);
				}
			}
			LogToFile.CreateWriteFile(this.nodeId, "------Below Keys need to be imported to start critical section Exection----");
			for(String key:keysRequired) {
				LogToFile.CreateWriteFile(this.nodeId, key);
			}
		}


		//Send Messages to the corresponding nodes
		if (needKeys) {
			for(String key:keysRequired) {

				if(!keyRequestedList.contains(key)) {
					LogToFile.CreateWriteFile(this.nodeId, "-----Creating Messages to be sent-----");
					MessageFormat message =  createMessageFromKey(key, earliestCSEnterTimeStamp);

					ProcessThread clientThread = ProcessThread.getProcessThread(this);
					try {
						clientThread.sendToNode(message.getDestServer(), message.getDestPort(), message);
						LogToFile.CreateWriteFile(this.nodeId, "-----Message sent to----->"+message.getDestId());
						LogToFile.CreateWriteFile(this.nodeId, "-----Message is----->");
						printMessage(message);

						//Add the key to keyRequestedList
						keyRequestedList.add(key);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}
		}

	}
	public void CSExecution() {

		LogToFile.CreateWriteFile(this.nodeId, "!!!!!----CS ENTER ----!!!!!");
		long earliestCSEnterTimeStamp = CSRequestTimeStampQueue.get(0);

		//Send Messages for keys
		sendMessagesForKeys(earliestCSEnterTimeStamp);

		//wait till all keys are collected()
		int keyListSize = keyList.size();
//		LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Present---->"+keyListSize);
		int noOfKeysRequired = this.totalNumOfNodes - 1 - keyList.size();
//		LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Required---->"+noOfKeysRequired);
		int keysReceivedSofar = ProcessThread.getProcessThread().getSizeOfReceivedKeyList();
//		LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Received---->"+keysReceivedSofar);
		boolean exitCondition = true; 
		if (noOfKeysRequired > 0) {
			exitCondition = false;
		}

		while(!exitCondition)
		{

			try {
				Thread.currentThread().sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keyListSize = keyList.size();
//			LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Present---->"+keyListSize);
			noOfKeysRequired = this.totalNumOfNodes - 1 - keyList.size();
//			LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Required---->"+noOfKeysRequired);
			keysReceivedSofar = ProcessThread.getProcessThread().getSizeOfReceivedKeyList();
//			LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++No of Keys Received---->"+keysReceivedSofar);
			if(keysReceivedSofar == noOfKeysRequired) {
				exitCondition = true;
			}
			sendMessagesForKeys(earliestCSEnterTimeStamp);
		}

		//Set critical Execution boolean to true
		setExecutingCriticalSection(true);


		//Update Nodes KeyList
		ArrayList<MessageFormat> keyListMessages = ProcessThread.getProcessThread().getReceivedKeyList();
		//LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++size of keyListMessages---->"+keyListMessages.size());
		int keyListMsgSize = keyListMessages.size();
		MessageFormat[] keyMsgList = new MessageFormat[keyListMessages.size()];
		keyMsgList = keyListMessages.toArray(keyMsgList);
		for(MessageFormat message:keyMsgList) {
			if(message == null) {
				LogToFile.CreateWriteFile(this.nodeId, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			}
			if(keyList == null) {
				LogToFile.CreateWriteFile(this.nodeId, "*********************************************************");
			}
			//Iterator<MessageFormat> iter = keyListMessages.iterator();
			//while (iter.hasNext()) {
			//MessageFormat message = iter.next();
			if(!keyList.contains(message.getKey())) {
				keyList.add(message.getKey());
				LogToFile.CreateWriteFile(this.nodeId, "-----Adding Required Key --> "+ message.getKey() +" For CS Execution"+"---New Size of keyList --------"+keyList.size());
			}
		}



		//Clear the ReceivedKeyList 
		ProcessThread.getProcessThread().clearReceivedKeyList();

		//Clear the keyRequestedList
		keyRequestedList.clear();

		//Remove the Earliest TimeStamp
		CSRequestTimeStampQueue.remove(0);

		LogToFile.CreateWriteFile(this.nodeId, "Now Ready to Execute Critical Section");
//		try			// added to handle exception
//		{
//			for (String key : keyList) {

//				LogToFile.CreateWriteFile(this.nodeId, key);

//			}
//		} catch (Exception ex) {
			//ex.printStackTrace();
//		}


	}

	public MessageFormat createMessageFromKey(String key, long timeStamp){
		MessageFormat message = new MessageFormat();
		String keyParts[] = key.split("-");
		String node1 = "n" + keyParts[1];
		String node2 = "n" + keyParts[2];
		String sourceNodeId = null;
		String destNodeId = null;
		if (node1.equals(this.nodeId)) {
			sourceNodeId = node1;
			destNodeId = node2;
		}
		if (node2.equals(this.nodeId)) {
			sourceNodeId = node2;
			destNodeId = node1;
		}

		String sourceServerInfo = this.nodesKnowledge.get(sourceNodeId);
		String sourceServerDetails[] = sourceServerInfo.split("@");
		String sourceServer = sourceServerDetails[0];
		int sourceServerPort = Integer.valueOf(sourceServerDetails[1]);

		String destServerInfo = this.nodesKnowledge.get(destNodeId);
		String destServerDetails[] = destServerInfo.split("@");
		String destServer = destServerDetails[0];
		int destServerPort = Integer.valueOf(destServerDetails[1]);


		message.setSourceId(sourceNodeId);
		message.setSourceServer(sourceServer);
		message.setSourcePort(sourceServerPort);
		message.setDestId(destNodeId);
		message.setDestServer(destServer);
		message.setDestPort(destServerPort);
		message.setKeyRequest(true);
		message.setKey(key);
		message.setTimeStamp(timeStamp);
		return message;
	}

	public ArrayList<String> getListOfKeysforCSExecution() {
		ArrayList<String> keysRequired = new ArrayList<String>();
		String node = this.nodeId.substring(1);
		int nodeNumber = Integer.valueOf(node);
		for (int i=0 ; i< this.totalNumOfNodes; i++) {
			if(i != nodeNumber) {
				String key = "k-";
				if (i < nodeNumber) {
					key = key + i + "-" + nodeNumber;
				} else if(i > nodeNumber)  {
					key = key + nodeNumber + "-" + i;
				}
				keysRequired.add(key);
			}
		}
		return keysRequired;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getTotalNumOfNodes() {
		return totalNumOfNodes;
	}
	public void setTotalNumOfNodes(int totalNumOfNodes) {
		this.totalNumOfNodes = totalNumOfNodes;
	}
	public TreeMap<String, String> getNodesKnowledge() {
		return nodesKnowledge;
	}
	public void setNodesKnowledge(TreeMap<String, String> nodesKnowledge) {
		this.nodesKnowledge = nodesKnowledge;
	}
	public ArrayList<String> getKeyList() {
		return keyList;
	}
	public void setKeyList(ArrayList<String> keyList) {
		this.keyList = keyList;
	}
	public boolean isExecutingCriticalSection() {
		return isExecutingCriticalSection;
	}
	public void setExecutingCriticalSection(boolean isExecutingCriticalSection) {
		this.isExecutingCriticalSection = isExecutingCriticalSection;
	}
	public static Node getInstance() {
		return instance;
	}
	public static void setInstance(Node instance) {
		Node.instance = instance;
	}


	public boolean isCSStopInvoked() {
		return isCSStopInvoked;
	}
	public void setCSStopInvoked(boolean isCSStopInvoked) {
		this.isCSStopInvoked = isCSStopInvoked;
	}
	public long getEarliestCSRequestTimeStamp() {
		long maxVal = 9999999999999l;

		if (CSRequestTimeStampQueue.size() == 0) {
			return maxVal;
		}
		return CSRequestTimeStampQueue.get(0);
	}

	public void removeKey(String key) {
		keyList.remove(key);
	}

	public void printMessage(MessageFormat message) {
		LogToFile.CreateWriteFile(this.nodeId, "####################Start of Printing Message###################");		
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getSourceId());
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getSourceServer());
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getSourcePort()+"");
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getDestId());
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getDestServer());
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getDestPort()+"");
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.getKey());
		LogToFile.CreateWriteFile(this.nodeId, "------>" + message.isKeyRequest()+"");
		LogToFile.CreateWriteFile(this.nodeId, "####################End of Printing Message###################");		

	}
	public int csLeave() {

		LogToFile.CreateWriteFile(this.nodeId, "$$$$$$$$$$$$$$$$$$$$------Entering csLeave --------$$$$$$$$$$$$$$$$$$$$");

		setCSStopInvoked(true);

		boolean exitCondition = isExecutingCriticalSection();
		while(exitCondition) {
			//LogToFile.CreateWriteFile(this.nodeId, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			try {
				Thread.currentThread().sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			exitCondition = isExecutingCriticalSection();
		}
		LogToFile.CreateWriteFile(this.nodeId, "$$$$$$$$$$$$$$$$$$$$------csLeave Executed Successfully--------$$$$$$$$$$$$$$$$$$$$");
		setCSStopInvoked(false);
		return 0;
	}
}

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class ProcessThread implements Runnable{
	private static long MAX_TIME_STAMP = 9999999999999l;
	private static ProcessThread instance = null;
	Node myNodeInfo;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private boolean receiveThreadTerminationCondition = false;

	//When Key is received from other processes
	ArrayList<MessageFormat> receivedKeyList = new ArrayList<MessageFormat>();

	//Queue to maintain keys requested by other processes
	ArrayList<MessageFormat> keyRequestList = new ArrayList<MessageFormat>();


	private ProcessThread() {

	}
	//Singleton Class // Easy to use the same object throughout
	public static ProcessThread getProcessThread(Node myNodeinfo) {
		if(instance == null) {
			System.out.println("ProcessThread Instance is null");
			instance = new ProcessThread();
			instance.myNodeInfo = myNodeinfo;
		}
		return instance;
	}

	public static ProcessThread getProcessThread() {
		return instance;
	}
	//Implement to receive
	public void run() {
		try {
			receiveData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		//Check if Node Executing Critical Section

		//If node not executing critical Section release key

	}

	//Methods For Sending Message
	public void initializeClientToSend(String serverName, int portNumber){
		//System.out.println("Client initializing");
		try {
			clientSocket = new Socket(serverName,portNumber);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(MessageFormat message) throws Exception{
		//System.out.println("sending 1 message");
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(message);
	}

	public void sendToNode(String server, int port, MessageFormat message) throws Exception {
		//System.out.println("--Sending to Node--");
		initializeClientToSend(server,port);
		send(message);
//		closeClientConnection();
		//System.out.println("--Message Sent to Node--");
	}
	public void closeClientConnection() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Methods to receive Data
	public String initializeServerToReceive(int serverPort)
	{
		String statusMessage="OK";
		try
		{
			LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Server initializing");
			System.out.println("Server initializing");
			serverSocket = new ServerSocket(myNodeInfo.getPort());
		} 
		catch (IOException e) {
			statusMessage = "TCP Port"+ serverPort +"is occupied.";
			e.printStackTrace();
		}
		return statusMessage;
	}
	public void receiveData() throws IOException, ClassNotFoundException {
		while(!receiveThreadTerminationCondition){
			//while(true){
			Socket connectionSocket = serverSocket.accept();
			InputStream is = connectionSocket.getInputStream();
			ObjectInputStream retrieveStream = new ObjectInputStream(is);
			try {
				Thread.currentThread().sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MessageFormat retrievedObject = (MessageFormat) retrieveStream.readObject();
			LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "--Message Received from-->"+retrievedObject.getSourceId());

			//If message contains key received from other process, store it in receivedKeyList
			// If message is keyRequest, store it in keyRequestList (based on time stamp)
			if(isMessageKeyRequest(retrievedObject)) {
				LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "--Received Message is key request--");
				keyRequestList.add(retrievedObject);
				//arrangeKeyRequestAsPerTimeStamp();
			} else {
				LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "--Received Message is key reply--");
				receivedKeyList.add(retrievedObject);
			}
			printMessage(retrievedObject);
		}
	}
	public void closeServerConnection() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processRequestKeyQueue() {
		if(!isProcessExecutingCriticalSection()) {
			//LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "-----Inside processRequestKeyQueue() 1---------");
			long latestCSRequestTime = Node.getInstance().getEarliestCSRequestTimeStamp();
			long latestKeyRequestTime = getEarliestKeyRequestTimeStamp();
			//LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "latestCSRequestTime-->"+latestCSRequestTime+"latestKeyRequestTime-->"+latestKeyRequestTime);			
			if( latestKeyRequestTime < latestCSRequestTime ) {
				//LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "-----Inside processRequestKeyQueue() 2---------");

				String requestedKey = keyRequestList.get(0).getKey();
				//Remove key from keyList of node
				Node.getInstance().removeKey(requestedKey);
				//create message to send 
				MessageFormat queuedMessage = keyRequestList.get(0);
				MessageFormat messageToSend = createMessageToSend(queuedMessage);

				//send key to the requesting Process
				LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "-----------Sending key to the requested process--------------");
				try {
					printMessage(messageToSend);
					sendToNode(messageToSend.getDestServer(), messageToSend.getDestPort(), messageToSend);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//remove the first message from the queue
				keyRequestList.remove(0);
			}

		}
	}

	public long getEarliestKeyRequestTimeStamp() {
		long maxVal = 9999999999999l;

		if (keyRequestList.size() == 0) {
			return maxVal;
		}
		return keyRequestList.get(0).getTimeStamp();
	}
	public MessageFormat createMessageToSend(MessageFormat queuedMessage) {
		MessageFormat messageToSend = new MessageFormat();
		messageToSend.setDestId(queuedMessage.getSourceId());
		messageToSend.setDestPort(queuedMessage.getSourcePort());
		messageToSend.setDestServer(queuedMessage.getSourceServer());
		messageToSend.setSourceId(queuedMessage.getDestId());
		messageToSend.setSourcePort(queuedMessage.getDestPort());
		messageToSend.setSourceServer(queuedMessage.getDestServer());
		messageToSend.setKey(queuedMessage.getKey());
		messageToSend.setKeyRequest(false);
		long timeStamp = new Date().getTime();
		messageToSend.setTimeStamp(timeStamp);
		return messageToSend;
	}

	public boolean isProcessExecutingCriticalSection() {
		return Node.getInstance().isExecutingCriticalSection();
	}

	public boolean isMessageKeyRequest(MessageFormat message) {
		return message.isKeyRequest();
	}
	
	public void arrangeKeyRequestAsPerTimeStamp() {
		TimeStampSort timeSort = new TimeStampSort();
		Collections.sort(keyRequestList, timeSort);
	}
	public boolean isReceiveThreadTerminationCondition() {
		return receiveThreadTerminationCondition;
	}
	public void setReceiveThreadTerminationCondition(
			boolean receiveThreadTerminationCondition) {
		this.receiveThreadTerminationCondition = receiveThreadTerminationCondition;
	}
	public ArrayList<MessageFormat> getReceivedKeyList() {
		return receivedKeyList;
	}
	public void setReceivedKeyList(ArrayList<MessageFormat> receivedKeyList) {
		this.receivedKeyList = receivedKeyList;
	}
	public ArrayList<MessageFormat> getKeyRequestList() {
		return keyRequestList;
	}
	public void setKeyRequestList(ArrayList<MessageFormat> keyRequestList) {
		this.keyRequestList = keyRequestList;
	}

	public void printMessage(MessageFormat message) {
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "####################Start of Printing Message###################");		
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getSourceId());
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getSourceServer());
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getSourcePort()+"");
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getDestId());
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getDestServer());
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getDestPort()+"");
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.getKey());
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "------>" + message.isKeyRequest()+"");
		LogToFile.CreateWriteFile(myNodeInfo.getNodeId(), "####################End of Printing Message###################");		

	}
	
	public int getSizeOfReceivedKeyList() {
		return this.receivedKeyList.size();
	}
	
	public void clearReceivedKeyList() {
		this.receivedKeyList.clear();
	}
	
	public static void useResource(String nodeId, String destServer, int destPort) throws Exception {
		//Create Message to send
		MessageFormat messageToSend = new MessageFormat();
		messageToSend.setSourceId(nodeId);
		messageToSend.setResourceGetRequest(true);
		
		//Send a message to Use Resource
		ProcessThread clientThread = ProcessThread.getProcessThread();
		clientThread.sendToNode(destServer, destPort, messageToSend);
	}

	public static void leaveResource(String sourceNodeId, String destServer, int destPort) throws Exception {
		//Create Message to send
		MessageFormat messageToSend = new MessageFormat();
		messageToSend.setSourceId(sourceNodeId);
		messageToSend.setResourceLeaveRequest(true);
		
		//Send a message to Use Resource
		ProcessThread clientThread = ProcessThread.getProcessThread();
		clientThread.sendToNode(destServer, destPort, messageToSend);
	}
}


class TimeStampSort implements Comparator<MessageFormat> {
	public int compare(MessageFormat o1, MessageFormat o2) {
		int retVal = 0;
		long difference = o1.getTimeStamp()-o2.getTimeStamp();
		if(difference > 0){
			retVal = 1;
		}
		if(difference < 0){
			retVal = -1;
		}
		if(difference == 0){
			retVal = 0;
		}

		return retVal;
	}
}

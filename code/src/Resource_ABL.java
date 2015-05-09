import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;


public class Resource_ABL implements Runnable{
	private String nodeId;
	private String server;
	private int port;
	private static Resource_ABL instance = null;
	private ServerSocket serverSocket;
	Set<String> resourRequestSet = new TreeSet<String>();



	private Resource_ABL() {

	}
	//Singleton Class // Easy to use the same object throughout
	public static Resource_ABL getReSource(String nodeId, String server, int port) {
		if(instance == null) {
			System.out.println("ProcessThread Instance is null");
			instance = new Resource_ABL();
			instance.nodeId = nodeId;
			instance.server = server;
			instance.port = port;
		}
		return instance;
	}

	public void run() {

		try {
			receiveData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	//Methods to receive Data
	public String initializeServerToReceive(int serverPort)
	{
		String statusMessage="OK";
		try
		{
			LogToFile.CreateWriteFile(this.nodeId, "Resource initializing");
			System.out.println("Resource initializing");
			serverSocket = new ServerSocket(this.port);
		} 
		catch (IOException e) {
			statusMessage = "TCP Port"+ serverPort +"is occupied.";
			e.printStackTrace();
		}
		return statusMessage;
	}
	public void receiveData() throws IOException, ClassNotFoundException {
		while(true){
			//while(true){
			Socket connectionSocket = serverSocket.accept();
			InputStream is = connectionSocket.getInputStream();
			ObjectInputStream retrieveStream = new ObjectInputStream(is);
			MessageFormat retrievedObject = (MessageFormat) retrieveStream.readObject();
//			LogToFile.CreateWriteFile(this.nodeId, "--Message Received from-->"+retrievedObject.getSourceId());

			//If message contains get resource Request add that to set
			//If message contains leave resource Request remove that from set
			if(isMessageResourceGetRequest(retrievedObject)) {
//				LogToFile.CreateWriteFile(this.nodeId, "--Received Message is Resource Get Request--");
				resourRequestSet.add(retrievedObject.getSourceId());
			} 
			if (isMessageResourceLeaveRequest(retrievedObject)) {
//				LogToFile.CreateWriteFile(this.nodeId, "--Received Message is Resource Leave Request--");
				resourRequestSet.remove(retrievedObject.getSourceId());
			}
		}
	}
	public void closeServerConnection() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isMessageResourceGetRequest(MessageFormat message) {
		return message.isResourceGetRequest();
	}
	public boolean isMessageResourceLeaveRequest(MessageFormat message) {
		return message.isResourceLeaveRequest();
	}

	public void checkForCSviolation() {
		long timeStamp = new Date().getTime();;
		while(true) {
			try {
				Thread.currentThread().sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeStamp = new Date().getTime();
			//LogToFile.CreateWriteFile(this.nodeId, "Size of Set"+ resourRequestSet.size());				
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			Date date = new Date();
			if(resourRequestSet.size() == 0) {
				//LogToFile.CreateWriteFile(this.nodeId, "Time->>"+ dateFormat.format(date) +" --NO Critical Section Violation--");
				//LogToFile.CreateWriteFile(this.nodeId, "--");				
			} 
			
			if(resourRequestSet.size() == 1) {
				LogToFile.CreateWriteFile(this.nodeId, "Time->>"+ dateFormat.format(date) +" --NO Critical Section Violation--");
				//LogToFile.CreateWriteFile(this.nodeId, "--");				
			} 
			if(resourRequestSet.size() > 1) {
//				LogToFile.CreateWriteFile(this.nodeId, "Time->>"+ timeStamp +" *****************Critical Section Violation**************************");				
				LogToFile.CreateWriteFile(this.nodeId, "Time->>"+ dateFormat.format(date) +" *****************Critical Section Violation**************************");				
			}
		}
	}
	public static void main(String[] args) {
		String nodeId = args[0].trim();
		String server = args[1].trim();
		int port = Integer.valueOf(args[2].trim());

		Resource_ABL resource = Resource_ABL.getReSource(nodeId, server, port);
		Thread resourceThread = new Thread(resource);
		resource.initializeServerToReceive(port);

		try {
			Thread.currentThread().sleep(40);
			resourceThread.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		resource.checkForCSviolation();
	}
}

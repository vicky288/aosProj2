import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;


public class MessageFormat implements Serializable{

	//for time being lets keep all these...
	private String messageId;
	private String sourceServer;
	private String destServer;
	private String sourceId;
	private String destId;
	private int sourcePort;
	private int destPort;
	//private long lStartTime = new Date().getTime();
	
	
	private long timeStamp;
	private String key;
	private boolean isKeyRequest=false;
	
	private boolean isResourceGetRequest=false;
	private boolean isResourceLeaveRequest=false;
	
	
	public boolean isResourceLeaveRequest() {
		return isResourceLeaveRequest;
	}
	public void setResourceLeaveRequest(boolean isResourceLeaveRequest) {
		this.isResourceLeaveRequest = isResourceLeaveRequest;
	}
	public boolean isResourceGetRequest() {
		return isResourceGetRequest;
	}
	public void setResourceGetRequest(boolean isResourceGetRequest) {
		this.isResourceGetRequest = isResourceGetRequest;
	}
	public boolean isKeyRequest() {
		return isKeyRequest;
	}
	public void setKeyRequest(boolean isKeyRequest) {
		this.isKeyRequest = isKeyRequest;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getSourceServer() {
		return sourceServer;
	}
	public void setSourceServer(String sourceServer) {
		this.sourceServer = sourceServer;
	}
	public String getDestServer() {
		return destServer;
	}
	public void setDestServer(String destServer) {
		this.destServer = destServer;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getDestId() {
		return destId;
	}
	public void setDestId(String destId) {
		this.destId = destId;
	}
	public int getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	public int getDestPort() {
		return destPort;
	}
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
}

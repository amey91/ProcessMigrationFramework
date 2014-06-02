package network;

import java.net.Socket;
import java.util.ArrayList;

public class ClientInfo {
	public int clientId;
	public Thread clientHandler;
	public ArrayList processes;
	public Location location;
	public int clientsideReceiverPort;
	public int receiverPort;
	public long currenttimeInMillis = 0;

	
	ClientInfo(int id, Thread ch, Socket clientSocket, int receiverPort){
		this.clientId = id;
		this.clientHandler = ch;
		this.processes = new ArrayList<Object>();
		this.location = new Location(clientSocket.getInetAddress().toString(),clientSocket.getPort());
		this.receiverPort = receiverPort;
		java.util.Date currentDate = new java.util.Date();
		this.currenttimeInMillis = currentDate.getTime();
	}
	
	public String toString(){
		return " clientId "+clientId+" clientHandler "+clientHandler+" processes "+displayProcesses(this.processes);
	}
	
	public String displayProcesses(ArrayList ob){
		String s="";
		for(int i=0; i<processes.size();i++)
			s = s + processes.get(i)+",";
		return s;
	}
}

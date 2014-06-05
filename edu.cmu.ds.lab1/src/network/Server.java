package network;


/**
 * class derived from the official Java documentation available at:
 * http://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
 * I have modified the program by removing the implicit try-with-resources block
 * and replacing it with a traditional try-catch block and a finally
 * block to explicitly free all the resources that were being freed implicitly before.
 */


import grepprocess.GrepProcess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.lang. Class;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Runnable;
import java.lang.reflect.Constructor;

import processmanager.MigratableProcess;

public class Server {
	
	// default port for initial communication
	public static int INITIAL_PORT = 2222;
	// default port for heart beat
	public static int HEARTBEAT_PORT =3333;
	
	//default initial host name
	public static String HOSTNAME = "127.0.0.1";
	
	// hashmap for storing info about clients
	// it maps a autogenerated key to all the information about a client
	public static ConcurrentHashMap<Integer,ClientInfo> clients; //= new HashMap<Integer, ClientInfo>();
	// maps process name to process information
	public static ConcurrentHashMap<String, ProcessInfo> processes;
	//used to assign unique process name to each process e.g. Process0, Process1,..
	public static int PROCESSES_SPAWNED = 0;
	
	// location consisting of ip and port for each client
	public Location location;
	
	//get initial server port for communication
	public int getInitialPort(){
		return INITIAL_PORT;
	}
	
	//default constructor for Server
	public Server(String ip, int port){
		this.location = new Location(ip,port);
	}
	
	//this method gives a new int key for new clients in the HashMap
	public static int getNewClientKey(){
		Random rand = new Random();
		return 123456 + rand.nextInt(65321);
	}

	//UI function
	public static void log(String a){
		System.out.println(a);
	}
	
	public static void main(String args[]) throws IOException{
		int portNumber = Server.INITIAL_PORT;  
		
		
		if (args.length >1) {
			System.err.println("Usage: java Server OR java Server <port>  (example java Server 2222)");
	        System.exit(0);
	        }
		
		if(args.length ==1){
			portNumber = Integer.parseInt(args[0]);
			Server.INITIAL_PORT = portNumber;
		}
		
			
		clients = new ConcurrentHashMap<Integer, ClientInfo>();
		processes = new ConcurrentHashMap<String, ProcessInfo>();
		// setup the hearbeat socket for the server that listens to clients 
		ServerSocket serverSocket1 = new ServerSocket(Server.HEARTBEAT_PORT);
		new Thread(new ReceiveHearBeats(serverSocket1)).start();
		System.out.println("Server ip = "+serverSocket1.getInetAddress());
		
	    
		// setup the time out thread which looks intot he hashmap for timedout clients
		// and deletes any which dont send heartbeats for more than 20 seconds
		log("Setting up timeout thread for clients..");
		Thread tot = new Thread(new DeleteTimedoutClients());
		tot.start();
		
		//setup the User console
		log("Setting up the Process manager console...");
		Thread console = new Thread(new ProcessManager());
		console.start();
		
		
			        try {
			        	// listen at initial connection port
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			            
			            while(true){
					            //accept a new client connection by listening to port
					            Socket clientSocket = serverSocket.accept();    
					            
					            //get a hashmap key for the new client
					            int key = 0;
					            while(true){
					            	key = getNewClientKey();
					            	if(!clients.containsKey(key))
					            		break; //break since we got a unique key					            	
					            }
					            
					            // read initial message containing client side receiving port 
							    // for other clients to contact this particular client 
					            PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
					            // open in stream
					            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
					            		clientSocket.getInputStream()));
					            String receiverPortInString = inputStream.readLine();
							    String[] port = receiverPortInString.split(" ");
							    //if the client did not inform me of its listening port
							    if(!port[0].equalsIgnoreCase("MyReceiver")){
							    	System.out.println("First Message is not not as excepted"+ receiverPortInString);
							    	System.exit(0);
							    }
							    
							    outputStream.println("YOURKEY "+key);
							    
							    // this is the receiver port for object communication from above
							    int receiverPort = Integer.parseInt(port[1]);
					            
							    
					            //once client connects, create a output stream at the socket
					            Thread pp = new ClientHandler(clientSocket,key);
					            
					            
					            // TODO
					            //if yes, mark it as process manager in ClientInfo 
					            Server.clients.put(key,new ClientInfo(key, pp,clientSocket,receiverPort));
					            //check if the imput is from process manager
					            if(port.length>2){
					            	if(port[2].equalsIgnoreCase("processmanager")){
					            		Server.clients.get(key).processManager=true;;
					            	}
					            }
					            //Server.displayClients();
					            pp.start();
			            }
			            
			        } catch (IOException e) {
			            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			            System.out.println(e.getMessage());
			        }
			          catch (Exception e) {
			        	  System.out.println("Exception occured in Server. Trace:");
			        	System.out.print(e.getStackTrace() + e.toString());
			          }    
	} // end of main()
	
	// displays all the clients connected to the server 
	public static void displayClients(){
		
		//this int stores 342
		int managerCount=0;
		for(int i : Server.clients.keySet())
			if(!Server.clients.get(i).processManager)
				System.out.println("Client id: "+ i +" Location: ip="+clients.get(i).location.ipAddress+
						" Connected to port="+clients.get(i).location.socketNumber+ " listening on port="+clients.get(i).receiverPort+" Last Seen:"+clients.get(i).lastSeen);
			else
				managerCount++;
		System.out.println("Total clients="+(Server.clients.size()-managerCount));
		System.out.println();
	}//end of displayClients()
	
	
	// displays all the clients connected to the server 
	public static void displayProcesses(){
		
		if(Server.processes.size()>0){
			log("Available processes are: ");
			for(String i : Server.processes.keySet()){
				log("Process Name:" + i +" | Located on client " + Server.processes.get(i).clientId);
			}
			System.out.println("Total processes="+(Server.processes.size()));
			System.out.println();
		} else
			System.out.println("No processes to display");
	}
} // end of server class


//this class handles each client separately as a thread
class ClientHandler extends Thread{

	public DataInputStream inputStream=null;
	public PrintWriter printStream = null;
	public Socket clientSocket = null;
	private int threadIdentifier;
	
	public ClientHandler(Socket clientSocket, int clientId) {
	    this.clientSocket = clientSocket;
	    this.threadIdentifier = clientId;
	  }
	  
	@Override
	public void run() {
		try{
			//create streams for given socket
			inputStream = new DataInputStream(clientSocket.getInputStream());
		    printStream =  new PrintWriter(clientSocket.getOutputStream(), true);
		    
		    
		    while (true) {
		        String line = inputStream.readLine();
		        printStream.flush();
		        String words[]=line.split(" ");
		        //System.out.println("Called by Client:"+ line);
		        //check if message from ProcessManager
		        if(words[0].equalsIgnoreCase("ProcessManager") && words.length>0){
		        	switch(Integer.parseInt(words[1])){
			        	case(StatusMessages.LIST_CLIENTS):
			        		System.out.println("Called by Message Manager: display clients below..");
			        		Server.displayClients();
			        		break;
			        	case(StatusMessages.LIST_PROCESSES):{
			        		System.out.println("Called by Message Manager: display processes below..");
			        		
			        		break;
			        	}
			        	case(StatusMessages.LAUNCH):{
			        		System.out.println("New Process luanched.");
			        		
		        	}
		        	}
		        }
		       
		        //if not process manager, then it is a regular client
		        else{
			        if (line.startsWith("/quit")) {
			          break;
			        }
			        
			        	
			        }
			        printStream.println("Received at Server: "+line);
		        }
		    
		    //free allocated communication paths
		    inputStream.close();
			printStream.close();
			clientSocket.close();
		} catch(IOException e){
			System.out.println("Thread ended for client");
		}
		
	}
		
}

class ReceiveHearBeats extends Thread{
	
	ServerSocket heartbeatSocket;
	
	public ReceiveHearBeats(ServerSocket s){
		heartbeatSocket = s;
	}
	
	@Override
	public void run(){
		 
		
		while(true){
            //accept a new client connection by listening to port
			try {
				
				Socket clientSocket1 = heartbeatSocket.accept();
				// read heartbeat 
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(
						clientSocket1.getInputStream()));
				Object receivedString = inputStream.readLine();
				Object receivedHashMap = inputStream.readLine();
				//check if it is simple heartbeat
				HandleHeartBeat hhb = new HandleHeartBeat((String)receivedString,(String)receivedHashMap);
				new Thread(hhb).start();
				inputStream.close();
				clientSocket1.close();
			    
			}catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
				continue;
			}    
		} // end of while		
	}//end of run
}// end of Receive Heart Beats 


/*
 * This thread accepts heartbeat string as well as current process list from each client 
 * 
 */
class HandleHeartBeat extends Thread {
	String hbMsg;
	String clientProcesses;
	
	public HandleHeartBeat(Object receiverPortInString, Object processList) {
		
		hbMsg = (String)receiverPortInString;
		clientProcesses = (String)processList;
//		clientProcesses = (ConcurrentHashMap<String,MigratableProcess>)processList;
	}

	@Override
	public void run(){
		//split input to receive client key
		String[] keyArray = hbMsg.split(" ");
		//extract client key
	    if(!keyArray[0].equalsIgnoreCase("HEARTBEAT")||keyArray.length<2){
	    	System.out.println("First Message is not not as excepted"+ hbMsg);
	    	System.exit(0);
	    }
	    
	    int clientKey = Integer.parseInt(keyArray[1]);
	    
        // implement client handler after updating client info
	    if(!Server.clients.containsKey(clientKey)){
	    	System.out.println("Client with key "+clientKey+" does not exist or has timed-out for more than 20 seconds. No action taken for this heartbeat.");
	    	
	    }
	    else{
	    	//for this client, delete all entries in the server's process list
	    	for(String s : Server.processes.keySet()){
	    		if(Server.processes.get(s).clientId==clientKey)
	    			Server.processes.remove(s);
	    	}
	    	
	    	
	    	//update the lastseen of this client
	    	java.util.Date currentDate1 = new java.util.Date();
	    	Server.clients.get(clientKey).lastSeen = currentDate1.getTime();
	    	//update processlist of this client
	    	// TODO proper update of client process list at server side
	    	//if(clientProcesses.size()!=0)
	    	//for(String kk : clientProcesses.keySet()){
	    	//	System.out.println("Received for client "+ clientKey +" processes "+ clientProcesses.get(kk).getName());
	    	//}
	    	clientProcesses = clientProcesses.replace("{", "");
	    	clientProcesses = clientProcesses.replace("}", "");
	    	clientProcesses = clientProcesses.replace("=", "");
	    	clientProcesses = clientProcesses.replace(",", "");
	    	String[] ppp = clientProcesses.split(" ");
	    	// TODO delete this line and actually update the processlist at server
	    	//System.out.println("Client" + clientKey + "  processes -> " + clientProcesses + ppp.length);
	    	ProcessInfo currentLocation = new ProcessInfo(clientKey);
	    	//if there are inputs
	    	if(ppp!=null && clientProcesses!=""){
	    	
	    		if(ppp.length>0 && clientProcesses.length()!=0){
	    			//System.out.println("inserting into process map : "+clientProcesses);
	    			//System.out.println(clientProcesses.length() +" --> length od inserted string");
	    		
	    			for(int i=0;i<ppp.length;i++){
	    				Server.processes.put(ppp[i], currentLocation);
	    			}
	    		}  
	    	} 
	    }
	    
	}
	
	
}// end of handle heart beats



class ProcessInfo implements java.io.Serializable {
	/**
	 * Autogenerated
	 */
	private static final long serialVersionUID = 1432283007962668575L;
	public int clientId; //client currently running this process 
	//public String processName; // this field is the key of the hashmap and not an element in the value
	public int port; //location of client. Not of much use in current implementation, but still stored it
	public String ip; // ip of client holding this process
	public int clientListeningPort; // port where to contact the client holding this process
	
	//processId, clientId, controllerThread (Runnable)
	public ProcessInfo(int client){
		clientId = client;
		if(Server.clients.containsKey(client)){
			ip = Server.clients.get(client).location.ipAddress;
			port = Server.clients.get(client).location.socketNumber;
			clientListeningPort = Server.clients.get(client).receiverPort;
		}
	}
	
}



//this class is responsible for updating the hashmap
// and eliminating clients which have timed out
class DeleteTimedoutClients extends Thread {
	
	
	public DeleteTimedoutClients(){
	}
	
	@Override
	public void run(){
		// for infinity
		while(true){

			//for every entry in the client hashmap
			for(int i : Server.clients.keySet()){
				//if client is inactive for more than 20 seconds
				java.util.Date currentDate = new java.util.Date();
				long currentTime = currentDate.getTime();
				long clientLastSeen = Server.clients.get(i).lastSeen;
				//System.out.println("TIMEOUT last seen for ID "+i+" : "+ (currentTime - clientLastSeen));
				//check for timeout
				if(currentTime - clientLastSeen > 20000 && Server.clients.get(i).processManager==false){
					// the client process has timed out. 
					// delete it from the hashmap
					System.out.println("TIMEOUT: Client with ID "+i+" timed-out and has been removed from records.");
					Server.clients.remove(i); 
					//also delete processes related to that client
					for(String p : Server.processes.keySet()){
						//if process->clientID == current client being deleted
						if(Server.processes.get(p).clientId==i){
							Server.processes.remove(p);
						}
						
					}
				}else //else check if it is a process manager with timeout greater than 50 sec
					if(currentTime - clientLastSeen > 20000 && Server.clients.get(i).processManager==true){
						//process amanger has timed out
						System.out.println("TIMEOUT: Process Manager with ID "+i+" timed-out and has been removed from records.");
						Server.clients.remove(i); 				
					}
			}
			
			//sleep for 10 seconds
			try {

				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}
}



//this thread is the userconsole
class ProcessManager extends Thread{
	
	public void log(String a){
		System.out.println(a);
	}
	
	@Override
	public void run(){
		//initial setup
		BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
		int choice; //choice for user input
		int processId; //process id for user input
		int clientId; //client id for user input
		String processName;
		Socket clientSocket = null;
		String processInform;
		String processCmd;
		
		//for infinity
		while(true){
			
			try{
				
				
				
				String inp = "";
				while(inp.length()==0 || inp == null){
					log("Press: 1.List Clients 2. List Processes 3.Remove Process 4.Migrate Process: 5.Launch Process");
					inp = br.readLine();
				}
				
				
				choice = Integer.parseInt(inp);
				switch(choice){
				case(1): // display clients
					System.out.println("Display Clients called by Process Manager:");
					Server.displayClients();
					break;
				
				case(2): //display processes 
					Server.displayProcesses();
					break;
				
				case(3)://remove process
					log("Enter process name to be removed");
					processName= br.readLine();
					if(!Server.processes.containsKey(processName)){
						System.out.println("No such process to remove");
					} else {
						//send the remove message
						Remove rem = new Remove(processName);
						// get clientInfo of the current process
						ProcessInfo processInfo = Server.processes.get(processName);
						String ip= Server.clients.get(processInfo.clientId).location.ipAddress;
						System.out.println("contacting client on socket  "+ processInfo.clientListeningPort);
						System.out.println("contacting client on socket  "+ ip.substring(1, ip.length()));

						//return inetaddress from string
						//System.out.println("This ip  and port --->"+(String)ip.substring(1, ip.length()) + processInfo.clientListeningPort);
						clientSocket = new Socket(InetAddress.getByName((String)ip.substring(1, ip.length())),processInfo.clientListeningPort);
						//send the remove class object
						ObjectOutputStream outObj = new ObjectOutputStream(clientSocket.getOutputStream());
						outObj.writeObject(rem);
						Thread.sleep(200);
						//clear references
						outObj = null;
						
						Server.processes.remove(processName);
					}
					
					break;
				
				
				case(4): //migrate thread
					log("Enter process name to be migrated (e.g. Process0, Process1,..):");
					String toBeMigrated = br.readLine();
					//check if process exists
					if(!Server.processes.containsKey(toBeMigrated)){
						log("This process does not exist");
						break;
					}else {
						log("Enter ID of destination client ");
						int destinationClient = Integer.parseInt(br.readLine());
						//if the destination client does not exist, display error
						if(!Server.clients.containsKey(destinationClient)){
							log("Client with this ID does not exist");
							break;
						} else{ //now, destination client and process to be migrated exist
								//thus, migrate the process
							Migrate m=new Migrate(toBeMigrated,
									Server.clients.get(destinationClient).location.ipAddress,
									Server.clients.get(destinationClient).receiverPort);
							//TODO send migrate object
							//find sourceIP and port of source client 
							int sourcePort = Server.clients.get(Server.processes.get(toBeMigrated).clientId).receiverPort;
							String sourceIp = Server.clients.get(Server.processes.get(toBeMigrated).clientId).location.ipAddress;
							
							Socket migrateSocket = new Socket(InetAddress.getByName((String)sourceIp.substring(1, sourceIp.length())),sourcePort);
							ObjectOutputStream outObj = new ObjectOutputStream(migrateSocket.getOutputStream());
	   					 	outObj.writeObject(m);
	   					 	Thread.sleep(200);
	            			migrateSocket.close();
	            			//update reference locally
							Server.processes.get(toBeMigrated).clientId = destinationClient;
						}
					}
					
					
					break;
				
				case(5)://working input: GrepProcess of C:\\input.txt C:\\javastuff\\output.txt
					log("Enter client ID where process is to be started:");
					clientId=Integer.parseInt(br.readLine());
					if(!Server.clients.containsKey(clientId)){
						log("No such client found");
						break;
					}
					log(" Enter the name of the process to be launched (CaseSensitive) along with its arguments: ");
					System.out.println("Examples: (GrepProcess <queryString> <input.txt> <output.txt>)" );
					System.out.println("(processmanager.StaticCounter )");
					System.out.println("(processmanager.EncryptProcess encrypt C:/input.txt C:/output/output.txt)");
					System.out.println("(processmanager.GitHubProcess <username> <output.txt>)");
					System.out.println("For Convenience, you can copy&paste the content in the parenthesis, pay attention to the spaces");
					
					processInform = br.readLine();
					
					// @referred http://stackoverflow.com/questions/10272773/split-string-on-the-first-white-space-occurence
					// @referred http://stackoverflow.com/questions/3663944/what-is-the-best-way-to-remove-the-first-element-from-an-array
					processName = processInform.substring(0,processInform.indexOf(" ")); 
					processCmd = processInform.substring(processInform.indexOf(" ")+1); 
					String[] processArgs = processCmd.split(" ");

					//construct new class message
					//@referred to http://stackoverflow.com/questions/9886266/is-there-a-way-to-instantiate-a-class-by-name-in-java
					Class<?> userClass = Class.forName(processName);
					Constructor<?> constructorNew = userClass.getConstructor(String[].class);
					MigratableProcess instance = (MigratableProcess)constructorNew.newInstance((Object)processArgs);
					instance.setName("Process"+Server.PROCESSES_SPAWNED);
					Server.PROCESSES_SPAWNED++;
					
					
					String ip= Server.clients.get(clientId).location.ipAddress;
					System.out.println("contacting client on socket  "+ Server.clients.get(clientId).receiverPort);
					System.out.println("contacting client on socket  "+ ip.substring(1, ip.length()));


					//return inetaddress from string
					System.out.println((String)ip.substring(1, ip.length()));
					clientSocket = new Socket(InetAddress.getByName((String)ip.substring(1, ip.length())),Server.clients.get(clientId).receiverPort);
					
					
					//Server.clients.get(clientId).launch(processArgs);
					//new Thread(instance).start(); //dont start the process at the server!
					 ObjectOutputStream outObj = new ObjectOutputStream(clientSocket.getOutputStream());
					 outObj.writeObject(instance);
					 Thread.sleep(200);
					 outObj = null;
					 Server.processes.put("Process"+(Server.PROCESSES_SPAWNED-1), new ProcessInfo(clientId));
					 
					 break;
					
					 
				default: 
					log(" Unexpected Input.");
					break;
				
				}//end of switch
					
				
			}
			catch(Exception e){
				log("something went wrong. Restarting ProcessManager. Refer error message below:");
				e.printStackTrace();
				//if process manager exits, start new process manager
				new Thread(new ProcessManager()).start();
				this.suspend();
				break;
				
			}//end of main try
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}//end of while
		
		
	}//end fo run
	
}


class Migrate implements java.io.Serializable{
	String processName;
	int destinationPort;
	String destinationIp;
	public Migrate(){
		
	}
	public Migrate(String p, String ip, int port){
		processName=p;
		destinationIp=ip;
		destinationPort=port;
	}
	
}

class Remove implements java.io.Serializable{
	String processName;
	public Remove(){
		
	}
	public Remove(String p){
		processName = p;
	}
}


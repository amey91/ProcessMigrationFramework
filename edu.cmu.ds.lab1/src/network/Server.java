package network;


/**
 * class derived from the official Java documentation available at:
 * http://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
 * I have modified the program by removing the implicit try-with-resources block
 * and replacing it with a traditional try-catch block and a finally
 * block to explicitly free all the resources that were being freed implicitly before.
 */


import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Runnable;
import java.lang.reflect.Constructor;

import processmanager.MigratableProcess;

import com.sun.xml.internal.fastinfoset.util.StringArray;

public class Server {
	// default port for initial communication
	public static final int INITIAL_PORT = 2222;
	// default port for heart beat
	public static final int HEARTBEAT_PORT =3333;
	// default port for heart beat
	public static final int PROCESS_LIST_PORT =4444;
	//default initial host name
	public static String HOSTNAME = "127.0.0.1";
	
	// hashmap for storing info about clients
	// it maps a autogenerated key to all the information about a client
	public static ConcurrentHashMap<Integer,ClientInfo> clients; //= new HashMap<Integer, ClientInfo>();
	
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
		if (args.length != 1) {
			System.err.println("Usage: java Server OR java Server <port>");
	            System.err.println("No port specified. Using default port 2222");
	        }
		else
			portNumber = Integer.parseInt(args[1]);
			
		clients = new ConcurrentHashMap<Integer, ClientInfo>();
		
		// setup the hearbeat socket for the server that listens to clients 
		ServerSocket serverSocket1 = new ServerSocket(Server.HEARTBEAT_PORT);
		new Thread(new ReceiveHearBeats(serverSocket1)).start();
		
		// setup the hearbeat socket for the server that listens to clients 
		ServerSocket serverSocket2 = new ServerSocket(Server.PROCESS_LIST_PORT);
		new Thread(new ReceiveProcessList(serverSocket2)).start();
	    
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
		
		
		int managerCount=0;
		for(int i : Server.clients.keySet())
			if(!Server.clients.get(i).processManager)
				System.out.println("Client " +i+": "+ Server.clients.get(i)+" Location: ip="+clients.get(i).location.ipAddress+
						" Connected to port="+clients.get(i).location.socketNumber+" listening on port="+clients.get(i).receiverPort+" Timestamp:"+clients.get(i).lastSeen);
			else
				managerCount++;
		System.out.println("Total clients="+(Server.clients.size()-managerCount)+" and ProcessManagers="+managerCount);
		System.out.println();
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
				String receiverPortInString = inputStream.readLine();
				HandleHeartBeat hhb = new HandleHeartBeat(receiverPortInString);
				new Thread(hhb).start();
				clientSocket1.close();
			    
			}catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
				continue;
			}    
		} // end of while		
	}//end of run
}// end of Receive Heart Beats 



class HandleHeartBeat extends Thread {
	String hbMsg;
	
	public HandleHeartBeat(String receiverPortInString) {
		hbMsg = receiverPortInString;
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
	    	System.out.println("Client with key "+clientKey+" does not exist or has timed-out.");
	    	System.exit(0);
	    }
	    else{
	    	java.util.Date currentDate1 = new java.util.Date();
	    	Server.clients.get(clientKey).lastSeen = currentDate1.getTime();
	    }
	    
	}
	
	
}// end of handle heart beats


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



//this class is responsible for updating the hashmap
//and eliminating clients which have timed out
class ReceiveProcessList extends Thread {
	
ServerSocket processListSocket;
	
	public ReceiveProcessList(ServerSocket s){
		processListSocket = s;
	}
	
	@Override
	public void run(){
		 
		
		while(true){
            //accept a new client connection by listening to port
			try {
				
				Socket clientSocket1 = processListSocket.accept();
				// read heartbeat 
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(
						clientSocket1.getInputStream()));
				String receiverPortInString = inputStream.readLine();
				HandleProcessList hhb = new HandleProcessList(receiverPortInString);
				new Thread(hhb).start();
				clientSocket1.close();
			    
			}catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
				continue;
			}    
		} // end of while		
	}//end of run
}


class HandleProcessList extends Thread {
	String hbMsg;
	
	public HandleProcessList(String receiverPortInString) {
		hbMsg = receiverPortInString;
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
	    	System.out.println("Client with key "+clientKey+" does not exist or has timed-out.");
	    	System.exit(0);
	    }
	    else{
	    	java.util.Date currentDate1 = new java.util.Date();
	    	Server.clients.get(clientKey).lastSeen = currentDate1.getTime();
	    }
	    
	}
	
	
}// end of handle process list


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
				
				log("Press: 1.List Clients 2. List Processes 3.Remove Process 4.Migrate Process: 5.Launch Process");
				choice = Integer.parseInt((br.readLine()));
				switch(choice){
				case(1): 
					System.out.println("Display Clients called by Process Manager:");
					Server.displayClients();
					break;
				
				case(2): 
					
					// TODO processmap
					log("Available processes are: "); // TODO
					log("Enter the ID of the process to be deleted: ");
					break;
					
	            	
					
					
				
				case(3):
					log("Enter process id to be removed");
					processId= Integer.parseInt(br.readLine());
					System.out.println("Process manager sending suspend request");
					// TODO remove a process	
					break;
				
				
				case(4): 
					log("Enter process to be migrated");
				
					// TODO
					break;
				
				case(5)://working input: GrepProcess of C:\\input.txt C:\\javastuff\\output.txt
					log("Enter client ID where process is to be started:");
					clientId=Integer.parseInt(br.readLine());
					if(!Server.clients.containsKey(clientId)){
						log("No such client found");
						break;
					}
					log(" Enter the name of the process to be launched (CaseSensitive) along with its arguments: ");
					System.out.println("(Example: GrepProcess <queryString> <input.txt> <output.txt>)" +
							"GrepProcess of C:\\input.txt C:\\javastuff\\output.txt");
					
					
					processInform = br.readLine();
					
					// @referred http://stackoverflow.com/questions/10272773/split-string-on-the-first-white-space-occurence
					processName = processInform.substring(0,processInform.indexOf(" ")); // "72"
					processCmd = processInform.substring(processInform.indexOf(" ")+1); // "tocirah sneab"
					String[] processArgs = processCmd.split(" ");
					
					// @referred http://stackoverflow.com/questions/3663944/what-is-the-best-way-to-remove-the-first-element-from-an-array
					// if array has any arguments

					
					//@referred to http://stackoverflow.com/questions/9886266/is-there-a-way-to-instantiate-a-class-by-name-in-java
					Class<?> userClass = Class.forName("GrepProcess");
					Constructor<?> constructorNew = userClass.getConstructor(String[].class);
					MigratableProcess instance = (MigratableProcess)constructorNew.newInstance((Object)processArgs);
					

					try{
						String ip= Server.clients.get(clientId).location.ipAddress;
						System.out.println("contacting client on socket  "+ Server.clients.get(clientId).receiverPort);
						System.out.println("contacting client on socket  "+ ip.substring(1, ip.length()));

						//return inetaddress from string
						
						clientSocket = new Socket(InetAddress.getByName((String)ip.substring(1, ip.length())),Server.clients.get(clientId).clientsideReceiverPort);

							break;
					} catch(Exception e){
						log("Failure to connect to client");	
						e.printStackTrace();
						
					}
					
					//new Thread(instance).start(); //dont start the process at the server!
					 ObjectOutputStream outObj = new ObjectOutputStream(clientSocket.getOutputStream());
					 outObj.writeObject(instance);
					 Thread.sleep(200);
					 outObj = null;
					 break;
					
				default: break;
				
				}//end of switch
					
				
			}catch(Exception e){
				log("something went wrong. Restarting ProcessManager. Refer error message below:");
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
				
			}//end of main try
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end of while
		
	}//end fo run
	
}


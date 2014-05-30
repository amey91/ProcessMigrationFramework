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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.lang. Class;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.lang.Runnable;

public class Server {
	// default port for initial communication
	public static int INITIAL_PORT = 2222;
	//default initial host name
	public static String HOSTNAME = "127.0.0.1";
	
	// arraylist for clients
	public static HashMap<Integer,ClientInfo> clients = new HashMap<Integer, ClientInfo>();
	
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

	public static void main(String args[]) throws IOException{
		/*  
		if (args.length != 1) {
	            System.err.println("Usage: java EchoServer <port number>");
	            System.exit(1);
	        }
      	*/  
	        int portNumber = Server.INITIAL_PORT;
	        
			        try {
			        	// listen at initial connection port
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			
			            while(true){
					            //accept a new client connecton by listening to port
					            Socket clientSocket = serverSocket.accept();     
					            
					            //get a hashmap key for the new client
					            int key = 0;
					            while(true){
					            	key = getNewClientKey();
					            	if(!clients.containsKey(key))
					            		break; //break since we got a unique key					            	
					            }
					            
					            /* TODO del
					            System.out.println("Connected from " + clientSocket .getInetAddress() + " on port "
					                    + clientSocket .getPort() + " to port " + clientSocket .getLocalPort() + " of "
					                    + clientSocket .getLocalAddress());
					            */
					            
					            //once client connects, create a output stream at the socket
					            ClientHandler pp = new ClientHandler(clientSocket,key);
					            Server.clients.put(key,new ClientInfo(key, pp,clientSocket));
					            pp.start();
					            //pp.stop();
			            }
			            
			        } catch (IOException e) {
			            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			            System.out.println(e.getMessage());
			        }
			          catch (Exception e) {
			        	  System.out.println("Exception occured in Server. Trace:");
			        	System.out.print(e.getStackTrace());
			          }

	        
	        
	} // end of main()
	
} // end of server class


//this class stores and returns information about client
class ClientInfo{
	public int clientId;
	public ClientHandler clientHandler;
	public ArrayList processes;
	public Location location;
	
	ClientInfo(int id, ClientHandler ch, Socket clientSocket){
		this.clientId = id;
		this.clientHandler = ch;
		this.processes = new ArrayList<Object>();
		this.location = new Location(clientSocket.getInetAddress().toString(),clientSocket.getPort());
	}
	
}


//this class handles each client separately as a thread
class ClientHandler extends Thread{

	public DataInputStream inputStream=null;
	public PrintStream printStream = null;
	public Socket clientSocket = null;
	private int threadIdentifier;
	
	public ClientHandler(Socket clientSocket, int threadID) {
	    this.clientSocket = clientSocket;
	    this.threadIdentifier = threadID;
	  }
	  
	@Override
	public void run() {
		try{
			//create streams for given socket
			inputStream = new DataInputStream(clientSocket.getInputStream());
		    printStream = new PrintStream(clientSocket.getOutputStream());
		    //tell server that it is ready
		    printStream.println("Thread connected");
		    
		    while (true) {
		        String line = inputStream.readLine();
		        if (line.startsWith("/quit")) {
		          break;
		        }
		        printStream.println("Receiveddasd: "+line);
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
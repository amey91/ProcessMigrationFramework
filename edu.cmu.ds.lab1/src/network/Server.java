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
import java.lang. Class;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class Server {
	// default port for initial communication
	public static int INITIAL_PORT = 2222;
	//default initial host name
	public static String HOSTNAME = "localhost";
	
	// map each client to its location
	private HashMap<String, Location> clients;
	// location consisting of ip and port for each client
	public Location location;
	
	//get initial server port for communication
	public int getInitialPort(){
		return INITIAL_PORT;
	}
	
	//default constructor for Server
	public Server(String ip, int port){
		this.location = new Location(ip,port);
		this.clients = new HashMap<String,Location>();
	}
	
	public Server(Socket soc) {
		
	}

	public static void main(String args[]) throws IOException{
		/*  
		if (args.length != 1) {
	            System.err.println("Usage: java EchoServer <port number>");
	            System.exit(1);
	        }
      	*/  
	        int portNumber = Server.INITIAL_PORT;
	        while(true){
			        try {
			        	// listen at initial connection port
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			            
			            //accept a new client connecton by listening to port
			            Socket clientSocket = serverSocket.accept();     
			            
			            //once client connects, create a output stream at the socket
			            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			            
			            //create a input stream for the new socket
			            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			            
			            
			            String inputLine;
			            while ((inputLine = in.readLine()) != null) {
			            	// TODO use the input line
			                out.println(inputLine);
			            }
			        } catch (IOException e) {
			            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			            System.out.println(e.getMessage());
			        }
			          catch (Exception e) {
			        	  System.out.println("Exception occured in Server. Trace:");
			        	System.out.print(e.getStackTrace());
			          }
			        finally {
			        	 
			        	 
			        	 
			        }
			        
	        }
	        
	} // end of main()
	
} // end of server class


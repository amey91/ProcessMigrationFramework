package network;
 
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
			        try (
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			            Socket clientSocket = serverSocket.accept();     
			            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
			            BufferedReader in = new BufferedReader(
			                new InputStreamReader(clientSocket.getInputStream()));
			        ) {
			            String inputLine;
			            while ((inputLine = in.readLine()) != null) {
			                out.println(inputLine);
			            }
			        } catch (IOException e) {
			            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			            System.out.println(e.getMessage());
			        }
	        }
	        
	} // end of main()
	
} // end of server class


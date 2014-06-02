package network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TestServer {
	public static void main(String args[]) throws IOException{
		int portNumber = Server.INITIAL_PORT;  
		if (args.length != 1) {
	            System.err.println("No port specified. Using default port 2222");
	        }
		else
			portNumber = Integer.parseInt(args[1]);
			
		
	        
	        
			        try {
			        	// listen at initial connection port
			            ServerSocket serverSocket = new ServerSocket(Server.INITIAL_PORT);
			
			            while(true){
					            //accept a new client connection by listening to port
					            Socket clientSocket = serverSocket.accept();    

					            ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());
					            TestObject fff = (TestObject)inobj.readObject();
					            fff.run();
							  
					          
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
}

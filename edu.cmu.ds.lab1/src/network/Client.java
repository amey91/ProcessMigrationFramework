package network;

/**
 * class derived from the official Java documentation available at:
 * http://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
 * I have modified the program by removing the implicit try-with-resources block
 * and replacing it with a traditional try-catch block and a finally
 * block to explicitly free all the resources that were being freed implicitly before.
 * I also extended their client to be runnable so that multiple clients can be instantiated.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public Location location;
	
	
	public int getSocketNumber(){
		return location.socketNumber;
	}
	
	public static void main(String ags[]) throws UnknownHostException, IOException{
		 

	        String hostName = Server.HOSTNAME;
	        int portNumber = Server.INITIAL_PORT;

	        try {
	        	
	        	//try to connect to server
	            Socket echoSocket = new Socket(hostName, portNumber);
	            //open print stream
	            PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
	            // open in stream
	            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
	            // standard input stream for user input
	            BufferedReader stdUserInput = new BufferedReader(new InputStreamReader(System.in));
	            // store input
	            String userInput;
	            while ((userInput = stdUserInput.readLine()) != "") {
	            	outToServer.println(userInput);
	                System.out.println("echo: " + inFromServer.readLine());
	            }
	        } catch (UnknownHostException e) {
	            System.err.println("Unknown host " + hostName);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " + hostName);
	            System.exit(1);
	        } 
	}
}


package processmanager;

import java.io.BufferedReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import network.Server;
import network.StatusMessages;

public class ProcessManager {
	public static int choice = 0;
	public static int messageCode = 0;
	static boolean sendMessage=false;
	
	public static void main(String args[]){
		Scanner sc= new Scanner(System.in); 
		log("Console for Process Management.");
		Thread contact = new Thread(new ContactServer());
		contact.start();
		while(true){
			log("Press: 1.List Processes 2.Suspend Process 3.Remove Process 4. Migrate Process: ");
			ProcessManager.choice = sc.nextInt();
			switch(choice){
			case(1): 
				ProcessManager.messageCode = StatusMessages.LIST_PROCESSES;
				ProcessManager.sendMessage = true;
				break;
			
			case(2): break;
			
			case(3): break;
			
			case(4): break;
			
			default: break;
			}
		}
		
		 
	}
	
	static void log(String a){
		System.out.println(a);
	}

}

class ContactServer extends Thread {

	@Override
	public void run() {
		String hostName = Server.HOSTNAME;
        int portNumber = Server.INITIAL_PORT;

        try {
        	
        	//try to connect to server
            Socket echoSocket = new Socket(hostName, portNumber);
            //open print stream
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            // open in stream
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            // standard input stream
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            // store input
            String userInput;
            while(true){
		            if (ProcessManager.sendMessage) {
		                out.println("ProcessManager "+ProcessManager.messageCode);
		                ProcessManager.sendMessage=false;
		            }
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

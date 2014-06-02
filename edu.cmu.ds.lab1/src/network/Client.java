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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processmanager.MigratableProcess;
import processmanager.ProcessInfo;
import processmanager.ProcessStatus;

public class Client {
	public Location location;
	
	private static HashMap<Integer, ProcessInfo> processMap = new HashMap<Integer, ProcessInfo>();
	private static int processID = 0;
	
	public int getSocketNumber(){
		return location.socketNumber;
	}
	
	public int receiverPort = 6666;
	
	public static void main(String ags[])
			throws UnknownHostException, IOException, ClassNotFoundException{
		 
				/* Try to connect to server */
		        String hostName = Server.HOSTNAME;
		        int portNumber = Server.INITIAL_PORT;
		        
		        //create a new receiver port for a client to receive messages from other clients
		        ServerSocket receiverSocket = new ServerSocket(0);
		        int receiverPort = receiverSocket.getLocalPort();
		        Thread t = new ClientsideReceiver(receiverSocket);
		        //start the receiver
		        t.start();
	        
	            Socket echoSocket = new Socket(hostName, portNumber);
	            //open print stream
	            PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
	            // open in stream
	            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
	            		echoSocket.getInputStream()));
	            
	            //tell the server the location of my clientside receiveing socket
	            // where all other clients can send serialized objects
	            outToServer.println("MyReceiver "+ receiverPort);
	            
	            // standard input stream for user input
	            BufferedReader stdUserInput = new BufferedReader(new InputStreamReader(System.in));
	            // store input
	            String userInput;
	            while ((userInput = stdUserInput.readLine()) != "") {
	            	outToServer.println(userInput);
	                System.out.println("echo: " + inFromServer.readLine());
	            }
	        
	        /* Prepare to read message from server */
			String str = null;
			String[] args = null;
			while ((str = inFromServer.readLine()) != null) {
				args = str.split(" ");

				if (args[0].equals("launch"))
					launch(args);
				
				//	 suspend a process given process ID 
				else if (args[0].equalsIgnoreCase("suspend")) {

					//check the process ID 
					int migrateProcessID = -1;
					try {
						migrateProcessID = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("error in process ID format");
						continue;
					}

					MigratableProcess mpWrite = processMap
							.get(migrateProcessID).process;

					if (mpWrite == null) {
						System.err.println("wrong process ID");
						continue;
					}

					mpWrite.suspend();
					processMap.get(migrateProcessID).status = ProcessStatus.SUSPENDED;

					//write the suspended process into a file 
					FileOutputStream outputFile = new FileOutputStream(args[1]
							+ args[2] + args[3] + ".obj");
					ObjectOutputStream outputObj = new ObjectOutputStream(
							outputFile);
					outputObj.writeObject(mpWrite);
					outputObj.flush();
					outputObj.close();
					outputFile.close();

					// acknowledge back to master server 
					outToServer.write("finish suspending\n");
					outToServer.flush();

					// remove the process from process list 
					processMap.remove(migrateProcessID);
				}

				
				 /* resume a suspended process by reading from an *.obj file
				 * previously dumped by another slave server
				 */
				else if (args[0].equals("resume")) {
					//read the *.obj file 
					FileInputStream inputFile = new FileInputStream(args[1]
							+ args[2] + args[3] + ".obj");
					ObjectInputStream inputObj = new ObjectInputStream(inputFile);
					MigratableProcess mpRead = (MigratableProcess) inputObj.readObject();
					inputObj.close();
					inputFile.close();
					
					// run the process 
					Thread newThread = new Thread(mpRead);
					newThread.start();

					// add this newly started process to the process list 
					ProcessInfo processInfo = new ProcessInfo();
					processInfo.process = mpRead;
					processInfo.status = ProcessStatus.RUNNING;
					processID++;
					processMap.put(processID, processInfo);
				}

				// iterate through the process list and send back to server 
				else if (str.equals("processlist")) {
					for (Map.Entry<Integer, ProcessInfo> entry : processMap
							.entrySet()) {
						if (entry.getValue().process.finalize())
							outToServer.write("#"
									+ entry.getKey()
									+ "\t"
									+ entry.getValue().process.getClass()
											.getSimpleName() + " "
									+ ProcessStatus.TERMINATED + "\n");
						else
							outToServer.write("#"
									+ entry.getKey()
									+ "\t"
									+ entry.getValue().process.getClass()
											.getSimpleName() + " "
									+ entry.getValue().status + "\n");
						outToServer.flush();
					}

					outToServer.write("process list finish\n");
					outToServer.flush();
				}

			}///
		}

		/**
		 * Instantiate a new object (running process)
		 * 
		 * @param args
		 */
		public static void launch(String[] args) {
			MigratableProcess newProcess = null;
			try {
				Class<MigratableProcess> processClass = (Class<MigratableProcess>) Class.forName(args[2]);
				Constructor<?> processConstructor = processClass
						.getConstructor(String[].class);
				Object[] processArgs = { Arrays.copyOfRange(args, 3, args.length) };
				newProcess = (MigratableProcess) processConstructor
						.newInstance(processArgs);
			} catch (ClassNotFoundException e) {
				System.out.println("Could not find class " + args[2]);
				e.printStackTrace();
				return;
			} catch (SecurityException e) {
				System.out.println("Security Exception getting constructor for "
						+ args[2]);
				return;
			} catch (NoSuchMethodException e) {
				System.out.println("Could not find proper constructor for "
						+ args[2]);
				return;
			} catch (IllegalArgumentException e) {
				System.out.println("Illegal arguments for " + args[2]);
				return;
			} catch (InstantiationException e) {
				System.out.println("Instantiation Exception for " + args[2]);
				return;
			} catch (IllegalAccessException e) {
				System.out.println("IIlegal access exception for " + args[2]);
				return;
			} catch (InvocationTargetException e) {
				System.out.println("Invocation target exception for " + args[2]);
				return;
			} catch (Exception e) {
				System.err.println(e.toString());
			}

			Thread newThread = new Thread(newProcess);
			newThread.start();

			/* add this newly started process to the process list */
			ProcessInfo processInfo = new ProcessInfo();
			processInfo.process = newProcess;
			processInfo.status = ProcessStatus.RUNNING;
			processID++;
			processMap.put(processID, processInfo);
		}

}

class ClientsideReceiver extends Thread{
	public DataInputStream inputStream=null;
	public PrintWriter printStream = null;
	public ServerSocket receiverSocket;
	private int threadIdentifier;
	
	public ClientsideReceiver(ServerSocket newReceiverSocket) {
		this.receiverSocket = newReceiverSocket;
	}
	
	@Override
	public void run(){
		try{
			//start to receive connections from other clients
		    while (true) {

			            //accept a new client connection by listening to port
			            Socket clientSocket = receiverSocket.accept();    
			            // TODO implement handling of serialized object
	
	            }

		} catch(IOException e){
			try {
				receiverSocket.close();
			} catch (IOException e1) {
				System.out.print("Failure Encountered and also could not close Clientside Receiver");
				e1.printStackTrace();
			}
			System.out.println("Thread ended for client");
		}
			
	}// end of ClientsideReceiver run
}
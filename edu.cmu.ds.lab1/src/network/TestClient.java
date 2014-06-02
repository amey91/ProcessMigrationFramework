package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestClient {
	public static void main(String args[]) throws UnknownHostException, IOException, InterruptedException{
		TestObject r = (new TestObject());
		Thread rp = new Thread(r);
		rp.start();
		
		String hostName = Server.HOSTNAME;
        int portNumber = Server.INITIAL_PORT;      
        Thread.sleep(6000);
        Socket echoSocket = new Socket(hostName, portNumber);
        //open print stream
        //PrintWriter outToServer = new PrintWriter(echoSocket.getOutputStream(), true);
        // open in stream
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        // standard input stream for user input
        BufferedReader stdUserInput = new BufferedReader(new InputStreamReader(System.in));
        // store input
        
        ObjectOutputStream outObj = new ObjectOutputStream(echoSocket.getOutputStream());
        outObj.writeObject(r);
        Thread.sleep(400);
        rp.stop();
	}
	
	
}

class TestObject implements Serializable, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4566509411963559780L;
	public int int1=1;
	public int int2=2;
	boolean suspending = false;
	
	public TestObject(){
		
	}
	public TestObject(int a, int b){
		int1 =a;
		int2=b;
	}
	
	@Override
	public void run(){
		while(!suspending){
			System.out.println(int1++);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			
		}
		
	}
}}

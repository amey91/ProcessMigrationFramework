package processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import network.Server;

public class ProcessManager {
	
	public static void main(String args[]){
		log("Start server.java");
		Server.displayClients();
		log("Start server.java");
	}
	
	static void log(String a){
		System.out.println(a);
	}

}

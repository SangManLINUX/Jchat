package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

public class chatServer {
	
	ArrayList clientOutputStreams;
	
	chatServer() {
		clientOutputStreams = new ArrayList();
		try {
			ServerSocket serverSock = new ServerSocket(5000);
			while(true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				System.out.println("Connection...");
				}
			} catch(Exception e) {e.printStackTrace();}
		}
	
	public void tellEveryone(String message) {
		Iterator it = clientOutputStreams.iterator();
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter)it.next();
				writer.println(message);
				writer.flush();
			} catch(Exception e) {e.printStackTrace();}
		}
	}
	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			} catch(Exception e) {e.printStackTrace();}
		}
		
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("¹ÞÀ½ " + message);
					tellEveryone(message);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				// socket close solution
				try {
					sock.close();
				} catch (Exception e1) {
					System.out.println("server down");
				}
				}
		}
	}
	
	public static void main(String[] args) {
		new chatServer();
	}
}

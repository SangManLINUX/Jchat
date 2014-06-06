package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class chatServer {
	
	ArrayList<Object> clientOutputStreams;
	ArrayList<Object> nickList;
		
	chatServer() {
		clientOutputStreams = new ArrayList<Object>();
		nickList = new ArrayList<Object>();	
		
		try {
			ServerSocket serverSock = new ServerSocket(5000);
			
			while(true) {
				Socket clientSocket = serverSock.accept();
				
<<<<<<< HEAD
				OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
				BufferedWriter writer = new BufferedWriter(osw);				
								
				clientOutputStreams.add(writer); 
// 아무래도 writer를 통째로 집어넣어서 socket close Exception이 뜨는거 같다.
// AllayList에 writer를 넣어놓고 tellEveryOne에서 it로 가져와서 write를 하는걸로 밝혀짐.
// 그래서 clientOutputStreams. 닫힌 소켓은 어떻게 ArrayList에서 제거하지???
// SocketException처리로 해결함.
=======
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());				
//				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				
				
				clientOutputStreams.add(writer);
>>>>>>> 80141a9e766b10d90e087a97221098676bb00bdd
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				System.out.println("Connection...");
				}
			
			
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}
		}

	// public void tellEveryone(String message) 을 	public class ClientHandler으로 이동.
	// 이동 취소. 그러면 모든 클라이언트한테 보낼 소켓에 안닫는다.
	public void tellEveryone(String message) {


		Iterator<Object> it = clientOutputStreams.iterator();
		
		while(it.hasNext()) {
<<<<<<< HEAD
			try {
				
				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write(message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("소켓이 닫힌 오브젝트드아");
				it.remove(); // 소켓이 닫힌 오브젝트를 제거.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}
			
=======
			try {	
				PrintWriter writer = (PrintWriter)it.next();
				//BufferedWriter writer = (BufferedWriter)it.next();
				
				writer.write(message+"\n");
				writer.flush();
				
			} catch(Exception e) {e.printStackTrace();}
>>>>>>> 80141a9e766b10d90e087a97221098676bb00bdd
		}
	}
	
	public void nickRefresh() { // 문제 많은 함수.
		
		Iterator<Object> it = nickList.iterator();
		while(it.hasNext()) {
			try {
				String nickbank = (String)it.next();
			PrintWriter writer = (PrintWriter)it; // problem
			writer.write(nickbank);
			writer.flush();
			} catch(Exception e) {e.printStackTrace(); System.out.println("no2.1");}
			}

	}

	
	public void setNick(String s) {
		nickList.add(s);
		
		nickRefresh();
	}
	
	public class ClientHandler implements Runnable {
		InputStreamReader isr;
		BufferedReader reader;
		OutputStreamWriter osr;
		BufferedWriter writer;
		Socket sock;
		String firstNick;
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader isr = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isr);				

			} catch(Exception e) {e.printStackTrace(); System.out.println("no3");}
		}
		
		public void run() {
			String message;
			
			/* 문제 많은 닉
			do {
			try {
			firstNick = reader.readLine(); // 닉 관련
			System.out.println(firstNick);
			setNick(firstNick); // 닉 관련
			} catch (Exception e) {e.printStackTrace(); System.out.println("no3.1");}
			} while (false);
			*/
			
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("받음 " + message);
					tellEveryone(message);
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}
		}
	
	}
	
	public static void main(String[] args) {
		new chatServer();
	}
}

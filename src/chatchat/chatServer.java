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
				
				OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
				BufferedWriter writer = new BufferedWriter(osw);				
								
				clientOutputStreams.add(writer); 
// �ƹ����� writer�� ��°�� ����־ socket close Exception�� �ߴ°� ����.
// AllayList�� writer�� �־���� tellEveryOne���� it�� �����ͼ� write�� �ϴ°ɷ� ������.
// �׷��� clientOutputStreams. ���� ������ ��� ArrayList���� ��������???
// SocketExceptionó���� �ذ���.
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				System.out.println("Connection...");
				}
			
			
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}
		}

	// public void tellEveryone(String message) �� 	public class ClientHandler���� �̵�.
	// �̵� ���. �׷��� ��� Ŭ���̾�Ʈ���� ���� ���Ͽ� �ȴݴ´�.
	public void tellEveryone(String message) {


		Iterator<Object> it = clientOutputStreams.iterator();
		
		while(it.hasNext()) {
			try {
				
				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write(message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("������ ���� ������Ʈ���");
				it.remove(); // ������ ���� ������Ʈ�� ����.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}
			
		}
	}
	
	public void nickRefresh() { // ���� ���� �Լ�.
		
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
			
			/* ���� ���� ��
			do {
			try {
			firstNick = reader.readLine(); // �� ����
			System.out.println(firstNick);
			setNick(firstNick); // �� ����
			} catch (Exception e) {e.printStackTrace(); System.out.println("no3.1");}
			} while (false);
			*/
			
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("���� " + message);
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

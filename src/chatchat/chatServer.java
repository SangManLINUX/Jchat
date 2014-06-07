package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class chatServer {
	
	ArrayList<Object> clientOutputStreams;
	ArrayList<String> nickList;
	

		
	chatServer() {
		
		clientOutputStreams = new ArrayList<Object>();
		nickList = new ArrayList<String>();	
		
		nickList.add("Test1");
		nickList.add("Test2");

		
		try {
			ServerSocket serverSock = new ServerSocket(5000);
			
			while(true) {
				Socket clientSocket = serverSock.accept();
				
				OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
				BufferedWriter writer = new BufferedWriter(osw);				
								
				clientOutputStreams.add(writer); 
// 아무래도 writer를 통째로 집어넣어서 socket close Exception이 뜨는거 같다.
// AllayList에 writer를 넣어놓고 tellEveryOne에서 it로 가져와서 write를 하는걸로 밝혀짐.
// 그래서 clientOutputStreams. 닫힌 소켓은 어떻게 ArrayList에서 제거하지???
// SocketException처리로 해결함.
				
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
			try {
				
				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write(message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("소켓이 닫힌 오브젝트드아");
				it.remove(); // 소켓이 닫힌 오브젝트를 제거.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}
			
		}
	}
	
	// nickRefresh 함수를 public class ClientHandler로 이사.

	
	public class ClientHandler implements Runnable {
		InputStreamReader isr;
		BufferedReader reader;
		OutputStreamWriter osr;
		BufferedWriter writer;
		Socket sock;
		String firstNick;
		String nickBank; // 클라이언트소켓 닉네임 기억용.
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
//				InputStreamReader isr = new InputStreamReader(sock.getInputStream());
				isr = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isr);				
				
				osr = new OutputStreamWriter(sock.getOutputStream());
				writer = new BufferedWriter(osr);

			} catch(Exception e) {e.printStackTrace(); System.out.println("no3");}
		}
		
		public void run() {
			String message;
			
			try {
				while((message = reader.readLine()) != null) {
					if(message.equals("/nick/"))
					{
						setNick();
						continue;
					}
					System.out.println("받음 " + message);
					tellEveryone(message);
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
					nickList.remove(nickBank);
					nickRefresh("2");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}
		}
		
		public String nickRefresh(Object o) {
			BufferedWriter writer = (BufferedWriter)o;
//			int dead;
//			Iterator<String> it = nickList.iterator();
			
			try {
				writer.write("/nick/" + "\n");
				writer.flush();
				System.out.println("닉네임 리스트 전송 시작");
			} catch (SocketException sc) {
//				dead = 3;
				System.out.println("소켓이 닫힌 오브젝트드아(nickRefresh로부터)");
//				return dead;
				return "/dead/";
//				it.remove(); // 소켓이 닫힌 오브젝트를 제거.
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("1여기다.");
				} 
			
			Iterator<String> it = nickList.iterator();
			while(it.hasNext()) {
				try {
					String test;
					
					writer.write(test = it.next() + "\n");

					System.out.println(test + " 을 전송");
					writer.flush();
					
					if(it.hasNext() == false)
					{
						System.out.println("닉네임 리스트 꼬리 전송");
						writer.write("/nick/" + "\n");
						writer.flush();
					}
					
					} catch(Exception e) {
						e.printStackTrace(); 
						//System.out.println("no2.1");
						System.out.println("1저기다.");
						}
				}
			System.out.println("닉네임 리스트 전송 종료");		
			
//			return 0;
			return "/fine/";
		}
		
		public void nickRefresh(String s) { // 문제 많은 함수.
			String deadCheck;
//			int deadCheck;
			if(s.equals("2")){

				Iterator<Object> it = clientOutputStreams.iterator();
				
				while(it.hasNext()) {
					try {
						
						BufferedWriter writer = (BufferedWriter)it.next();
						deadCheck = nickRefresh(writer);
						if(deadCheck.equals("/dead/"))
						{
							it.remove();
							deadCheck = null;
						}
						// it.next()로 어느 클라이언트의 스트림을 받아서 nickRefresh함수로 재귀함수처리.
						/*
						BufferedWriter writer = (BufferedWriter)it.next();
						writer.write(message + "\n");
						writer.flush();
						*/
						

					} 
					/*
					catch(SocketException se) { 
						System.out.println("소켓이 닫힌 오브젝트드아");
						it.remove(); // 소켓이 닫힌 오브젝트를 제거.
						}
					*/
					catch(Exception e) {e.printStackTrace(); System.out.println("가나다");}
					
				}

			}
		
			else if(s.equals("1"))
			{
				try {
					writer.write("/nick/" + "\n");
					writer.flush();
					System.out.println("닉네임 리스트 전송 시작");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("2여기다.");
					} 
				
				Iterator<String> it = nickList.iterator();
				while(it.hasNext()) {
					try {
						String test;
						/*
						if(it.hasNext() == false) // 조건이 안걸린다. "\n" 때문일까. 
						{
							System.out.println("닉네임 리스트 꼬리 전송");
							writer.write("/nick/");
							writer.flush();
						}
						*/
						writer.write(test = it.next() + "\n");

						System.out.println(test + " 을 전송");
						writer.flush();
						
						if(it.hasNext() == false)
						{
							System.out.println("닉네임 리스트 꼬리 전송");
							writer.write("/nick/" + "\n");
							writer.flush();
						}
						
						} catch(Exception e) {
							e.printStackTrace(); 
							//System.out.println("no2.1");
							System.out.println("2저기다.");
							}
					}
				System.out.println("닉네임 리스트 전송 종료");
				
			}

		}

		
		public void setNick() {
			String s;
			try {
				s = reader.readLine();
				nickBank = s;
				System.out.println("받은 닉네임은: " + s);
				//System.out.println("닉네임 저장은: " + nickBank);
				nickList.add(s);
			} catch (Exception e) {e.printStackTrace(); }
			
			nickRefresh("1");
		}

	
	}
	
	public static void main(String[] args) {
		new chatServer();
	}
}

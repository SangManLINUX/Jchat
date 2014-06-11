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

//				nickRefresh("2"); // �ٸ� Ŭ������ �Լ�.

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
	public void tellEveryone(String nickBank,String message) {


		Iterator<Object> it = clientOutputStreams.iterator();

		while(it.hasNext()) {
			try {

				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write(nickBank + ": " + message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("������ ���� ������Ʈ���");
				it.remove(); // ������ ���� ������Ʈ�� ����.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}

		}
	}

	// nickRefresh �Լ��� public class ClientHandler�� �̻�.


	public class ClientHandler implements Runnable {
		InputStreamReader isr;
		BufferedReader reader;
		OutputStreamWriter osr;
		BufferedWriter writer;
		Socket sock;
		String firstNick;
		String nickBank; // Ŭ���̾�Ʈ���� �г��� ����.

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
					if(message.equals("/disconnect/"))
					{
						sock.close();
						System.out.println("Client down");
						nickList.remove(nickBank);
						nickRefresh("2");
						continue;
					}
					System.out.println("���� " + message);
					tellEveryone(nickBank, message);
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

		public void nickCheck() {
			Iterator<String> it = nickList.iterator();
			while(it.hasNext()) {
				if(it.next().compareTo(nickBank) == 0) { // �۵� Ȯ�� ��.
					System.out.println("�г����� ������ �ִ�.");
					try {
						writer.write("/denied/" + "\n");
						writer.flush();
						sock.close();

					} catch(Exception e) {e.printStackTrace();}
				}
			}

			/*
			int count = 1;
			String nickName = nickBank;
			Iterator<String> it = nickList.iterator();
			while(it.hasNext()) {
				if(it.next().compareTo(nickBank) == 0) { // �۵� Ȯ�� ��.
					System.out.println("�г����� ������ �ִ�.");
					nickBank = nickName + "(" + count + ")";
					System.out.println("�г�����" +nickBank + "����");
					count++;
				}
					
			}
			*/

		}

		public String nickRefresh(Object o) {
			BufferedWriter writer = (BufferedWriter)o;

			try {
				writer.write("/nick/" + "\n");
				writer.flush();
				System.out.println("�г��� ����Ʈ ���� ����");
			} catch (SocketException sc) {
				System.out.println("������ ���� ������Ʈ���(nickRefresh�κ���)");
				return "/dead/";

			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("1�����.");
				} 

			Iterator<String> it = nickList.iterator();
			while(it.hasNext()) {
				try {
					String test;

					writer.write(test = it.next() + "\n");

					System.out.println(test + " �� ����");
					writer.flush();

					if(it.hasNext() == false)
					{
						System.out.println("�г��� ����Ʈ ���� ����");
						writer.write("/nick/" + "\n");
						writer.flush();
					}

					} catch(Exception e) {
						e.printStackTrace(); 
						//System.out.println("no2.1");
						System.out.println("1�����.");
						}
				}
			System.out.println("�г��� ����Ʈ ���� ����");		

			return "/fine/";
		}

		public void nickRefresh(String s) { // ���� ���� �Լ�.
			String deadCheck;
//			int deadCheck;
			if(s.equals("2")){ // ��ο��� �г��� ���ΰ�ħ ����.

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

					} catch(Exception e) {e.printStackTrace(); System.out.println("������");}

				}

			}

			else if(s.equals("1")) // �ش� Ŭ���̾�Ʈ �ϳ����Ը� �г��� ���ΰ�ħ ����.
				// �����غ��� nickRefresh("2")�� nickRefresh(writer)�� �� �Լ��� ���� �ʿ���� �� ����.
			{
				try {
					writer.write("/nick/" + "\n");
					writer.flush();
					System.out.println("�г��� ����Ʈ ���� ����");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("2�����.");
					} 

				Iterator<String> it = nickList.iterator();
				while(it.hasNext()) {
					try {
						String test;
						/*
						if(it.hasNext() == false) // ������ �Ȱɸ���. "\n" �����ϱ�. 
						{
							System.out.println("�г��� ����Ʈ ���� ����");
							writer.write("/nick/");
							writer.flush();
						}
						*/
						writer.write(test = it.next() + "\n");

						System.out.println(test + " �� ����");
						writer.flush();

						if(it.hasNext() == false)
						{
							System.out.println("�г��� ����Ʈ ���� ����");
							writer.write("/nick/" + "\n");
							writer.flush();
						}

						} catch(Exception e) {
							e.printStackTrace(); 
							//System.out.println("no2.1");
							System.out.println("2�����.");
							}
					}
				System.out.println("�г��� ����Ʈ ���� ����");

			}

		}


		public void setNick() {
//			String s;
			try {
//				s = reader.readLine();
				nickBank = reader.readLine();
//				nickBank = s;
				System.out.println("���� �г�����: " + nickBank);
				//System.out.println("�г��� ������: " + nickBank);

				nickCheck();

				nickList.add(nickBank);

				Collections.sort(nickList); // nickList �������� ����.
//				Collections.reverse(nickList); // nickList �������� ����.

				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

//			nickRefresh("1");
//			nickCheck();

		}


	}

	public static void main(String[] args) {
		new chatServer();
	}
}
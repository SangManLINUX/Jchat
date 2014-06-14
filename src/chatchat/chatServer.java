package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class chatServer {

	ArrayList<Object> clientOutputStreams;
	ArrayList<String> nickList; // ���� �ʿ������ ���.
	ArrayList<String[]> abd; // �̰� ����?
//	HashMap<String, Object> newClientOutputStreams; // ��ȭ���, Ŭ���̾�Ʈ writer
	HashMap<String, Object> newNickList; // �г���, �г����� �� ��ȭ�� ArrayList

	chatServer() {

		clientOutputStreams = new ArrayList<Object>();
		nickList = new ArrayList<String>();	// �� ���δ�.
		newNickList = new HashMap<String, Object>();	

		nickList.add("Test1"); // �� ���δ�.
		nickList.add("Test2"); // �� ���δ�.

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

	// newNickList, nickBank, tabName�� �̿��Ͽ� tabName�� �ش��ϴ� ������ ������.
	public void tellEveryone(String tabName, String nickBank, String message) {

		Iterator<Object> it = clientOutputStreams.iterator();

		while(it.hasNext()) {
			try {

				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write("/say" + "\n" + tabName + "\n" + nickBank + "\n" + message + "\n");
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
		String firstNick; // �Ⱦ��δ�...?
		String nickBank; // Ŭ���̾�Ʈ���� �г��� ����.
		String defaultChatRoom; // �⺻������ ���ӵǴ� system ��.
		ArrayList<String> chatRooms = new ArrayList<String>();
		List<String> SortedKeys; // HashMap�� keys�� ��Ƽ� ���Ľ�Ű�� ��.

		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				
				isr = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isr);				

				osr = new OutputStreamWriter(sock.getOutputStream());
				writer = new BufferedWriter(osr);

			} catch(Exception e) {e.printStackTrace(); System.out.println("no3");}
		}

		public void run() {
			String message; // �����޽���
			String tabName; // ��ȭ���
			String content; // ��ȭ����

			try {
				while((message = reader.readLine()) != null) {
					if(message.equals("/nick"))
					{
						setNick();
						continue;
					}
					if(message.equals("/join"))
					{
						setChatroom();
						continue;
					}
					if(message.equals("/say"))
					{
						System.out.println("���ϱ� ����.");
						tabName = reader.readLine();
						content = reader.readLine();
						tellEveryone(tabName, nickBank, content);
						continue;
					}
					if(message.equals("/query"))
					{
						System.out.println("�ӼӸ� ���� ����.");
						setQuery();
						continue;
					}
					if(message.equals("/sendQuery"))
					{
						System.out.println("�ӼӸ� ���� ����.");
						tabName = reader.readLine(); // �ӼӸ� ���
						content = reader.readLine(); // �ӼӸ� ����
						sendQuery(tabName, content);
						continue;
					}
					if(message.equals("/disconnect/"))
					{
						sock.close();
						System.out.println("Client down");
						nickList.remove(nickBank); // �� ���δ�.
						nickRefresh("2");
						continue;
					}
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
					nickList.remove(nickBank); // �� ���δ�.
					newNickList.remove(nickBank); // ��������� �г����� HaspMap ����.
					nickRefresh("2");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}
		}

		public void nickCheck() {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> it = coll.iterator();
			
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

/* �̰� ������? �г��� �������� �ڷ� ���ڸ� �ο��ϴ� �Լ�����.
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
			
			Collection<String> coll = newNickList.keySet();
			Iterator<String> itForKey = coll.iterator();

			while(itForKey.hasNext()) {
				try {
					String key;
					String value;

					// Key���� ������,
					writer.write( "/key/" + "\n" + (key = itForKey.next() ) + "\n");
					// ��ȣ �Ⱦ���ϱ� ������� ����.
					writer.flush();
					System.out.println("key���� " + key + " �� ���۰�");
					
					// Key�� Value ���� ������.
					Iterator<String> itForValue;
					// ���� �ʱ����ӽ� key ���� null �̱⿡ ���� �߻�.(�ذ�)
					
//					System.out.println((newNickList.get(key).getClass())); // value Ŭ���� Ȯ�ο�
					// String�� ArrayList�� ����ȯ �Ҽ� ���ٰ�?? (�ذ�)
					ArrayList<String> arrayForValue = (ArrayList)newNickList.get(key);
					itForValue = arrayForValue.iterator();
					
					while(itForValue.hasNext())
					{
						writer.write( "/value/" + "\n" + (value = itForValue.next()) + "\n");
						// ��ȣ �Ⱦ���ϱ� ������� ����.
						writer.flush();
						System.out.println("value�� " + value + " �� ����");
						
						if(itForValue.hasNext() == false)
						{
							writer.write("/next/" + "\n");
							writer.flush();
						}
					}

					if(itForKey.hasNext() == false)
					{
						System.out.println("�г��� ����Ʈ ���� ����");
						writer.write("/nick/" + "\n");
						writer.flush();
					}

					} catch(Exception e) {
						e.printStackTrace(); 
						System.out.println("1�����.");
						}
				}
			System.out.println("�г��� ����Ʈ ���� ����");		

			return "/fine/";
		}
		
		public void nickRefresh(String s) {
			String deadCheck;
			
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
							// ��Ʈ���� ���� �г��� ����Ʈ������ ���� �ʿ䰡 �ִµ�,
							// ���⼭�� �Ұ����ϰ� �Լ��� �������Ѵ�.
							// Ŭ���̾�Ʈ����(�ذ�Ϸ�?) 
						}			

					} catch(Exception e) {e.printStackTrace(); System.out.println("������");}

				}

			}

		}

		public void setNick() { // ó�� ���� ��ȸ��
			try {
				nickBank = reader.readLine();
				System.out.println("���� �г�����: " + nickBank);
				defaultChatRoom = reader.readLine();
				System.out.println("���� �⺻ ��ȭ����: " + defaultChatRoom);

				nickCheck();

				// value�� null�̱⿡ ������ �ٷ궧 ���� �߻�.
//				newNickList.put(nickBank, null);
				
				chatRooms.add(defaultChatRoom);
				
				// ��, ���� String�� �־�������...(�ذ�)
//				newNickList.put(nickBank, defaultChatRoom);
				newNickList.put(nickBank, chatRooms);
				
				// Keys�� ���� �����ϴ� �뵵.
				List<String> SortedKeys = new ArrayList<String>(newNickList.keySet());
				Collections.sort(SortedKeys);
				
				writer.write("/joined/" + "\n" + defaultChatRoom + "\n");
				writer.flush();
				
				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

		}
				
		public void setChatroom() { // �ʱ� ���� ä�ù� ���� �Լ�
			String chatRoom;
			try {
				chatRoom = reader.readLine();
				// ArrayList charRooms�� chatRoom�� �ߺ��� ���� �ִٸ�, �����ؾ��Ѵ�. 
				chatRooms.add(chatRoom);
				newNickList.put(nickBank, chatRooms);
								
				writer.write("/joined/" + "\n" + chatRoom + "\n");
				writer.flush();
				
				nickRefresh("2");
						
			} catch(Exception e) {e.printStackTrace(); }
		}
		
		public void setQuery() {
			String targetNick;
			try {
				targetNick = reader.readLine();
				
				writer.write("/joined/" + "\n" + targetNick + "\n");
				writer.flush();
			} catch(Exception e) {e.printStackTrace(); }
		}
		
		public void sendQuery(String tabName, String content) {
			BufferedWriter bw;
			try {
				// nickBank�� �ӼӸ� ������ ��, tabName �ӼӸ� ���, content ����.
				// ������ ���� �޴� �п��� �޽����� ������.
				Iterator<Object> it = clientOutputStreams.iterator();
				while(it.hasNext())
				{
					bw = (BufferedWriter)it.next();
					bw.write("/sendQuery/" + "\n" + nickBank + "\n" + tabName + "\n" + content + "\n");
					bw.flush();
					// �ӼӸ��� ����� �޽��� ������ �Ⱥ����°� ���� �̰� ���� ����.
					// it���� �޴� bw�� ���� �������� ���� writer�� �ٸ���.
//					writer.flush();
				}
			} catch(Exception e) {e.printStackTrace(); }

		}

	}

	public static void main(String[] args) {
		new chatServer();
	}
}
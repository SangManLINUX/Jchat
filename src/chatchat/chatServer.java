package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

//import java.awt.*;
import java.awt.event.*;

import chatchat.chatClient.LoginButtonListener;
import chatchat.chatClient.LoginKeyListener;
import chatchat.chatClient.LoginMouseListener;

public class chatServer extends JFrame {
	
	ServerSocket serverSock;

	ArrayList<Object> clientOutputStreams;
//	ArrayList<String> nickList; // ���� �ʿ䰡 ��������.
	ArrayList<String[]> abd; // �̰� ����?
//	HashMap<String, Object> newClientOutputStreams; // ��ȭ���, Ŭ���̾�Ʈ writer
	HashMap<String, Object> newNickList; // �г���, �г����� �� ��ȭ�� ArrayList
	
	InetAddress myIP;
	String portNumber;
	
	JFrame serverFrame;
	JPanel sPanel;
	JLabel ipLabel, ipShow, portLabel;
	JTextField portField;
	JButton portButton;

	chatServer() throws UnknownHostException, NullPointerException {
		serverFrame = new JFrame("���� ����");
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sPanel = new JPanel();
		serverFrame.add(sPanel);
		sPanel.setLayout(null);
		
		myIP = InetAddress.getLocalHost();
		
		ipLabel = new JLabel("���� �ɣ�");
		ipLabel.setBounds(10, 10, 60, 20);
		sPanel.add(ipLabel);
		
		ipShow = new JLabel(myIP.getHostAddress());
		ipShow.setBounds(70, 10, 160, 20);
		sPanel.add(ipShow);
		
		portLabel = new JLabel("��Ʈ ��ȣ");
		portLabel.setBounds(10, 30, 60, 20);
		sPanel.add(portLabel);
		
		portField = new JTextField("5000");
		portField.setBounds(70, 30, 80, 20);
		portField.addKeyListener(new PortKeyListener());
		portField.addMouseListener(new PortMouseListener());
		sPanel.add(portField);
		
		portButton = new JButton("Ȱ��ȭ");
		portButton.setBounds(160, 30, 80, 20);
		portButton.addActionListener(new PortButtonListener());
		sPanel.add(portButton);

		serverFrame.setSize(260, 90);
		serverFrame.setResizable(false);
		serverFrame.setVisible(true);
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
	
	public class PortKeyListener extends KeyAdapter {
		boolean toggle = true;
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 10) {
				try
				{
					for(int i = 0; i < 3; i++) {
						clientOutputStreams = new ArrayList<Object>();		// ENTER Ű�� ���� ���� Ȱ��ȭ. ��, �����嶧������ ������ ���� �Ұ�.
						newNickList = new HashMap<String, Object>();
						try {
							serverSock = new ServerSocket(Integer.valueOf(portField.getText()));

							while(true) {
								Socket clientSocket = serverSock.accept();

								OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
								BufferedWriter writer = new BufferedWriter(osw);				

								clientOutputStreams.add(writer);

								Thread t = new Thread(new ClientHandler(clientSocket));
								t.start();
								System.out.println("Connection...");
							}
						} catch(Exception ex) {ex.printStackTrace(); System.out.println("no1");}
					}
				} catch (Exception ex) {ex.printStackTrace(); System.out.println("Porting Error!!");}
			}
			if(toggle)
			{
				portField.setText("");
				toggle = false;
			}
		}
	}
	
	public class PortMouseListener extends MouseAdapter {
		boolean toggle = true;
		public void mousePressed(MouseEvent e)  {
			if(toggle)
			{
				portField.setText("");
				toggle = false;
			}
		}
	}
	
	public class PortButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton)e.getSource();
			
			if(button.getText().equals("Ȱ��ȭ"))	{
				portNumber = portField.getText();
//				portField.setEditable(false);
//				portButton.setText("��Ȱ��ȭ");

				clientOutputStreams = new ArrayList<Object>();		// 'Ȱ��ȭ'��ư�� ���� ������ Ȱ��ȭ. ��, �����嶧������ ����� ����� ���� ����.
				newNickList = new HashMap<String, Object>();
				try {
					serverSock = new ServerSocket(Integer.valueOf(portNumber));

					while(true) {
						Socket clientSocket = serverSock.accept();

						OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
						BufferedWriter writer = new BufferedWriter(osw);				

						clientOutputStreams.add(writer);

						Thread t = new Thread(new ClientHandler(clientSocket));
						t.start();
						System.out.println("Connection...");
					}
				} catch(Exception ex) {ex.printStackTrace(); System.out.println("no1");}
				
			}
/*			else if(button.getText().equals("��Ȱ��ȭ")) {
				portNumber = null;
				portField.setEditable(true);
				portButton.setText("Ȱ��ȭ");
			}*/
		}
	}

	public class LoginMouseListener extends MouseAdapter {
		boolean toggle = true;

		public void mousePressed(MouseEvent e)  {

			if(toggle)
			{
				JTextField tf = (JTextField)e.getSource();
				tf.setText("");
				toggle = false;
			}
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
					if(message.equals("/initialNick"))
					{
						setNick();
						continue;
					}
					if(message.equals("/join"))
					{
						setChatroom();
						continue;
					}
					if(message.equals("/nick"))
					{
						changeNick();
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
					if(message.equals("/exit"))
					{
						System.out.println("��ȭ�� ������ ����.");
						tabName = reader.readLine(); // ��ȭ���
						doExit(tabName);
						nickRefresh("2");
						continue;
					}
					if(message.equals("/disconnect/"))
					{
						sock.close();
						System.out.println("Client down");
//						nickList.remove(nickBank); // �� ���δ�.
						nickRefresh("2");
						continue;
					}
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
//					nickList.remove(nickBank); // �� ���δ�.
					newNickList.remove(nickBank); // ��������� �г����� HaspMap ����.
					nickRefresh("2");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}
		}

		public void nickCheck(String postNick) {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> it = coll.iterator();

			while(it.hasNext()) {
				if(it.next().compareTo(postNick) == 0) { // �۵� Ȯ�� ��.
					System.out.println("�г����� �̹� �����Ѵ�.");
					try {
						writer.write("/changeNickDenied/" + "\n");
						writer.flush();
						sock.close();

					} catch(Exception e) {e.printStackTrace();}
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
				// ���� ������ �������� �ʴ´�...
				List<String> SortedKeys = new ArrayList<String>(newNickList.keySet());
				Collections.sort(SortedKeys);
				// �Ⱦ���.

				writer.write("/joined/" + "\n" + defaultChatRoom + "\n");
				writer.flush();

				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

		}

		public void changeNick() {
			String preNick;
			String postNick;

			BufferedWriter bw;

			try {
				preNick = nickBank;
				postNick = reader.readLine();

				nickCheck(postNick);

				// �������� arraylist�� �� ��ȭ��(value)�� ���ο���� value�� �ִ´�.
				// �� �� ���� ���� ����.
				newNickList.put(postNick, newNickList.get(preNick));
				newNickList.remove(preNick);

				Iterator<Object> it = clientOutputStreams.iterator();
				while(it.hasNext())
				{
					bw = (BufferedWriter)it.next();
					bw.write("/nickChanged/" + "\n" + preNick + "\n" + postNick + "\n");
					bw.flush();
				}

				nickRefresh("2");

			} catch(Exception e) {e.printStackTrace();}
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

		public void doExit(String tabName) {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> itForKey = coll.iterator();

			while(itForKey.hasNext()) {
				try {

					String key;

					// key��
					key = itForKey.next();
					if(key.equals(nickBank))
					{
						System.out.println("key���� " + key + " ��");

//						System.out.println((newNickList.get(key).getClass())); // value Ŭ���� Ȯ�ο�
						// String�� ArrayList�� ����ȯ �Ҽ� ���ٰ�?? (�ذ�)
						ArrayList<String> arrayForValue = (ArrayList)newNickList.get(key);
						if( arrayForValue.contains(tabName) )
						{
							System.out.println(tabName + " �� �߰���.");
						}
						arrayForValue.remove(tabName);
						if( arrayForValue.contains(tabName) == false )
						{
							System.out.println(tabName + " �� ������.");
						}
						writer.write("/exited/" + "\n");
						writer.flush();

					}


				} catch(Exception e) { e.printStackTrace(); }



				}
			System.out.println("��ȭ�� ������ ���� ����");		
		}

	}

	public static void main(String[] args) throws UnknownHostException, NullPointerException {
		new chatServer();
	}
}
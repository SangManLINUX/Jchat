package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import java.awt.Toolkit;
//import java.awt.*;
import java.awt.event.*;

import chatchat.chatClient.IncomingReader;
import chatchat.chatClient.LoginButtonListener;
import chatchat.chatClient.LoginKeyListener;
import chatchat.chatClient.LoginMouseListener;

public class chatServer extends JFrame {
	
	Thread serverRunnerThread;
	ServerSocket serverSock;
	boolean serverOnOff = false; // 참이면 켜짐, 거짓이면 꺼짐.

	ArrayList<Object> clientOutputStreams; // 클라이언트 writer용.
	ArrayList<Object> clientSockets; // 클라이언트 소켓용.
	HashMap<String, Object> newNickList; // 닉네임, 닉네임이 들어간 대화방 ArrayList
	
	InetAddress myIP;
	String portNumber;
	
	JFrame serverFrame;
	JPanel sPanel;
	JLabel ipLabel, ipShow, portLabel;
	JTextField portField;
	JButton portButton;

	chatServer() throws UnknownHostException, NullPointerException {
		serverFrame = new JFrame("서버 설정");
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sPanel = new JPanel();
		serverFrame.add(sPanel);
		sPanel.setLayout(null);
		
		myIP = InetAddress.getLocalHost();
		
		ipLabel = new JLabel("서버 ＩＰ");
		ipLabel.setBounds(10, 10, 60, 20);
		sPanel.add(ipLabel);
		
		ipShow = new JLabel(myIP.getHostAddress());
		ipShow.setBounds(70, 10, 160, 20);
		sPanel.add(ipShow);
		
		portLabel = new JLabel("포트 번호");
		portLabel.setBounds(10, 30, 60, 20);
		sPanel.add(portLabel);
		
		portField = new JTextField("5000");
		portField.setBounds(70, 30, 80, 20);
		portField.addMouseListener(new PortMouseListener());
		sPanel.add(portField);
		
		portButton = new JButton("활성화");
		portButton.setBounds(160, 30, 100, 20);
		portButton.addActionListener(new PortButtonListener());
		sPanel.add(portButton);

		serverFrame.setSize(280, 90);
		serverFrame.setResizable(false);
		serverFrame.setVisible(true);
		serverFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2-100, (Toolkit.getDefaultToolkit().getScreenSize().height)/2-100);
	}

	// newNickList, nickBank, tabName을 이용하여 tabName에 해당하는 곳에만 보낸다.
	public void tellEveryone(String tabName, String nickBank, String message) {

		Iterator<Object> it = clientOutputStreams.iterator();

		while(it.hasNext()) {
			try {

				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write("/say" + "\n" + tabName + "\n" + nickBank + "\n" + message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("소켓이 닫힌 오브젝트드아");
				it.remove(); // 소켓이 닫힌 오브젝트를 제거.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}

		}
	}
	
	public void serverRun() {
		portNumber = portField.getText();
		
		serverRunnerThread = new Thread(new ServerRunner());
		serverRunnerThread.start();
		if(serverRunnerThread.isAlive())
		{
			serverOnOff = true;
			portField.setEditable(false);
			portButton.setText("비활성화");	
		}
	}
	
	public void serverDown() {
		try{
			serverSock.close();
			serverRunnerThread.interrupt();
			Iterator<Object> itForSocket = clientSockets.iterator();
			
			while(itForSocket.hasNext())
			{
				Socket s = (Socket)itForSocket.next();
				try{
					OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
					BufferedWriter writer = new BufferedWriter(osw);
					writer.write("/serverDown/" + "\n");
					writer.flush();
					s.close();
					} 
					catch(Exception e) 
					{
						System.out.println("클라이언트 소켓 닫힘");
						itForSocket.remove();
						continue;
					} 					
			}
			
			System.out.println("서버 정상 종료.");
			Thread.sleep(100);
		} catch(Exception e) { e.printStackTrace(); }
				
		if(serverRunnerThread.isAlive() == false)
		{
			serverOnOff = false;
			portField.setEditable(true);
			portButton.setText("활성화");
		}
	}
	
	public class ServerRunner implements Runnable
	{
		public ServerRunner()
		{
			
		}

		public void run() {
			
			clientOutputStreams = new ArrayList<Object>();
			clientSockets = new ArrayList<Object>();
			newNickList = new HashMap<String, Object>();
			try {
				serverSock = new ServerSocket(Integer.valueOf(portNumber));

				while(true)
				{
					Socket clientSocket = serverSock.accept();

					OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
					BufferedWriter writer = new BufferedWriter(osw);				
					
					clientOutputStreams.add(writer);
					clientSockets.add(clientSocket);

					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();
					System.out.println("Connection...");
				}
			}
			catch(Exception ex) 
			{
//				ex.printStackTrace(); 
				System.out.println("서버 소켓 닫힘.");
			}
			
		}
		
	}
	
	public class PortMouseListener extends MouseAdapter {
		boolean toggle = true;
		
		public void mousePressed(MouseEvent e)  {
			
			if(toggle == true && serverOnOff == false)
			{
				portField.setText("");
				toggle = false;
			}
			
		}
	}
	
	public class PortButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if(serverOnOff == false)
			{
				serverRun();				
			}
			else if(serverOnOff == true)
			{
				serverDown();
			}
			
		}
	}

	public class ClientHandler implements Runnable {
		Socket sock;
		InputStreamReader isr;
		BufferedReader reader;
		OutputStreamWriter osr;
		BufferedWriter writer;
		
		String nickBank; // 클라이언트소켓 닉네임 기억용.
		String defaultChatRoom; // 기본적으로 접속되는 system 방.
		
		ArrayList<String> chatRooms = new ArrayList<String>();
		List<String> SortedKeys; // HashMap의 keys만 모아서 정렬시키는 거.

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
			String message; // 받은메시지
			String tabName; // 대화방명
			String content; // 대화내용
			
			try {
				while((message = reader.readLine()) != null) 
				{
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
						System.out.println("말하기 받음.");
						tabName = reader.readLine();
						content = reader.readLine();
						tellEveryone(tabName, nickBank, content);
						continue;
					}
					if(message.equals("/query"))
					{
						System.out.println("귓속말 생성 받음.");
						setQuery();
						continue;
					}
					if(message.equals("/sendQuery"))
					{
						System.out.println("귓속말 내용 받음.");
						tabName = reader.readLine(); // 귓속말 대상
						content = reader.readLine(); // 귓속말 내용
						sendQuery(tabName, content);
						continue;
					}
					if(message.equals("/exit"))
					{
						System.out.println("대화방 나가기 받음.");
						tabName = reader.readLine(); // 대화방명
						doExit(tabName);
						nickRefresh("2");
						continue;
					}
					if(message.equals("/disconnect/"))
					{
						sock.close();
						System.out.println("Client down");
						nickRefresh("2");
						continue;
					}
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
					newNickList.remove(nickBank); // 접속종료된 닉네임의 HaspMap 제거.
					nickRefresh("2");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}	
		}

		public boolean nickCheck(String postNick) {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> it = coll.iterator();

			while(it.hasNext()) {
				if(it.next().compareTo(postNick) == 0) { // 작동 확인 됨.
					System.out.println("닉네임이 이미 존재한다.");
					try {
						writer.write("/changeNickDenied/" + "\n");
						writer.flush();
						return true;

					} catch(Exception e) {e.printStackTrace();}
				}
			}
			return false;
		}

		public void nickCheck() {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> it = coll.iterator();

			while(it.hasNext()) {
				if(it.next().compareTo(nickBank) == 0) { // 작동 확인 됨.
					System.out.println("닉네임이 같은게 있다.");
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
				System.out.println("닉네임 리스트 전송 시작");
			} catch (SocketException sc) {
				System.out.println("소켓이 닫힌 오브젝트드아(nickRefresh로부터)");
				return "/dead/";

			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("1여기다.");
				} 

			Collection<String> coll = newNickList.keySet();
			Iterator<String> itForKey = coll.iterator();

			while(itForKey.hasNext()) {
				try {
					String key;
					String value;

					// Key값을 보내고, (괄호 안씌우니까 개행까지 들어간다.)
					writer.write( "/key/" + "\n" + (key = itForKey.next() ) + "\n");
					writer.flush();
					System.out.println("key값인 " + key + " 을 전송과");

					// Key의 Value 들을 보낸다.
					Iterator<String> itForValue;
					// 현재 초기접속시 key 값이 null 이기에 문제 발생.(해결)
					
					// String을 ArrayList로 형변환 할수 없을까 (해결)
					ArrayList<String> arrayForValue = (ArrayList)newNickList.get(key);
					itForValue = arrayForValue.iterator();

					while(itForValue.hasNext())
					{
						// 괄호 안씌우니까 개행까지 들어간다.
						writer.write( "/value/" + "\n" + (value = itForValue.next()) + "\n");
						writer.flush();
						System.out.println("value인 " + value + " 을 전송");

						if(itForValue.hasNext() == false)
						{
							writer.write("/next/" + "\n");
							writer.flush();
						}
					}

					if(itForKey.hasNext() == false)
					{
						System.out.println("닉네임 리스트 꼬리 전송");
						writer.write("/nick/" + "\n");
						writer.flush();
					}

					} catch(Exception e) {
						e.printStackTrace(); 
						System.out.println("1저기다.");
						}
				}
			System.out.println("닉네임 리스트 전송 종료");		

			return "/fine/";
		}

		public void nickRefresh(String s) {
			String deadCheck;

			if(s.equals("2")){ // 모두에게 닉네임 새로고침 보냄.

				Iterator<Object> it = clientOutputStreams.iterator();

				while(it.hasNext()) {
					try {

						BufferedWriter writer = (BufferedWriter)it.next();
						deadCheck = nickRefresh(writer);
						if(deadCheck.equals("/dead/"))
						{
							it.remove();
							deadCheck = null;
							// 스트림용 말고도 닉네임 리스트에서도 지울 필요가 있다.(해결)
						}			

					} catch(Exception e) {e.printStackTrace(); System.out.println("가나다");}

				}

			}

		}

		public void setNick() { // 처음 접속 일회용
			try {
				nickBank = reader.readLine();
				System.out.println("받은 닉네임은: " + nickBank);
				defaultChatRoom = reader.readLine();
				System.out.println("받은 기본 대화방은: " + defaultChatRoom);

				nickCheck();

				// value가 null이기에 위에서 다룰때 문제 발생.
//				newNickList.put(nickBank, null);

				chatRooms.add(defaultChatRoom);

				// 아, 내가 String을 넣었었구나...(해결)
				newNickList.put(nickBank, chatRooms);

				// Keys만 따로 정렬하는 용도.
				// 지금 실제로 쓰이지는 않는다...
				List<String> SortedKeys = new ArrayList<String>(newNickList.keySet());
				Collections.sort(SortedKeys);
				// 안쓰임.

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

				if( nickCheck(postNick) == false )
				{
					// 이전닉의 arraylist로 된 대화방(value)을 새로운닉의 value로 넣는다.
					// 그 후 이전 닉을 제거.
					newNickList.put(postNick, newNickList.get(preNick));
					newNickList.remove(preNick);

					// 새로 받은 닉네임을 넣는다.
					nickBank = postNick;
					
					Iterator<Object> it = clientOutputStreams.iterator();
					while(it.hasNext())
					{
						bw = (BufferedWriter)it.next();
						bw.write("/nickChanged/" + "\n" + preNick + "\n" + postNick + "\n");
						bw.flush();
					}

					nickRefresh("2");
					
				}



			} catch(Exception e) {e.printStackTrace();}
		}

		public void setChatroom() { // 초기 후의 채팅방 생성 함수
			String chatRoom;
			try {
				chatRoom = reader.readLine();
				// ArrayList charRooms에 chatRoom과 중복된 값이 있다면, 거절해야한다. 
				
				if(chatRooms.contains(chatRoom))
				{
					writer.write("/alreadyJoined/" + "\n" + chatRoom + "\n");
					writer.flush();
				}
				else
				{
					chatRooms.add(chatRoom);
					newNickList.put(nickBank, chatRooms);

					writer.write("/joined/" + "\n" + chatRoom + "\n");
					writer.flush();
					
					nickRefresh("2");	
				}

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
				// nickBank는 귓속말 보내는 곳, tabName 귓속말 대상, content 내용.
				// 보내는 닉이 받는 닉에게 메시지를 보낸다.
				
				if(newNickList.containsKey(tabName) == false)
				{
					writer.write("/noNick/" + "\n");
					writer.flush();
					return;
				}
				
				Iterator<Object> it = clientOutputStreams.iterator();
				while(it.hasNext())
				{
					bw = (BufferedWriter)it.next();
					bw.write("/sendQuery/" + "\n" + nickBank + "\n" + tabName + "\n" + content + "\n");
					bw.flush();
					// 귓속말방 만들고 메시지 보내고 안보내는거 보니 이게 문제 같다.
					// it에서 받는 bw랑 여기 스레드의 전역 writer랑 다르다.
					// writer.flush();
				}
			} catch(Exception e) {e.printStackTrace(); }

		}

		public void doExit(String tabName) {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> itForKey = coll.iterator();

			while(itForKey.hasNext()) {
				try {

					String key;

					// key값
					key = itForKey.next();
					if(key.equals(nickBank))
					{
						System.out.println("key값인 " + key + " 의");

						// System.out.println((newNickList.get(key).getClass())); // value 클래스 확인용
						// String을 ArrayList로 형변환 할수 없다고?? (해결)
						ArrayList<String> arrayForValue = (ArrayList)newNickList.get(key);
						if( arrayForValue.contains(tabName) )
						{
							System.out.println(tabName + " 을 발견함.");
						}
						arrayForValue.remove(tabName);
						if( arrayForValue.contains(tabName) == false )
						{
							System.out.println(tabName + " 을 제거함.");
						}
						writer.write("/exited/" + "\n");
						writer.flush();

					}

				} catch(Exception e) { e.printStackTrace(); }

				}
			System.out.println("대화방 나가기 정리 종료");		
		}

	}

	public static void main(String[] args) throws UnknownHostException, NullPointerException {
		new chatServer();
	}
}
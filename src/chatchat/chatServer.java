package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class chatServer extends JFrame {

	ArrayList<Object> clientOutputStreams;
//	ArrayList<String> nickList; // 이제 필요없을듯 허다.
	ArrayList<String[]> abd; // 이게 뭐지?
//	HashMap<String, Object> newClientOutputStreams; // 대화방명, 클라이언트 writer
	HashMap<String, Object> newNickList; // 닉네임, 닉네임이 들어간 대화방 ArrayList

	chatServer() {

		clientOutputStreams = new ArrayList<Object>();
//		nickList = new ArrayList<String>();	// 안 쓰인다.
		newNickList = new HashMap<String, Object>();	

//		nickList.add("Test1"); // 안 쓰인다.
//		nickList.add("Test2"); // 안 쓰인다.

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
		
	// nickRefresh 함수를 public class ClientHandler로 이사.

	public class ClientHandler implements Runnable {
		InputStreamReader isr;
		BufferedReader reader;
		OutputStreamWriter osr;
		BufferedWriter writer;
		Socket sock;
		String firstNick; // 안쓰인다...?
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
//						nickList.remove(nickBank); // 안 쓰인다.
						nickRefresh("2");
						continue;
					}
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
//					nickList.remove(nickBank); // 안 쓰인다.
					newNickList.remove(nickBank); // 접속종료된 닉네임의 HaspMap 제거.
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
				if(it.next().compareTo(postNick) == 0) { // 작동 확인 됨.
					System.out.println("닉네임이 이미 존재한다.");
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

					// Key값을 보내고,
					writer.write( "/key/" + "\n" + (key = itForKey.next() ) + "\n");
					// 괄호 안씌우니까 개행까지 들어간다.
					writer.flush();
					System.out.println("key값인 " + key + " 을 전송과");
					
					// Key의 Value 들을 보낸다.
					Iterator<String> itForValue;
					// 현재 초기접속시 key 값이 null 이기에 문제 발생.(해결)
					
//					System.out.println((newNickList.get(key).getClass())); // value 클래스 확인용
					// String을 ArrayList로 형변환 할수 없다고?? (해결)
					ArrayList<String> arrayForValue = (ArrayList)newNickList.get(key);
					itForValue = arrayForValue.iterator();
					
					while(itForValue.hasNext())
					{
						writer.write( "/value/" + "\n" + (value = itForValue.next()) + "\n");
						// 괄호 안씌우니까 개행까지 들어간다.
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
							// 스트림용 말고도 닉네임 리스트에서도 지울 필요가 있는데,
							// 여기서는 불가능하고 함수를 만들어야한다.
							// 클라이언트에서(해결완료?) 
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
//				newNickList.put(nickBank, defaultChatRoom);
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
				
				nickCheck(postNick);
				
				// 이전닉의 arraylist로 된 대화방(value)을 새로운닉의 value로 넣는다.
				// 그 후 이전 닉을 제거.
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
				
		public void setChatroom() { // 초기 후의 채팅방 생성 함수
			String chatRoom;
			try {
				chatRoom = reader.readLine();
				// ArrayList charRooms에 chatRoom과 중복된 값이 있다면, 거절해야한다. 
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
				// nickBank는 귓속말 보내는 곳, tabName 귓속말 대상, content 내용.
				// 보내는 닉이 받는 닉에게 메시지를 보낸다.
				Iterator<Object> it = clientOutputStreams.iterator();
				while(it.hasNext())
				{
					bw = (BufferedWriter)it.next();
					bw.write("/sendQuery/" + "\n" + nickBank + "\n" + tabName + "\n" + content + "\n");
					bw.flush();
					// 귓속말방 만들고 메시지 보내고 안보내는거 보니 이게 문제 같다.
					// it에서 받는 bw랑 여기 스레드의 전역 writer랑 다르다.
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

					// key값
					key = itForKey.next();
					if(key.equals(nickBank))
					{
						System.out.println("key값인 " + key + " 의");
						
//						System.out.println((newNickList.get(key).getClass())); // value 클래스 확인용
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

	public static void main(String[] args) {
		new chatServer();
	}
}
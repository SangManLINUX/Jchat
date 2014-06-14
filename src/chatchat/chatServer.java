package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class chatServer {

	ArrayList<Object> clientOutputStreams;
	ArrayList<String> nickList; // 이제 필요없을듯 허다.
<<<<<<< HEAD
	ArrayList<String[]> abd;
=======
	ArrayList<String[]> abd; // 이게 뭐지?
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
//	HashMap<String, Object> newClientOutputStreams; // 대화방명, 클라이언트 writer
	HashMap<String, Object> newNickList; // 닉네임, 닉네임이 들어간 대화방 ArrayList

	chatServer() {

		clientOutputStreams = new ArrayList<Object>();
		nickList = new ArrayList<String>();	
//		newClientOutputStreams = new HashMap<String, Object>();
		newNickList = new HashMap<String, Object>();
<<<<<<< HEAD

=======
	
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b

		nickList.add("Test1");
		nickList.add("Test2");


		try {
			ServerSocket serverSock = new ServerSocket(5000);

			while(true) {
				Socket clientSocket = serverSock.accept();

				OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
				BufferedWriter writer = new BufferedWriter(osw);				

//				nickRefresh("2"); // 다른 클래스의 함수.

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
	
/*	구식 단일 채팅 함수
	// public void tellEveryone(String message) 을 	public class ClientHandler으로 이동.
	// 이동 취소. 그러면 모든 클라이언트한테 보낼 소켓에 안닫는다.
	public void tellEveryone(String nickBank,String message) {


		Iterator<Object> it = clientOutputStreams.iterator();

		while(it.hasNext()) {
			try {

				BufferedWriter writer = (BufferedWriter)it.next();
				writer.write(nickBank + ": " + message + "\n");
				writer.flush();

			} catch(SocketException se) { 
				System.out.println("소켓이 닫힌 오브젝트드아");
				it.remove(); // 소켓이 닫힌 오브젝트를 제거.
				}
			catch(Exception e) {e.printStackTrace(); System.out.println("no2");}

		}
	}
*/
	
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
//				InputStreamReader isr = new InputStreamReader(sock.getInputStream());
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
<<<<<<< HEAD
=======
					if(message.equals("/say"))
					{
						System.out.println("말하기 받음.");
						tabName = reader.readLine();
						content = reader.readLine();
						tellEveryone(tabName, nickBank, content);
//						tellEveryone()
						continue;
					}
					if(message.equals("/query"))
					{
						System.out.println("귓속말 생성 받음.");
						setQuery();
						continue;
						//setChatroom();
					}
					if(message.equals("/sendQuery"))
					{
						System.out.println("귓속말 내용 받음.");
						tabName = reader.readLine(); // 귓속말 대상
						content = reader.readLine(); // 귓속말 내용
						sendQuery(tabName, content);
						continue;
					}
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
					if(message.equals("/disconnect/"))
					{
						sock.close();
						System.out.println("Client down");
						nickList.remove(nickBank);
						nickRefresh("2");
						continue;
					}
//					System.out.println("받음 " + message);
//					tellEveryone(nickBank, message);
				}
			} catch (Exception e) {
				System.out.println("no4(normal)");
				try {
					sock.close();
					System.out.println("Client down");
					nickList.remove(nickBank);
					newNickList.remove(nickBank); // 접속종료된 닉네임의 HaspMap 제거.
					nickRefresh("2");
				} catch (Exception e1) {
					System.out.println("Server down");
					}
				}
		}

		public void nickCheck() {
			Collection<String> coll = newNickList.keySet();
			Iterator<String> it = coll.iterator();
<<<<<<< HEAD

=======
			
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
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

/* 단일 채팅 방식의 함수		
		public void nickCheck() {
			Iterator<String> it = nickList.iterator();
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
*/

/* 이게 뭐더라? 닉네임 거절말고 뒤로 숫자를 부여하는 함수던가.
			int count = 1;
			String nickName = nickBank;
			Iterator<String> it = nickList.iterator();
			while(it.hasNext()) {
				if(it.next().compareTo(nickBank) == 0) { // 작동 확인 됨.
					System.out.println("닉네임이 같은게 있다.");
					nickBank = nickName + "(" + count + ")";
					System.out.println("닉네임이" +nickBank + "으로");
					count++;
				}
					
			}
*/
<<<<<<< HEAD

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

//			Iterator<String> it = nickList.iterator();
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
=======
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b

					} catch(Exception e) {
						e.printStackTrace(); 
						//System.out.println("no2.1");
						System.out.println("1저기다.");
						}
				}
			System.out.println("닉네임 리스트 전송 종료");		

			return "/fine/";
		}

/* 단일 채팅 전용 함수 
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

//			Iterator<String> it = nickList.iterator();
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
						//System.out.println("no2.1");
						System.out.println("1저기다.");
						}
				}
			System.out.println("닉네임 리스트 전송 종료");		

			return "/fine/";
		}
		
/* 단일 채팅 전용 함수 
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

			return "/fine/";
		}
*/
		public void nickRefresh(String s) {
			String deadCheck;
<<<<<<< HEAD

=======
			
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
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
							// 클라이언트에서 
						}			

					} catch(Exception e) {e.printStackTrace(); System.out.println("가나다");}

				}

			}

		}
<<<<<<< HEAD

		public void setNick() { // 처음 접속 일회용
//			String s;
			try {
//				s = reader.readLine();
				nickBank = reader.readLine();
//				nickBank = s;
				System.out.println("받은 닉네임은: " + nickBank);
				defaultChatRoom = reader.readLine();
				//System.out.println("닉네임 저장은: " + nickBank);
				System.out.println("받은 기본 대화방은: " + defaultChatRoom);

				nickCheck();

				// value가 null이기에 위에서 다룰때 문제 발생.
//				newNickList.put(nickBank, null);

				chatRooms.add(defaultChatRoom);

				// 아, 내가 String을 넣었었구나...(해결)
//				newNickList.put(nickBank, defaultChatRoom);
				newNickList.put(nickBank, chatRooms);

				// Keys만 따로 정렬하는 용도.
				List<String> SortedKeys = new ArrayList<String>(newNickList.keySet());
				Collections.sort(SortedKeys);

				writer.write("/joined/" + "\n" + defaultChatRoom + "\n");
				writer.flush();

				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

=======

		public void setNick() { // 처음 접속 일회용
//			String s;
			try {
//				s = reader.readLine();
				nickBank = reader.readLine();
//				nickBank = s;
				System.out.println("받은 닉네임은: " + nickBank);
				defaultChatRoom = reader.readLine();
				//System.out.println("닉네임 저장은: " + nickBank);
				System.out.println("받은 기본 대화방은: " + defaultChatRoom);

				nickCheck();

				// value가 null이기에 위에서 다룰때 문제 발생.
//				newNickList.put(nickBank, null);
				
				chatRooms.add(defaultChatRoom);
				
				// 아, 내가 String을 넣었었구나...(해결)
//				newNickList.put(nickBank, defaultChatRoom);
				newNickList.put(nickBank, chatRooms);
				
				// Keys만 따로 정렬하는 용도.
				List<String> SortedKeys = new ArrayList<String>(newNickList.keySet());
				Collections.sort(SortedKeys);
				
				writer.write("/joined/" + "\n" + defaultChatRoom + "\n");
				writer.flush();
				
				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
//			nickRefresh("1");
//			nickCheck();

		}
		

<<<<<<< HEAD


=======
		
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
/* 단일 채팅 전용 함수
		public void setNick() {
//			String s;
			try {
//				s = reader.readLine();
				nickBank = reader.readLine();
//				nickBank = s;
				System.out.println("받은 닉네임은: " + nickBank);
				//System.out.println("닉네임 저장은: " + nickBank);

				nickCheck();

				nickList.add(nickBank);

				Collections.sort(nickList); // nickList 오름차순 정렬.
//				Collections.reverse(nickList); // nickList 내림차순 정렬.

				nickRefresh("2");
			} catch (Exception e) {e.printStackTrace(); }

//			nickRefresh("1");
//			nickCheck();

		}
<<<<<<< HEAD
*/		
		public void setChatroom() {
=======
*/
		public void setChatroom() { // 초기 후의 채팅방 생성 함수
>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
			String chatRoom;
			try {
				chatRoom = reader.readLine();
				// ArrayList charRooms에 chatRoom과 중복된 값이 있다면, 거절해야한다. 
				chatRooms.add(chatRoom);
				newNickList.put(nickBank, chatRooms);
<<<<<<< HEAD

				// 이걸 왜 넣었지?
//				writer.write("/nick/" + "\n");
//				writer.flush();

				writer.write("/joined/" + "\n" + chatRoom + "\n");
				writer.flush();

				nickRefresh("2");




			} catch(Exception e) {e.printStackTrace(); }
=======
				
				// 이걸 왜 넣었지?
//				writer.write("/nick/" + "\n");
//				writer.flush();
				
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
					writer.flush();
				}
//				writer.write("/sendQuery/" + "\n" + nickBank + "\n" + tabName + "\n" + content + "\n"); 
//				writer.flush();
			} catch(Exception e) {e.printStackTrace(); }

>>>>>>> 8c34b7a095b810288a1ad2f9142b1b77203cce4b
		}


	}

	public static void main(String[] args) {
		new chatServer();
	}
}
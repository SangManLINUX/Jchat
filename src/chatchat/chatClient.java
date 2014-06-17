package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.sound.sampled.*;

import java.awt.*;
import java.awt.event.*;


public class chatClient extends JFrame {
	boolean clientOnOff = false; // 탭변경 이벤트에 대응해서 재접속시 오류 잡기 위함.
	boolean onceOn = false; // 한번 켜지면 무조건 true로 창 대응.
	JFrame chatFrame; // 채팅창
	JFrame settingFrame; // 설정창
	JPanel mainPanel; // 채팅창 패널
	JScrollPane qScroller; // 스크롤팬
	JTabbedPane tabPane; // 채팅창 탭팬
	
	String help = "도움:             /help" + "\n"
				+ "닉네임 변경:      /nick <닉네임>" + "\n"
				+ "채널 입장:        /join <#채널명>" + "\n"
				+ "귓속말:           /query <닉네임>" + "\n"
				+ "채널 퇴장:        /exit" + "\n"
				+ "서버 연결해제:    /disconnect" + "\n";
	
	// 가끔씩 tabName에 system이 들어가지 않은 상태로 sendManager에서 조건을 걸기에
	// 오류가 뜬다...(해결)
	String tabName;

	JMenuBar mb; // 메뉴바
	JMenu fileMenu; // 파일 메뉴
	JMenuItem[] menuItem, menuImg, menuThema; // 메뉴아이템
	JFileChooser setCalling = new JFileChooser();	// '열기'창을 위한 JFileChooser.
	File callingFile = new File("Calling.wav");		// 초기 호출음 기본값. 동 폴더 내에 있는 Calling.wav파일.

// 폐기됨
//	ImageIcon bgImage;	// 이미지 파일
//	String bgiRoute;	// 이미지 파일의 경로

//	JTextArea incoming; // 구식의 텍스트영역
						// 나중에 incoming에 해당하는것을 다루려면 tabPane자체를 다루던가
						// 탭이름에 해당하면 컴포넌트를 리턴하는 함수를 만들어야 할지도...

	JTextArea newIncoming;
	JTextField outgoing;

	JLabel[] loginLabel;
	JTextField[] loginTextField;
	JButton[] loginButton;

	// 닉리스트 전용으로 쓸까, 따로 컬렉터를 쓸까 고민중.
	// 닉리스트 보여줄 때 사용하기로 함.
	ArrayList<String> nickList = new ArrayList<String>(); 

	// 닉네임, 닉네임이 들어간 대화방 ArrayList
	HashMap<String, Object> newNickList = new HashMap<String, Object>();

	String[] info = new String[3]; // IP, Port, NickName 순으로 들어감.
	String defaultChatRoom = "system";

	// lista에 키만 가져온 ArrayList를 넣기
	JList lista = new JList(nickList.toArray());

	InputStreamReader streamReader;
	OutputStreamWriter streamWriter;

	BufferedReader reader;
	BufferedWriter writer;

	String[] chatBuffered = new String [5];		// 이전까지 썼던 채팅 메시지를 5개까지 저장하는 공간.
	String nickBuffered;						// 닉네임 자동완성을 위한 문자열 저장.
	
	Socket sock;
	Thread readerThread; // 리더기 스레드

	Font serious = new Font("궁서체", Font.BOLD, 12);
	Font hmrolls = new Font("휴먼굴림체", Font.ITALIC, 12);
	Font nrfonts = new Font("굴림체", Font.BOLD, 12);

	chatClient() {

		createLogin();

		chatFrame = new JFrame("클라이언트");
		chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		createMenu();
		
// 폐기됨		
//		bgImage = new ImageIcon("");						// textArea에 채워넣을 이미지의 경로를 지정하는 부분.
//		incoming = new JTextArea(15, 50);
/*		incoming = new JTextArea() {						// 이미지를 불러들여 textArea에 채워넣는 부분. 여러 문제로 보류.
		      Image image = bgImage.getImage();

		      Image grayImage = GrayFilter.createDisabledImage(image);
		      {
		        setOpaque(false);
		      }

		      public void paint(Graphics g) {
		        g.drawImage(grayImage, 0, 0, this);
		        super.paint(g);
		      }
		    };
*/

		tabPane = new JTabbedPane(JTabbedPane.NORTH);    
		tabPane.addChangeListener(new tabChangeListener()); // 탭변경리스너.

		outgoing = new JTextField(20);
		outgoing.setFocusTraversalKeysEnabled(false);
		outgoing.addKeyListener(new MyKeyListener());
		
		lista.setPreferredSize(new Dimension(120, 100));

		mainPanel.add(tabPane, BorderLayout.CENTER);
		mainPanel.add(outgoing, BorderLayout.SOUTH);
		mainPanel.add(lista, BorderLayout.EAST);

		chatFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		chatFrame.setSize(800, 600);

		chatFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2-400, (Toolkit.getDefaultToolkit().getScreenSize().height)/2-300);
		chatFrame.setVisible(false); // 메인 프레임은 기본적으로 안보인다.
	}

	private void createLogin() {
		settingFrame = new JFrame("접속 설정");
		JPanel oPanel = new JPanel();
		settingFrame.add(oPanel);
		oPanel.setLayout(null);
		// 이걸 그냥 쓰지 닫으면 프로그램이 꺼진다.
//		settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		settingFrame.addWindowListener(new windowListener());

		loginLabel = new JLabel[3];
		String[] textForLabel = {"IP", "PORT", "NICK"};

		loginTextField = new JTextField[3];
		String[] textForTextField = {"localhost", "5000", "defaultNickname"};

		loginButton = new JButton[2];
		String[] textForButton = {"접속", "종료"};

		for( int i = 0; i < 3; i++)
		{
			loginLabel[i] = new JLabel(textForLabel[i]);
			loginLabel[i].setBounds(20, 10+(i*20), 50, 20);
			oPanel.add(loginLabel[i]);

			loginTextField[i] = new JTextField(textForTextField[i]);
			loginTextField[i].setBounds(70, 10+(i*20), 100, 20);
			loginTextField[i].addKeyListener(new LoginKeyListener());
			loginTextField[i].addMouseListener(new LoginMouseListener());			
			oPanel.add(loginTextField[i]);

			if(i != 2)
			{
				loginButton[i] = new JButton(textForButton[i]);
				loginButton[i].setBounds(20+(i*80), 80, 70, 30);
				loginButton[i].addActionListener(new LoginButtonListener());
				oPanel.add(loginButton[i]);
			}
		}
		settingFrame.setSize(200, 150);
		settingFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2-100, (Toolkit.getDefaultToolkit().getScreenSize().height)/2-75);
		settingFrame.setVisible(true); // 설정창은 기본적으로 보여진다.		
	}

	private void createMenu() {
		mb = new JMenuBar();

		fileMenu = new JMenu("메뉴");
		menuItem = new JMenuItem [4];
		String[] itemTitle = {"연결", "연결해제", "설정", "끝내기"};
		for(int i=0; i<menuItem.length; i++) {
			menuItem[i] = new JMenuItem(itemTitle[i]);
			menuItem[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuItem[i]);
		}
		mb.add(fileMenu);

		fileMenu = new JMenu("기능");
		menuImg = new JMenuItem [2];
		String[] imgTitle = {"대화창 청소", "호출음 변경"};
		for(int i=0; i<menuImg.length; i++) {
			menuImg[i] = new JMenuItem(imgTitle[i]);
			menuImg[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuImg[i]);
		}
		mb.add(fileMenu);

		fileMenu = new JMenu("테마");
		menuThema = new JMenuItem [3];
		String[] themaTitle = {"진지한", "굴리는", "눈아픈"};	
		for(int i=0; i<menuThema.length; i++) {
			menuThema[i] = new JMenuItem(themaTitle[i]);
			menuThema[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuThema[i]);
		}
		mb.add(fileMenu);

		chatFrame.setJMenuBar(mb); // 우리는 frame이 최초의 두번째 컴포넌트를 쓴다.

	}

	private void initialSetMyNick() { // 처음 접속 일회용.
		try {
			System.out.println("나의 닉을 맞추는 중 + 기본 대화방 system 맞추기");
			writer.write("/initialNick" + "\n" + info[2] + "\n" + defaultChatRoom + "\n");
			writer.flush();
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}
	}

	private void setQueryJoin(String receivedTabName, String receivedNick, String receivedMessage) {
		String chatRoom;
		try {
			chatRoom = receivedTabName;

			newIncoming = new JTextArea();			
			newIncoming.append(receivedTabName + ": " + receivedMessage + "\n");			
			newIncoming.setLineWrap(true);
			newIncoming.setWrapStyleWord(true);
			newIncoming.setEditable(false);

			qScroller = new JScrollPane(newIncoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			// 탭팬을 새로 만든다.
			tabPane.addTab(chatRoom, qScroller);

			if( info[2].equals(receivedTabName))
			{
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{				
					if(tabPane.getTitleAt(i).equals(chatRoom) )
					{
						tabPane.setSelectedIndex(i);
						break;
					}
				}				
			}

		} catch(Exception e) {e.printStackTrace(); }
	}

	private void setJoin() {
		String chatRoom;
		try {
			chatRoom = reader.readLine();
			tabName = chatRoom; // 대화방의 이름을 넣는다.

			newIncoming = new JTextArea();

			newIncoming.setLineWrap(true);
			newIncoming.setWrapStyleWord(true);
			newIncoming.setEditable(false);
			qScroller = new JScrollPane(newIncoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			// 탭팬을 새로 만든다.
			tabPane.addTab(chatRoom, qScroller);
			
			// 새로 생긴 탭을 선택하는 기능.
			for( int i = 0; i < tabPane.getTabCount(); i++)
			{				
				if(tabPane.getTitleAt(i).equals(chatRoom) )
				{
					tabPane.setSelectedIndex(i);
					break;
				}
			}	

		} catch(Exception e) {e.printStackTrace(); }
	}
	
	private void setUpNetworking() {

		try {

			tabPane.removeAll();

			sock = new Socket(info[0], Integer.valueOf(info[1]));

			streamReader = new InputStreamReader(sock.getInputStream());
			streamWriter = new OutputStreamWriter(sock.getOutputStream()); // OutputStreamWriter도 밖에서 미리 선언
			reader = new BufferedReader(streamReader);
			writer = new BufferedWriter(streamWriter);

			// 서버로부터 오는 리더기 생성.
			readerThread = new Thread(new IncomingReader());
			readerThread.start();

			initialSetMyNick();
			
			chatFrame.setTitle("클라이언트" + "<" + info[2] + ">");

			System.out.println("Established...");

			// 프레임 전환
			settingFrame.setVisible(false);
			chatFrame.setVisible(true);

			menuItem[0].setEnabled(false); // 연결 버튼 비활성화
			menuItem[1].setEnabled(true); //  연결해제 버튼 활성화

			outgoing.setEditable(true);

			clientOnOff = true;
			onceOn = true;

		} catch(IOException e) {e.printStackTrace(); System.out.println("no2"); clientOnOff = false;}
	}

	private void disconnect() {
		JScrollPane sp; // 해당하는 스크롤팬 담을 곳.
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.

		try {
			writer.write("/disconnect/" + "\n");
			writer.flush();
			reader.close();
			sock.close();
			menuItem[0].setEnabled(true);
			menuItem[1].setEnabled(false);
			nickList.clear();

			// 신식 방식(HashMap)의 키만 컬렉터에 넣고			
			Collection<String> coll = newNickList.keySet();

			// 컬렉터를 toarray로 lista에 업데이트
			lista.setListData(coll.toArray());
			lista.repaint();

			outgoing.setEditable(false);

			clientOnOff = false;
			
			if( tabSearcher(tabName) )
			{
				ta = tabDispenser(tabName);
				ta.append("<system> 서버와의 연결이 해제되었습니다." + "\n");
				return;
			}
			
		} catch(Exception e1) { // writer, reader, sock 중 어느것이 죽을때 걸림.
			e1.printStackTrace();
			System.out.println("Client socket down");
		}
	}
	
	private void changeNick()
	{
		String preNick; // 이전 닉
		String postNick; // 이후 닉
		
		JScrollPane sp; // 해당하는 스크롤팬 담을 곳.
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.
		
		try {
			preNick = reader.readLine();
			postNick = reader.readLine();
			
			// 닉 바꾼 본인일때
			if(info[2].equals(preNick))
			{
				info[2] = postNick;
				
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 닉네임 변경 완료." + "\n");
					chatFrame.setTitle("클라이언트" + "<" + info[2] + ">");
				}			
			}
			else if(info[2].endsWith(preNick) == false)
			{
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{				
					if(tabPane.getTitleAt(i).equals(preNick) )
					{
						tabPane.setTitleAt(i, postNick);
					}
				}
			}
			
		} catch(Exception e){ e.printStackTrace(); }
			
	}

	private void sendingManager() {
		String input; // 입력받은거
		String command; // 명령어
		String content; // 내용물
		input = outgoing.getText();
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.

		if(!(input.startsWith("/")) && tabName.equals(defaultChatRoom))
		{
			if( tabSearcher(defaultChatRoom) )
			{
				ta = tabDispenser(defaultChatRoom);
				ta.append("<system> system에서는 대화가 불가능합니다." + "\n");
				return; // 밑에거랑 같은 효과같은데...
			}			

			return; // 밑의 "/" 명령어함수 걸리전에 리턴시킴.
		}
		else if(input.startsWith("/"))
		{
			try {
				command = input.substring(0, input.indexOf((" ")));
				content = input.substring(command.length()+1 ).trim(); // 커맨드 문자자리수 + 1 부터
				System.out.println("1받은 문자열: " + input);
				System.out.println("1앞의 명령어: " + command);
				System.out.println("1내용물: " + content);
			} 
			
			catch(Exception e) 
			{
				System.out.println("이런 content 내용이 없다. /exit인가?");
				command = input.trim();
				content = null;
				System.out.println("2받은 문자열: " + input);
				System.out.println("2앞의 명령어: " + command);
				System.out.println("2내용물: " + content);
			}

		}
		else if(tabName.startsWith("#"))
		{
			command = "/say";
			content = input;
		}
		else if(tabName.startsWith("#") == false) // #으로 시작하지 않으면 쿼리탭이다.
		{
			command = "/sendQuery";
			content = input;
		}
		else
		{
			System.out.println("잘못된 입력.");
			return;
		}

		System.out.println("최종 받은 문자열: " + input);
		System.out.println("최종 앞의 명령어: " + command);
		System.out.println("최종 내용물: " + content);

		if(command.equals("/help"))
		{
			if( tabSearcher(tabName) )
			{
				ta = tabDispenser(tabName);
				ta.append(help);
				return;
			}
		}
		if(command.equals("/join"))
		{
			if(spellChecker(input, command, content))
			{
				try {
					writer.write(command + "\n" + content + "\n");
					writer.flush();
					} catch(Exception e) { e.printStackTrace(); }				
			}			
		}
	
		else if(command.equals("/query"))
		{
			if(spellChecker(input, command, content))
			{
				try {
					writer.write(command + "\n" + content + "\n");
					writer.flush();
				} catch(Exception e) { e.printStackTrace(); }				
			}

		}
		else if(command.equals("/nick"))
		{
			if(spellChecker(input, command, content))
			{
				try {
					writer.write(command + "\n" + content + "\n");
					writer.flush();
				} catch(Exception e) { e.printStackTrace(); }				
			}

		}
		else if(command.equals("/sendQuery"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		else if(command.equals("/say"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }		
		}
		else if(command.equals("/exit"))
		{
			if(tabName.equals(defaultChatRoom))
			{
				disconnect();
				return;
			}
			if(tabName.startsWith("#") == false) {
				exitor();
				return;
			}
			try {
				writer.write(command + "\n" + tabName + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		
		else
		{
			if( tabSearcher(tabName) )
			{
				ta = tabDispenser(tabName);
				ta.append("<system> 잘못된 입력입니다." + "\n");
				return;
			}
						
		}

	}
	
	private void receivingManager()
	{
		String receivedTabName;
		String receivedNick;
		String receivedMessage;
		JScrollPane sp; // 해당하는 스크롤팬 담을 곳.
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.
		boolean tabExist = false;
		boolean sayQuerySwitch = true;

		try {
			receivedTabName = reader.readLine(); // 귓속말시 nickBank가 여기로 들어감.
			receivedNick = reader.readLine();
			receivedMessage = reader.readLine();

			// 참이면 일반 say, 거짓이면 쿼리.
			sayQuerySwitch = receivedTabName.startsWith("#");

			// 귓속말 할때 보내는 닉이 내 클라의 닉과 같은때
			// 내 닉이 쓴것도 내 클라에 떠야하니 조건을 건다.

			// self
			if(sayQuerySwitch == false && info[2].equals(receivedTabName))
			{
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{				
					if(tabPane.getTitleAt(i).equals(receivedNick) )
					{
						System.out.println("본인귓속말");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						// 스크롤팬의 sp의 컴포넌트를 가져와 ta에 넣음.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedTabName + ": " + receivedMessage + "\n");

						tabExist = true; // 탭이 존재한다.
					}
				}	
			}			
			// receiver
			else if(sayQuerySwitch == false && info[2].equals(receivedNick))
			{
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{					
					if(tabPane.getTitleAt(i).equals(receivedTabName))
					{
						System.out.println("받은귓속말");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedTabName + ": " + receivedMessage + "\n");

						tabExist = true; // 탭이 존재한다.
					}
				}
			}
			// 평범한 대화방 채팅
			else if(sayQuerySwitch == true)
			{	

				for( int i = 0; i < tabPane.getTabCount(); i++)
				{
					if(tabPane.getTitleAt(i).equals(receivedTabName))
					{
						System.out.println(receivedTabName + " 대화방채팅");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedNick + ": " + receivedMessage + "\n");

						tabExist = true; // 탭이 존재한다.
					}
				}
				// 호출기능. 메시지가 완전히 '닉네임만'을 보내야 반응하도록 변경.
				// (이전엔 메시지에 포함되기만 해도 호출.)
				// 본인이 본인닉을 언급할때는 제외.
				if(receivedMessage.contains(info[2]) && receivedNick.equals(info[2]) == false ) {	
					try{
						AudioInputStream callSound = AudioSystem.getAudioInputStream(callingFile);
						Clip callClip = AudioSystem.getClip();							
						callClip.open(callSound);
						callClip.start();
					}catch(Exception e){
						System.out.println("Sound Error!!");
					}
				}
			}

			// 존재하지 않는 탭이고 받은 탭이름이 앞에 #이 붙지 않아야한다.
			// 귓속말 보내는 클라이언트한테도 생기는거 보니 닉네임이 다른 조건도 걸어야겠다.
			// 받는 닉이 본인일 때만 조건도 걸어야지.
			if(info[2].equals(receivedTabName) == false && info[2].equals(receivedNick) == true 
					&& receivedTabName.startsWith("#") == false && tabExist == false) 
			{
				System.out.println(receivedTabName + "로 부터 새로운 귓속말 받음");
				setQueryJoin(receivedTabName, receivedNick, receivedMessage);
			}

		} catch(Exception e) { e.printStackTrace(); }

	}

	private void refreshNick(String s) {
		System.out.println("해당 대화방의 닉네임 리스트로 변경중");
		nickList.clear(); // ArrayList 초기화

		Collection<String> collForKey = newNickList.keySet();
		Iterator<String> itForKey = collForKey.iterator();

		while(itForKey.hasNext()) {
			String key;
			key = itForKey.next();
			System.out.println("먼저 닉네임이 " + key + " 일 때");
			// 왠지 이쯤에서 문제 발생.(refreshNick()에서 해결)
			Object o = newNickList.get(key);
			ArrayList<String> arForValue = (ArrayList)o;
			Iterator<String> itForValue = arForValue.iterator();
			while(itForValue.hasNext()) {
				System.out.println("대조시도");
				if(itForValue.next().equals(s))
				{
					System.out.println("해당 대화방명 " + s + " 을 " + key + " 에서 발견");
					nickList.add(key);
					break;
				}
			}
		}

		// ArrayList nickList 정렬
		Collections.sort(nickList);
		
		// 새로 구성한 ArrayList nickList로 새로고침.
		lista.setListData(nickList.toArray());
		lista.repaint();
		
	}

	private void refreshNick() {
		newNickList.clear();
		ArrayList<String> arrayForValue = null;

		String message;
		String keyForNick = null;
		try {
			while( (message = reader.readLine()) != null) {

				if(message.equals("/nick/"))
				{
					System.out.println("닉네임 리스트 꼬리 받음");
					break;
				}
				if(message.equals("/next/"))
				{
					System.out.println("다음 키!");
					newNickList.put(keyForNick, arrayForValue);
					keyForNick = null;
					// arrayForValue.clear()하니 newNickList에 들어가는 오브젝트도 숭당 날라간다.
					// 매번 ArrayList를 새로 생성하니 된다.(해결)
//					arrayForValue.clear();
				}
				if(message.startsWith("/key/"))
				{
					message = reader.readLine();
					System.out.println("닉네임 리스트의 Key: " + message + " 받음");
					keyForNick = message;
					arrayForValue = new ArrayList<String>();
				}
				if(message.startsWith("/value/"))
				{
					message = reader.readLine();
					System.out.println("닉네임의 접속된 대화방: " + message + " 들어옴");
					arrayForValue.add(message);
				}

			} 
		} catch (Exception e) { e.printStackTrace();}

//		refreshNick("system"); // 이걸 쓰면 누가 들어오면 닉리스트가 system으로 되버림.
		refreshNick(tabName);

	}

	private void tabSelector() {
		outgoing.setText("");
		outgoing.requestFocus();
		refreshNick(tabName); // tabName을 매개변수로 보냄. 오버로딩.		
	}

	private void exitor() {
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(tabName) )
			{
				System.out.println("해당탭 제거 중");
				tabPane.removeTabAt(i);
				System.out.println("해당탭 제거 완료");
			}
		}	
	}
	
	private boolean spellChecker(String input, String command, String content) {
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.
	
		if(command.equals("/join"))
		{
			if(content == null)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 대상이 없습니다." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#") == false)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 대화방은 \"#\"으로 시작합니다." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#") && content.length() == 1 )
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 대화방명을 입력하세요." + "\n");
					return false;
				}
			}
			else if(content.contains(" "))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 공백은 불가능합니다." + "\n");
					return false;
				}
			}
			
		}
		else if(command.equals("/query"))
		{
			if(content == null || content.length() == 0)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 대상이 없습니다." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#"))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 대화방에 쿼리할 수 없습니다." + "\n");
					return false;
				}
			}
			else if(content.contains(" "))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 공백은 불가능합니다." + "\n");
					return false;
				}
			}	
		}
		else if(command.equals("/nick"))
		{
			if(content == null || content.length() == 0)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 변경할 닉네임을 입력하세요." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#"))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 닉네임은 #으로 시작할 수 없습니다." + "\n");
					return false;
				}
			}
			else if(content.contains(" "))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> 공백은 불가능합니다." + "\n");
					return false;
				}
			}	
		}

		return true;
		
	}
	
	// 주어진 String과 이름이 같은 탭의 텍스트영역을 반환하는 함수.
	private JTextArea tabDispenser(String s) {
		JScrollPane sp; // 해당하는 스크롤팬 담을 곳.
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(s) )
			{
				System.out.println("해당 탭 보냄");
				sp = (JScrollPane)tabPane.getComponentAt(i);
				ta = (JTextArea)sp.getViewport().getView();
				return ta; // 해당 텍스트영역을 리턴.
			}
		}
		return null; // 생각해봐도 걸릴 이유가 없는 리턴.		
	}
	
	// 탭이 있는지 참,거짓을 반환하는 함수.
	private boolean tabSearcher(String s) {
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(s) )
			{
				System.out.println("해당 탭 탐색됨");
				return true;
			}
		}
		return false;
	}

	public class tabChangeListener implements ChangeListener {
	      public void stateChanged(ChangeEvent c) {
	    	  if(clientOnOff == true)
	    	  {
	    		  JTabbedPane sourceTabbedPane = (JTabbedPane) c.getSource();
	    		  int index = sourceTabbedPane.getSelectedIndex();
	    		  tabName = sourceTabbedPane.getTitleAt(index);
	    		  System.out.println("Tab changed to: " + tabName);

	    		  // 채팅창 변경(대화 기록)과 입력창 초기화, 닉네임 리스트 변경이 필요함.(해결완료)
	    		  tabSelector();
	    	  }
	      }
	}

	public class MenuActionListener implements ActionListener {
		JScrollPane sp; // 해당하는 스크롤팬 담을 곳.
		JTextArea ta; // 해당하는 텍스트영역 담을 곳.
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			if(cmd.equals("연결")) {
				// 연결 상태이면 비활성화이다.
				setUpNetworking();
			}

			if(cmd.equals("연결해제")) {
				// 비연결 상태이면 비활성화이다.
				disconnect();
			}

			if(cmd.equals("설정")) {
				// 채팅프레임은 그대로 두고 설정 띄우기이다.
				// 설정 창 재활성화 시 현재 설정중인 값을 textField에 복원.

				settingFrame.setVisible(true);
				
				for(int i=0; i<3; i++)							
					loginTextField[i].setText(info[i]);

				if(clientOnOff == true)
				{
					loginButton[0].setEnabled(false);
					loginButton[1].setText("닫기");
				}
				else if(clientOnOff == false)
				{
					loginButton[0].setEnabled(true);
					loginButton[1].setText("닫기");
				}
				
			}

			if(cmd.equals("끝내기")) {
				System.exit(0);
			}

			if(cmd.equals("대화창 청소")) {							// 생각나서 넣어본 기능.
				sp = (JScrollPane)tabPane.getComponentAt(tabPane.getSelectedIndex());
				ta = (JTextArea)sp.getViewport().getView();
				ta.setText("");
				ta.append("<SYSTEM> 대화창을 청소하였습니다.\n");
			}

			if(cmd.equals("호출음 변경")) {
				setCalling.setFileFilter(new FileNameExtensionFilter("wav", "wav"));	// 파일 필터.
				setCalling.setMultiSelectionEnabled(false);								// 다중 선택할 수 없음.
				if(setCalling.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)		// 열기 대화 상자를 열고, 확인을 눌렀는지 체크.
					callingFile = setCalling.getSelectedFile();							// 불러올 호출음의 경로에 선택한 파일 경로를 대입.
				System.out.println("<SYSTEM> " + callingFile + "\t호출음 변경됨.");
			}

			if(cmd.equals("진지한")) {
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{	
					sp = (JScrollPane)tabPane.getComponentAt(i);
					ta = (JTextArea)sp.getViewport().getView();
					ta.setBackground(Color.BLACK);
					ta.setForeground(Color.WHITE);
					ta.setFont(serious);
				}
				outgoing.setBackground(Color.BLACK);
				outgoing.setForeground(Color.WHITE);
				outgoing.setFont(serious);
				lista.setBackground(Color.BLACK);
				lista.setForeground(Color.WHITE);
				lista.setFont(serious);
				System.out.println("<SYSTEM> 테마 - 진지한");
			}

			if(cmd.equals("굴리는")) {
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{	
					sp = (JScrollPane)tabPane.getComponentAt(i);
					ta = (JTextArea)sp.getViewport().getView();
					ta.setBackground(Color.LIGHT_GRAY);
					ta.setForeground(Color.darkGray);
					ta.setFont(hmrolls);
				}
				outgoing.setBackground(Color.LIGHT_GRAY);
				outgoing.setForeground(Color.darkGray);
				outgoing.setFont(hmrolls);
				lista.setBackground(Color.LIGHT_GRAY);
				lista.setForeground(Color.darkGray);
				lista.setFont(hmrolls);
				System.out.println("<SYSTEM> 테마 - 굴리는");
			}

			if(cmd.equals("눈아픈")) {
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{	
					sp = (JScrollPane)tabPane.getComponentAt(i);
					ta = (JTextArea)sp.getViewport().getView();
					ta.setBackground(Color.CYAN);
					ta.setForeground(Color.MAGENTA);
					ta.setFont(nrfonts);
				}
				outgoing.setBackground(Color.CYAN);
				outgoing.setForeground(Color.MAGENTA);
				outgoing.setFont(nrfonts);
				lista.setBackground(Color.CYAN);
				lista.setForeground(Color.MAGENTA);
				lista.setFont(nrfonts);
				System.out.println("<SYSTEM> 테마 - 눈아픈");
			}
		
		}
	}

	public class LoginKeyListener extends KeyAdapter {
		boolean toggle = true;

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 10) {
				try
				{
					for(int i = 0; i < 3; i++) {
						info[i] = loginTextField[i].getText();					
					}
					setUpNetworking();
					outgoing.requestFocus();
				} catch (Exception ex) {ex.printStackTrace(); System.out.println("Login Error!!");}
			}
			if(toggle)
			{
				JTextField tf = (JTextField)e.getSource();
				tf.setText("");
				toggle = false;
			}
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

	public class LoginButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			JButton button = (JButton)e.getSource();
			if(button.getText().equals("접속"))
			{
				for(int i = 0; i < 3; i++) {
					info[i] = loginTextField[i].getText();					
				}
				setUpNetworking(); // 생성자에서 여기로 이사함.
				outgoing.requestFocus();
			}
			else if(button.getText().equals("종료"))
			{
				System.exit(0);
			}
			else if(button.getText().equals("닫기"))
			{
				settingFrame.setVisible(false);
			}

		}
	}

	public class MyKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if (keyCode == 10 && outgoing.getText().length() != 0) {
				sendingManager();
				
				// chatBuffered에 '명령어가 아닌' 이전까지의 메시지를 집어넣는 부분.
				if(!(outgoing.getText()).startsWith("/")) {		
					chatBuffered[4] = chatBuffered[3];
					chatBuffered[3] = chatBuffered[2];
					chatBuffered[2] = chatBuffered[1];
					chatBuffered[1] = chatBuffered[0];
					chatBuffered[0] = outgoing.getText();
				}
				
				outgoing.setText("");
				outgoing.requestFocus();
			}
			
			// ↑를 눌렀을 경우 이전까지 보냈던 메시지를 5개까지 자동완성.
			if (keyCode == 38) {
				if((outgoing.getText()).equals(chatBuffered[0]))
						outgoing.setText(chatBuffered[1]);
				else if((outgoing.getText()).equals(chatBuffered[1]))
					outgoing.setText(chatBuffered[2]);
				else if((outgoing.getText()).equals(chatBuffered[2]))
					outgoing.setText(chatBuffered[3]);
				else if((outgoing.getText()).equals(chatBuffered[3]))
					outgoing.setText(chatBuffered[4]);
				else if((outgoing.getText()).equals(chatBuffered[4]))
					outgoing.setText(chatBuffered[0]);
				else
					outgoing.setText(chatBuffered[0]);
			}
			
			// ↓를 눌렀을 경우 ↑의 역순으로 자동완성.
			//  단, ↑키를 한 번이라도 눌러 '이전 메시지'를 불러와야만 작동함.
			//  일부러 else를 제거했기 때문.
			if (keyCode == 40) {
				if((outgoing.getText()).equals(chatBuffered[0]))
					outgoing.setText(chatBuffered[4]);
				else if((outgoing.getText()).equals(chatBuffered[1]))
					outgoing.setText(chatBuffered[0]);
				else if((outgoing.getText()).equals(chatBuffered[2]))
					outgoing.setText(chatBuffered[1]);
				else if((outgoing.getText()).equals(chatBuffered[3]))
					outgoing.setText(chatBuffered[2]);
				else if((outgoing.getText()).equals(chatBuffered[4]))
					outgoing.setText(chatBuffered[3]);
			}	
			
			// 닉네임 앞부분을 입력 후 Tab을 누르면,
			// 해당되는 닉네임 중 가장 마지막에 접속한 사람의 닉네임으로 자동완성.
			if (keyCode == 9) {
				nickBuffered = outgoing.getText();
				if(nickBuffered.isEmpty() == false)
				{
					for(int i=0; i<nickList.size(); i++)
					{
						if( (nickList.get(i) ).startsWith( (nickBuffered) ) )
						{
							outgoing.setText(nickList.get(i));
						}
					}					
				}
			}	
		}
	}
	
	public class windowListener extends WindowAdapter 
	{
        public void windowClosing(WindowEvent evt) {
            Frame frame = (Frame)evt.getSource();
    
            if(onceOn == true)
            {
            	frame.setVisible(false);	
            }
            else if(onceOn == false)
            {
            	System.exit(0);
            }
        }
	}

	public class IncomingReader implements Runnable {
		public void run() {

			String message;
			JTextArea ta; // 해당하는 텍스트영역 담을 곳.

			try {
				while((message = reader.readLine()) != null) {
					if(message.equals("/nick/"))
					{
						System.out.println("닉네임리스트가 온다");
						refreshNick();
						continue;
					}
					if(message.equals("/denied/"))
					{
						System.out.println("사용중인 닉네임입니다.");
						if( tabSearcher(defaultChatRoom) )
						{
							ta = tabDispenser(defaultChatRoom);
							ta.append("<system> 사용중인 닉네임입니다." + "\n");
							break; // 브레이크나 리턴이나...
						}	
						break;
					}
					if(message.equals("/changeNickDenied/"))
					{
						System.out.println("변경하려는 닉네임은 이미 사용중입니다.");
						if( tabSearcher(tabName) )
						{
							ta = tabDispenser(tabName);
							ta.append("<system> 변경하려는 닉네임은 이미 사용중입니다." + "\n");
						}
						continue;
					}
					if(message.equals("/nickChanged/"))
					{
						System.out.println("어느 닉네임 변경됨.");
						changeNick();
						continue;
					}
					if(message.equals("/joined/"))
					{
						System.out.println("새로운 채팅방 접속");
						setJoin();
						continue;
					}
					if(message.equals("/alreadyJoined/"))
					{
						String chatRoom = reader.readLine();
						
						for( int i = 0; i < tabPane.getTabCount(); i++)
						{				
							if(tabPane.getTitleAt(i).equals(chatRoom) )
							{
								tabPane.setSelectedIndex(i);
								break;
							}
						}	
					}
					if(message.equals("/say"))
					{
						System.out.println("메시지 받음");
						receivingManager();
						continue;
					}
					if(message.equals("/sendQuery/"))
					{
						System.out.println("귓속말 받음");
						receivingManager();
						continue;
					}
					if(message.equals("/noNick/"))
					{
						if( tabSearcher(tabName) )
						{
							ta = tabDispenser(tabName);
							ta.append("<system> 존재하지 않는 닉네임입니다." + "\n");
							continue;
						}
					}
					if(message.equals("/exited/"))
					{
						System.out.println("대화방 닫기 받음");
						exitor();
						continue;
					}
					if(message.equals("/serverDown/"))
					{
						System.out.println("서버 내려감.");
						if( tabSearcher(defaultChatRoom) )
						{
							ta = tabDispenser(defaultChatRoom);
							ta.append("<system> 서버가 종료되었습니다." + "\n");
						}
						if( tabSearcher(tabName) && tabPane.getComponentCount() != 1)
						{
							ta = tabDispenser(tabName);
							ta.append("<system> 서버가 종료되었습니다." + "\n");
						}
						sock.close();
						outgoing.setEditable(false);
						menuItem[0].setEnabled(true); // 연결 버튼 비활성화
						menuItem[1].setEnabled(false); //  연결해제 버튼 활성화
						clientOnOff = false;
						
					}

				}
			}
			catch (IOException e) {
				e.printStackTrace(); 
				System.out.println("클라이언트 소켓 닫음."); 
			}
		}
	}

	public static void main(String[] args) {
		new chatClient();
	}
}
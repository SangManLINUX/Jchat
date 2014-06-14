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
	JFrame chatFrame; // 채팅창
	JFrame settingFrame; // 설정창
	JPanel mainPanel; // 채팅창 패널
	JScrollPane qScroller; // 스크롤팬
	JTabbedPane tabPane; // 채팅창 탭팬
	String tabName; // 탭 이름. 이벤트, 전송용.

	JMenuBar mb; // 메뉴바
	JMenu fileMenu; // 파일 메뉴
	JMenuItem[] menuItem, menuImg, menuThema; // 메뉴아이템
	JFileChooser setCalling = new JFileChooser();	// '열기'창을 위한 JFileChooser.
	File callingFile = new File("Calling.wav");		// 초기 호출음 기본값. 동 폴더 내에 있는 Calling.wav파일.

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

	// 닉리스트 전용으로 쓸까, 따로 컬렉터를 쓸까 고민중. 사용해야겠다.
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
		
//		tabPane = createTabbedPane();
		
		tabPane = new JTabbedPane(JTabbedPane.NORTH);    
		tabPane.addChangeListener(new tabChangeListener()); // 탭변경리스너.

		outgoing = new JTextField(20);
		outgoing.addKeyListener(new MyKeyListener());

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
		settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		menuImg = new JMenuItem [3];
		String[] imgTitle = {"대화창 청소", "호출음 변경", "유저 리스트"};
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

	private void setMyNick() { // 처음 접속 일회용.
		try {
			System.out.println("나의 닉을 맞추는 중 + 기본 대화방 system 맞추기");
			writer.write("/nick" + "\n" + info[2] + "\n" + defaultChatRoom + "\n");
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

		} catch(Exception e) {e.printStackTrace(); }
	}
	
	private void setJoin() {
		String chatRoom;
		try {
			chatRoom = reader.readLine();
			
			newIncoming = new JTextArea();
			
			newIncoming.setLineWrap(true);;
			newIncoming.setWrapStyleWord(true);
			newIncoming.setEditable(false);
			qScroller = new JScrollPane(newIncoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			// 탭팬을 새로 만든다.
			tabPane.addTab(chatRoom, qScroller);

		} catch(Exception e) {e.printStackTrace(); }
	}
/*
	private void requestJoin() { // 처음 접속할 때 매개변수 없이
		try {
			writer.write("/join" + "\n" + "defaultChatRoom" + "\n");
			writer.flush();
		} catch(Exception e) {e.printStackTrace();}
	}

	private void requestJoin(String s) { // 나중에 대화방 들어갈 때 매개변수로
		try {
			writer.write("/join" + "\n" + s + "\n");
			writer.flush();
		} catch(Exception e) {e.printStackTrace();}
	}
*/
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

			setMyNick();

			System.out.println("Established...");

			// 프레임 전환
			settingFrame.setVisible(false);
			chatFrame.setVisible(true);

			menuItem[0].setEnabled(false); // 연결 버튼 비활성화
			menuItem[1].setEnabled(true); //  연결해제 버튼 활성화

			outgoing.setEditable(true);
			
			clientOnOff = true;

		} catch(IOException e) {e.printStackTrace(); System.out.println("no2"); clientOnOff = false;}
	}

	private void disconnect() {
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

		} catch(Exception e1) { // writer, reader, sock 중 어느것이 죽을때 걸림.
			e1.printStackTrace();
			System.out.println("Client socket down");
		}
	}
	

	private void sendingManager() {
		String input; // 입력받은거
		String command; // 명령어
		String content; // 내용물
		input = outgoing.getText();
		
		// 여기서 귓속말일 때는 앞에 #이 없으니, 귓속말 탭에서 보낸다면 다르게 보낸다.
		
		if(input.startsWith("/"))
		{
//			command = input.substring(0, input.indexOf((" "))); // 첫번째부터 공백까지
			try {
				command = input.substring(0, input.indexOf((" ")));
				content = input.substring(input.indexOf(" ")).trim();
			} 
			catch(Exception e) 
			{
				System.out.println("이런 content 내용이 없다. /exit인가?");
				command = input.trim();
				content = null;
			}
//			content = input.substring(input.indexOf(" ")).trim(); // 공백부터 뒤에 앞뒤로 공백제거
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
		
		System.out.println("받은 문자열: " + input);
		System.out.println("앞의 명령어: " + command);
		System.out.println("내용물: " + content);

		if(command.startsWith("/join"))
		{
			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }
		}
		else if(command.startsWith("/query"))
		{
			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		else if(command.startsWith("/sendQuery"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		else if(command.startsWith("/say"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }		
		}
		else if(command.startsWith("/exit"))
		{
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
			System.out.println("잘못된 입력입니다.");
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
						// 스크롤팬의 sp의 컴포넌트를 가져와 ta에 넣음.
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
						// 스크롤팬의 sp의 컴포넌트를 가져와 ta에 넣음.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedNick + ": " + receivedMessage + "\n");
						
						tabExist = true; // 탭이 존재한다.
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
		
		// (구식) 쿼리 구상
		// 클라이언트의 tabName에 인덱스를 for문을 돌려서 이름이 receivedTabName과
		// 같은 탭의 컴포넌트(아마스크롤달린 JPanel)에 receivedNick과 receivedMessage를 넣는다.
		
	}

	private void refreshNick(String s) {
		System.out.println("해당 대화방의 닉네임 리스트로 변경중");
		nickList.clear(); // ArrayList 초기화
		
		// newNickList의 value의 ArrayList중에서 대화방명인 String s을 가지는
		// ArrayList인 value의 key(닉네임)를 가져와서 ArrayList에 넣고 repaint로 lista에 새로고침.
		// 그러고보면 매개변수가 없는 refreshNick()에 repaint는 필요없을듯하다.
		// 처음 접속시 system이니까 거기에 이 함수가 걸리도록 하면 될거 같다.
		
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
					// 매번 ArrayList를 새로 생성하니 된다.
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

		refreshNick("system");
		
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
				for(int i=0; i<3; i++)							
					loginTextField[i].setText(info[i]);
				
				settingFrame.setVisible(true);
				settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}

			if(cmd.equals("끝내기")) {
				System.exit(0);
			}

/* 일단 기능이지만 현재 불확실로 주석처리
 * 
			if(cmd.equals("대화창 청소")) {							// 생각나서 넣어본 기능.
				incoming.setText("");
				incoming.append("<SYSTEM> 대화창을 청소하였습니다.\n");
			}

			if(cmd.equals("호출음 변경")) {
				setCalling.setFileFilter(new FileNameExtensionFilter("wav", "wav"));	// 파일 필터.
				setCalling.setMultiSelectionEnabled(false);								// 다중 선택할 수 없음.
				if(setCalling.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)		// 열기 대화 상자를 열고, 확인을 눌렀는지 체크.
					callingFile = setCalling.getSelectedFile();							// 불러올 호출음의 경로에 선택한 파일 경로를 대입.
			}

			if(cmd.equals("진지한")) {
				qScroller.getViewport().setBackground(Color.BLACK);
				incoming.setBackground(Color.BLACK);
				incoming.setForeground(Color.WHITE);
				incoming.setFont(serious);
				outgoing.setBackground(Color.BLACK);
				outgoing.setForeground(Color.WHITE);
				outgoing.setFont(serious);
				lista.setBackground(Color.BLACK);
				lista.setForeground(Color.WHITE);
				lista.setFont(serious);
				incoming.append("<SYSTEM> 테마 - 진지한\n");
			}

			if(cmd.equals("굴리는")) {
				incoming.setBackground(Color.LIGHT_GRAY);
				incoming.setForeground(Color.darkGray);
				incoming.setFont(hmrolls);
				outgoing.setBackground(Color.LIGHT_GRAY);
				outgoing.setForeground(Color.darkGray);
				outgoing.setFont(hmrolls);
				lista.setBackground(Color.LIGHT_GRAY);
				lista.setForeground(Color.darkGray);
				lista.setFont(hmrolls);
				incoming.append("<SYSTEM> 테마 - 굴리는\n");
			}

			if(cmd.equals("눈아픈")) {
				incoming.setBackground(Color.CYAN);
				incoming.setForeground(Color.MAGENTA);
				incoming.setFont(nrfonts);
				outgoing.setBackground(Color.CYAN);
				outgoing.setForeground(Color.MAGENTA);
				outgoing.setFont(nrfonts);
				lista.setBackground(Color.CYAN);
				lista.setForeground(Color.MAGENTA);
				lista.setFont(nrfonts);
				incoming.append("<SYSTEM> 테마 - 눈아픈\n");
			}
*/			
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
			}
			else if(button.getText().equals("종료"))
			{
				System.exit(0);
			}
		}
	}
	
	public class MyKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if (keyCode == 10 && outgoing.getText().length() != 0) {
				sendingManager();
				outgoing.setText("");
				outgoing.requestFocus();
				}
			}
		}
	
	public class IncomingReader implements Runnable {
		public void run() {

			String message;

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
//						incoming.append("<SYSTEM> 사용중인 닉네임입니다.");
						break;
					}
					if(message.equals("/joined/"))
					{
						System.out.println("새로운 채팅방 접속");
						setJoin();
						continue;
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
					if(message.equals("/exited/"))
					{
						System.out.println("대화방 닫기 받음");
						exitor();
						continue;
					}

/* 지금 상태로 작동안할테니 일단 주석처리.
 * 
					if((message.substring(message.indexOf(": "))).contains(info[2])){		// 호출기능.
						try{
							AudioInputStream callSound = AudioSystem.getAudioInputStream(callingFile);
							Clip callClip = AudioSystem.getClip();							
							callClip.open(callSound);
							callClip.start();
						}catch(Exception e){
							e.printStackTrace();
							System.out.println("Sound Error!!");
						}
					}
*/
				}
			}
			catch (IOException e) {
				e.printStackTrace(); System.out.println("no6"); 
			}
		}
	}

	public static void main(String[] args) {
		new chatClient();
	}
}
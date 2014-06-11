package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import sun.audio.*;

public class chatClient extends JFrame {
	JFrame chatFrame; // 채팅창
	JFrame settingFrame; // 설정창
	JPanel mainPanel; // 채팅창 패널
	JScrollPane qScroller; // 스크롤팬

	JMenuBar mb; // 메뉴바
	JMenu fileMenu; // 파일 메뉴
	JMenuItem[] menuItem, menuImg, menuThema; // 메뉴아이템
	
//	ImageIcon bgImage;	// 이미지 파일
//	String bgiRoute;	// 이미지 파일의 경로

	JTextArea incoming;
	JTextField outgoing;

	JLabel[] loginLabel;
	JTextField[] loginTextField;
	JButton[] loginButton;

	ArrayList<String> nickList = new ArrayList<String>();

	String[] info = new String[3]; // IP, Port, NickName 순으로 들어감.

	JList lista = new JList(nickList.toArray()); // 이상함.

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
		incoming = new JTextArea() /*{						// 이미지를 불러들여 textArea에 채워넣는 부분. 여러 문제로 보류.
		      Image image = bgImage.getImage();

		      Image grayImage = GrayFilter.createDisabledImage(image);
		      {
		        setOpaque(false);
		      }

		      public void paint(Graphics g) {
		        g.drawImage(grayImage, 0, 0, this);
		        super.paint(g);
		      }
		    }*/;
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		outgoing = new JTextField(20);
		outgoing.addKeyListener(new MyKeyListener());

		mainPanel.add(qScroller, BorderLayout.CENTER);
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
		String[] imgTitle = {"대화창 청소", "메시지 박스", "유저 리스트"};
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


	private void setMyNick() {
		try {
			System.out.println("나의 닉을 맞추는 중");
			writer.write("/nick/" + "\n" + info[2] + "\n");
			writer.flush();
			/*
			String o = reader.readLine();
			if(o.equals("/ok/"))
			{
				// 그냥 계속 진행하고
			}
			else if(o.equals("/nok/"))
			{
				// 닉네임이 중복된다는 메시지 띄우고, 접속 시도 종료.
			}
			*/
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}
	}

	private void setUpNetworking() {
		/*
		try {
			if(sock.isBound()) {
				System.out.println("이미 접속하기에 연결을 해지합니다.");
				sock.close();
			}
		} catch(Exception e) {e.printStackTrace(); System.out.println("이미 접속했음.");}
		*/
		/*
		if( sock.isConnected()) {
			System.out.println("이미 접속하기에 연결을 해지합니다.");
			try {
				sock.close();
			} catch(Exception e) {e.printStackTrace();}
		}
		*/

		try {

			sock = new Socket(info[0], Integer.valueOf(info[1]));

			streamReader = new InputStreamReader(sock.getInputStream());
			streamWriter = new OutputStreamWriter(sock.getOutputStream()); // OutputStreamWriter도 밖에서 미리 선언
			reader = new BufferedReader(streamReader);
			writer = new BufferedWriter(streamWriter);

//			setMyNick(); //닉관련, 아래로 이사

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

		} catch(IOException e) {e.printStackTrace(); System.out.println("no2");}
	}

	private void disconnect() {
		try {
			writer.write("/disconnect/" + "\n");
			writer.flush();
			reader.close();
//			reader.close();
//			readerThread.interrupt();
			sock.close(); // 소켓을 닫는다 인데 의도대로 안되는듯.
			menuItem[0].setEnabled(true);
			menuItem[1].setEnabled(false);
			nickList.clear();
			lista.setListData(nickList.toArray());
			lista.repaint();
			outgoing.setEditable(false);

		} catch(Exception e1) { // writer, reader, sock 중 어느것이 죽을때 걸림.
			e1.printStackTrace();
			System.out.println("Client socket down");
			/*
			try {
				sock.close();
				System.out.println("Client socket down");
			} catch (Exception e2) {
				System.out.println("예측하지 못한 결과");
			}
			*/
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
				for(int i=0; i<3; i++)							// 설정 창 재활성화 시 현재 설정중인 값을 textField에 복원.
					loginTextField[i].setText(info[i]);
				settingFrame.setVisible(true);
				settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}

			if(cmd.equals("끝내기")) {
				System.exit(0);
			}
			

			if(cmd.equals("대화창 청소")) {							// 생각나서 넣어본 기능.
				incoming.setText("");
				incoming.append("<SYSTEM> 대화창을 청소하였습니다.\n");
			}
			
			if(cmd.equals("메시지 박스")) {
/*				bgImage = new ImageIcon("C:\\Users\\Administrator\\Documents\\chkn.jpg");
			    incoming.validate();
				incoming.repaint();*/
				
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
				try {
					writer.write(outgoing.getText()+"\n");
					writer.flush();
					} catch (Exception ex) {ex.printStackTrace(); System.out.println("no4");}
				outgoing.setText("");
				outgoing.requestFocus();
				}
			}
		}

	public void refreshNick() {
		nickList.clear();

		String message;
		try {
			while( (message = reader.readLine()) != null) {

				if(message.equals("/nick/")) // 기묘함.
				{
					System.out.println("닉네임 리스트 꼬리 받음");
					break;
				}

				System.out.println("닉네임" + message + "들어옴");
				nickList.add(message);
			} 
		} catch (Exception e) { e.printStackTrace();}
		 lista.setListData(nickList.toArray());
		 lista.repaint();

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
//						System.out.println("사용중인 닉네임입니다.");
						incoming.append("<SYSTEM> 사용중인 닉네임입니다.");
						break;
					}
					System.out.println("read " + message);
					incoming.append(message + "\n");
					
					
					if((message.substring(message.indexOf(": "))).contains(info[2])){		// 호출기능.
						try{
							File snd = new File("Calling.wav");								// 호출 시 출력될 사운드 파일.
							FileInputStream fis = new FileInputStream(snd);
							AudioStream as = new AudioStream(fis);
							AudioPlayer.player.start(as);
						}catch(Exception e){
							e.printStackTrace();
							System.out.println("Sound Error!!");
						}
					}
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
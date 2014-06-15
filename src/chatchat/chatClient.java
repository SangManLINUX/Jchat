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
	boolean clientOnOff = false; // �Ǻ��� �̺�Ʈ�� �����ؼ� �����ӽ� ���� ��� ����.
	boolean onceOn = false; // �ѹ� ������ ������ true�� â ����.
	JFrame chatFrame; // ä��â
	JFrame settingFrame; // ����â
	JPanel mainPanel; // ä��â �г�
	JScrollPane qScroller; // ��ũ����
	JTabbedPane tabPane; // ä��â ����
	String tabName; // �� �̸�. �̺�Ʈ, ���ۿ�.

	JMenuBar mb; // �޴���
	JMenu fileMenu; // ���� �޴�
	JMenuItem[] menuItem, menuImg, menuThema; // �޴�������
	JFileChooser setCalling = new JFileChooser();	// '����'â�� ���� JFileChooser.
	File callingFile = new File("Calling.wav");		// �ʱ� ȣ���� �⺻��. �� ���� ���� �ִ� Calling.wav����.

//	ImageIcon bgImage;	// �̹��� ����
//	String bgiRoute;	// �̹��� ������ ���

//	JTextArea incoming; // ������ �ؽ�Ʈ����
						// ���߿� incoming�� �ش��ϴ°��� �ٷ���� tabPane��ü�� �ٷ����
						// ���̸��� �ش��ϸ� ������Ʈ�� �����ϴ� �Լ��� ������ ������...
	JTextArea newIncoming;
	JTextField outgoing;

	JLabel[] loginLabel;
	JTextField[] loginTextField;
	JButton[] loginButton;

	// �и���Ʈ �������� ����, ���� �÷��͸� ���� �����. ����ؾ߰ڴ�.
	ArrayList<String> nickList = new ArrayList<String>(); 

	// �г���, �г����� �� ��ȭ�� ArrayList
	HashMap<String, Object> newNickList = new HashMap<String, Object>();

	String[] info = new String[3]; // IP, Port, NickName ������ ��.
	String defaultChatRoom = "system";

	// lista�� Ű�� ������ ArrayList�� �ֱ�
	JList lista = new JList(nickList.toArray());

	InputStreamReader streamReader;
	OutputStreamWriter streamWriter;

	BufferedReader reader;
	BufferedWriter writer;

	String[] chatBuffered = new String [5];		// �������� ��� ä�� �޽����� 5������ �����ϴ� ����.
	String nickBuffered;						// �г��� �ڵ��ϼ��� ���� ���ڿ� ����.
	
	Socket sock;
	Thread readerThread; // ������ ������

	Font serious = new Font("�ü�ü", Font.BOLD, 12);
	Font hmrolls = new Font("�޸ձ���ü", Font.ITALIC, 12);
	Font nrfonts = new Font("����ü", Font.BOLD, 12);

	chatClient() {

		createLogin();

		chatFrame = new JFrame("Ŭ���̾�Ʈ");
		chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		createMenu();
//		bgImage = new ImageIcon("");						// textArea�� ä������ �̹����� ��θ� �����ϴ� �κ�.

//		incoming = new JTextArea(15, 50);
/*		incoming = new JTextArea() {						// �̹����� �ҷ��鿩 textArea�� ä���ִ� �κ�. ���� ������ ����.
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
		tabPane.addChangeListener(new tabChangeListener()); // �Ǻ��渮����.

		outgoing = new JTextField(20);
		outgoing.setFocusTraversalKeysEnabled(false);
		outgoing.addKeyListener(new MyKeyListener());

		mainPanel.add(tabPane, BorderLayout.CENTER);
		mainPanel.add(outgoing, BorderLayout.SOUTH);
		mainPanel.add(lista, BorderLayout.EAST);

		chatFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		chatFrame.setSize(800, 600);

		chatFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2-400, (Toolkit.getDefaultToolkit().getScreenSize().height)/2-300);
		chatFrame.setVisible(false); // ���� �������� �⺻������ �Ⱥ��δ�.
	}

	private void createLogin() {
		settingFrame = new JFrame("���� ����");
		JPanel oPanel = new JPanel();
		settingFrame.add(oPanel);
		oPanel.setLayout(null);
//		settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		settingFrame.addWindowListener(new windowListener());

		loginLabel = new JLabel[3];
		String[] textForLabel = {"IP", "PORT", "NICK"};

		loginTextField = new JTextField[3];
		String[] textForTextField = {"localhost", "5000", "defaultNickname"};

		loginButton = new JButton[2];
		String[] textForButton = {"����", "����"};

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
		settingFrame.setVisible(true); // ����â�� �⺻������ ��������.		
	}

	private void createMenu() {
		mb = new JMenuBar();

		fileMenu = new JMenu("�޴�");
		menuItem = new JMenuItem [4];
		String[] itemTitle = {"����", "��������", "����", "������"};
		for(int i=0; i<menuItem.length; i++) {
			menuItem[i] = new JMenuItem(itemTitle[i]);
			menuItem[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuItem[i]);
		}
		mb.add(fileMenu);

		fileMenu = new JMenu("���");
		menuImg = new JMenuItem [2];
		String[] imgTitle = {"��ȭâ û��", "ȣ���� ����"};
		for(int i=0; i<menuImg.length; i++) {
			menuImg[i] = new JMenuItem(imgTitle[i]);
			menuImg[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuImg[i]);
		}
		mb.add(fileMenu);

		fileMenu = new JMenu("�׸�");
		menuThema = new JMenuItem [3];
		String[] themaTitle = {"������", "������", "������"};	
		for(int i=0; i<menuThema.length; i++) {
			menuThema[i] = new JMenuItem(themaTitle[i]);
			menuThema[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuThema[i]);
		}
		mb.add(fileMenu);

		chatFrame.setJMenuBar(mb); // �츮�� frame�� ������ �ι�° ������Ʈ�� ����.

	}

	private void initialSetMyNick() { // ó�� ���� ��ȸ��.
		try {
			System.out.println("���� ���� ���ߴ� �� + �⺻ ��ȭ�� system ���߱�");
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

			// ������ ���� �����.
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

			newIncoming = new JTextArea();

			newIncoming.setLineWrap(true);;
			newIncoming.setWrapStyleWord(true);
			newIncoming.setEditable(false);
			qScroller = new JScrollPane(newIncoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			// ������ ���� �����.
			tabPane.addTab(chatRoom, qScroller);
			
			// ���� ���� ���� �����ϴ� ���.
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
			streamWriter = new OutputStreamWriter(sock.getOutputStream()); // OutputStreamWriter�� �ۿ��� �̸� ����
			reader = new BufferedReader(streamReader);
			writer = new BufferedWriter(streamWriter);

			// �����κ��� ���� ������ ����.
			readerThread = new Thread(new IncomingReader());
			readerThread.start();

			initialSetMyNick();

			System.out.println("Established...");

			// ������ ��ȯ
			settingFrame.setVisible(false);
			chatFrame.setVisible(true);

			menuItem[0].setEnabled(false); // ���� ��ư ��Ȱ��ȭ
			menuItem[1].setEnabled(true); //  �������� ��ư Ȱ��ȭ

			outgoing.setEditable(true);

			clientOnOff = true;
			onceOn = true;

		} catch(IOException e) {e.printStackTrace(); System.out.println("no2"); clientOnOff = false;}
	}

	private void disconnect() {
		JScrollPane sp; // �ش��ϴ� ��ũ���� ���� ��.
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.

		try {
			writer.write("/disconnect/" + "\n");
			writer.flush();
			reader.close();
			sock.close();
			menuItem[0].setEnabled(true);
			menuItem[1].setEnabled(false);
			nickList.clear();

			// �Ž� ���(HashMap)�� Ű�� �÷��Ϳ� �ְ�			
			Collection<String> coll = newNickList.keySet();

			// �÷��͸� toarray�� lista�� ������Ʈ
			lista.setListData(coll.toArray());
			lista.repaint();

			outgoing.setEditable(false);

			clientOnOff = false;
			
			if( tabSearcher(tabName) )
			{
				ta = tabDispenser(tabName);
				ta.append("<system> �������� ������ �����Ǿ����ϴ�." + "\n");
				return;
			}

			/*
			for( int i = 0; i < tabPane.getTabCount(); i++)
			{				
				if(tabPane.getTitleAt(i).equals(tabName) )
				{
					sp = (JScrollPane)tabPane.getComponentAt(i);
					// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
					ta = (JTextArea)sp.getViewport().getView();
					ta.append("<system> �������� ������ �����Ǿ����ϴ�." + "\n");
					break;
				}
			}
			*/
			
		} catch(Exception e1) { // writer, reader, sock �� ������� ������ �ɸ�.
			e1.printStackTrace();
			System.out.println("Client socket down");
		}
	}

	private void sendingManager() {
		String input; // �Է¹�����
		String command; // ��ɾ�
		String content; // ���빰
		input = outgoing.getText();
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.

		if(input.startsWith("/") == false && tabName.equals(defaultChatRoom))
		{
			if( tabSearcher(defaultChatRoom) )
			{
				ta = tabDispenser(defaultChatRoom);
				ta.append("<system> system������ ��ȭ�� �Ұ����մϴ�." + "\n");
				return; // �ؿ��Ŷ� ���� ȿ��������...
			}			

			return; // ���� "/" ��ɾ��Լ� �ɸ����� ���Ͻ�Ŵ.
		}
		else if(input.startsWith("/"))
		{
			try {
				command = input.substring(0, input.indexOf((" ")));
				content = input.substring(command.length()+1 ).trim(); // Ŀ�ǵ� �����ڸ��� + 1 ����
				System.out.println("1���� ���ڿ�: " + input);
				System.out.println("1���� ��ɾ�: " + command);
				System.out.println("1���빰: " + content);
			} 
			
			catch(Exception e) 
			{
				System.out.println("�̷� content ������ ����. /exit�ΰ�?");
				command = input.trim();
				content = null;
				System.out.println("2���� ���ڿ�: " + input);
				System.out.println("2���� ��ɾ�: " + command);
				System.out.println("2���빰: " + content);
			}

		}
		else if(tabName.startsWith("#"))
		{
			command = "/say";
			content = input;
		}
		else if(tabName.startsWith("#") == false) // #���� �������� ������ �������̴�.
		{
			command = "/sendQuery";
			content = input;
		}
		else
		{
			System.out.println("�߸��� �Է�.");
			return;
		}

		System.out.println("���� ���� ���ڿ�: " + input);
		System.out.println("���� ���� ��ɾ�: " + command);
		System.out.println("���� ���빰: " + content);

		/*
		if(command.equals("/join"))
		{
			
			if(content == null )
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ��ȭ����� #���� �����ϰ� ������ �ƴϿ����մϴ�.(1)" + "\n");
					return;
				}
				
				/*
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{				
					if(tabPane.getTitleAt(i).equals(tabName))
					{
						System.out.println("��ȭ����� #�� �ƴϰų� ������.");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append("<system> ��ȭ����� #���� �����ϰ� ������ �ƴϿ����մϴ�.(1)" + "\n");
						return;
					}
				}
				
			}
			*/
		
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
			/*
			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
			*/
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
				ta.append("<system> �߸��� �Է��Դϴ�." + "\n");
				return;
			}
			/*
			for( int i = 0; i < tabPane.getTabCount(); i++)
			{				
				if(tabPane.getTitleAt(i).equals(tabName) )
				{
					System.out.println("������� ����");
					sp = (JScrollPane)tabPane.getComponentAt(i);
					// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
					ta = (JTextArea)sp.getViewport().getView();
					ta.append("<system> �߸��� �Է��Դϴ�." + "\n");
					break;
				}
			}
			*/
		}

	}

	private void receivingManager()
	{
		String receivedTabName;
		String receivedNick;
		String receivedMessage;
		JScrollPane sp; // �ش��ϴ� ��ũ���� ���� ��.
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.
		boolean tabExist = false;
		boolean sayQuerySwitch = true;

		try {
			receivedTabName = reader.readLine(); // �ӼӸ��� nickBank�� ����� ��.
			receivedNick = reader.readLine();
			receivedMessage = reader.readLine();

			// ���̸� �Ϲ� say, �����̸� ����.
			sayQuerySwitch = receivedTabName.startsWith("#");

			// �ӼӸ� �Ҷ� ������ ���� �� Ŭ���� �а� ������
			// �� ���� ���͵� �� Ŭ�� �����ϴ� ������ �Ǵ�.

			// self
			if(sayQuerySwitch == false && info[2].equals(receivedTabName))
			{
				for( int i = 0; i < tabPane.getTabCount(); i++)
				{				
					if(tabPane.getTitleAt(i).equals(receivedNick) )
					{
						System.out.println("���αӼӸ�");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedTabName + ": " + receivedMessage + "\n");

						tabExist = true; // ���� �����Ѵ�.
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
						System.out.println("�����ӼӸ�");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedTabName + ": " + receivedMessage + "\n");

						tabExist = true; // ���� �����Ѵ�.
					}
				}
			}
			// ����� ��ȭ�� ä��
			else if(sayQuerySwitch == true)
			{	

				for( int i = 0; i < tabPane.getTabCount(); i++)
				{
					if(tabPane.getTitleAt(i).equals(receivedTabName))
					{
						System.out.println(receivedTabName + " ��ȭ��ä��");
						sp = (JScrollPane)tabPane.getComponentAt(i);
						// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
						ta = (JTextArea)sp.getViewport().getView();
						ta.append(receivedNick + ": " + receivedMessage + "\n");

						tabExist = true; // ���� �����Ѵ�.
					}
				}
				if(receivedMessage.equals(info[2])) {	// ȣ����. �޽����� ������ '�г��Ӹ�'�� ������ �����ϵ��� ����.(������ �޽����� ���ԵǱ⸸ �ص� ȣ��.)
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

			// �������� �ʴ� ���̰� ���� ���̸��� �տ� #�� ���� �ʾƾ��Ѵ�.
			// �ӼӸ� ������ Ŭ���̾�Ʈ���׵� ����°� ���� �г����� �ٸ� ���ǵ� �ɾ�߰ڴ�.
			// �޴� ���� ������ ���� ���ǵ� �ɾ����.
			if(info[2].equals(receivedTabName) == false && info[2].equals(receivedNick) == true 
					&& receivedTabName.startsWith("#") == false && tabExist == false) 
			{
				System.out.println(receivedTabName + "�� ���� ���ο� �ӼӸ� ����");
				setQueryJoin(receivedTabName, receivedNick, receivedMessage);
			}

		} catch(Exception e) { e.printStackTrace(); }

		// (����) ���� ����
		// Ŭ���̾�Ʈ�� tabName�� �ε����� for���� ������ �̸��� receivedTabName��
		// ���� ���� ������Ʈ(�Ƹ���ũ�Ѵ޸� JPanel)�� receivedNick�� receivedMessage�� �ִ´�.

	}

	private void refreshNick(String s) {
		System.out.println("�ش� ��ȭ���� �г��� ����Ʈ�� ������");
		nickList.clear(); // ArrayList �ʱ�ȭ

		// newNickList�� value�� ArrayList�߿��� ��ȭ����� String s�� ������
		// ArrayList�� value�� key(�г���)�� �����ͼ� ArrayList�� �ְ� repaint�� lista�� ���ΰ�ħ.
		// �׷����� �Ű������� ���� refreshNick()�� repaint�� �ʿ�������ϴ�.
		// ó�� ���ӽ� system�̴ϱ� �ű⿡ �� �Լ��� �ɸ����� �ϸ� �ɰ� ����.

		Collection<String> collForKey = newNickList.keySet();
		Iterator<String> itForKey = collForKey.iterator();

		while(itForKey.hasNext()) {
			String key;
			key = itForKey.next();
			System.out.println("���� �г����� " + key + " �� ��");
			// ���� ���뿡�� ���� �߻�.(refreshNick()���� �ذ�)
			Object o = newNickList.get(key);
			ArrayList<String> arForValue = (ArrayList)o;
			Iterator<String> itForValue = arForValue.iterator();
			while(itForValue.hasNext()) {
				System.out.println("�����õ�");
				if(itForValue.next().equals(s))
				{
					System.out.println("�ش� ��ȭ��� " + s + " �� " + key + " ���� �߰�");
					nickList.add(key);
					break;
				}
			}
		}

		// ���� ������ ArrayList nickList�� ���ΰ�ħ.
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
					System.out.println("�г��� ����Ʈ ���� ����");
					break;
				}
				if(message.equals("/next/"))
				{
					System.out.println("���� Ű!");
					newNickList.put(keyForNick, arrayForValue);
					keyForNick = null;
					// arrayForValue.clear()�ϴ� newNickList�� ���� ������Ʈ�� ���� ���󰣴�.
					// �Ź� ArrayList�� ���� �����ϴ� �ȴ�.
//					arrayForValue.clear();
				}
				if(message.startsWith("/key/"))
				{
					message = reader.readLine();
					System.out.println("�г��� ����Ʈ�� Key: " + message + " ����");
					keyForNick = message;
					arrayForValue = new ArrayList<String>();
				}
				if(message.startsWith("/value/"))
				{
					message = reader.readLine();
					System.out.println("�г����� ���ӵ� ��ȭ��: " + message + " ����");
					arrayForValue.add(message);
				}

			} 
		} catch (Exception e) { e.printStackTrace();}

//		refreshNick("system"); // �̰� ���� ���� ������ �и���Ʈ�� system���� �ǹ���.
		refreshNick(tabName);

	}

	private void tabSelector() {
		outgoing.setText("");
		outgoing.requestFocus();
		refreshNick(tabName); // tabName�� �Ű������� ����. �����ε�.		
	}

	private void exitor() {
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(tabName) )
			{
				System.out.println("�ش��� ���� ��");
				tabPane.removeTabAt(i);
				System.out.println("�ش��� ���� �Ϸ�");
			}
		}	
	}
	
	private boolean spellChecker(String input, String command, String content) {
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.
	
		if(command.equals("/join"))
		{
			if(content == null)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ����� �����ϴ�." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#") == false)
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ��ȭ���� \"#\"���� �����մϴ�." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#") && content.length() == 1 )
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ��ȭ����� �Է��ϼ���." + "\n");
					return false;
				}
			}
			else if(content.contains(" "))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ������ �Ұ����մϴ�." + "\n");
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
					ta.append("<system> ����� �����ϴ�." + "\n");
					return false;
				}
			}
			else if(content.startsWith("#"))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ��ȭ�濡 ������ �� �����ϴ�." + "\n");
					return false;
				}
			}
			else if(content.contains(" "))
			{
				if( tabSearcher(tabName) )
				{
					ta = tabDispenser(tabName);
					ta.append("<system> ������ �Ұ����մϴ�." + "\n");
					return false;
				}
			}	
		}
		
		return true;
		
	}
	
	// �־��� String�� �̸��� ���� ���� �ؽ�Ʈ������ ��ȯ�ϴ� �Լ�.
	private JTextArea tabDispenser(String s) {
		JScrollPane sp; // �ش��ϴ� ��ũ���� ���� ��.
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(s) )
			{
				System.out.println("�ش� �� ����");
				sp = (JScrollPane)tabPane.getComponentAt(i);
				// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
				ta = (JTextArea)sp.getViewport().getView();
				return ta; // �ش� �ؽ�Ʈ������ ����.
//				break;
			}
		}
		return null; // �����غ��� �ɸ� ������ ���� ����.		
	}
	
	// ���� �ִ��� ��,������ ��ȯ�ϴ� �Լ�.
	private boolean tabSearcher(String s) {
		
		for( int i = 0; i < tabPane.getTabCount(); i++)
		{				
			if(tabPane.getTitleAt(i).equals(s) )
			{
				System.out.println("�ش� �� Ž����");
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

	    		  // ä��â ����(��ȭ ���)�� �Է�â �ʱ�ȭ, �г��� ����Ʈ ������ �ʿ���.(�ذ�Ϸ�)
	    		  tabSelector();
	    	  }
	      }
	}

	public class MenuActionListener implements ActionListener {
		JScrollPane sp; // �ش��ϴ� ��ũ���� ���� ��.
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			if(cmd.equals("����")) {
				// ���� �����̸� ��Ȱ��ȭ�̴�.
				setUpNetworking();
			}

			if(cmd.equals("��������")) {
				// �񿬰� �����̸� ��Ȱ��ȭ�̴�.
				disconnect();
			}

			if(cmd.equals("����")) {
				// ä���������� �״�� �ΰ� ���� �����̴�.
				// ���� â ��Ȱ��ȭ �� ���� �������� ���� textField�� ����.

				settingFrame.setVisible(true);
				
				for(int i=0; i<3; i++)							
					loginTextField[i].setText(info[i]);

				if(clientOnOff == true)
				{
					loginButton[0].setEnabled(false);
					loginButton[1].setText("�ݱ�");
				}
				else if(clientOnOff == false)
				{
					loginButton[0].setEnabled(true);
				}
				
			}

			if(cmd.equals("������")) {
				System.exit(0);
			}


			if(cmd.equals("��ȭâ û��")) {							// �������� �־ ���.
				sp = (JScrollPane)tabPane.getComponentAt(tabPane.getSelectedIndex());
				ta = (JTextArea)sp.getViewport().getView();
				ta.setText("");
				ta.append("<SYSTEM> ��ȭâ�� û���Ͽ����ϴ�.\n");
			}

			if(cmd.equals("ȣ���� ����")) {
				setCalling.setFileFilter(new FileNameExtensionFilter("wav", "wav"));	// ���� ����.
				setCalling.setMultiSelectionEnabled(false);								// ���� ������ �� ����.
				if(setCalling.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)		// ���� ��ȭ ���ڸ� ����, Ȯ���� �������� üũ.
					callingFile = setCalling.getSelectedFile();							// �ҷ��� ȣ������ ��ο� ������ ���� ��θ� ����.
				System.out.println("<SYSTEM> " + callingFile + "\tȣ���� �����.");
			}

			if(cmd.equals("������")) {
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
				System.out.println("<SYSTEM> �׸� - ������");
			}

			if(cmd.equals("������")) {
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
				System.out.println("<SYSTEM> �׸� - ������");
			}

			if(cmd.equals("������")) {
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
				System.out.println("<SYSTEM> �׸� - ������");
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
			if(button.getText().equals("����"))
			{
				for(int i = 0; i < 3; i++) {
					info[i] = loginTextField[i].getText();					
				}
				setUpNetworking(); // �����ڿ��� ����� �̻���.
				outgoing.requestFocus();
			}
			else if(button.getText().equals("����"))
			{
				System.exit(0);
			}
			else if(button.getText().equals("�ݱ�"))
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
				
				if(!(outgoing.getText()).startsWith("/")) {		// chatBuffered�� '��ɾ �ƴ�' ���������� �޽����� ����ִ� �κ�.
					chatBuffered[4] = chatBuffered[3];
					chatBuffered[3] = chatBuffered[2];
					chatBuffered[2] = chatBuffered[1];
					chatBuffered[1] = chatBuffered[0];
					chatBuffered[0] = outgoing.getText();
				}
				
				outgoing.setText("");
				outgoing.requestFocus();
			}
			
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
			}	// �踦 ������ ��� �������� ���´� �޽����� 5������ �ڵ��ϼ�.
			
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
			}	// �鸦 ������ ��� ���� �������� �ڵ��ϼ�. ��, ��Ű�� �� ���̶� ���� '���� �޽���'�� �ҷ��;߸� �۵���. �Ϻη� else�� �����߱� ����.
			
			if (keyCode == 9) {
				nickBuffered = outgoing.getText();
				if(nickBuffered.isEmpty() == false)
				{
					for(int i=0; i<nickList.size(); i++)
					{
//						if((nickList.get(i)).contains(nickBuffered))
						if( (nickList.get(i) ).startsWith( (nickBuffered) ) )
						{
							outgoing.setText(nickList.get(i));
						}
					}					
				}
			}	// �г��� �պκ��� �Է� �� Tab�� ������, �ش�Ǵ� �г��� �� ���� �������� ������ ����� �г������� �ڵ��ϼ�.
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
            // ������ �����
            //frame.setVisible(false);
    
            // ������ ������ ����
            //frame.dispose();
        }
	}

	public class IncomingReader implements Runnable {
		public void run() {

			String message;

			try {
				while((message = reader.readLine()) != null) {
					if(message.equals("/nick/"))
					{
						System.out.println("�г��Ӹ���Ʈ�� �´�");
						refreshNick();
						continue;
					}
					if(message.equals("/denied/"))
					{
						System.out.println("������� �г����Դϴ�.");
//						incoming.append("<SYSTEM> ������� �г����Դϴ�.");
						break;
					}
					if(message.equals("/joined/"))
					{
						System.out.println("���ο� ä�ù� ����");
						setJoin();
						continue;
					}
					if(message.equals("/say"))
					{
						System.out.println("�޽��� ����");
						receivingManager();
						continue;
					}
					if(message.equals("/sendQuery/"))
					{
						System.out.println("�ӼӸ� ����");
						receivingManager();
						continue;
					}
					if(message.equals("/exited/"))
					{
						System.out.println("��ȭ�� �ݱ� ����");
						exitor();
						continue;
					}

/*					if((message.substring(message.indexOf(": "))).contains(info[2])){		// ȣ����.
						try{
							AudioInputStream callSound = AudioSystem.getAudioInputStream(callingFile);
							Clip callClip = AudioSystem.getClip();							
							callClip.open(callSound);
							callClip.start();
							System.out.println("Calling!!");
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
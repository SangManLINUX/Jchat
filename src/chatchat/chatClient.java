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
		    }*/;
		
/* ���� ����� JTextArea
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
*/
		tabPane = createTabbedPane();
		tabPane.addChangeListener(new tabChangeListener()); // �Ǻ��渮����.
//		tabPane.addMouseListener(new TabActionListener());

		outgoing = new JTextField(20);
		outgoing.addKeyListener(new MyKeyListener());


		mainPanel.add(tabPane, BorderLayout.CENTER);
//		mainPanel.add(qScroller, BorderLayout.CENTER);
		mainPanel.add(outgoing, BorderLayout.SOUTH);
		mainPanel.add(lista, BorderLayout.EAST);

		chatFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		chatFrame.setSize(800, 600);

		chatFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2-400, (Toolkit.getDefaultToolkit().getScreenSize().height)/2-300);
		chatFrame.setVisible(false); // ���� �������� �⺻������ �Ⱥ��δ�.
	}


	JTabbedPane createTabbedPane() {
		JTabbedPane pane = new JTabbedPane(JTabbedPane.NORTH);
//		pane.addTab("System", qScroller);
//		pane.addTab("tab1", new JTextArea());
//		pane.addTab("tab2", new JTextArea());
//		pane.addTab("tab3", new JPanel());
		return pane;	
	}

	private void createLogin() {
		settingFrame = new JFrame("���� ����");
		JPanel oPanel = new JPanel();
		settingFrame.add(oPanel);
		oPanel.setLayout(null);
		settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		menuImg = new JMenuItem [3];
		String[] imgTitle = {"��ȭâ û��", "ȣ���� ����", "���� ����Ʈ"};
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


	private void setMyNick() { // ó�� ���� ��ȸ��.
		try {
			System.out.println("���� ���� ���ߴ� �� + �⺻ ��ȭ�� system ���߱�");
			writer.write("/nick" + "\n" + info[2] + "\n" + defaultChatRoom + "\n");
			writer.flush();
			/*
			String o = reader.readLine();
			if(o.equals("/ok/"))
			{
				// �׳� ��� �����ϰ�
			}
			else if(o.equals("/nok/"))
			{
				// �г����� �ߺ��ȴٴ� �޽��� ����, ���� �õ� ����.
			}
			*/
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}
	}

	private void setQueryJoin(String receivedTabName, String receivedNick, String receivedMessage) {
		String chatRoom;
		try {
			chatRoom = receivedTabName;
			
			newIncoming = new JTextArea();
			
			newIncoming.append(receivedNick + ": " + receivedMessage + "\n");
			
			newIncoming.setLineWrap(true);;
			newIncoming.setWrapStyleWord(true);
			newIncoming.setEditable(false);
			qScroller = new JScrollPane(newIncoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			// ������ ���� �����.
			tabPane.addTab(chatRoom, qScroller);
//			tabPane.addTab(chatRoom, new JTextArea());
			

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
//			tabPane.addTab(chatRoom, new JTextArea());
			
//			incoming.setLineWrap(true);
//			incoming.setWrapStyleWord(true);
//			incoming.setEditable(false);
//			qScroller = new JScrollPane(incoming);
//			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);




		} catch(Exception e) {e.printStackTrace(); }
	}

	private void requestJoin() { // ó�� ������ �� �Ű����� ����
		try {
			writer.write("/join" + "\n" + "defaultChatRoom" + "\n");
			writer.flush();
		} catch(Exception e) {e.printStackTrace();}
	}

	private void requestJoin(String s) { // ���߿� ��ȭ�� �� �� �Ű�������
		try {
			writer.write("/join" + "\n" + s + "\n");
			writer.flush();
		} catch(Exception e) {e.printStackTrace();}
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

			setMyNick();

			System.out.println("Established...");

			// ������ ��ȯ
			settingFrame.setVisible(false);
			chatFrame.setVisible(true);

			menuItem[0].setEnabled(false); // ���� ��ư ��Ȱ��ȭ
			menuItem[1].setEnabled(true); //  �������� ��ư Ȱ��ȭ

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
			
			// �Ž� ���(HashMap�� Ű�� �÷��Ϳ� �ְ�			
			Collection<String> coll = newNickList.keySet();
//			Iterator<String> itForKey = coll.iterator();

			// �÷��͸� toarray�� lista�� ������Ʈ
			lista.setListData(coll.toArray());
			lista.repaint();
			
			// ���� ���
//			lista.setListData(nickList.toArray());
//			lista.repaint();
			outgoing.setEditable(false);
			
			clientOnOff = false;

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
		
		// ���⼭ �ӼӸ��� ���� �տ� #�� ������, �ӼӸ� �ǿ��� �����ٸ� �ٸ��� ������.
		
		if(input.startsWith("/"))
		{
			command = input.substring(0, input.indexOf((" "))); // ù��°���� �������
			content = input.substring(input.indexOf(" ")).trim(); // ������� �ڿ� �յڷ� ��������
		}
		else if(tabName.startsWith("#"))
		{
			command = "/sendQuery";
			content = input;
		}
		else
		{
			command = "/say";
			content = input;
		}

		System.out.println("���� ���ڿ�: " + input);
		System.out.println("���� ��ɾ�: " + command);
		System.out.println("���빰: " + content);

//		if(input.startsWith("/join"))
		if(command.startsWith("/join"))
		{
			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }
		}
//		if(input.startsWith("/say"))
		if(command.startsWith("/query"))
		{
			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		if(command.startsWith("/sendQuery"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
			} catch(Exception e) { e.printStackTrace(); }
		}
		if(command.startsWith("/say"))
		{
			try {
				writer.write(command + "\n" + tabName + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }		
		}
		else
		{
			System.out.println("�߸��� �Է��Դϴ�.");
		}
/*		
		else {
			try {
				writer.write(outgoing.getText()+"\n");
				writer.flush();
				} catch (Exception ex) {ex.printStackTrace(); System.out.println("no4");}

		}
*/

	}
	
	private void receivingManager()
	{
		String receivedTabName;
		String receivedNick;
		String receivedMessage;
		JTabbedPane tp; // �ش��ϴ� ������ ���� ��.
		JScrollPane sp; // �ش��ϴ� ��ũ���� ���� ��.
		JTextArea ta; // �ش��ϴ� �ؽ�Ʈ���� ���� ��.
		
		

		try {
			receivedTabName = reader.readLine(); // �ӼӸ��� nickBank�� ����� ��.
			receivedNick = reader.readLine();
			receivedMessage = reader.readLine();
			
			for( int i = 0; i < tabPane.getTabCount(); i++)
			{
				if(tabPane.getTitleAt(i).equals(receivedTabName))
				{
					System.out.println(receivedTabName + " �� �ش��ϴ� �� �߰�");
//					tp = tabPane.getComponentAt(i);
					sp = (JScrollPane)tabPane.getComponentAt(i);
					// ��ũ������ sp�� ������Ʈ�� ������ ta�� ����.
					ta = (JTextArea)sp.getViewport().getView();
					//ta = (JTextArea)sp.getComponent(0);
					ta.append(receivedNick + ": " + receivedMessage + "\n");
				}
				else
				{
					System.out.println(receivedTabName + "�� ���� ���ο� �ӼӸ� ����");
					setQueryJoin(receivedTabName, receivedNick, receivedMessage);
					
				}
			}
//			tabPane.getTabCount();
			
//			tabPane.getTitleAt(index);
			
			//  tabName = sourceTabbedPane.getTitleAt(index);
			
			
			
			
			
		} catch(Exception e) { e.printStackTrace(); }
		
		// Ŭ���̾�Ʈ�� tabName�� �ε����� for���� ������ �̸��� receivedTabName��
		// ���� ���� ������Ʈ(�Ƹ���ũ�Ѵ޸� JPanel)�� receivedNick�� receivedMessage�� �ִ´�.
		
		//writer.write("/say" + "\n" + tabName + "\n" + nickBank + "\n" + message + "\n");
		
	}

/* ���� ���� ä�� ���� �Լ�.	
	private void sendingManager() {
		String input; // �Է¹�����
		String command; // ��ɾ�
		String content; // ���빰
		input = outgoing.getText();

//		if(command.startsWith("/join"))
		if(input.startsWith("/join"))
		{
			command = input.substring(0, input.indexOf((" "))); // ù��°���� �������
			content = input.substring(input.indexOf(" ")).trim(); // ������� �ڿ� �յڷ� ��������

//			content = command.substring(command.indexOf(" "));
//			content = content.trim();

			System.out.println("���� ���ڿ�: " + input);
			System.out.println("���� ��ɾ�: " + command);
			System.out.println("���빰: " + content);

//			System.out.println("���� ���ڿ�: " + command);
//			System.out.println("���� ��ɾ�: " + command.substring(1, command.indexOf(" ")-1 ) );
//			System.out.println("���빰: " + content); // content Ȯ�ο�

			try {
				writer.write(command + "\n" + content + "\n");
				writer.flush();
				} catch(Exception e) { e.printStackTrace(); }
		}

//			if((message.substring(message.indexOf(": "))).contains(info[2])){	
		else {
			try {
				writer.write(outgoing.getText()+"\n");
				writer.flush();
				} catch (Exception ex) {ex.printStackTrace(); System.out.println("no4");}

		}

	}
*/

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
//			ArrayList<String> arForValue = (ArrayList)newNickList.get(key);
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
		
//		Collection<String> coll = newNickList.keySet();
//		Iterator<String> itForKey = coll.iterator();

		// ���� ������ ArrayList nickList�� ���ΰ�ħ.
		lista.setListData(nickList.toArray());
		lista.repaint();
		
	}
	
	private void refreshNick() {
		newNickList.clear();
//		ArrayList<String> arrayForValue = new ArrayList<String>();
		ArrayList<String> arrayForValue = null;

		String message;
		String keyForNick = null;
		try {
			while( (message = reader.readLine()) != null) {

				if(message.equals("/nick/")) // �⹦��.
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

				// ���� ���⼭ #�� ���� ������ ����. �г����ε�.. �����ʿ�
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

//					newNickList.put(message, value)
				}

			} 
		} catch (Exception e) { e.printStackTrace();}

		refreshNick("system");
		
/* ���ο� refreshNick(String s)�� ���⿡ repaint�� �ʿ����.
		Collection<String> coll = newNickList.keySet();
//		Iterator<String> itForKey = coll.iterator();

		lista.setListData(coll.toArray());
		lista.repaint();

//		lista.setListData(nickList.toArray());
//		lista.repaint();
*/

	}
/* ���� ä�� ���� �Լ�	
	public void refreshNick() {
		nickList.clear();

		String message;
		try {
			while( (message = reader.readLine()) != null) {

				if(message.equals("/nick/")) // �⹦��.
				{
					System.out.println("�г��� ����Ʈ ���� ����");
					break;
				}

				System.out.println("�г���" + message + "����");
				nickList.add(message);
			} 
		} catch (Exception e) { e.printStackTrace();}
		 lista.setListData(nickList.toArray());
		 lista.repaint();

	}
*/

	private void tabSelector() {
		outgoing.setText("");
		outgoing.requestFocus();
		refreshNick(tabName); // tabName�� �Ű������� ����. �����ε�.
		
		
	}
	
	public class tabChangeListener implements ChangeListener {
	      public void stateChanged(ChangeEvent c) {
	    	  if(clientOnOff == true)
	    	  {
	    		  JTabbedPane sourceTabbedPane = (JTabbedPane) c.getSource();
	    		  int index = sourceTabbedPane.getSelectedIndex();
	    		  tabName = sourceTabbedPane.getTitleAt(index);
	    		  System.out.println("Tab changed to: " + tabName);
	    		  tabSelector();
  	        // ä��â ����(��ȭ ���)�� �Է�â �ʱ�ȭ, �г��� ����Ʈ ������ �ʿ���.
    		  
	    	  }
	     
	        
	      }
	}

	public class MenuActionListener implements ActionListener {
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
				for(int i=0; i<3; i++)							// ���� â ��Ȱ��ȭ �� ���� �������� ���� textField�� ����.
					loginTextField[i].setText(info[i]);
				settingFrame.setVisible(true);
				settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}

			if(cmd.equals("������")) {
				System.exit(0);
			}

/*
			if(cmd.equals("��ȭâ û��")) {							// �������� �־ ���.
				incoming.setText("");
				incoming.append("<SYSTEM> ��ȭâ�� û���Ͽ����ϴ�.\n");
			}

			if(cmd.equals("ȣ���� ����")) {
				setCalling.setFileFilter(new FileNameExtensionFilter("wav", "wav"));	// ���� ����.
				setCalling.setMultiSelectionEnabled(false);								// ���� ������ �� ����.
				if(setCalling.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)		// ���� ��ȭ ���ڸ� ����, Ȯ���� �������� üũ.
					callingFile = setCalling.getSelectedFile();							// �ҷ��� ȣ������ ��ο� ������ ���� ��θ� ����.
			}

			if(cmd.equals("������")) {
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
				incoming.append("<SYSTEM> �׸� - ������\n");
			}

			if(cmd.equals("������")) {
				incoming.setBackground(Color.LIGHT_GRAY);
				incoming.setForeground(Color.darkGray);
				incoming.setFont(hmrolls);
				outgoing.setBackground(Color.LIGHT_GRAY);
				outgoing.setForeground(Color.darkGray);
				outgoing.setFont(hmrolls);
				lista.setBackground(Color.LIGHT_GRAY);
				lista.setForeground(Color.darkGray);
				lista.setFont(hmrolls);
				incoming.append("<SYSTEM> �׸� - ������\n");
			}

			if(cmd.equals("������")) {
				incoming.setBackground(Color.CYAN);
				incoming.setForeground(Color.MAGENTA);
				incoming.setFont(nrfonts);
				outgoing.setBackground(Color.CYAN);
				outgoing.setForeground(Color.MAGENTA);
				outgoing.setFont(nrfonts);
				lista.setBackground(Color.CYAN);
				lista.setForeground(Color.MAGENTA);
				lista.setFont(nrfonts);
				incoming.append("<SYSTEM> �׸� - ������\n");
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
			if(button.getText().equals("����"))
			{
				for(int i = 0; i < 3; i++) {
					info[i] = loginTextField[i].getText();					
				}
				setUpNetworking(); // �����ڿ��� ����� �̻���.
			}
			else if(button.getText().equals("����"))
			{
				System.exit(0);
			}
		}
	}
/*
	public class MyKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if (keyCode == 10 && outgoing.getText().length() != 0) {
				try {
					sendingManager();
//					writer.write(outgoing.getText()+"\n");
//					writer.flush();
					} catch (Exception ex) {ex.printStackTrace(); System.out.println("no4");}
				outgoing.setText("");
				outgoing.requestFocus();
				}
			}
		}
*/
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
					}
//					System.out.println("read " + message);
//					incoming.append(message + "\n");					

/* ���� ���·� �۵������״� �ϴ� �ּ�ó��.
					if((message.substring(message.indexOf(": "))).contains(info[2])){		// ȣ����.
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
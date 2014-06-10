package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class chatClient extends JFrame {
	
	JFrame chatFrame; // ä��â
	JFrame settingFrame; // ����â
	JPanel mainPanel; // ä��â �г�
	
	JMenuBar mb; // �޴���
	JMenu fileMenu; // ���� �޴�
	JMenuItem[] menuItem; // �޴�������
	
	JTextArea incoming;
	JTextField outgoing;
	
	JLabel[] loginLabel;
	JTextField[] loginTextField;
	JButton[] loginButton;
	
	ArrayList<String> nickList = new ArrayList<String>();
	
	String[] info = new String[3]; // IP, Port, NickName ������ ��.
		
	JList lista = new JList(nickList.toArray()); // �̻���.
		
	InputStreamReader streamReader;
	OutputStreamWriter streamWriter;

	BufferedReader reader;
	BufferedWriter writer;

	Socket sock;
	Thread readerThread; // ������ ������
	
	chatClient() {
		
		createLogin();
		
		chatFrame = new JFrame("Ŭ���̾�Ʈ");
		chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		createMenu();

		incoming = new JTextArea(15, 50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		outgoing = new JTextField(20);
		outgoing.addKeyListener(new MyKeyListener());
		JButton sendButton = new JButton("����");
		sendButton.addActionListener(new SendButtonListener());
		
		mainPanel.add(qScroller, BorderLayout.CENTER);
		mainPanel.add(outgoing, BorderLayout.SOUTH);
//		mainPanel.add(sendButton, BorderLayout.NORTH);
		mainPanel.add(lista, BorderLayout.EAST);
		
		
//		setUpNetworking(); // ���� ��ư���� �Űܾ� �Ѵ�.
//		�����尡 �̻簡 �ʿ��ϴ�.
//		Thread readerThread = new Thread(new IncomingReader());
//		readerThread.start();
		
		chatFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		chatFrame.setSize(800, 600);
		
//		frame.setVisible(true);
		chatFrame.setVisible(false); // ���� �������� �⺻������ �Ⱥ��δ�.
	}
	
	private void createLogin() {
		settingFrame = new JFrame("����");
		JPanel oPanel = new JPanel();
		settingFrame.add(oPanel);
		oPanel.setLayout(null);
		settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
//		JLabel[] loginLabel = new JLabel[3];
		loginLabel = new JLabel[3];
		String[] textForLabel = {"IP", "PORT", "NICK"};
		
//		JTextField[] textField = new JTextField[3];
		loginTextField = new JTextField[3];
		String[] textForTextField = {"localhost", "5000", "defaultNickname"};

//		JButton[] loginButton = new JButton[2];
		loginButton = new JButton[2];
		String[] textForButton = {"����", "����"};
		
		for( int i = 0; i < 3; i++)
		{
			loginLabel[i] = new JLabel(textForLabel[i]);
			loginLabel[i].setBounds(10, 10+(i*20), 50, 20);
			oPanel.add(loginLabel[i]);
			
			loginTextField[i] = new JTextField(textForTextField[i]);
			loginTextField[i].setBounds(60, 10+(i*20), 100, 20);
			loginTextField[i].addKeyListener(new LoginKeyListener());
			loginTextField[i].addMouseListener(new LoginMouseListener());			
			oPanel.add(loginTextField[i]);
			
			if(i != 2)
			{
				loginButton[i] = new JButton(textForButton[i]);
				loginButton[i].setBounds(10+(i*110), 80, 80, 30);
				loginButton[i].addActionListener(new LoginButtonListener());
				oPanel.add(loginButton[i]);
			}	
			
		}
/* ��° JTextField�� ���� �� �����´�. Final�� �ƴϷ���?
 * ���� �˰� �� ���ε�, textField[]�� ���������� �����ϱ� ���⼭�� �ȴ�.
 * ���� ������� �̺�Ʈ ������� ���� ��� �����ε� ���.
		button[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

//				String s = textField[0].getText();
				
				for(int i = 0; i < 3; i++) {
//					info[i]
//					info[i] = textField[i].getText();					
				}
			}
		});
*/		
		settingFrame.setSize(320, 240);
		settingFrame.setVisible(true); // ����â�� �⺻������ ��������.		
	}
	
	private void setClient() {
		
	}
	
	
	private void createMenu() {
		
		mb = new JMenuBar();
		
		menuItem = new JMenuItem [4];
		String[] itemTitle = {"����", "��������", "����", "������"};
		
		fileMenu = new JMenu("�޴�");
		for(int i=0; i<menuItem.length; i++) {
			menuItem[i] = new JMenuItem(itemTitle[i]);
			menuItem[i].addActionListener(new MenuActionListener());
			fileMenu.add(menuItem[i]);
		}
		mb.add(fileMenu);
		chatFrame.setJMenuBar(mb); // �츮�� frame�� ������ �ι�° ������Ʈ�� ����.


	}
	
	
	private void setMyNick() {
		try {
			System.out.println("���� ���� ���ߴ� ��");
			writer.write("/nick/" + "\n" + info[2] + "\n");
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

	private void setUpNetworking() {
		/*
		try {
			if(sock.isBound()) {
				System.out.println("�̹� �����ϱ⿡ ������ �����մϴ�.");
				sock.close();
			}
		} catch(Exception e) {e.printStackTrace(); System.out.println("�̹� ��������.");}
		*/
		/*
		if( sock.isConnected()) {
			System.out.println("�̹� �����ϱ⿡ ������ �����մϴ�.");
			try {
				sock.close();
			} catch(Exception e) {e.printStackTrace();}
		}
		*/
			
		try {
			
			sock = new Socket(info[0], Integer.valueOf(info[1]));
			
			streamReader = new InputStreamReader(sock.getInputStream());
			streamWriter = new OutputStreamWriter(sock.getOutputStream()); // OutputStreamWriter�� �ۿ��� �̸� ����
			reader = new BufferedReader(streamReader);
			writer = new BufferedWriter(streamWriter);
			
//			setMyNick(); //�а���, �Ʒ��� �̻�
			
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
			
		} catch(IOException e) {e.printStackTrace(); System.out.println("no2");}
	}
	
	private void disconnect() {
		try {
			writer.write("/disconnect/" + "\n");
			writer.flush();
			reader.close();
//			reader.close();
//			readerThread.interrupt();
			sock.close(); // ������ �ݴ´� �ε� �ǵ���� �ȵǴµ�.
			menuItem[0].setEnabled(true);
			menuItem[1].setEnabled(false);
			nickList.clear();
			lista.setListData(nickList.toArray());
			lista.repaint();
			outgoing.setEditable(false);
			
		} catch(Exception e1) { // writer, reader, sock �� ������� ������ �ɸ�.
			e1.printStackTrace();
			System.out.println("Client socket down");
			/*
			try {
				sock.close();
				System.out.println("Client socket down");
			} catch (Exception e2) {
				System.out.println("�������� ���� ���");
			}
			*/
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
				settingFrame.setVisible(true);
//				chatFrame.setVisible(true);			}
			}
		
			if(cmd.equals("������")) {
				System.exit(0);
			}
				
		}
	}
	
	public class LoginKeyListener extends KeyAdapter {
		boolean toggle = true;
		
		public void keyPressed(KeyEvent e) {	
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
	
	
	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if ( outgoing.getText().length() != 0) {
				try {
					writer.write(outgoing.getText()+"\n");
					writer.flush();
					} catch(Exception ex) {ex.printStackTrace(); System.out.println("no3");}
				outgoing.setText("");
				outgoing.requestFocus();
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
//						System.out.println("������� �г����Դϴ�.");
						incoming.append("<SYSTEM> ������� �г����Դϴ�.");
						break;
					}
					System.out.println("read " + message);
					incoming.append(message + "\n");
				}
			}
			/*
			catch (InterruptedException ie) {
				return; // run() �޼ҵ带 �����Ͽ� �����带 ������ ����
			}
			*/
			catch (IOException e) {
				e.printStackTrace(); System.out.println("no6"); 
//				return; 
				}
		}
	}
	
	public static void main(String[] args) {
		new chatClient();
	}
}

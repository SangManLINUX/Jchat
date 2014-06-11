package chatchat;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import sun.audio.*;

public class chatClient extends JFrame {
	JFrame chatFrame; // ä��â
	JFrame settingFrame; // ����â
	JPanel mainPanel; // ä��â �г�
	JScrollPane qScroller; // ��ũ����

	JMenuBar mb; // �޴���
	JMenu fileMenu; // ���� �޴�
	JMenuItem[] menuItem, menuImg, menuThema; // �޴�������
	
//	ImageIcon bgImage;	// �̹��� ����
//	String bgiRoute;	// �̹��� ������ ���

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
		incoming = new JTextArea() /*{						// �̹����� �ҷ��鿩 textArea�� ä���ִ� �κ�. ���� ������ ����.
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
		chatFrame.setVisible(false); // ���� �������� �⺻������ �Ⱥ��δ�.
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
		String[] imgTitle = {"��ȭâ û��", "�޽��� �ڽ�", "���� ����Ʈ"};
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
				for(int i=0; i<3; i++)							// ���� â ��Ȱ��ȭ �� ���� �������� ���� textField�� ����.
					loginTextField[i].setText(info[i]);
				settingFrame.setVisible(true);
				settingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}

			if(cmd.equals("������")) {
				System.exit(0);
			}
			

			if(cmd.equals("��ȭâ û��")) {							// �������� �־ ���.
				incoming.setText("");
				incoming.append("<SYSTEM> ��ȭâ�� û���Ͽ����ϴ�.\n");
			}
			
			if(cmd.equals("�޽��� �ڽ�")) {
/*				bgImage = new ImageIcon("C:\\Users\\Administrator\\Documents\\chkn.jpg");
			    incoming.validate();
				incoming.repaint();*/
				
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
					
					
					if((message.substring(message.indexOf(": "))).contains(info[2])){		// ȣ����.
						try{
							File snd = new File("Calling.wav");								// ȣ�� �� ��µ� ���� ����.
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
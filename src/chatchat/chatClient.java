package chatchat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class chatClient extends JFrame {
	JTextArea incoming;
	JTextField outgoing;
	
	ArrayList<String> ar = new ArrayList<String>();
	
	String myNick = "WhoMe"; // 이상함.
	JList lista = new JList(ar.toArray()); // 이상함.
	
	String[] user = { "A", "B", "C" };
	JList listb =  new JList(user);
	
	InputStreamReader streamReader;
	OutputStreamWriter streamWriter;

	BufferedReader reader;
	BufferedWriter writer;

	Socket sock;
	
	chatClient() {
		JFrame frame = new JFrame("클라이언트");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		incoming = new JTextArea(15, 50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoing = new JTextField(20);
		outgoing.addKeyListener(new MyKeyListener());
		JButton sendButton = new JButton("전송");
		sendButton.addActionListener(new SendButtonListener());
		mainPanel.add(qScroller);
		mainPanel.add(outgoing);
		mainPanel.add(sendButton);
		
		// 리스트 고민 지역.
		mainPanel.add(lista);
		mainPanel.add(listb);
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	
	private void setMyNick() {
		try {
			System.out.println("나의 닉을 맞추는 중");
			writer.write(myNick);
			writer.flush();
			} catch(Exception e) {e.printStackTrace(); System.out.println("no1");}		
	}

	private void setUpNetworking() {
		try {
			sock = new Socket("localhost", 5000);
			streamReader = new InputStreamReader(sock.getInputStream());
			streamWriter = new OutputStreamWriter(sock.getOutputStream()); // OutputStreamWriter도 밖에서 미리 선언
			reader = new BufferedReader(streamReader);
			writer = new BufferedWriter(streamWriter);
			
			setMyNick(); //닉관련
			
			System.out.println("Established...");
		} catch(IOException e) {e.printStackTrace(); System.out.println("no2");}
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
	
	public class IncomingReader implements Runnable {
		public void run() {

			String message;
			
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					incoming.append(message + "\n");
				}
			} catch (Exception e) {e.printStackTrace(); System.out.println("no6");}
		}
	}
	
	public static void main(String[] args) {
		new chatClient();
	}
}

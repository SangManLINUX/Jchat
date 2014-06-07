package chatchat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class chatClient extends JFrame {
	JTextArea incoming;
	JTextField outgoing;
	
	ArrayList<String> nickList = new ArrayList<String>();
//	ArrayList nickList = new ArrayList();
	
	String myNick = "WhoMe"; // 이상함.
	
//	DefaultListModel info = new DefaultListModel();
//	JList lista = new JList(info);
	
	JList lista = new JList(nickList.toArray()); // 이상함.

	
//	String[] user = { "A", "B", "C" };
//	JList listb =  new JList(user);
	ArrayList<String> testList = new ArrayList<String>();
	JList listb =  new JList(testList.toArray());
	
	
	InputStreamReader streamReader;
	OutputStreamWriter streamWriter;

	BufferedReader reader;
	BufferedWriter writer;

	Socket sock;
	
	chatClient() {
		testList.add("test1");
		testList.add("Test2");
		testList.add("tests");
		listb =  new JList(testList.toArray()); // 이건 작동되네.
		
//		info.addElement("new list entry");
		
		JFrame frame = new JFrame("클라이언트");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setDefaultCloseOperation(nickList.remove(myNick));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
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
		mainPanel.add(qScroller, BorderLayout.WEST);
		mainPanel.add(outgoing, BorderLayout.CENTER);
		mainPanel.add(sendButton, BorderLayout.EAST);
		
//		lista.setVisible(true);
		// 리스트 고민 지역.
		mainPanel.add(lista, BorderLayout.SOUTH);
		mainPanel.add(listb, BorderLayout.NORTH);
		
		
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
			writer.write("/nick/" + "\n" + myNick + "\n");
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
			/*
			while(true)
			{
				if( sock.isClosed() )
				{
					nickList.remove(myNick);
				}
			}
			*/
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
	
	public void refreshNick() { // 문제 많은 함수.
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
		 
		 
//		 lista = new JList(nickList.toArray()); // 작동을 안함. ArrayList 가공이 필요한듯.
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

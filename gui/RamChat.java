package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RamChat extends JFrame {
	private JPanel chatPanel = new JPanel();
	private JTextArea chatText = new JTextArea(6, 12);
	private JTextArea sendText = new JTextArea(4, 12);
	private JPanel serverPanel = new JPanel();
	private JRadioButton serverBtn = new JRadioButton("Server side");
	private JLabel listenLabel = new JLabel("Listening port:");
	private JTextField listeningPortText = new JTextField(5);
	private JPanel listenPanel = new JPanel();
	private JPanel clientPanel = new JPanel();
	private JRadioButton clientBtn = new JRadioButton("Client side");
	private JLabel hostLabel = new JLabel("Host:");
	private JTextField hostText = new JTextField(11);
	private JLabel portLabel = new JLabel("Port:");
	private JTextField portText = new JTextField(5);
	private JPanel hostPanel = new JPanel();
	private ButtonGroup btnGroup = new ButtonGroup();
	private JPanel modePanel = new JPanel();
	private JButton sendBtn = new JButton("Send");
	private JPanel bottomPanel = new JPanel();
	private JButton applyBtn = new JButton("Apply");
	private JButton disposeBtn = new JButton("Dispose");
	
	private boolean serverRunning = false;
	private boolean clientRunning = false;
	private PrintWriter writer;
	private BufferedReader reader;
	private ServerSocket serverSocket;
	private Socket socket;
	
	public RamChat() {
		chatPanel.setLayout(new BorderLayout());
		chatText.setLineWrap(true);
		chatText.setWrapStyleWord(true);
		chatText.setEditable(false);
		sendText.setLineWrap(true);
		sendText.setWrapStyleWord(true);
		chatPanel.add(new JScrollPane(chatText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		chatPanel.add(new JScrollPane(sendText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.SOUTH);
		
		listeningPortText.setEditable(false);
		listenPanel.add(listenLabel);
		listenPanel.add(listeningPortText);
		serverPanel.add(serverBtn);
		serverPanel.add(listenPanel);
		
		hostText.setEditable(false);
		portText.setEditable(false);
		hostPanel.add(hostLabel);
		hostPanel.add(hostText);
		hostPanel.add(portLabel);
		hostPanel.add(portText);
		clientPanel.add(clientBtn);
		clientPanel.add(hostPanel);
		
		btnGroup.add(serverBtn);
		btnGroup.add(clientBtn);
		
		modePanel.add(serverPanel);
		modePanel.add(clientPanel);
		
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(modePanel, BorderLayout.CENTER);
		bottomPanel.add(sendBtn, BorderLayout.NORTH);
		
		modePanel.add(applyBtn);
		modePanel.add(disposeBtn);
		
		serverBtn.addActionListener(e -> {
			listeningPortText.setEditable(true);
			hostText.setEditable(false);
			portText.setEditable(false);
		});
		
		clientBtn.addActionListener(e -> {
			listeningPortText.setEditable(false);
			hostText.setEditable(true);
			portText.setEditable(true);
		});
		
		applyBtn.addActionListener(e -> {
			if (serverBtn.isSelected()) {
				serverRunning = true;
				clientRunning = false;
				new Thread(() -> {
					try {
						serverSocket = new ServerSocket(Integer.parseInt(listeningPortText.getText()));
						socket = serverSocket.accept();
						writer = new PrintWriter(socket.getOutputStream(), true);
						reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
						try {
							while (serverRunning) {
								if (!reader.ready()) {
									Thread.sleep(200);
									continue;
								}
								
								StringBuilder in = new StringBuilder();
								while (reader.ready()) {
									in.append(reader.readLine());
									in.append("\n");
								}
								
								RamChat.this.writeToChatBox(socket.getRemoteSocketAddress().toString() + ": " + in.toString());
							} 
						} catch (IOException ex) { 
							writeToChatBox(ex.getMessage() + "\n");
						} catch (InterruptedException ex) { 
							writeToChatBox(ex.getMessage() + "\n");
						} finally {
							writer.close();
							reader.close();
							socket.close();
							serverSocket.close();
						}
					} catch (IOException ex) {
						writeToChatBox(ex.getMessage() + "\n");
					} finally {
						serverRunning = false;
						writer = null;
						reader = null;
						socket = null;
						serverSocket = null;
					}
				}).start();
			} else if (clientBtn.isSelected()) {
				serverRunning = false;
				clientRunning = true;
				
				new Thread(() -> {
					try {
						socket = new Socket(hostText.getText(), Integer.parseInt(portText.getText()));
						writer = new PrintWriter(socket.getOutputStream(), true);
						reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
						try {
							while (clientRunning) {
								if (!reader.ready()) {
									Thread.sleep(200);
									continue;
								}

								StringBuilder in = new StringBuilder();
								while (reader.ready()) {
									in.append(reader.readLine());
									in.append("\n");
								}
								
								RamChat.this.writeToChatBox(socket.getRemoteSocketAddress().toString() + ": " + in.toString());
							}	
						} catch (IOException ex) { 
							writeToChatBox(ex.getMessage() + "\n"); 
						} catch (InterruptedException ex) { 
							writeToChatBox(ex.getMessage() + "\n");
						} finally {
							writer.close();
							reader.close();
							socket.close();
						}
					} catch (Exception ex) {
						writeToChatBox(ex.getMessage() + "\n");
					} finally {
						clientRunning = false;
						writer = null;
						reader = null;
						socket = null;
					}
				}).start();
			}
		});
		
		sendBtn.addActionListener(e -> {
			if (writer != null && socket != null) {
				writer.println(sendText.getText());
				writeToChatBox(socket.getLocalAddress().toString() + ": " + sendText.getText() + "\n");
				sendText.setText("");
			}
		});
		
		disposeBtn.addActionListener(e -> {
			serverRunning = false;
			clientRunning = false;
			
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (Exception e1) {}
		});
		
		this.add(chatPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.setTitle("RamChat");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
	
	protected synchronized void writeToChatBox(String s) {
		chatText.append(s);
	}
	
	public static void main(String[] args) {
		new RamChat();
	}
}

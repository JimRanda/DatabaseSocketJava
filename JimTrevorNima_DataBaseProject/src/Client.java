import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame implements Runnable {
	
	private JTextField inputBar;
	private JButton btnGo;
	private JTextArea areaResults;
	private JScrollPane sp;
	private Dimension winD;
	
	final int PORT = 8001;
	final String HOST = "localhost";
	private Socket connectionSocket = null;
	private PrintWriter sendToServer = null;
	private BufferedReader serverOutput = null;
	
	public Client() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 750, 500);	
		this.setLayout(null);
		this.setMinimumSize(this.getSize());
		this.setTitle("Client");

		winD = new Dimension(734, 462);
		
		//TODO Allow enter key to send message
		
		areaResults = new JTextArea();
		areaResults.setEditable(false);
		sp = new JScrollPane(areaResults);
		sp.setBounds(5, 5, winD.width -10, winD.height - 50);
		this.add(sp);

		inputBar = new JTextField();
		inputBar.setBounds(5, sp.getSize().height + 15,  winD.width - 230, 25);
		this.add(inputBar);
		
		btnGo = new JButton("GO");
		btnGo.setBounds(inputBar.getSize().width + 10, sp.getSize().height + 15, 100, 25);
		this.add(btnGo);
		
		this.setVisible(true);
		connect();
		new Thread(this).start();
		
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  {
				if (sendToServer != null) {
					if(inputBar.getText().compareTo("") != 0) {
						sendToServer.println(inputBar.getText()); //Sends text box contents to sever via print writer connection
						areaResults.append(inputBar.getText() + "\n");
						inputBar.setText("");
					}
				}
			}
		});
		
		this.addComponentListener(new ComponentListener() { //Dynamic resizing without layouts
			public void componentResized(ComponentEvent e) {
				winD = getContentPane().getSize();
				sp.setBounds(5, 5, winD.width -10, winD.height - 50);
				inputBar.setBounds(5, sp.getSize().height + 15, winD.width - 230, 25);
				btnGo.setBounds(inputBar.getSize().width + 10, sp.getSize().height + 15, 100, 25);
			}
			public void componentHidden(ComponentEvent e) {} //Have to override
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
		});
	}
	
	private void connect() {
		try {
			connectionSocket = new Socket(HOST,PORT);
			InputStreamReader isr = new InputStreamReader(connectionSocket.getInputStream());
			serverOutput = new BufferedReader(isr);
			sendToServer = new PrintWriter(connectionSocket.getOutputStream(),true);
		} catch (UnknownHostException e) {
			areaResults.setText(e.getMessage());
		} catch (IOException e) {
			areaResults.setText(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		new Client();
	}

	public void run() {
		String serverMsg;
		try {
			while(true) {
				serverMsg = serverOutput.readLine();
				areaResults.append(serverMsg + "\n");
			}
		}catch(Exception ex) {
			areaResults.append(ex.getMessage());
		}
	}
}

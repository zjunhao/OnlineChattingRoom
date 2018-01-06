import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
	
	String username; // username cannot contain white space, punctuation accepted
	JTextArea incoming;
	JTextField outgoing;
	BufferedReader reader;
	PrintWriter writer;
	Socket sock;
	
	public static void main(String[] args) {
		ChatClient client = new ChatClient();
		if(args.length!=1) {
			System.out.println("Ussage: java ChatClient <username> \nPlease try again");
		} else {
			client.start(args[0]);
		}	
	}
	
	public void start(String usernameFromCmd) {
		this.username = usernameFromCmd;
		/* user interface */
		JFrame frame = new JFrame("Welcome " + username);
		JPanel mainPanel = new JPanel();
		
		// window to show chat record 
		incoming = new JTextArea(15,40);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		DefaultCaret caret = (DefaultCaret)incoming.getCaret(); // set JTextArea always scroll to the bottom
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);      // set JTextArea always scroll to the bottom
		// add scroll to window
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// window to send your words
		outgoing = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		
		mainPanel.add(qScroller);
		mainPanel.add(outgoing);
		mainPanel.add(sendButton);
		
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(650, 320);
		frame.setVisible(true);
		
		/* setup network */
		setUpNetworking();
		
		/* open new threads to listen from server */
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		
	}
	
	private void setUpNetworking() {
		try {
			sock = new Socket("127.0.0.1",9999);
			
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			
			writer = new PrintWriter(sock.getOutputStream());
			
			System.out.println("Success: connecting to server successfully");
			
			writer.println("author:" + " time:" + LocalDateTime.now() + " msgbody:" + username + " joined the chat room");
			writer.flush();
			
		} catch(Exception ex) {
			System.out.println("Err: connecting to server failed, detail: ");
			ex.printStackTrace();
		}
	}
	
	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
		      if(!outgoing.getText().equals("")) {
		    	  	  
				  writer.println("author:" + username + " time:" + LocalDateTime.now() + " msgbody:" + outgoing.getText());
				  writer.flush();
		      }
			} catch (Exception ex) {
				System.out.println("Err: fail to send text, detail: ");
				ex.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();
 		}
	}
	
	public class IncomingReader implements Runnable {
		public void run() {
			String message;
			String author;
			String day;
			String second;
			String info;
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("read > " + message);
					
					//parse text received from server
				    String pattern = "\n*author:(.*) time:(.+)T(.+)\\.\\d\\d\\d msgbody:(.*)";   // "\n*" at beginning used for debugging, sometimes server returns "\nauthor:blahblahblah..."
				    Pattern r = Pattern.compile(pattern);
				    Matcher m = r.matcher(message);		
				    
				    if(!m.find()) {
				    	  System.out.println("Err: fail to parse text got from server, text content: \n");
				    	  System.out.println(message);
				    } else {
				      author = m.group(1);
				      day = m.group(2);
					  second = m.group(3);
					  info = m.group(4); 
				    
				      if(author.equals("")) {            // system notification "testuser joined the chat room"
				        //put new comming msg on board
				        incoming.append("SYSTEM MSG > " + info + ", TIME: " + day + " " + second + "\n\n");
				      } else {   				        // users chat body
					    //put new comming msg on board
					    incoming.append(author + ": " + day + " " + second + "\n");
					    incoming.append(info + "\n\n");
				      }
				    }
				}
			} catch (Exception ex) {
				System.out.println("Err: fail to parse and display text from server, detail: ");
				ex.printStackTrace();
			}
		}
	}
}

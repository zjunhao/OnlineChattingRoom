import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
	ArrayList<PrintWriter> clientOutputStreams;
	
	public static void main (String[] args) {
		ChatServer server = new ChatServer();
		server.start();
	}
	
	public void start() {
		clientOutputStreams = new ArrayList<PrintWriter>();
		try {
			ServerSocket serverSock = new ServerSocket(9999);
			System.out.println("Server started, listening at port 9999");
			while (true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				Thread clientListener = new Thread(new ClientHandler(clientSocket));
				clientListener.start();
				
				System.out.println("Success: connected to requested client successfully");
			} 
		} catch (Exception ex) {
			System.out.println("Err: fail to connect to requested client, detail: ");
			ex.printStackTrace();
		}
	}
	
	public void sendMsgToAllClients(String message) {
		Iterator<PrintWriter> it = clientOutputStreams.iterator();
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			} catch(Exception ex) {
				System.out.println("Err: fail to send message to client, detail: ");
				ex.printStackTrace();
			}
		}
	}
	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader iReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(iReader);
			} catch (Exception ex) {
				System.out.println("Err: fail to get BufferedReader from client socket, detail: ");
				ex.printStackTrace();
			}
		}
		
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read > " + message);
					sendMsgToAllClients(message);
				}
			} catch (Exception ex) {
				System.out.println("Err: fail to send incoming message to all clients, detail: ");
				ex.printStackTrace();
			}
		}
	}
}

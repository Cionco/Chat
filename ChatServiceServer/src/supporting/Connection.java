package supporting;

import java.io.*;
import java.net.*;

public class Connection {

	private InetAddress ip;
	private ConnectionSocket socket;
	private PrintWriter output;
	
	public Connection(InetAddress ip, ConnectionSocket socket) {
		this.ip = ip;
		this.socket = socket;
		try {
			output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getSocket().getOutputStream())),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Socket getSocket() {
		return socket.getSocket();
	}
	
	public ConnectionSocket getConnectionSocket() {
		return socket;
	}
	
	public InetAddress getIP() {
		return ip;
	}
	
	public void send(String message) {
		output.println(message);
	}
}

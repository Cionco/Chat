package supporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import frontend.Server;

public class ConnectionSocket {

	private ArrayList<Connection> connections;
	private Socket socket;
	public Scanner input;
	private PrintWriter output;
	private Connection mainConnection;
	private String prompt = "> ";
	
	public ConnectionSocket(Socket socket) {
		this.socket = socket;
		try {
			connections = new ArrayList<>();
			input = new Scanner(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public Socket getSocket() { return socket; }
	
	private void addConnection(Connection connection) {
		connections.add(connection);
		mainConnection = connection;
	}
	
	public void addConnection(InetAddress ip, ConnectionSocket socket) {
		addConnection(new Connection(ip, socket));
	}
	
	public void removeConnection(Connection connection) {
		if(connection.getIP().toString().equals(socket.getInetAddress().toString())) mainConnection = null;
		connections.remove(connection);		
	}
	
	public void removeConnection(InetAddress ip) throws UnknownHostException {
		mainConnection = null;
		removeConnection(findConnectionByIp(ip));	
	}
	
	public Connection findConnectionByIp(InetAddress ip) throws UnknownHostException {
		for(Connection c : connections) {
			if(c.getIP().toString().equals(ip.toString())) return c; 
		}
		throw new UnknownHostException();
	}
	
	public static void close(ConnectionSocket socket) {
		for(Connection c : socket.connections) {
			for( Connection cc : c.getConnectionSocket().connections) //Find and remove the corresponding Connection
				if(cc.getIP() == socket.getSocket().getInetAddress()) //in the connected Socket´s list of Connections
					c.getConnectionSocket().connections.remove(cc);
			socket.connections.remove(c);
		}
		socket.send(frontend.Server.EXIT_CONNECTION_CODE);
		try {
			socket.getSocket().close();
			socket.input.close();
			socket.output.close();
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server.removeSocket(socket);
	}
	
	/**
	 * Only for Server Messages
	 * @param message The message that will be sent to the Client
	 */
	public void send(String message) {
		output.println(message);
	}
	
	public void send(InetAddress ip, String message) throws UnknownHostException {
		try {
			findConnectionByIp(ip).send(message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void sendServerMessage(String message) {
		System.out.println("[Server] --> " + this.getSocket().getInetAddress() + " " + message);
		send("[Server] " + message);
	}
	
	public void prompt() {
		send(frontend.Server.PROMPT_CODE + prompt);
	}
	
	public Connection getMainConnection() {
		return mainConnection;
	}
	
	public void setMainConnection(Connection c) {
		mainConnection = c;
	}
	
	public ArrayList<Connection> getConnections() {
		return connections;
	}
}

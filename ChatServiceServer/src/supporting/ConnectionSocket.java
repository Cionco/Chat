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
	private Connection mainConnection = null;
	private String prompt = "> ";
	public boolean prompted = false;
	
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
		if(mainConnection == null) setMainConnection(connection);
	}
	
	public void addConnection(InetAddress ip, ConnectionSocket socket) {
		addConnection(new Connection(ip, socket));
	}
	
	public void removeConnection(Connection connection) {
		if(connection.getIP().toString().equals(socket.getInetAddress().toString())) setMainConnection(null);
		connections.remove(connection);		
	}
	
	public void removeConnection(InetAddress ip) throws UnknownHostException {
		if(mainConnection.getIP().equals(ip)) setMainConnection(null);
		removeConnection(findConnectionByIp(ip));	
	}
	
	public Connection findConnectionByIp(InetAddress ip) throws UnknownHostException {
		for(Connection c : connections) {
			if(c.getIP().toString().equals(ip.toString())) return c; 
		}
		throw new UnknownHostException();
	}
	
	public static void close(ConnectionSocket socket) {
		try {
			Command.DISCONNECTALL.execute(socket, new String[0]);
		} catch(UnknownHostException e) {
			e.printStackTrace();
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
	public void send(String message, boolean delprompt) {
		String mes = (delprompt?deletePrompt():"").concat(message);
		output.println(mes);
		prompted = false;
	}
	
	public void send(String message) {
		send(message, false);
	}
	
	public void send(InetAddress ip, String message) throws UnknownHostException {
		try {
			findConnectionByIp(ip).send(message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void sendServerMessage(String message, boolean delprompt) {
		System.out.println("[Server] --> " + this.getSocket().getInetAddress() + " " + message);
		send("[Server] " + message, delprompt);
	}
	
	public void sendServerMessage(String message) {
		sendServerMessage(message, false);
	}
	
	public String deletePrompt() {
		String buf = "";
		for(int i = 0; i < prompt.length(); i++) buf += "\b \b";
		return buf;
	}
	
	public void prompt() {
		output.println(frontend.Server.PROMPT_CODE + prompt);
		prompted = true;
	}
	
	public Connection getMainConnection() {
		return mainConnection;
	}
	
	public void setMainConnection(Connection c) {
		mainConnection = c;
		if(c == null) prompt = "> ";
		else prompt = c.getIP().toString() + "> ";
	}
	
	public ArrayList<Connection> getConnections() {
		return connections;
	}
}

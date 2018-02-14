package frontend;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import supporting.Command;
import supporting.ConnectionSocket;

public class Server {

	public static String EXIT_CONNECTION_CODE = "%&/(=)(/&%%ojgoOUHFEXITOihg&%&/()";
	public final static String PROMPT_CODE = "/§)§/IFJFPROMPTJFZD%UW&\"";
	static ArrayList<ConnectionSocket> sockets = new ArrayList<ConnectionSocket>();
	static ServerSocket socket;
	static String illegalArgumentError = "Command does not exist or is not implemented yet";
	
	public static void main(String[] args) {
		try {
			socket = new ServerSocket(5555);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						try {
							Socket newSocket = socket.accept();
							addNewSocket(newSocket);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			
			while(true) {
				checkSocketsForMessage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized static void checkSocketsForMessage() {
		try {
			for(ConnectionSocket s : sockets) {	
				try {
					if(s.getSocket().getInputStream().available() != 0) {
						String message = s.input.nextLine();
						process_message(s, message);
					}
				} catch (IOException e) {
					System.out.println("Removing Socket " + s.getSocket().getInetAddress() + " from List");
					sockets.remove(s);
					System.out.println(sockets.size() + " sockets remaining");
				}
			}			
		} catch(ConcurrentModificationException e) {
			
		}
			
	}

	private static void process_message(ConnectionSocket s, String message) throws IOException {
		String[] params = message.split(" ");
		Command command = null;
		if(message.startsWith("/")) 
			try {
				command = Command.valueOf(params[0].substring(1).toUpperCase());
				command.execute(s, params);
			}catch(IllegalArgumentException | UnknownHostException | ArrayIndexOutOfBoundsException | AbstractMethodError e) {
				if(e.getClass().equals(IllegalArgumentException.class)) {
					System.out.println("[Server] --> " + s.getSocket().getInetAddress() + " " + illegalArgumentError);
					s.send(illegalArgumentError);
				} else if(e.getClass().equals(ArrayIndexOutOfBoundsException.class)) {
					s.sendServerMessage("Syntax Error"); 
					command.help(s); 
				} else if(e.getClass().equals(AbstractMethodError.class)) {
					s.send("Command does not exist or is not implemented yet");
				}
				else s.sendServerMessage("Unknown host with ip address: " + params[1]);
				
			}
		else {
			if(s.getMainConnection() == null) System.out.println("Message from " + s.getSocket().getInetAddress() + ": " + message);
			else {
				System.out.println(s.getSocket().getInetAddress() + " --> " + s.getMainConnection().getIP() + " " + message);
				s.getMainConnection().send(s.deletePrompt() + "[" + s.getSocket().getInetAddress() + "] " + message);
				s.getMainConnection().getConnectionSocket().prompt();
			}
		}
		s.prompt();
	}

	private synchronized static void addNewSocket(Socket newSocket) {
		ConnectionSocket newCSocket = new ConnectionSocket(newSocket);
		System.out.println("New Connection: " + newCSocket.getSocket().getInetAddress());
		sendHello(newCSocket);
		sockets.add(newCSocket);
		newCSocket.prompt();
	}
	
	public synchronized static void removeSocket(ConnectionSocket socket) {
		sockets.remove(socket);
	}
	
	/**
	 * 
	 * @param ip The ip address of the Socket that the function is searching for
	 * @return Returns the Socket with the requested ip
	 * @throws UnknownHostException in case there is no socket with the requested ip
	 */
	public static ConnectionSocket findSocketByIp(String ip) throws UnknownHostException{
		for(ConnectionSocket c : sockets) {
			if(c.getSocket().getInetAddress().toString().equals("/" + ip)) return c;
		}
		throw new UnknownHostException();
	}
	
	public static void sendHello(ConnectionSocket s) {
		String message = "";
		message += "/help for list of all commands\n"
				+  "\tConnect to other users to chat.\n\tFor changing the recipient of your messages use the /with command.\n"
				+  "\tTo end the program use the /quit command";
		s.sendServerMessage(message);
	}
}

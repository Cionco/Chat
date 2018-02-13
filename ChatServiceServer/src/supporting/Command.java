package supporting;

import java.net.InetAddress;
import java.net.UnknownHostException;

import frontend.Server;

public enum Command {

	QUIT {
		public void execute(ConnectionSocket s, String[] params) {
			ConnectionSocket.close(s);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Closes all open connections and quits program");
			s.sendServerMessage("Usage of /quit:\n\t\t/quit");
		}
	},
	Q {
		public void execute(ConnectionSocket s, String[] params) throws ArrayIndexOutOfBoundsException, UnknownHostException {
			QUIT.execute(s, null);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /quit");
		}
	},
	WITH {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			s.setMainConnection(s.findConnectionByIp(InetAddress.getByName(params[1])));
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Set Main Connection to <destination ip adress>");
			s.sendServerMessage("Usage of /with:\n\t\t/with <destination ip adress>");
		}
	},
	W {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			WITH.execute(s, params);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /with");
		}
	},
	CONNECT {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
				ConnectionSocket socket = Server.findSocketByIp(params[1]); //Find the corresponding Socket in the Servers list
				s.addConnection(InetAddress.getByName(params[1]), socket); //Add the Connection to the requesting Socket
				socket.addConnection(s.getSocket().getInetAddress(), s); //Add the Connection to the found Socket
				System.out.println(s.getSocket().getInetAddress() + " now connected to " + socket.getSocket().getInetAddress());
				s.sendServerMessage("You are now connected to " + socket.getSocket().getInetAddress());
				socket.sendServerMessage(s.getSocket().getInetAddress() + " has just connected to you!");
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Add <ip adress> to list of connections");
			s.sendServerMessage("Usage of /connect:\n\t\t/connect <ip adress of intended connection>");
		}
	},
	C {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			CONNECT.execute(s, params);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /connect");
		}
	},
	DISCONNECT {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			ConnectionSocket socket = s.findConnectionByIp(InetAddress.getByName(params[1])).getConnectionSocket();
			socket.removeConnection(s.getSocket().getInetAddress());
			socket.sendServerMessage(s.getSocket().getInetAddress() + " has ended the connection to you");
			s.removeConnection(InetAddress.getByName(params[1]));
			s.sendServerMessage(socket.getSocket().getInetAddress() + " is no longer connected to you!");
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("End connection to <ip adress>");
			s.sendServerMessage("Usage of /disconnect:\n\t\t/disconnect <ip adress>");
		}
	},
	DC {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			DISCONNECT.execute(s, params);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /disconnect");
		}
	},
	DISCONNECTALL {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			for(Connection c : s.getConnections()) DISCONNECT.execute(s, new String[]{"", c.getIP().toString().substring(1)});
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("End connection to all connected IPs");
			s.sendServerMessage("Usage of /disconnectall:\n\t\t/disconnectall");
		}
	},
	DCA {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			DISCONNECTALL.execute(s, params);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /disconnectall");
		}
	},
	TO {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			s.send(InetAddress.getByName(params[1]), params[2]);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Send message only to specified ip adress");
			s.sendServerMessage("Usage of /to:\n\t\t/to <destination ip adress> <message>");
		}
	},
	ALL {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			for(Connection c : s.getConnections()) {
				c.send(params[1]);
			}
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Send message to all connected ip adresses");
			s.sendServerMessage("Usage of /all:\n\t\t/all <message>");
		}
	},
	HELP {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			try {
				if(params[1].length() > 0) Command.valueOf(params[1].toUpperCase()).help(s);
				else throw new ArrayIndexOutOfBoundsException();
			} catch(ArrayIndexOutOfBoundsException e) {
				s.sendServerMessage("List of all commands:");
				for(Command command : Command.values()) {
					s.send("\t\t" + command.toString().toUpperCase());
				}
			}
		}
		
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Show usage of specific command or list all commands");
			s.sendServerMessage("Usage of /help:\n\t\t/help [<command>]");
		}
	},
	H {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException{
			HELP.execute(s, params);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("See /help");
		}
	};
	
	public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
		throw new AbstractMethodError();
	}
	
	public void help(ConnectionSocket s) {
		throw new AbstractMethodError();
	}
}

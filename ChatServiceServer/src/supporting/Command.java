package supporting;

import java.net.InetAddress;
import java.net.UnknownHostException;

import frontend.Server;

public enum Command {

	QUIT {
		public void execute(ConnectionSocket s, String[] params) {
			ConnectionSocket.close(s);
		}
	},
	Q {
		public void execute(ConnectionSocket s, String[] params) throws ArrayIndexOutOfBoundsException, UnknownHostException {
			QUIT.execute(s, null);
		}
	},
	WITH {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			s.setMainConnection(s.findConnectionByIp(InetAddress.getByName(params[1])));
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Use of /with:\n\t/with <destination ip addresss>");
		}
	},
	W {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			WITH.execute(s, params);
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
			s.sendServerMessage("Use of /connect:\n\t/connect <ip address of intended connection>");
		}
	},
	C {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			CONNECT.execute(s, params);
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
	},
	DC {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			DISCONNECT.execute(s, params);
		}
	},
	DISCONNECTALL {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			for(Connection c : s.getConnections()) DISCONNECT.execute(s, new String[]{"", c.getIP().toString().substring(1)});
		}
	},
	DCA {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			DISCONNECTALL.execute(s, params);
		}
	},
	TO {
		public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
			s.send(InetAddress.getByName(params[1]), params[2]);
		}
		public void help(ConnectionSocket s) {
			s.sendServerMessage("Use of /to:\n\t/to <destination ip address> <message>");
		}
	};
	
	public void execute(ConnectionSocket s, String[] params) throws UnknownHostException, ArrayIndexOutOfBoundsException {
		throw new AbstractMethodError();
	}
	
	public void help(ConnectionSocket s) {
		throw new AbstractMethodError();
	}
}

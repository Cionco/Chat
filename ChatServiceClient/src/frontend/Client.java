package frontend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import supporting.Encryption;

public class Client {
	
	private static volatile boolean run = true;
	final static String EXIT_CONNECTION_CODE = "%&/(=)(/&%%ojgoOUHFEXITOihg&%&/()";
	final static String PROMPT_CODE = "/§)§/IFJFPROMPTJFZD%UW&\"";
	private static byte[] aesKey;

	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Auf welcher IP Adresse läuft der Server?: ");
		String ip = keyboard.nextLine();
		if(!isIp(ip)) {
			System.out.println(ip + " ist keine richtige IP-Adresse.");
			System.exit(-1);
		}
		
		System.out.println("Versuche Verbindung mit Server aufzubauen...");
		
		try (Socket socket = createSocket(ip, 5555, 2000)){
			System.out.println("Verbindung aufgebaut!");
			PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			Scanner input = new Scanner(new InputStreamReader(socket.getInputStream()));
			aesKey = Encryption.initializeEncryptionProtocol(socket.getInputStream(), socket.getOutputStream());

			Thread reciever = new Thread(new Runnable() {
				@Override
				public void run() {
					while(run) {
						String read = input.nextLine();
						if(read.equals(EXIT_CONNECTION_CODE)) lock();
						else if(read.startsWith(PROMPT_CODE)) System.out.print(read.substring(PROMPT_CODE.length()));
						else System.out.println(read);					
					}
				}
				
			});
			reciever.start();
			
			while(run) {
				if(System.in.available() != 0) output.println(keyboard.nextLine());
			}
			
			keyboard.close();
			input.close();
			output.close();
		} catch(IOException e) {
			if(e.getClass().equals(SocketTimeoutException.class)) {
				System.out.println("Server konnte nicht erreicht werden!");
				System.exit(-1);
			}
			e.printStackTrace();
		}
	}
	public static void lock() {
		run = false;
	}

	public static boolean isIp(String check) {
		boolean _byte = true;
		String[] split = check.split("\\.");
		for(String _bytecheck : split)
			_byte = (Integer.parseInt(_bytecheck) & 0xFFFFFF00) == 0 && _byte;
		return check.split("\\.").length == 4 && _byte;
	}
	
	private static Socket createSocket(String ip, int port, int timeout) throws IOException, SocketTimeoutException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(ip, port), timeout);
		return socket;
	}
}

package frontend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	private static volatile boolean run = true;
	static String EXIT_CONNECTION_CODE = "%&/(=)(/&%%ojgoOUHFEXITOihg&%&/()";

	public static void main(String[] args)  {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Auf welcher IP Adresse läuft der Server?: ");
		String ip = keyboard.nextLine();
		if(!isIp(ip)) {
			System.out.println(ip + " ist keine richtige IP-Adresse.");
			System.exit(-1);
		}
		
		System.out.println("Versuche Verbindung mit Server aufzubauen...");
		
		try {
			//Socket socket = new Socket("10.61.14.182", 5555);
			Socket socket = new Socket(ip, 5555);
			System.out.println("Verbindung aufgebaut!");
			PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			Scanner input = new Scanner(new InputStreamReader(socket.getInputStream()));
		
			Thread reciever = new Thread(new Runnable() {

				@Override
				public void run() {
					while(run) {
						String read = input.nextLine();
						if(read.equals(EXIT_CONNECTION_CODE)) lock();
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
			socket.close();
		} catch(IOException e) {
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
}

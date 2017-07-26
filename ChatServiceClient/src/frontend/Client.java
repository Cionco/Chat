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

	public static void main(String[] args) {
		System.out.println("Versuche Verbindung mit Server aufzubauen...");
		
		try {
			Socket socket = new Socket("10.61.14.182", 5555);
			System.out.println("Verbindung aufgebaut!");
			PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			Scanner input = new Scanner(new InputStreamReader(socket.getInputStream()));
			Scanner keyboard = new Scanner(System.in);
			
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
}

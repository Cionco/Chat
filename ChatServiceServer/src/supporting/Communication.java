package supporting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import encryption.AESEncryption;

public class Communication {
	
	private static boolean DEBUG_FLAG = false;
	
	public static String read(DataInputStream dIn, byte[] key) {
		try {
			int length = dIn.readInt();
			if(length>0) {
				byte[] in = new byte[length];
				dIn.readFully(in, 0, in.length);
				if(DEBUG_FLAG) {
					System.out.print("\n[DEBUG]: array before decryption: ");
					printByteArray(in);
					System.out.print("\n[DEBUG]: array after decryption: ");
				}
				return byteArrayToString(trim(AESEncryption.AESDecrypt(in, key)));
			}
		} catch (IOException e) {}
		return "";
	}
	
	public static void write(DataOutputStream s_output, String s, byte[] key) throws IOException {
		byte[] ba = stringToByteArray(s);
		if(DEBUG_FLAG) {
			System.out.print("\n[DEBUG]: array before encryption: ");
			printByteArray(ba);
		}
		ba = AESEncryption.AESEncrypt(ba, key);
		if(DEBUG_FLAG) {
			System.out.print("\n[DEBUG]: array after encryption: ");
			printByteArray(ba);
		}
		s_output.writeInt(ba.length);
		s_output.write(ba);
	}
	
	public static String byteArrayToString(byte[] array) {
		return new String(array, StandardCharsets.UTF_8);
	}

	public static byte[] stringToByteArray(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}
	
	
	private static byte[] printByteArray(byte[] array) {
		for(byte b : array) System.out.print(b + " ");
		return array;
	}
	
	private static byte[] trim(byte[] array) {
		ArrayList<Byte> newlist = new ArrayList<>();
		for(byte b : array)
			newlist.add(b);
		
		for(int i = 0; i < newlist.size(); i++) {
			if(newlist.get(0) == 0x00) 
				newlist.remove(0);
			else break;
		}
		
		for(int i = newlist.size() - 1; i >= 0;i--) {
			if(newlist.get(newlist.size() - 1) == 0x00)
				newlist.remove(newlist.size() - 1);
			else break;
		}
		
		byte[] newarray = new byte[newlist.size()];
		for(int i = 0; i < newarray.length; i++) newarray[i] = newlist.get(i);
		return newarray;
	}
}

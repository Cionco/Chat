package encryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import supporting.ConnectionSocket;

public class Encryption {
	
	private static Integer[] masks = new Integer[]{0xff000000, 0x00ff0000, 0x0000ff00, 0x000000ff};
	
	public static void main(String[] args) {
		//initializeEncryptionProtocol(null);
		AESEncrypt(new byte[]{ (byte) 0xa1, 0x34, (byte) 0xb5, 0x51, 0x6e, (byte) 0xf0, 0x31, 0x4a, (byte) 0xb7, 0x2c, 0x46, (byte) 0xe3, 0x51, 0x1b, (byte) 0xca, 0x6c
				, (byte) 0xa1, 0x34, (byte) 0x00b5, 0x51, 0x6e, (byte) 0xf0, 0x31, 0x4a, (byte) 0xb7, 0x2c, 0x46, (byte) 0xe3, 0x51, 0x1b, (byte) 0xca, 0x6b
				, (byte) 0xa1, 0x34, (byte) 0x00b5, 0x51, 0x6e, (byte) 0xf0, 0x31, 0x4a, (byte) 0xb7, 0x2c, 0x46, (byte) 0xe3, 0x51, 0x1b, (byte) 0xca, 0x6a}, null);
	}
	
	private static final int ENCRYPTION_KEY_LENGTH = 512;//0x400;

	public static byte[] initializeEncryptionProtocol(ConnectionSocket s) {
		KeyPairGenerator kpg = null; 
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		kpg.initialize(ENCRYPTION_KEY_LENGTH);
		KeyPair kp = kpg.generateKeyPair();
		Key publicKey = kp.getPublic();
		byte[] ba = publicKey.getEncoded();

		try {
			DataOutputStream dOut = new DataOutputStream(s.getSocket().getOutputStream());
			DataInputStream dIn = new DataInputStream(s.getSocket().getInputStream());
			dOut.writeInt(ba.length);
			dOut.write(ba);    
			byte[] encrypted_aesKey = null;
			int length = dIn.readInt();
			if(length>0) {
				encrypted_aesKey = new byte[length];
				dIn.readFully(encrypted_aesKey, 0, encrypted_aesKey.length); // read the message
			}
			return decrypt(kp.getPrivate(), encrypted_aesKey);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
		
	}
	
	public static byte[] decrypt(PrivateKey key, byte[] ciphertext) {
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);  
			return cipher.doFinal(ciphertext);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		return null;
	}
	
	public static byte[] AESEncrypt(byte[] ciphertext, byte[] key) {
		if(ciphertext.length == 0) return null;
		//printHex(ciphertext);
		Integer[] block = buildBlock(ciphertext);
		Block blockclass = new Block(arrayToIndex(15, ciphertext));
		//printHex(block);
		blockclass.printBlock();
		
		
		//byte[] encrypted_block = null;
		
		//byte[] encrypted_message = AESEncrypt(arrayFromIndex(16, ciphertext), key);
		//return expand_array(encrypted_block, encrypted_message);
		//return AESEncrypt(arrayFromIndex(16, ciphertext), key);
		return null;
	}

	/** 
	 * Build 16 byte Block from the Ciphertext split into 4 DWORDS represented by Integers
	 * @param ciphertext
	 * @return the block as Integer[]
	 */
	private static Integer[] buildBlock(byte[] ciphertext) {
		Integer[] block = new Integer[]{0, 0, 0, 0};
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				int b = ciphertext[i * 4 + j]<< ((3 - j) * 8);
				b = b & masks[j];
				block[i] = block[i] | b ;
			}
		}
		return block;
	}
	
	private static byte[] arrayFromIndex(int startindex, byte[] array) {
		byte[] newarray = new byte[array.length - startindex];
		for(int i = startindex; i < array.length; i++) newarray[i - startindex] = array[i];
		return newarray;
	}
	
	private static byte[] arrayToIndex(int endindex, byte[] array) {
		byte[] newarray = new byte[endindex + 1];
		for(int i = 0; i < endindex; i++) newarray[i] = array[i];
		return newarray;
	}
	
	private static byte[] expand_array(byte[] array_one, byte[] array_two) {
		byte[] newarray = new byte[array_one.length + array_two.length];
		for(int i = 0; i < array_one.length; i++)
			newarray[i] = array_one[i];
		for(int i = array_one.length; i < newarray.length; i++) 
			newarray[i] = array_two[i - array_one.length];
		return newarray;
	}
	
	private static <E> void printHex(E[] array) {
		for(int i = 0; i < array.length; i++) {
			System.out.printf("%08x\n", array[i]);
		}
		System.out.println();
	}
	
	private static <E> void printHex(E value) {
		System.out.printf("%08x\n", value);
	}
	
	private static class Block {
		byte[] bytes;
		
		public Block(byte[] bytes) {
			this.bytes = bytes;
		}
		
		public int getDWORD(int index) {
			int DWORD = 0x000000;
			for(int i = 0; i < 4; i++)
				DWORD |= (bytes[i + index * 4] << (3 - i) * 8) & masks[i];
			
			return DWORD;
		}
		
		private int index(int wordIndex, int byteIndex) {
			return wordIndex * 4 + byteIndex;
		}
		
		public void printBlock() {
			for(int i = 0; i < 4; i++)
				System.out.printf("%08x\n", getDWORD(i));
		}
		
		public void shiftWord(int i, int bytes) {
			if(bytes == 0) return;
			byte help = this.bytes[index(i, 3)];
			for(int j = 3; j > 0; j--)
				this.bytes[index(i, j)] = this.bytes[index(i, j - 1)];
			this.bytes[index(i, 0)] = help;
			shiftWord(i, bytes - 1);
			return;
		}
	}
}
package encryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import supporting.ConnectionSocket;

public class Encryption {
	private static final int ENCRYPTION_KEY_LENGTH = 0x400;
	
	public static void main(String[] args) {
		//initializeEncryptionProtocol(null);
		byte[] key;
		byte[] cipher;
		int i = 0;
		key = new byte[]{0x2b, 0x28, (byte) 0xab, 0x09, 0x7e, (byte) 0xae, (byte) 0xf7, (byte) 0xcf, 0x15, (byte) 0xd2, 0x15, 0x4f, 0x16, (byte) 0xa6, (byte) 0x88, 0x3c};
		cipher = new byte[]{0x32, (byte) 0x88, 0x31, (byte) 0xe0, 0x43, 0x5a, 0x31, 0x37, (byte) 0xf6, 0x30, (byte) 0x98, 0x07, (byte) 0xa8, (byte) 0x8d, (byte) 0xa2, 0x34};
		cipher = new byte[]{0x34, 0x19};
		byte[] encrypted = AESEncryption.AESEncrypt(cipher, key);
		for(byte b : encrypted) { 
			if(i % 16 == 0 && i != 0) System.out.println();
			if(i++ % 4 == 0 && i != 0) System.out.println();
			System.out.printf("%02x", b);
		}
		
		
		System.out.println();
		System.out.println("----------------------------DECRYPTION DEBUG-----------------------------");
		
		for(byte b : AESEncryption.AESDecrypt(encrypted, key)) { 
			if(i % 16 == 0 && i != 0) System.out.println();
			if(i++ % 4 == 0 && i != 0) System.out.println();
			System.out.printf("%02x", b);
		}
	}	

	/**
	 * Initializes the encryption for the Server side
	 * @param s
	 * @return the decrypted aes key
	 */
	public static byte[] initializeEncryptionProtocol(ConnectionSocket s) {
		KeyPairGenerator kpg = null; 
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		kpg.initialize(ENCRYPTION_KEY_LENGTH);
		java.security.KeyPair kp = kpg.generateKeyPair();
		java.security.Key publicKey = kp.getPublic();
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

	/**
	 * Initializes the encryption for the Client side
	 * @param input
	 * @param output
	 * @return  the decrypted aes key
	 */
	public static byte[] initializeEncryptionProtocol(InputStream input, OutputStream output) {
		byte[] message = null;
		DataInputStream dIn = new DataInputStream(input);
		DataOutputStream dOut = new DataOutputStream(output);
		try {
			int length = dIn.readInt();
			if(length>0) {
				message = new byte[length];
				dIn.readFully(message, 0, message.length); // read the message
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		PublicKey rsaKey = generatePublicKeyFromByteArray(message);
		
		byte[] aesKey = generateAESKey();
		byte[] encrypted_aesKey = encrypt(rsaKey, aesKey);
		try {
			dOut.writeInt(encrypted_aesKey.length);
			dOut.write(encrypted_aesKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aesKey;
		
	}
	
	/**
	 * RSA encrypts the plaintext and returns the encrypted
	 * @param key
	 * @param plaintext
	 * @return
	 */
	public static byte[] encrypt(PublicKey key, byte[] plaintext) {
	    try {
	    	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");   
	    	cipher.init(Cipher.ENCRYPT_MODE, key);  
	    	return cipher.doFinal(plaintext);
	    } catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) { return null;}
	}
	
	/**
	 * Decrypts RSA encrypted text
	 * @param key
	 * @param ciphertext
	 * @return decrypted text or null if an exception occured
	 */
	public static byte[] decrypt(PrivateKey key, byte[] ciphertext) {
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);  
			return cipher.doFinal(ciphertext);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}   
		return null;
	}

	/**
	 * Generates java.security.PublicKey from byte Array
	 * @param bytes
	 * @return the generated PublicKey Object
	 */
	private static PublicKey generatePublicKeyFromByteArray(byte[] bytes) {
		Key publicKey = null;
		X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			publicKey = kf.generatePublic(ks);
		} catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			publicKey = null;
		}
		return (PublicKey) publicKey;
	}
	
	private static byte[] generateAESKey() {
		KeyGenerator gen = null;
		try {gen = KeyGenerator.getInstance("AES");} catch (NoSuchAlgorithmException e) {}
		gen.init(128); /* 128-bit AES */
		SecretKey secret = gen.generateKey();
		return secret.getEncoded();
	}
		
}

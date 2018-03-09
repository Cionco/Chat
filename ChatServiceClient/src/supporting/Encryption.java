package supporting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;

public class Encryption {
	
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
	
	public static byte[] encrypt(PublicKey key, byte[] plaintext) {
	    try {
	    	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");   
	    	cipher.init(Cipher.ENCRYPT_MODE, key);  
	    	return cipher.doFinal(plaintext);
	    } catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) { return null;}
	}
}

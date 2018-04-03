package encryption;

import java.nio.charset.StandardCharsets;

public class AESEncryption {

	public static byte[] AESEncrypt(byte[] ciphertext, byte[] key) {
		if(ciphertext.length == 0) return null;
		int buffer = 16 - (ciphertext.length % 16);
		buffer %= 16;				//In case ciphertext.length is exactly 16
		for(int i = 0; i < buffer; i++) ciphertext = expand_array(ciphertext, new byte[]{0x00});
		Block.EncryptionBlock block = new Block.EncryptionBlock(arrayToIndex(15, ciphertext));
		Block.Key blockkey = new Block.Key(key);
		
		block.encrypt(blockkey);

		byte[] encrypted_block = block.toByteArray();		
		//block.print();
		byte[] encrypted_message = AESEncrypt(arrayFromIndex(16, ciphertext), key);
		if(encrypted_message == null) return encrypted_block;
		return expand_array(encrypted_block, encrypted_message);
	}
	
	public static byte[] AESDecrypt(byte[] encrypted, byte[] key) {
		if(encrypted.length == 0) return null;
		int buffer = 16 - (encrypted.length % 16);
		buffer %= 16;			//In case encrypted.length is exactly 16
		for(int i = 0; i < buffer; i++) encrypted = expand_array(encrypted, new byte[]{0x00});
		Block.DecryptionBlock block = new Block.DecryptionBlock(arrayToIndex(15, encrypted));
		Block.Key blockkey = new Block.Key(key);
		
		block.decrypt(blockkey);
		
		byte[] decrypted_block = block.toByteArray();
		
		byte[] decrypted_message = AESDecrypt(arrayFromIndex(16, encrypted), key);
		if(decrypted_message == null) return decrypted_block;
		return expand_array(decrypted_block, decrypted_message);
	}
	
	/**
	 * Removes first startindex elements from the array
	 * @param startindex
	 * @param array
	 * @return the original array without the first startindex values 
	 */
	private static byte[] arrayFromIndex(int startindex, byte[] array) {
		byte[] newarray = new byte[array.length - startindex];
		for(int i = startindex; i < array.length; i++) newarray[i - startindex] = array[i];
		return newarray;
	}
	
	/**
	 * Removes the last elements from the array
	 * @param endindex
	 * @param array
	 * @return the original array until endindex
	 */
	private static byte[] arrayToIndex(int endindex, byte[] array) {
		byte[] newarray = new byte[endindex + 1];
		for(int i = 0; i <= endindex; i++) newarray[i] = array[i];
		return newarray;
	}
	
	/**
	 * Concatinates two arrays
	 * @param array_one
	 * @param array_two
	 * @return new array with the length of both inputs combined. array_two[0] is the first element after the last of array_one
	 */
	private static byte[] expand_array(byte[] array_one, byte[] array_two) {
		byte[] newarray = new byte[array_one.length + array_two.length];
		for(int i = 0; i < array_one.length; i++)
			newarray[i] = array_one[i];
		for(int i = array_one.length; i < newarray.length; i++) 
			newarray[i] = array_two[i - array_one.length];
		return newarray;
	}
	

}

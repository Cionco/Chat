package encryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import supporting.ConnectionSocket;

public class Encryption {
	
	private static Integer[] masks = new Integer[]{0xff000000, 0x00ff0000, 0x0000ff00, 0x000000ff};
	
	private static final int ENCRYPTION_KEY_LENGTH = 0x400;
	
	public static void main(String[] args) {
		//initializeEncryptionProtocol(null);
		byte[] key;
		byte[] cipher;
		int i = 0;
		key = new byte[]{0x2b, 0x28, (byte) 0xab, 0x09, 0x7e, (byte) 0xae, (byte) 0xf7, (byte) 0xcf, 0x15, (byte) 0xd2, 0x15, 0x4f, 0x16, (byte) 0xa6, (byte) 0x88, 0x3c};
		cipher = new byte[]{0x32, (byte) 0x88, 0x31, (byte) 0xe0, 0x43, 0x5a, 0x31, 0x37, (byte) 0xf6, 0x30, (byte) 0x98, 0x07, (byte) 0xa8, (byte) 0x8d, (byte) 0xa2, 0x34};
		cipher = new byte[]{0x34, 0x19};
		byte[] encrypted = AESEncrypt(cipher, key);
		for(byte b : encrypted) { 
			if(i % 16 == 0 && i != 0) System.out.println();
			if(i++ % 4 == 0 && i != 0) System.out.println();
			System.out.printf("%02x", b);
		}
		
		
		System.out.println();
		System.out.println("----------------------------DECRYPTION DEBUG-----------------------------");
		
		for(byte b : AESDecrypt(encrypted, key)) { 
			if(i % 16 == 0 && i != 0) System.out.println();
			if(i++ % 4 == 0 && i != 0) System.out.println();
			System.out.printf("%02x", b);
		}
	}	

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
		int buffer = 16 - (ciphertext.length % 16);
		buffer %= 16;				//In case ciphertext.length is exactly 16
		for(int i = 0; i < buffer; i++) ciphertext = expand_array(ciphertext, new byte[]{0x00});
		EncryptionBlock block = new EncryptionBlock(arrayToIndex(15, ciphertext));
		Key blockkey = new Key(key);
		
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
		DecryptionBlock block = new DecryptionBlock(arrayToIndex(15, encrypted));
		Key blockkey = new Key(key);
		
		block.decrypt(blockkey);
		
		byte[] decrypted_block = block.toByteArray();
		
		byte[] decrypted_message = AESDecrypt(arrayFromIndex(16, encrypted), key);
		if(decrypted_message == null) return decrypted_block;
		return expand_array(decrypted_block, decrypted_message);
	}
	
	private static byte[] arrayFromIndex(int startindex, byte[] array) {
		byte[] newarray = new byte[array.length - startindex];
		for(int i = startindex; i < array.length; i++) newarray[i - startindex] = array[i];
		return newarray;
	}
	
	private static byte[] arrayToIndex(int endindex, byte[] array) {
		byte[] newarray = new byte[endindex + 1];
		for(int i = 0; i <= endindex; i++) newarray[i] = array[i];
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
	
	private static abstract class Block {
		protected byte[] bytes;
		protected static final boolean DEBUG_FLAG = false;
		
		private static final int[] sbox = new int[]{
			0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76, 
			0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
			0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
			0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
			0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
			0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
			0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
			0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
			0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
			0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
			0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
			0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
			0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
			0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
			0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
			0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
		};
		
		public Block(byte[] bytes) {
			this.bytes = bytes;
		}
		
		public Block(int[] DWORDS) {
			bytes = new byte[16];
			for(int i = 0; i < 4; i++)
				setDWORD(i, DWORDS[i]);
		}
		
		protected byte[] subBytes(byte[] bytes) {
			for(int i = 0; i < bytes.length; i++)
				subByte(bytes, i);
			return bytes;
		}
		
		public void subBytes() {
			for(int i = 0; i < bytes.length; i++)
				subByte(i);
		}
		
		protected void shiftWords() {
			for(int i = 1; i <= 3; i++)
				shiftWord(i, i);
		}
		
		protected abstract void shiftWord(int index, int bytes);
		
		public int getDWORD(int index) {
			int DWORD = 0x00000000;
			for(int i = 0; i < 4; i++)
				DWORD |= (bytes[i + index * 4] << (3 - i) * 8) & masks[i];
			
			return DWORD;
		}
		
		/**
		 * Get Vertical DWORD
		 * @param index starts at 0
		 * @return
		 */
		public int getVDWORD(int index) {
			int DWORD = 0x00000000;
			for(int i = 0; i < 4; i++)
				DWORD |= (bytes[i * 4 + index] << (3 - i) * 8) & masks[i];
			return DWORD;
		}
		
		public void setDWORD(int index, int DWORD) {
			for(int i = 0; i < 4; i++)
				bytes[i + index * 4] = (byte) (DWORD >> (( 3 - i) * 8) & 0xFF);
		}
		
		public void setVDWORD(int index, int DWORD) {
			for(int i = 0; i < 4; i++)
				bytes[i * 4 + index] = (byte) (DWORD >> ((3 - i) * 8) & 0xFF);
		}
		
		protected int index(int wordIndex, int byteIndex) {
			return wordIndex * 4 + byteIndex;
		}
		
		public void print() {
			for(int i = 0; i < 4; i++)
				System.out.printf("%08x\n", getDWORD(i));
		}

		public void subByte(int index) {
			bytes[index] = (byte) sbox[((int) bytes[index]) & 0xff];
		}
		
		protected void subByte(byte[] bytes, int index) {
			bytes[index] = (byte) sbox[((int) bytes[index]) & 0xff];
		}
		
		/**
		 * Shifts a DWORD "bits" bits to the left moving the overflowing MSB to LSB
		 * @param DWORD
		 * @param bits
		 * @return
		 */
		protected int OFLeftShift(int DWORD, int bits) {
			return (DWORD << bits) | (DWORD >>> 32 - bits);
		}
		
		protected int OFRightShift(int DWORD, int bits) {
			return (DWORD >>> bits) | (DWORD << 32 - bits);
		}
		
		@Deprecated
		private byte OFLeftShift(byte BYTE, int bits) {
			return (byte) (BYTE << bits | ((BYTE & 0xff) >>> (8 - bits)));
		}
		
		@Deprecated
		private byte OFRightShift(byte BYTE, int bits) {
			return (byte) (((BYTE & 0xff) >>> bits) | (BYTE << (8 - bits)));
		}
	
		protected byte[] DWORDtoByteArray(int DWORD) {
			byte[] b = new byte[4];
			for(int i = 0; i < 4; i++)
				b[i] = (byte) (DWORD >> (( 3 - i) * 8) & 0xFF);
			return b;
		}
		
		protected int ByteArrayToDWORD(byte[] b) {
			int DWORD = 0x00000000;
			for(int i = 0; i < 4; i++)
				DWORD |= (b[i] << (3 - i) * 8) & masks[i];
			return DWORD;
		}
		
		
		public byte[] toByteArray() {
			return bytes;
		}
	}
	
	private static class Key extends Block {

		private int round = 0;
		
		public Key(int round) {
			super(new int[]{0x00000000, 0x00000000, 0x00000000, 0x00000000});
			this.round = round;
		}
		
		public Key(byte[] bytes) {
			super(bytes);
			this.round = 0;
		}
	
	
		public Key nextRoundKey() {
			Key newRoundKey = new Key(round + 1);
			int DWORD = getVDWORD(3);
			DWORD = OFLeftShift(DWORD, 8);
			DWORD = ByteArrayToDWORD(subBytes(DWORDtoByteArray(DWORD)));
			newRoundKey.setVDWORD(0, (DWORD ^ getVDWORD(0)) ^ Key.getRcon(round + 1));
			for(int i = 0; i < 3; i++) 
				newRoundKey.setVDWORD(i + 1, getVDWORD(i + 1) ^ newRoundKey.getVDWORD(i));
		
			return newRoundKey;
		}
		
		private static int getRcon(int a) {
			if(a == 0) return 0x00;
			byte[] rcon = new byte[a];
			for (int i = 0, x = 1; i < rcon.length; i++) {
				rcon[i] = (byte) x;
		    	x = (byte) ((x << 1) ^ (((x & 0x80) != 0) ? 0x1B:0x00));
			}
			return (rcon[rcon.length - 1] << 24) & masks[0];
		}

		
		@Override
		protected void shiftWord(int index, int bytes) {
		}	
	}
	
	private static class EncryptionBlock extends Block {	
		public EncryptionBlock(byte[] bytes) {
			super(bytes);
		}
		
		public void encrypt(Key key) {
			addRoundKey(key);
			key = key.nextRoundKey();
			
			if(DEBUG_FLAG) {
				System.out.println("After Round Key");
				this.print();			
			}
			
			for(int i = 0; i < 10; key = key.nextRoundKey(), i++) {
				if(DEBUG_FLAG){ 
					System.out.println("---------------------------------");
					System.out.println("Round " + i);
					System.out.println("---------------------------------");
					System.out.println("Round Key: ");
					key.print();
					System.out.println("---------------------------------");
				}
				subBytes();
				if(DEBUG_FLAG) {
					System.out.println("After subbytes");
					this.print();
				}
				shiftWords();
				if(DEBUG_FLAG) {
					System.out.println("After shiftWords");
					this.print();
				}
				if(i != 9) {
					mixColumns();
					if(DEBUG_FLAG) {
						System.out.println("After mixColumns");
						this.print();
					}
				}
				addRoundKey(key);
				if(DEBUG_FLAG) {
					System.out.println("After Round Key");
					this.print();
				}
			}
		}
		
		private void mixColumns() {
			for(int i = 0; i < 4; i++)
				setVDWORD(i, ByteArrayToDWORD(mixColumn(DWORDtoByteArray(getVDWORD(i)))));
		}
		
		private void addRoundKey(Key key) {
			for(int i = 0; i < 4; i++)
				this.setDWORD(i, getDWORD(i) ^ key.getDWORD(i));
		}
			
		private byte[] mixColumn(byte[] r) {
			byte a[] = new byte[4];
			byte b[] = new byte[4];
			byte c;
			byte h;
			/* The array 'a' is simply a copy of the input array 'r'
			 * The array 'b' is each element of the array 'a' multiplied by 2
			 * in Rijndael's Galois field
			 * a[n] ^ b[n] is element n multiplied by 3 in Rijndael's Galois field */ 
			for (c = 0; c < 4; c++) {
				a[c] = r[c];
				/* h is 0xff if the high bit of r[c] is set, 0 otherwise */
				h = (byte)(r[c] >> 7); /* arithmetic right shift, thus shifting in either zeros or ones */
				b[c] = (byte) (r[c] << 1); /* implicitly removes high bit because b[c] is an 8-bit char, so we xor by 0x1b and not 0x11b in the next line */
				b[c] ^= 0x1B & h; /* Rijndael's Galois field */
			}
			r[0] = (byte) (b[0] ^ a[3] ^ a[2] ^ b[1] ^ a[1]); /* 2 * a0 + a3 + a2 + 3 * a1 */
			r[1] = (byte) (b[1] ^ a[0] ^ a[3] ^ b[2] ^ a[2]); /* 2 * a1 + a0 + a3 + 3 * a2 */
			r[2] = (byte) (b[2] ^ a[1] ^ a[0] ^ b[3] ^ a[3]); /* 2 * a2 + a1 + a0 + 3 * a3 */
			r[3] = (byte) (b[3] ^ a[2] ^ a[1] ^ b[0] ^ a[0]); /* 2 * a3 + a2 + a1 + 3 * a0 */
			
			return r;
		}
		
		@Override
		protected void shiftWord(int index, int bytes) {
			int DWORD = getDWORD(index);
			DWORD = OFLeftShift(DWORD, 8 * bytes);
			setDWORD(index, DWORD);
		}
	}
	
	private static class DecryptionBlock extends Block {
		private static final int[] i_sbox = new int[]{
				0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
				0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
				0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
				0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
				0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
				0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
				0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
				0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
				0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
				0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
				0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
				0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
				0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
				0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
				0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
				0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d,
		};
		
		Key[] roundKeys = new Key[10];
		
		public DecryptionBlock(byte[] bytes) {
			super(bytes);
		}
		
		public void decrypt(Key key) {
			Key lastRound = key;
			for(int i = 0; i < roundKeys.length; lastRound = roundKeys[i++]) 
				roundKeys[i] = lastRound.nextRoundKey();
			
			for(int i = 9; i >= 0; i--) {
				if(DEBUG_FLAG){ 
					System.out.println("---------------------------------");
					System.out.println("Round " + i);
					System.out.println("---------------------------------");
					System.out.println("Round Key: ");
					roundKeys[i].print();
					System.out.println("---------------------------------");
				}
				
				addRoundKey(roundKeys[i]);
				if(DEBUG_FLAG) {
					System.out.println("After Round Key");
					this.print();
				}
				
				if(i != 9) {
					mixColumns();
					if(DEBUG_FLAG) {
						System.out.println("After mixColumns");
						this.print();
					}
				}
				
				shiftWords();
				if(DEBUG_FLAG) {
					System.out.println("After shiftWords");
					this.print();
				}
				
				subBytes();
				if(DEBUG_FLAG) {
					System.out.println("After subBytes");
					this.print();
				}
			}
			
			addRoundKey(key);
			if(DEBUG_FLAG) {
				System.out.println("After Round Key");
				this.print();
			}
		}
		
		public void subBytes() {
			for(int i = 0; i < bytes.length; i++)
				subByte(i);
		}
		
		private void mixColumns() {
			for(int i = 0; i < 4; i++) 
				setVDWORD(i, ByteArrayToDWORD(mixColumn(DWORDtoByteArray(getVDWORD(i)))));
		}
		
		private void addRoundKey(Key key) {
			for(int i = 0; i < 4; i++)
				this.setDWORD(i, getDWORD(i) ^ key.getDWORD(i));
		}
		
		private byte[] mixColumn(byte[] r) {
			byte a[] = new byte[4]		//09 times r
				, b[] = new byte[4]		//0B times r
				, c[] = new byte[4]		//0D times r
				, d[] = new byte[4];	//0E times r
			byte x;
			
			for(int i = 0; i < 4; i++) {
				x = r[i];
				a[i] = (byte) (x ^ GFmult2(GFmult2(GFmult2(x)))); //because 9x = (((x * 2) * 2) * 2) + x
				b[i] = (byte) (x ^ GFmult2((byte) (x ^ GFmult2(GFmult2(x))))); //because Bx = ((((x * 2) * 2) + x) * 2) + x
				c[i] = (byte) (x ^ GFmult2(GFmult2((byte) (x ^ GFmult2(x))))); //because Dx = ((((x * 2) + x) * 2) * 2) + x
				d[i] = GFmult2((byte) (x ^ GFmult2((byte) (x ^ GFmult2(x))))); //because Ex = ((((x * 2) + x) * 2) + x) * 2
			}
			
			r[0] = (byte) (a[3] ^ b[1] ^ c[2] ^ d[0]);
			r[1] = (byte) (a[0] ^ b[2] ^ c[3] ^ d[1]);
			r[2] = (byte) (a[1] ^ b[3] ^ c[0] ^ d[2]);
			r[3] = (byte) (a[2] ^ b[0] ^ c[1] ^ d[3]);			
			
			return r;
		}
		
		private byte GFmult2(byte b) {
			byte h;
			h = (byte) (b >> 7);
			b = (byte) (b << 1);
			b ^= 0x1B & h;
			return b;
		}
		
		public void subByte(int index) {
			bytes[index] = (byte) i_sbox[((int) bytes[index]) & 0xff];
		}
		
		@Override
		protected void shiftWord(int index, int bytes) {
			int DWORD = getDWORD(index);
			DWORD = OFRightShift(DWORD, 8 * bytes);
			setDWORD(index, DWORD);
		}
	}
	//GENERATE ANOTHER ABSTRACT CLASS EnDeCryptBlock that holds all methods needed for both, Decrypt and Encrypt
}

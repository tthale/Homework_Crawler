// Ref: https://stackoverflow.com/questions/40004858/encrypt-in-python-and-decrypt-in-java-with-aes-cfb
package encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



// Ref: https://stackoverflow.com/questions/40004858/encrypt-in-python-and-decrypt-in-java-with-aes-cfb

class Decrypt {
	
	Integer SEGMENT_SIZE = 16;	// CRITICAL: These sizes need to exactly match that of the encrypter
	static Integer KEY_SIZE = 32;

	private static String key;

	public Decrypt() {
		super();
		String path = System.getenv("PATH");
		key = path.substring(0, KEY_SIZE);	// CRITICAL: This key & key size must match the seed used by the encrypter
	}

	public static String decrypt(String encrypted_encoded_string) throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{

      String plain_text = "";
      try{
          byte[] encrypted_decoded_bytes = Base64.getDecoder().decode(encrypted_encoded_string);
          String encrypted_decoded_string = new String(encrypted_decoded_bytes);
          String iv_string = encrypted_decoded_string.substring(0,16); //IV is retrieved correctly.

          IvParameterSpec iv = new IvParameterSpec(iv_string.getBytes());
          SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

          Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
          cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

          plain_text = new String(cipher.doFinal(encrypted_decoded_bytes));//Returns garbage characters
          return plain_text;

      }  catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
      }

      return plain_text;
   }

	public static void main(String[] args) 
	{
		String encrypted = "SG9tZXdvcmtfQ3Jhd2xlcqKM3fs+YLrpxW/mlpBQKVH9X9CWPU0RjmktSR4dofAm" ;
		Decrypt decrypt = new Decrypt();
		
		try {
			String plain_text = decrypt.decrypt(encrypted);
			System.out.print("decrypted string='" + plain_text + "'");
		} catch (Exception exc) {
			System.err.print("decryption exception" + exc.getClass().getSimpleName() + ":" + exc.getMessage());
		}
	}

}

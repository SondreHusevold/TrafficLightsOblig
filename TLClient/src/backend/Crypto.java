package backend;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;

/*!
 * Cryptographic class.
 * 
 * This class is used to decrypt and encrypt the input and output from the server/client using an AES with an 128-bit key.
 * It has a symmetrical key as seen below and will use a random generated 16-byte IV which is sent with the message to always output
 * different encrypted results every time.
 * 
 * This will ensure that no one will be able to see the different commands sent by the server/client to interact which means
 * no one can act like the server and send the stop command to the clients without being the server with the correctly encrypted commands.
 * 
 */

public class Crypto {
	
	private final static String key = "MuHFSAtwjwfRfXss";
	
	/*!
	 * Encrypts input by using the incoming IV and the command.
	 * It uses AES with the static key before returning a Base64 encoded String which makes it easy to decode into bytes again once it is received
	 * on the other end.
	 * 
	 */
	public static String encrypt(String message, String initVector) {
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(message.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
	
	/*!
	 * Decrypts the input by using the encrypted text and the IV. 
	 * 
	 * Will first decode the IV into a 16-byte array and then use the static symmetric key to decrypt the message through AES.
	 * 
	 */

	 public static String decrypt(String encrypted, String initVector) {
	        try {
	            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector));
	            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

	            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

	            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

	            return new String(original);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }

	        return null;
	 }


	 /*!
	  * Creates a random IV.
	  * 
	  * This is used to ensure that the encrypted message is different every single time it is sent. If this wasn't the case
	  * it would be easy to see that the same messages are being sent as the encrypted text would be the same every time.
	  * 
	  */
	 public static String getRandomIV(){
		 SecureRandom r = new SecureRandom();
		 byte[] IV = new byte[16];
		 r.nextBytes(IV);
		 return Base64.getEncoder().withoutPadding().encodeToString(IV);
	 }
}
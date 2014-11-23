package chatchat;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CryptoModule {

	
	public static SecretKey desKeyGenarator() {
		try {
		      KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
		      SecretKey myDesKey = keygenerator.generateKey();
		      
		      return myDesKey;			
		}
		catch( Exception e )
		{
			return null;
		}

	}
	  public static byte[] desEncrypt(String plainText, SecretKey desKey) throws Exception {
		    Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		    cipher.init(Cipher.ENCRYPT_MODE, desKey);
		    return cipher.doFinal(plainText.getBytes("UTF-8"));
		  }
		 
		  public static String desDecrypt(byte[] cipherText, SecretKey desKey) throws Exception{
		    Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		    cipher.init(Cipher.DECRYPT_MODE, desKey);
		    return new String(cipher.doFinal(cipherText),"UTF-8");
		  }
		  
		  public static SecretKey aesKeyGenerator() {
			  try {
			      KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
			      SecretKey myAesKey = keygenerator.generateKey();				  
			      return myAesKey;
			  }
			  catch( Exception e )
			  {
				  return null;
			  }
		  }
		  
	  public static byte[] aesEncrypt(String plainText, SecretKey aesKey) throws Exception {
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
		    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
		    return cipher.doFinal(plainText.getBytes("UTF-8"));
		  }
		 
	  public static String aesDecrypt(byte[] cipherText, SecretKey aesKey) throws Exception{
	    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
	    cipher.init(Cipher.DECRYPT_MODE, aesKey);
	    return new String(cipher.doFinal(cipherText),"UTF-8");
	  }
	  
	  public static KeyPair rsaKeyGenerator() {
		  try {
			   KeyPairGenerator clsKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
			   clsKeyPairGenerator.initialize(2048);
			   
			   KeyPair clsKeyPair = clsKeyPairGenerator.genKeyPair();
			   
			   return clsKeyPair;
			  
		  }
		  catch( Exception e )
		  {
			  return null;
		  }

	  }

		public static byte[] rsaEncrypt(String plainText, Key publicKey) throws Exception {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(plainText.getBytes("UTF-8"));
			}
			
		public static String rsaDecrypt(byte[] cipherText, Key privateKey) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(cipher.doFinal(cipherText),"UTF-8");
		}
			 	  
	  
}

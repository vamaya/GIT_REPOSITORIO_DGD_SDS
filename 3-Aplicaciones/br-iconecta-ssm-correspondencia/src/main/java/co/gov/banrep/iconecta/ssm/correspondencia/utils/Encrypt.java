package co.gov.banrep.iconecta.ssm.correspondencia.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {

	 private static final String key = "BancoRep2018*/14";
	
	 public static String encrypt(String initVector, String value) {
	        try {
	            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
	            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

	            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

	            byte[] encrypted = cipher.doFinal(value.getBytes());
	           

	            return Base64.getEncoder().encodeToString(encrypted);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }

	        return null;
	    }

	public static String decrypt(String initVector, String encrypted) throws Exception {

		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

		byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

		return new String(original);

	}
	
	
}

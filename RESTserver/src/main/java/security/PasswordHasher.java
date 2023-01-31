package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PasswordHasher {
	
	@ToString
	@AllArgsConstructor
	@Getter
	public static class HashedPassword {
		byte[] salt;
		byte[] hashedPwd;
	}
	
	public static HashedPassword hash(String pwd) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[64];
		random.nextBytes(salt);
		
		byte[] hashedPwd = hash(pwd, salt);
		
		return new HashedPassword(salt, hashedPwd);
	}
	
	private static byte[] hash(String pwd, byte[] salt) {

		byte[] pwdHash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt);
			//equivalent to calling one more update() and then digest()
			pwdHash = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
			
		} catch (NoSuchAlgorithmException e) {
			log.error("SHA-512 not available", e);
		} 
		
		return pwdHash;
	}
	
	
	public static boolean checkPassword(String pwd,
			byte[] hashedPwd, byte[] salt) {
		//array1.EQUALS(array2) IS THE SAME AS array1 == array2
		//So, it has to be compared differently!
		return Arrays.equals(hashedPwd, hash(pwd, salt));
	}
}

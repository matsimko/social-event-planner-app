package clients;

import java.util.Base64;

public class BasicAuthenticationEncoder {
	
	public static String encode(String username, String pwd ) {
		String credentials = username + ":" + pwd;
		String header = Base64.getEncoder().encodeToString(credentials.getBytes()).toString();
		header = "Basic " + header;
		return header;
	}
}

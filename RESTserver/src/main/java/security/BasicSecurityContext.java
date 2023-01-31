package security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class BasicSecurityContext implements SecurityContext {
	
	private boolean secure;
	private UserDetails userDetails;
	
	public BasicSecurityContext(UserDetails userDetails, boolean secure) {
		this.userDetails = userDetails;
		this.secure = secure;
	}
	
	@Override
	public boolean isUserInRole(String role) {
		return true;
	}
	
	@Override
	public boolean isSecure() {
		return secure;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return userDetails;
	}
	
	@Override
	public String getAuthenticationScheme() {
		return "Basic";
	}
}

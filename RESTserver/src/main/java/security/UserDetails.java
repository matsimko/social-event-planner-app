package security;

import java.security.Principal;

import domain.UserEntity;

public class UserDetails implements Principal {

	private UserEntity user;
	
	public UserDetails(UserEntity user) {
		this.user = user;
	}
	
	@Override
	public String getName() {
		return user.getUsername();
	}
	
	//is this reasonable???
	public UserEntity getUser() {
		return user;
	}
	
}

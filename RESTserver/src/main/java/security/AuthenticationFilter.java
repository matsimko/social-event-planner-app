package security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import domain.UserEntity;
import lombok.extern.log4j.Log4j2;
import repositories.UserRepository;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
@Log4j2
public class AuthenticationFilter implements ContainerRequestFilter {

	//Basic HTTP auth has the following format:
	//"Basic <credentials>", where <credentials> is base64 encoded "username:password"
	private static final String AUTHENTICATION_SCHEME = "Basic";
	
	@Inject
	UserRepository userRepo;
	

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isBasicAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            log.info("Authorization isn't of the type Basic");
            return;
        }

        UserEntity user = null;
        try {
            user = validateCredentials(authorizationHeader);

        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
            return;
        }
        
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        
        //set a new security context with UserDetails as its principal
        //so that the the UserEntity can be retrieved inside the Resourse classes
        UserDetails userDetails = new UserDetails(user);
        SecurityContext securityContext = new BasicSecurityContext(userDetails,
        		currentSecurityContext.isSecure());
        requestContext.setSecurityContext(securityContext);
    }

    private boolean isBasicAuthentication(String authorizationHeader) {
    	// It is case insensitive
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                    .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {

        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response 
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, 
                                AUTHENTICATION_SCHEME)
                        .build());
    }

    

    private UserEntity validateCredentials(String authorizationHeader) throws Exception {
    	//Remove the "Basic"
    	String base64EncodedCredentials = authorizationHeader
    			.substring(AUTHENTICATION_SCHEME.length()).trim();
    	log.info("encoded credentials: {}", base64EncodedCredentials);
    	
    	byte[] decodedCredentials = Base64.getDecoder().decode(base64EncodedCredentials);
    	String credentials = new String(decodedCredentials/*, StandardCharsets.UTF_8*/);
    	log.info("decoded credentials: {}", credentials);
    	
    	//split the credentials into username and password
    	//(the "2" is important, as it allows ":" in the password, because only one split is done)
    	String[] credentialsArray = credentials.split(":", 2);
        if (credentialsArray.length != 2) {
        	throw new Exception();
        }
        //check the credentials against the database
        String username = credentialsArray[0];
        String password = credentialsArray[1];
        UserEntity user = userRepo.findByUsername(username);
        if(user == null) {
        	log.info("User not found in db");
        	throw new Exception();
        }
        
        log.info("hashedPwd={}",
        		new PasswordHasher.HashedPassword(user.getSalt(), user.getHashedPassword()));
        if(!PasswordHasher.checkPassword(password,
        		user.getHashedPassword(), user.getSalt())) {
        	log.info("Pwd doesnt match");
        	throw new Exception();
        }
        
        return user;
    }
}

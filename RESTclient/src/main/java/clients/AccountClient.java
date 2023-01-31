package clients;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dtos.UserDTO;

@Path("/accounts")
public interface AccountClient {
	
	@Path("/user")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void newUser(UserDTO user);
	
}

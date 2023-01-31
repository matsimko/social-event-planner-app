package resources;

import java.io.Console;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import domain.UserEntity;
import dtos.UserDTO;
import lombok.extern.log4j.Log4j2;
import repositories.UserRepository;
import security.PasswordHasher;
import security.PasswordHasher.HashedPassword;

@Log4j2
@Path("/accounts")
public class AccountResource {
	
	@Inject
	UserRepository userRepo;
	
	@POST
	@Path("user")
	@Transactional
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML }) //can consume any of the stated ones
	public Response newUser(UserDTO user) {
		//persist doesn't throw any exception, so the check probably has to be explicit
		if(user.getUsername() == null || user.getUsername().length() == 0
				|| user.getPassword() == null || user.getPassword().length() == 0) {
			return Response.status(422, "Username or password were empty").build();
		}
		if(userRepo.findByUsername(user.getUsername()) != null) {
			return Response.status(409,
					"User with the given username already exists").build();
		}
		
		HashedPassword hashedPwd = PasswordHasher.hash(user.getPassword());
		log.info("hashedPwd=" + hashedPwd);
		log.info("pwd={}", user.getPassword());
		UserEntity userEntity = UserEntity.builder()
				.username(user.getUsername())
				.hashedPassword(hashedPwd.getHashedPwd())
				.salt(hashedPwd.getSalt())
				.build();
		userRepo.persist(userEntity);
		
		return Response.ok().build();
	}
}

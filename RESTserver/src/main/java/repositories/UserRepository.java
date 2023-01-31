package repositories;

import javax.enterprise.context.ApplicationScoped;

import domain.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {
	
	public UserEntity findByUsername(String username) {
		return find("username", username).firstResult();
	}
}

package repositories;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import domain.EventEntity;
import domain.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class EventRepository implements PanacheRepository<EventEntity>{

	//No query is needed, after all...
	public Stream<EventEntity> findByUser(UserEntity user) {
		return user.getParticipations().stream().map(p -> p.getEvent());
	}

	public Stream<EventEntity> findOrganizedByUser(String username) {
		return find("SELECT e FROM EventEntity e JOIN e.organizer o WHERE o.username = ?1",
				username).stream();
	}

}

package repositories;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;

import domain.EventEntity;
import domain.ParticipationEntity;
import domain.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ApplicationScoped
public class ParticipationRepository implements PanacheRepository<ParticipationEntity> {
	public ParticipationEntity newParticipant(Long eventId, UserEntity user) {
		ParticipationEntity participationEntity = ParticipationEntity
				.builder()
				.dateOfJoining(Instant.now())
				//the UserEntity is out of the persist context, so I cannot pass it (merge instead of persist should propably work as well)
				.user(getEntityManager().getReference(UserEntity.class, user.getId()))
				//get only the reference without unnecessarily querying for the EventEntity
				.event(getEntityManager().getReference(EventEntity.class, eventId))
				.build();
		persist(participationEntity);
		
		
		return participationEntity;
	}

	
	public int removeParticipant(Long eventId, UserEntity user) {
		Query query = getEntityManager().createQuery("DELETE FROM ParticipationEntity p "
				+ "WHERE p.user = ?1 AND p.event.id = ?2");
		query.setParameter(1, user); //here it works just fine, because it is not INSERT/UPDATE
		query.setParameter(2, eventId);
		return query.executeUpdate();
	}
}

package domain;

import java.time.Instant;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ParticipationId.class) //composite key
@Table(name="Participation")
@Entity
public class ParticipationEntity {
	
	@Id
	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private UserEntity user;
	
	@Id
	@ManyToOne
	@JoinColumn(name="event_id", nullable = false)
	private EventEntity event;
	
	@Builder.Default
	private Instant dateOfJoining = Instant.now(); //default value
	
}

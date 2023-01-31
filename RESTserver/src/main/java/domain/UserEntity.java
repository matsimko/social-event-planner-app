package domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
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
@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"User\"") //User is a reserved word
@Entity
public class UserEntity extends BaseEntity {
	//this makes it NOT NULL in the db, unlike @NotNull 
	//(but @NotNull has better performance, because it checks it without sending an INSERT to the db) 
	@Column(nullable = false, unique = true)
	private String username;
	
	@ToString.Exclude
	@Lob
	@Column(nullable = false)
	private byte[] salt;
	
	@ToString.Exclude
	@Lob
	@Column(nullable = false)
	private byte[] hashedPassword;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(mappedBy = "user")
	Set<ParticipationEntity> participations;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(mappedBy = "organizer")
	Set<EventEntity> organizedEvents;
}

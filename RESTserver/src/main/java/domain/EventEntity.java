package domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
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
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name="Event")
@Entity
public class EventEntity extends BaseEntity {
	@Column(nullable = false)
	private String name;
	
	@Lob //so that the string is stored as a Text in the db
	@Column(columnDefinition="clob") //the lob would by default be 255 chars long...
	private String description;
	
	private Instant date;
	
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE)
	private List<ImageEntity> images;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE)
	private Set<ParticipationEntity> participations;
	
	@ManyToOne
	@JoinColumn(name="organizer_id", nullable = false)
	private UserEntity organizer;
	
	public String getDescriptionTease() {
		if(description == null || description.length() <= 200) {
			return description;
		}
		return description.substring(0, 200);
	}
}

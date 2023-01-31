package domain;

import java.io.File;
import java.time.Instant;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name="Image")
@Entity
public class ImageEntity extends BaseEntity{
	@Lob
    @Basic(fetch = FetchType.LAZY)
	File file; 
	
	@ManyToOne
	@JoinColumn(name="event_id")
	EventEntity event;
	
}

package dtos;

import java.time.Instant;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@XmlRootElement
public class EventDTO {
	private Long id;
	private String name;
	private String description;
	private Instant date;
	private int numberOfParticipants;
	private String organizerUsername;
}

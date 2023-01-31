package dtos;

import java.time.Instant;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@XmlRootElement
public class ParticipantDTO {
	private String username;
	private Instant dateOfJoining;
}

package dtos;

import java.io.File;
import java.time.Instant;
import java.util.List;

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
public class EventDetailDTO {
	private Long id;
	private String name;
	private String description;
	private Instant date;
	private List<File> images;
	//
	private List<ParticipantDTO> participants;
}

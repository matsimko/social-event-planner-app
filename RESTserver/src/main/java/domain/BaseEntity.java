package domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@MappedSuperclass
@EqualsAndHashCode
public class BaseEntity {

	@Getter
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
}

package test;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import clients.AccountClient;
import clients.BasicAuthenticationEncoder;
import clients.EventClient;
import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;
import dtos.UserDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@TestMethodOrder(OrderAnnotation.class)
public class clientTest {
	private <T> T getClient(Class<T> clientClass) {
		Map<String, Object> properties = new HashMap<String, Object>();

		ObjectMapper mapper = new ObjectMapper();
		// Register the JavaTimeModule so that class like Instant
		// can be serialized/deserialized
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		JacksonJsonProvider jsonProvider = new JacksonJaxbJsonProvider()
				.disable(SerializationFeature.WRAP_ROOT_VALUE)
				.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
		jsonProvider.setMapper(mapper);

		T client = JAXRSClientFactory.create("http://localhost:8000", clientClass,
				Collections.singletonList(jsonProvider),
				properties, true);
		return client;
	}

	public String randomUsername() {
		String username = RandomStringUtils.randomAlphabetic(10);
		log.info("generated username={}", username);
		return username;
	}
	
	private String username = randomUsername();

	@Test
	@Order(1)
	public void test() {
		AccountClient client = getClient(AccountClient.class);
		UserDTO user = UserDTO.builder().username(username).password("123").build();
		client.newUser(user);

		EventClient eventClient = getClient(EventClient.class);
		String encodedCredentials = BasicAuthenticationEncoder.encode(user.getUsername(), user.getPassword());
		log.info("encoded credentials: {}", encodedCredentials);

		WebClient.client(eventClient).header(HttpHeaders.AUTHORIZATION, encodedCredentials);

		EventDetailDTO event = EventDetailDTO.builder().name("My event").build();
		Long eventId = eventClient.newEvent(event);
		event.setId(eventId);

		WebClient.client(eventClient).reset();
		List<EventDTO> events = eventClient.getAllEvents();
		log.info("All events:\n{}", events);

		events = eventClient.getEventsOrganizedByUser(user.getUsername());
		log.info("Organized events:\n{}", events);

		WebClient.client(eventClient).header(HttpHeaders.AUTHORIZATION, encodedCredentials);
		ParticipantDTO participant = eventClient.joinEvent(event.getId());
		log.info(participant);

		events = eventClient.getEventsForUser(user.getUsername());
		log.info("User's events:\n{}", events);

		log.info(eventClient.leaveEvent(event.getId()));

		events = eventClient.getEventsForUser(user.getUsername());
		log.info("User's events:\n{}", events);
		
		event = EventDetailDTO.builder().name("2nd event").build();
		eventId = eventClient.newEvent(event);
		event.setId(eventId);
		events = eventClient.getEventsOrganizedByUser(user.getUsername());
		log.info("Organized events:\n{}", events);
		eventClient.deleteEvent(event.getId());
		events = eventClient.getEventsOrganizedByUser(user.getUsername());
		log.info("Organized events after delete:\n{}", events);
		
		EventDetailDTO eventDetail = eventClient.getEvent(events.get(0).getId());
		log.info("eventDetail:{}", eventDetail);
		eventDetail.setDescription("Notice: The event will take place in 5 days!");
		eventDetail.setDate(Instant.now().plus(Period.ofDays(5)));		
		eventClient.updateEvent(eventDetail.getId(), eventDetail);
		
		eventClient.joinEvent(eventDetail.getId());
		eventDetail = eventClient.getEvent(events.get(0).getId());
		log.info("eventDetail:{}", eventDetail);
		
	}
}

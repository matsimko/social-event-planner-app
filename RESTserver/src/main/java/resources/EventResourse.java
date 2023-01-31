package resources;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import domain.EventEntity;
import domain.ImageEntity;
import domain.ParticipationEntity;
import domain.UserEntity;
import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;
import lombok.extern.log4j.Log4j2;
import repositories.EventRepository;
import repositories.ImageRepository;
import repositories.ParticipationRepository;
import security.Secured;
import security.UserDetails;

@Log4j2
@Path("/events")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class EventResourse {
	
	@Inject
	EventRepository eventRepo;
	
	@Inject
	ParticipationRepository participationRepo;
	
	@Inject
	ImageRepository imageRepo;
	
	public UserEntity getUser(SecurityContext securityContext) {
		UserDetails userDetails = (UserDetails) securityContext.getUserPrincipal();
		UserEntity user = userDetails.getUser();
		return user;
	}
	
	@GET
	@Path("")
	public List<EventDTO> getAllEvents() {
		log.info("getAllEvents called");
		log.info(eventRepo.count());
		List<EventDTO> events = eventEntityStreamToDTOList(eventRepo.streamAll());
		
		return events;
	}
	
	@GET
	@Path("user/{username}")
	@Secured
	public Response getEventsForUser(@PathParam("username") String username,
			@Context SecurityContext securityContext) {

		UserEntity user = getUser(securityContext);
		if (!user.getUsername().equals(username)) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("Requesting events of a different user").build();
		}
		
		Stream<EventEntity> events = eventRepo.findByUser(user);
		List<EventDTO> eventsDTO = eventEntityStreamToDTOList(events);
		
		return Response.ok(eventsDTO).build();
	}
	
	@GET
	@Path("organizer/{username}")
	public Response getEventsOrganizedByUser(@PathParam("username") String username) {
		Stream<EventEntity> events = eventRepo.findOrganizedByUser(username);
		List<EventDTO> eventsDTO = eventEntityStreamToDTOList(events);
		
		return Response.ok(eventsDTO).build();
	}
	
	@GET
	@Path("event/{event_id}")
	public Response getEvent( @PathParam("event_id") Long eventId) {
		EventEntity eventEntity = eventRepo.findById(eventId);
		if(eventEntity == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		EventDetailDTO eventDetailDto = eventEntityToDTODetail(eventEntity);
		
		return Response.ok(eventDetailDto).build();
	}
	
	@PUT
	@Path("event/{event_id}")
	@Transactional
	@Secured
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response updateEvent( @PathParam("event_id") Long eventId, EventDetailDTO event,
			@Context SecurityContext securityContext) {
		UserEntity user = getUser(securityContext);
		
		//I will just ignore the the id in the payload and use the one from the path
		event.setId(eventId);
		EventEntity eventEntity = eventRepo.findById(event.getId());
		if(eventEntity == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} else if(!eventEntity.getOrganizer().equals(user)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		
		eventEntity.setDate(event.getDate());
		eventEntity.setDescription(event.getDescription());
		imageRepo.delete("event", eventEntity);
		insertEventImages(eventEntity, event);
		eventEntity.setName(event.getName());
		
		log.info("updatedEntity: {}", eventEntity);
		
		return Response.ok().build();
				
	}
	
	
	@POST
	@Path("event")
	@Transactional
	@Secured
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response newEvent(EventDetailDTO event, @Context SecurityContext securityContext) {
		UserEntity user = getUser(securityContext);
		if(event.getName() == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		EventEntity eventEntity = eventDetailDTOToEntity(event);
		eventEntity.setOrganizer(user);
		eventRepo.persist(eventEntity);
		
		insertEventImages(eventEntity, event);
		
		return Response.ok(eventEntity.getId()).build();
	}


	@DELETE
	@Path("event/{event_id}")
	@Transactional
	@Secured
	public Response deleteEvent(@PathParam("event_id") Long eventId,
			@Context SecurityContext securityContext) {
		UserEntity user = getUser(securityContext);
		EventEntity event = eventRepo.findById(eventId);
		if (event == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		else if(!event.getOrganizer().equals(user)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		
		eventRepo.delete(event);
		
		return Response.ok().build();
	}
	
	@POST
	//including the username in the path would make the url more clear
	@Path("user/event/{event_id}") 
	//data doesn't have to be sent as the user is identified by the authentication
	//@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Transactional
	@Secured
	public Response joinEvent(@PathParam("event_id") Long eventId,
			@Context SecurityContext securityContext) {
		
		UserEntity user = getUser(securityContext);
		ParticipationEntity participationEntity = participationRepo
				.newParticipant(eventId, user);
		//log.info(participationEntity.getEvent().getParticipations());
		ParticipantDTO participantDTO = participationEntityToDTO(participationEntity);
		log.info("joinEvent:" + participantDTO);
		
		return Response.ok(participantDTO).build();
	}
	

	@DELETE
	@Path("user/event/{event_id}")
	@Produces(MediaType.TEXT_PLAIN)
	@Transactional
	@Secured
	public int leaveEvent(@PathParam("event_id") Long eventId,
			@Context SecurityContext securityContext) {
		
		UserEntity user = getUser(securityContext);
		int deletedCount = participationRepo
				.removeParticipant(eventId, user);
		
		return deletedCount;
	}
	
	
	
	
	
	
	private List<EventDTO> eventEntityStreamToDTOList(Stream<EventEntity> eventsStream) {
		List<EventDTO> events = eventsStream
				.map((e) -> eventEntityToDTO(e))
				.collect(Collectors.toList());
		
		return events;
	}

	
	private EventDTO eventEntityToDTO(EventEntity e) {
		return EventDTO.builder()
		.id(e.getId())
		.name(e.getName())
		.date(e.getDate())
		.description(e.getDescriptionTease())
		.numberOfParticipants(e.getParticipations().size())
		.organizerUsername(e.getOrganizer().getUsername())
		.build();
	}
	
	private EventEntity eventDTOToEntity(EventDTO e) {
		return EventEntity.builder()
				.name(e.getName())
				.date(e.getDate())
				.description(e.getDescription())
				.build();
	}
	
	private ParticipantDTO participationEntityToDTO(ParticipationEntity participationEntity) {
		ParticipantDTO participantDTO = 
				new ParticipantDTO(participationEntity.getUser().getUsername(),
						participationEntity.getDateOfJoining());
		return participantDTO;
	}
	
	private EventDetailDTO eventEntityToDTODetail(EventEntity e) {
		return EventDetailDTO.builder()
				.name(e.getName())
				.description(e.getDescription())
				.date(e.getDate())
				.images(e.getImages().stream()
						.map(img -> img.getFile())
						.collect(Collectors.toList()))
				.participants(e.getParticipations().stream()
						.map(this::participationEntityToDTO)
						.collect(Collectors.toList()))
				.id(e.getId())
				.build();		
	}
	
	private EventEntity eventDetailDTOToEntity(EventDetailDTO e) {
		return EventEntity.builder()
				.name(e.getName())
				.date(e.getDate())
				.description(e.getDescription())
				.build();
	}
	

	private void insertEventImages(EventEntity eventEntity, EventDetailDTO event) {
		if(event.getImages() == null) {
			return;
		}
		Stream<ImageEntity> imgStream = event.getImages().stream()
			.map(f -> new ImageEntity(f, eventEntity));
		imageRepo.persist(imgStream);
	}
}



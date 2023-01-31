package clients;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;


@Path("/events")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface EventClient {
	

	
	@GET
	@Path("")
	public List<EventDTO> getAllEvents();
	
	@GET
	@Path("user/{username}")
	public List<EventDTO> getEventsForUser(@PathParam("username") String username);
	
	@GET
	@Path("organizer/{username}")
	public List<EventDTO> getEventsOrganizedByUser(@PathParam("username") String username);
	
	@POST
	@Path("event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Long newEvent(EventDetailDTO event);

	@POST
	@Path("user/event/{event_id}") 
	public ParticipantDTO joinEvent(@PathParam("event_id") Long eventId);
	
	@DELETE
	@Path("user/event/{event_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public int leaveEvent(@PathParam("event_id") Long eventId);

	@DELETE
	@Path("event/{event_id}")
	public void deleteEvent(@PathParam("event_id") Long eventId);
	
	
	@GET
	@Path("event/{event_id}")
	public EventDetailDTO getEvent( @PathParam("event_id") Long eventId);
	
	@PUT
	@Path("event/{event_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateEvent( @PathParam("event_id") Long eventId, EventDetailDTO event);
	
	
}

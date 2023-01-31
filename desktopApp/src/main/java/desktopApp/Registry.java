package desktopApp;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import clients.Client;
import clients.EventClient;
import controllers.MainController;
import dtos.EventDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Registry {
	private Registry() {
		
	}
	
	private static Registry instance;
	public static Registry getInstance() {
		if(instance == null) {
			instance = new Registry();
		}
		return instance;
	}
	
	private ResourceBundle resourceBundle;
	private MainController mainController;
	
	//private EventClient eventClient;
	
	private List<EventDTO> joinedEvents = new LinkedList<>();
	private List<EventDTO> organizedEvents = new LinkedList<>();
	private List<EventDTO> allEvents = new LinkedList<>();

	private String username;
	private String encodedCredentials;
	
	// For some reason, the authentication header is otherwise not present (if the client is set just once in LoginController)
	// when calling refreshAsync on myParticipationTabController...
	public EventClient getEventClient() {
		if(encodedCredentials != null) {
			return Client.getAuthClient(EventClient.class, encodedCredentials);
		}
		else {
			return Client.getClient(EventClient.class);
		}
		
	}
	
	public DateTimeFormatter getDateTimeFormatter() {
		if(resourceBundle == null) {
			throw new IllegalStateException("Resource bundle is not set");
		}
		Locale locale = resourceBundle.getLocale();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.
				ofLocalizedDateTime(FormatStyle.LONG)
				.withLocale(locale)
				//using the system's timezone is fine, because it is a client-sided app
				.withZone(TimeZone.getDefault().toZoneId());
		
		return dateTimeFormatter;
	}
	
	
}

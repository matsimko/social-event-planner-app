package utils;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

import desktopApp.Registry;
import dtos.EventDTO;
import dtos.ParticipantDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class ConreteTableViewCreator {
	public static void initializeEventsTableView(TableView<EventDTO> table,
			List<EventDTO> events) {
		String[] propertyNames = { "name", "description", "date",
				"numberOfParticipants", "organizerUsername" };

		initializeTableView(table, propertyNames, events, EventDTO.class);
	}

	public static void initializeParticipantsTableView(TableView<ParticipantDTO> table,
			List<ParticipantDTO> events) {
		String[] propertyNames = { "username", "dateOfJoining" };

		initializeTableView(table, propertyNames, events, ParticipantDTO.class);		
	}

	private static <C> void initializeTableView(TableView<C> tableView,
			String[] propertyNames, List<C> items, Class<C> clas) {

		ResourceBundle rb = Registry.getInstance().getResourceBundle();
		Label placeholder = new Label(rb.getString("tablePlaceholder"));
		tableView.setPlaceholder(placeholder);
		
		String[] names = new String[propertyNames.length];

		for (int i = 0; i < propertyNames.length; i++) {
			names[i] = rb.getString(propertyNames[i]);
		}

		TableViewCreator.initializeTable(tableView, names, propertyNames, clas);
		tableView.setItems(FXCollections.observableArrayList(items));
	}
}

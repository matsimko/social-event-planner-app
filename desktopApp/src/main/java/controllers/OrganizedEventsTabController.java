package controllers;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import desktopApp.Registry;
import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j2;
import utils.Alerts;
import utils.ConreteTableViewCreator;

@Log4j2
public class OrganizedEventsTabController extends AbstractController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<EventDTO> eventsTable;

    @FXML
    private Label nameLbl;

    @FXML
    private TextField nameTF;

    @FXML
    private Text myParDescTxt;

    @FXML
    private TextArea descTA;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Button createBtn;

    @FXML
    private TableView<ParticipantDTO> participantsTable;

    @FXML
    private DatePicker dateDP;

    @FXML
    private Spinner<Integer> hoursSpinner;

    @FXML
    private Spinner<Integer> minsSpinner;
    
    private EventDetailDTO selectedEventDetail;

    @FXML
    void initialize() {
    	//listen to changes in the selected event and fetch and display its detail  
    	eventsTable.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> {
    				if(newValue != null) {
    					getEventDetail(newValue);
        				log.info("selectedEvent:{}", newValue);
    				}
    				else {
    					updateButtons(false);
    				}
    				
    			});
    	
    	deleteBtn.setOnAction(this::deleteEvent);
    	updateBtn.setOnAction(this::updateEvent);
    	createBtn.setOnAction(this::createEvent);
    	
    	//set value range for hours and mins spinners
    	SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory
    			.IntegerSpinnerValueFactory(0, 23, 0);
    	hoursSpinner.setValueFactory(valueFactory);
    	valueFactory = new SpinnerValueFactory
    			.IntegerSpinnerValueFactory(0, 59, 0);
    	minsSpinner.setValueFactory(valueFactory);
    }
    
    private void updateButtons(boolean isEventSelected) {
    	updateBtn.setDisable(!isEventSelected);
    	deleteBtn.setDisable(!isEventSelected);
    }

	private void getEventDetail(EventDTO event) {   	
    	selectedEventDetail = Registry.getInstance()
				.getEventClient().getEvent(event.getId());

    	updateButtons(true);
		nameTF.setText(selectedEventDetail.getName());

		Instant date = selectedEventDetail.getDate();
		if(date != null) {
			ZoneId zoneId = TimeZone.getDefault().toZoneId(); 
			LocalDate localDate = LocalDate.ofInstant(date, zoneId);
			dateDP.setValue(localDate);
					
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date, zoneId);
			log.info("EventDetailDTO ZonedDateTime: {}", zonedDateTime);
			hoursSpinner.getValueFactory()
				.setValue(zonedDateTime.get(ChronoField.HOUR_OF_DAY));
			minsSpinner.getValueFactory()
				.setValue(zonedDateTime.get(ChronoField.MINUTE_OF_HOUR));
		}
		else {
			dateDP.setValue(null);
			hoursSpinner.getValueFactory().setValue(0);
			minsSpinner.getValueFactory().setValue(0);
		}
		
		descTA.setText(selectedEventDetail.getDescription());
		participantsTable.setItems(FXCollections
				.observableArrayList(selectedEventDetail.getParticipants()));


	}

	@Override
    public void load() {
    	ConreteTableViewCreator.initializeEventsTableView(eventsTable, 
    			Registry.getInstance().getOrganizedEvents());
    	
    	ConreteTableViewCreator.initializeParticipantsTableView(participantsTable,
    			Collections.emptyList());
    }
    
    
    
    @Override
    public void refresh() {
    	Registry registry = Registry.getInstance();
		String username = registry.getUsername();
		List<EventDTO> organizedEvents = registry.
				getEventClient().getEventsOrganizedByUser(username);
		
		Platform.runLater(() -> {
			eventsTable.setItems(FXCollections.observableArrayList(organizedEvents));
		});
    }
    
    public EventController getEventController() {
    	return (EventController)Registry.getInstance()
    			.getMainController().getController("events");
    }
    
    private EventDetailDTO buildEvent() {
    	String name = nameTF.getText();
		if(name.length() == 0) {
			Alerts.showCannotBeEmptyAlert("name");
			throw new IllegalStateException();
		}
		String description = descTA.getText();
		
		Instant date = null;
		LocalDate localDate = dateDP.getValue();
		if(localDate != null) {
			int hours = hoursSpinner.getValue();
			int mins = minsSpinner.getValue();
			date = localDate
					.atStartOfDay(TimeZone.getDefault().toZoneId())
					.toInstant()
					.plus(hours, ChronoUnit.HOURS)
					.plus(mins, ChronoUnit.MINUTES);
		}
		
		EventDetailDTO event = EventDetailDTO.builder()
				.name(name)
				.description(description)
				.date(date)
				.build();
		log.info("built event: {}", event);
		
		return event;
    }

	private void createEvent(ActionEvent e) {
		
		try {
			EventDetailDTO event = buildEvent();
			Registry.getInstance().getEventClient().newEvent(event);
			notifyEventController();
		}
		catch (IllegalStateException ex) {}
		
	}

	private void updateEvent(ActionEvent e) {
		try {
			EventDetailDTO event = buildEvent();
			event.setId(selectedEventDetail.getId());
			Registry.getInstance().getEventClient().updateEvent(event.getId(), event);
			notifyEventController();
		}
		catch (IllegalStateException ex) {}
	}

	private void deleteEvent(ActionEvent e) {
		try {
			Registry.getInstance().getEventClient()
				.deleteEvent(selectedEventDetail.getId());
			notifyEventController();
		}
		catch (IllegalStateException ex) {}
	}
	
	private void notifyEventController() {
		EventController eventController = (EventController)Registry.getInstance().getMainController()
				.getController("events");
		eventController.eventUpdated(); 
	}
}

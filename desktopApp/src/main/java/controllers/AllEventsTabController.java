package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import desktopApp.Registry;
import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j2;
import utils.Alerts;
import utils.ConreteTableViewCreator;

@Log4j2
public class AllEventsTabController extends AbstractController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button filterBtn;

    @FXML
    private TextField nameTF;

    @FXML
    private DatePicker dateDP;

    @FXML
    private TableView<EventDTO> eventsTable;

    @FXML
    private Label nameLbl;

    @FXML
    private Text descTxt;

    @FXML
    private Button leaveBtn;

    @FXML
    private Button joinBtn;

    @FXML
    private TableView<ParticipantDTO> participantsTable;
    
    @FXML
    private Label dateLbl;
    
    EventDetailDTO selectedEventDetail = null;
    List<EventDTO> events;

    @FXML
    void initialize() {
    	//listen to changes in the selected event and fetch and display its detail  
    	eventsTable.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> {
    				if(newValue != null) {
    					getEventDetail(newValue);
        				log.info("selectedEvent:{}", newValue);
    				}
    				
    			});
    	
    	leaveBtn.setOnAction(this::leaveSelectedEvent);
    	joinBtn.setOnAction(this::joinSelectedEvent);
    	filterBtn.setOnAction(this::filterEvents);
    }
    
    private void getEventDetail(EventDTO event) {   	
    	selectedEventDetail = Registry.getInstance()
				.getEventClient().getEvent(event.getId());

		nameLbl.setText(selectedEventDetail.getName());
		
		if(event.getDate() != null) {
			DateTimeFormatter dateTimeFormatter = Registry.getInstance().getDateTimeFormatter();
			String formattedDate = dateTimeFormatter.format(selectedEventDetail.getDate());
			dateLbl.setText(formattedDate);
		}
		
		
		descTxt.setText(selectedEventDetail.getDescription());
		participantsTable.setItems(FXCollections
				.observableArrayList(selectedEventDetail.getParticipants()));

		// Set the buttons according to
		// whether the user is already participating in the event
		String username = Registry.getInstance().getUsername();
		if(selectedEventDetail.getParticipants().stream()
				.map(e -> e.getUsername())
				.anyMatch(u -> username.equals(u))) {
			
			updateButtons(true);
		}
		else {
			updateButtons(false);
		}
	}
    
    private void updateButtons(boolean isJoined) {
    	joinBtn.setDisable(isJoined);
		leaveBtn.setDisable(!isJoined);
    }
    
    public void deselect() {
    	selectedEventDetail = null;
    	joinBtn.setDisable(true);
    	leaveBtn.setDisable(true);
    	dateLbl.setText(null);
    	nameLbl.setText(null);
    	descTxt.setText(null);
    	participantsTable.setItems(FXCollections.emptyObservableList());
    }
    
    @Override
    public void refresh() {
		Registry registry = Registry.getInstance();
		events = registry.
				getEventClient().getAllEvents();
		
		Platform.runLater(() -> {
	    	deselect();
			eventsTable.setItems(FXCollections.observableArrayList(events));
		});
    }
    
    
	@Override
    public void load() {
		events = Registry.getInstance().getAllEvents();
    	ConreteTableViewCreator.initializeEventsTableView(eventsTable, 
    			events);
    	
    	ConreteTableViewCreator.initializeParticipantsTableView(participantsTable,
    			Collections.emptyList());
    }

	private void leaveSelectedEvent(ActionEvent e) {
		try {
			Registry.getInstance().getEventClient()
			.leaveEvent(selectedEventDetail.getId());
			updateButtons(false);
			
			notifyEventController();
			
		}
		catch (Exception exc) {
			String msg = resources.getString("errorLeaving");
			log.info("errorLeaving", exc);
			Alerts.showErrorAlert(msg);
		}
	}


	private void joinSelectedEvent(ActionEvent e) {
		try {
			Registry.getInstance().getEventClient()
			.joinEvent(selectedEventDetail.getId());
			updateButtons(true);
			
			notifyEventController();
		}
		catch (Exception exc) {
			String msg = resources.getString("errorJoining");
			log.info("errorJoining", exc);
			Alerts.showErrorAlert(msg);
		}
	}
	
	//notify the mediator EventController that the state of user's participations changed
	//I will even let it take care of updating the tab of the sender (allEventsTab in this case)
	private void notifyEventController() {
		EventController eventController = (EventController)Registry.getInstance().getMainController()
				.getController("events");
		eventController.participationsChanged(); 
	}

	private void filterEvents(ActionEvent actionEvent) {
		ZoneId zoneId = TimeZone.getDefault().toZoneId();
		LocalDate date = dateDP.getValue();
		String name = nameTF.getText();
		List<EventDTO> filteredEvents = events.stream()
				.filter(e -> 
						(name.length() == 0 ||
							StringUtils.containsIgnoreCase(e.getName(), name)) && 
						(date == null || (e.getDate() != null &&
							date.equals(LocalDate.ofInstant(e.getDate(), zoneId)))))
				.collect(Collectors.toList());
		
		eventsTable.setItems(FXCollections.observableArrayList(filteredEvents));
	}

}

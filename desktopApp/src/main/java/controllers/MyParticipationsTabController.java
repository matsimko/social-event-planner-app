package controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import clients.BasicAuthenticationEncoder;
import clients.Client;
import clients.EventClient;
import desktopApp.Registry;
import dtos.EventDTO;
import dtos.EventDetailDTO;
import dtos.ParticipantDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j2;
import utils.Alerts;
import utils.ConreteTableViewCreator;

@Log4j2
public class MyParticipationsTabController extends AbstractController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<EventDTO> eventsTable;

    @FXML
    private Label nameLbl;

    @FXML
    private Text descTxt;

    @FXML
    private Button leaveBtn;

    @FXML
    private TableView<ParticipantDTO> participantsTable;
    
    @FXML
    private Label dateLbl;
    
    EventDetailDTO selectedEventDetail = null;

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
    					
    				}
    				
    			});
    	
    	leaveBtn.setOnAction(this::leaveSelectedEvent);
    }
    

    private void getEventDetail(EventDTO event) {   	
    	selectedEventDetail = Registry.getInstance()
				.getEventClient().getEvent(event.getId());

    	leaveBtn.setDisable(false);
		nameLbl.setText(selectedEventDetail.getName());
		
		if(event.getDate() != null) {
			DateTimeFormatter dateTimeFormatter = Registry.getInstance().getDateTimeFormatter();
			String formattedDate = dateTimeFormatter.format(selectedEventDetail.getDate());
			dateLbl.setText(formattedDate);
		}
		
		descTxt.setText(selectedEventDetail.getDescription());
		participantsTable.setItems(FXCollections
				.observableArrayList(selectedEventDetail.getParticipants()));

	}
    

    
    @Override
    public void load() {
    	ConreteTableViewCreator.initializeEventsTableView(eventsTable, 
    			Registry.getInstance().getJoinedEvents());
    	
    	ConreteTableViewCreator.initializeParticipantsTableView(participantsTable,
    			Collections.emptyList());    	
    }
    
    private List<EventDTO> fetchJoinedEvents() {
    	Registry registry = Registry.getInstance();
    	String username = registry.getUsername();
    	List<EventDTO> events = registry.getEventClient().getEventsForUser(username);
    	return events;
    }
    
    @Override
    public void refresh() {
    	List<EventDTO> events = fetchJoinedEvents();
    	Platform.runLater(() -> {
        	deselect();
    		eventsTable.setItems(FXCollections.observableArrayList(events));
    	});
    	
    }
    

    public void deselect() {
    	selectedEventDetail = null;
    	leaveBtn.setDisable(true);
    	dateLbl.setText(null);
    	nameLbl.setText(null);
    	descTxt.setText(null);
    	participantsTable.setItems(FXCollections.emptyObservableList());
    }
    
   
	private void leaveSelectedEvent(ActionEvent e) {
		try {
			Registry.getInstance().getEventClient()
			.leaveEvent(selectedEventDetail.getId());
			
			notifyEventController();
			
		}
		catch (Exception exc) {
			String msg = resources.getString("errorLeaving");
			log.info("errorLeaving", exc);
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

    
    /*@Override
    public void refreshAsync() {
//    	Executors.newSingleThreadExecutor().submit(() -> {
//    		List<EventDTO> events = fetchJoinedEvents();
//    		Platform.runLater(() -> {
//    			eventsTable.setItems(FXCollections.observableArrayList(events));
//    		});
//    	});	
    	
    	CompletableFuture.runAsync(() -> {
    		List<EventDTO> events = fetchJoinedEvents();
    		Platform.runLater(() -> {
    			eventsTable.setItems(FXCollections.observableArrayList(events));
    		});
        	
    	});
    	
    }*/
    
}

package controllers;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import desktopApp.Registry;
import dtos.EventDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.log4j.Log4j2;

@Log4j2
/*
 * Also acts as a mediator between the tabs
 */
public class EventController extends AbstractController {
	
	@FXML
	ResourceBundle resources;
	
	@FXML
	//variable name of included controller must be: fx:include's fx:id + "Controller"
	private MyParticipationsTabController myParticipationsTabController;
	
	@FXML
	private OrganizedEventsTabController organizedEventsTabController;
	
	@FXML
	private AllEventsTabController allEventsTabController;
	
	
	@FXML
	TabPane tabPane;
	
	@FXML 
	private Tab myParticipationsTabNode;
	
	@FXML 
	private Tab organizedEventsTabNode;
	
	@FXML 
	private Tab allEventsTabNode;

	@FXML
	ProgressIndicator progressIndicator;
	
	private boolean loaded = false;
	//ConcurrentHashMap is used to ensure thread safety
	private Map<Tab, Boolean> isTabContentLoadedMap = new ConcurrentHashMap<>();
	
	@FXML
	void initialize() {
		MainController mainController = Registry.getInstance().getMainController();
		isTabContentLoadedMap.put(myParticipationsTabNode, true);
		isTabContentLoadedMap.put(organizedEventsTabNode, false);
		isTabContentLoadedMap.put(allEventsTabNode, false);
		
		//listen for tab change event to hide/display the progress indicator accordingly
		tabPane.getSelectionModel().
			selectedItemProperty().addListener(new ChangeListener<Tab>() {

			    @Override
			    public void changed(ObservableValue<? extends Tab> observable,
			    		Tab oldTab, Tab newTab) {
			    	
			    		boolean isTabLoaded = isTabContentLoadedMap.get(newTab);
			    		progressIndicator.setVisible(!isTabLoaded);		    	
			    }
		});		
		
	}
	
	@Override
	public void load() {
		if(!loaded) {
			loaded = true;
			myParticipationsTabController.load();
			allEventsTabController.load();
			organizedEventsTabController.load();
			
			//myParticipations are already fetched by logging in
			
			refreshTabAsync(allEventsTabNode, allEventsTabController);
			refreshTabAsync(organizedEventsTabNode, organizedEventsTabController);	
			
		}
	}
	
	private void refreshTabAsync(Tab tab, Controller controller) {
		controller.refreshAsync()
		.thenRun(() -> {
			tabLoaded(tab, true);
		});
	}

	private void tabLoaded(Tab tab, boolean loaded) {
		isTabContentLoadedMap.put(tab, loaded);
		//if the tab is the currently viewed one, update the progress indicator
		Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
		if(tab == currentTab) {
			progressIndicator.setVisible(!loaded);
		}
	}
	

	public void participationsChanged() {
		//just deselecting in case of the allEventsTabNode would be sufficient,
		//but I will refresh it for now
		refreshAllTabs();
	}
	
	/*
	 * Call this on insert, update or delete of an event
	 */
	public void eventUpdated() {
		refreshAllTabs();
	}
	
	private void refreshAllTabs() {
		tabLoaded(allEventsTabNode, false);
		tabLoaded(myParticipationsTabNode, false);
		tabLoaded(organizedEventsTabNode, false);
		
		refreshTabAsync(allEventsTabNode, allEventsTabController);
		refreshTabAsync(myParticipationsTabNode, myParticipationsTabController);
		refreshTabAsync(organizedEventsTabNode, organizedEventsTabController);	
	}
	
}

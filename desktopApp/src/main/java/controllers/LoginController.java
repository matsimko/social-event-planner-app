package controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;

import dtos.EventDTO;
import dtos.UserDTO;
import clients.AccountClient;
import clients.BasicAuthenticationEncoder;
import clients.Client;
import clients.EventClient;
import desktopApp.Registry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoginController extends AbstractController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField usernameTF;

    @FXML
    private TextField passwordTF;

    @FXML
    private Button loginBtn;
    
    @FXML
    private Label errorLbl;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    private String EMPTY_FIELD;
    private String USERNAME_EXISTS;
    private String INVALID_PASSWORD;
    
    private AccountClient accClient = Client.getClient(AccountClient.class);

    @FXML
    void register(ActionEvent event) {
    	errorLbl.setText(null);
    	
		try {
			UserDTO user = retrieveCredentials();
			if(user == null) {
				return;
			}
			progressIndicator.setVisible(true);
			accClient.newUser(user);
		}
		catch(ClientErrorException e) {
			errorLbl.setText(USERNAME_EXISTS);
		}
		catch(Exception e) {
			log.error("error registering", e);
		}
		
		progressIndicator.setVisible(false);
    }
    
    @FXML
    void login(ActionEvent event) { 
		errorLbl.setText(null);
    	
		try {
			UserDTO user = retrieveCredentials();
			if(user == null) {
				return;
			}
			progressIndicator.setVisible(true);
			Registry reg = Registry.getInstance();
			

			String encodedCredentials = BasicAuthenticationEncoder
					.encode(user.getUsername(), user.getPassword());
			//The client would somehow be reset in other threads, losing its headers.........
			/*EventClient eventClient = Client.getAuthClient(EventClient.class,
					encodedCredentials);
			
			reg.setEventClient(eventClient);*/
			
			reg.setEncodedCredentials(encodedCredentials);
			EventClient eventClient = reg.getEventClient();
			
			List<EventDTO> joinedEvents = eventClient.getEventsForUser(user.getUsername());
			reg.setJoinedEvents(joinedEvents);
			reg.setUsername(user.getUsername());
			reg.getMainController().swapScene("events");
		}
		catch(NotAuthorizedException e) {
			errorLbl.setText(INVALID_PASSWORD);
		}
		catch(Exception e) {
			log.error("error loging in", e);
		}
		
		progressIndicator.setVisible(false);
    }
    
    private UserDTO retrieveCredentials() {
    	String username = usernameTF.getText();
		String pwd = passwordTF.getText();
		if(pwd.isEmpty() || username.isEmpty()) {
			errorLbl.setText(EMPTY_FIELD);
			return null;
		}
		
		return new UserDTO(username,pwd);
    }

    @FXML
    void initialize() {
    	errorLbl.setWrapText(true);
    	
    	EMPTY_FIELD = resources.getString("emptyLoginField");
        USERNAME_EXISTS = resources.getString("usernameExists");
        INVALID_PASSWORD = resources.getString("invalidPassword");
    }
}

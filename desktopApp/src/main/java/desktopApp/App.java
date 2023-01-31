package desktopApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import controllers.Controller;
import controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class App extends Application {
	
	private final Path propsPath = Paths.get("src/main/resources/app.properties");
	private Properties props = new Properties();
	private Locale locale;
	private ResourceBundle rb;
	private MainController mainController;
	private int startingWidth = 800;
	private int startHeight = 900;

    @Override
    public void start(Stage primaryStage) {
    	log.info(propsPath.toAbsolutePath());
    	try(InputStream is = Files.newInputStream(propsPath)) {
    		props.load(is);
    		determineLocale();
    	} catch (IOException e) {
			locale = Locale.getDefault();
			props.setProperty("language", locale.getLanguage());
			props.setProperty("country", locale.getCountry());
		}
    	
    	
    	try {
    		mainController = new MainController();
    		Registry.getInstance().setMainController(mainController);
    		
    		rb = ResourceBundle.getBundle("Translations.Translations", locale);
    		Registry.getInstance().setResourceBundle(rb);
    		
    		Controller loginController = loadController("/scenes/login.fxml", "login");
    		loadController("/scenes/events.fxml", "events");
    		
    		Scene scene = new Scene(loginController.getRoot());
    		mainController.setScene(scene);

			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setWidth(startingWidth);
			primaryStage.setHeight(startHeight);
			primaryStage.setOnCloseRequest(this::exitApp);
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    public void determineLocale() {
    	
    	String lang = props.getProperty("language");
    	if (lang != null) {
    		String country = props.getProperty("country");
    		if(country != null) {
    			locale = new Locale(lang, country);
    		}
    		else {
    			locale = new Locale(lang);
    		}
    	}
    	else {
    		locale = Locale.getDefault();
    	}
    }
    
    
    public Controller loadController(String resourceName, String sceneName) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(resourceName));
		loader.setResources(rb);
		Parent root = loader.load();
		Controller controller = loader.getController();
		controller.setRoot(root);
		mainController.addScene(sceneName, controller);
		
		return controller;
		
    }

    public static void main(String[] args) {
        launch();
    }

	private void exitApp(WindowEvent e) {
		System.exit(0);
	}

}
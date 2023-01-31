package controllers;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.Setter;

public class MainController {
	@Getter
	private Map<String, Controller> controllers = new HashMap<>();
	@Getter @Setter
	private Scene scene;
	
	public void swapScene(String name) {
		Controller controller = controllers.get(name);
		Parent root = controller.getRoot();
		scene.setRoot(root);
		controller.load();
		
	}
	
	public void addScene(String name, Controller controller) {
		controllers.put(name, controller);
	}
	
	public Controller getController(String name) {
		return controllers.get(name);
	}
}

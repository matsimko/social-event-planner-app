package utils;

import java.util.ResourceBundle;

import javax.annotation.Resource;

import desktopApp.Registry;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Alerts {
	
	public static void showErrorAlert(String msg) {
		showAlert(AlertType.ERROR, msg);
	}
	
	public static void showCannotBeEmptyAlert(String field) {
		ResourceBundle rb = Registry.getInstance().getResourceBundle();
		showErrorAlert(rb.getString("field") + " " + rb.getString(field) +
				" " + rb.getString("cannotBeEmpty"));
	}
	
	public static void showAlert(AlertType type, String msg) {
		Alert alert = new Alert(type, msg);
		alert.show(); //showAndWait is not needed
	}
	
	
}

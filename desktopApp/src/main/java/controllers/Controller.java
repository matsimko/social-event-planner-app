package controllers;

import java.util.concurrent.CompletableFuture;

import javafx.scene.Parent;

public interface Controller {
	public Parent getRoot();

	public void setRoot(Parent root);
	
	public void load();
	public void loadRunLater();
	public void refresh();
	public CompletableFuture<Void> refreshAsync();
	
}

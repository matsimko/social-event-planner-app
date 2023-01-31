package controllers;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.scene.Parent;

public abstract class AbstractController implements Controller {
	/**
	 * The root node of the scene
	 */
	protected Parent root;

	public Parent getRoot() {
		return root;
	}

	public void setRoot(Parent root) {
		this.root = root;
	}

	@Override
	public void load() {
		
	}

	/**
	 * javaFX stuff must be executed inside JavaFX threads, and runLater queues
	 * it for such execution
	 */
	@Override
	public void loadRunLater() {
		Platform.runLater(this::load);
	}
	
	@Override
	public void refresh() {
		
	}
	
	@Override
	public CompletableFuture<Void> refreshAsync() {
		return CompletableFuture.runAsync(this::refresh);
	}
	

	
	
	
	
}

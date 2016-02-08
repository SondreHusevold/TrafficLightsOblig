package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import javafx.event.ActionEvent;

import javafx.scene.image.ImageView;

public class GUIController {
	@FXML
	private TextField textfield_server;
	@FXML
	private Button button_connect;
	@FXML
	private TextField textfield_port;
	@FXML
	private Button button_disconnect;
	@FXML
	public ImageView image_currentLight;
	
	private Kickstarter appStarter;

	// Event Listener on Button[#button_connect].onAction
	@FXML
	public void connect(ActionEvent event) {
		appStarter = new Kickstarter(textfield_server.getText(), Integer.parseInt(textfield_port.getText()), image_currentLight);
		appStarter.connect();
		button_connect.setDisable(true);
		button_disconnect.setDisable(false);
	}
	
	// Event Listener on Button[#button_disconnect].onAction
	@FXML
	public void disconnect(ActionEvent event) {
		appStarter.disconnect();
		appStarter = null;
		button_connect.setDisable(false);
		button_disconnect.setDisable(true);
	}
}

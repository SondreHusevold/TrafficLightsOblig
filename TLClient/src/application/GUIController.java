package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
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
    @FXML
    private RadioButton radio_standard;
    @FXML
    private RadioButton radio_walk;
	
	private Kickstarter appStarter;

	// Event Listener on Button[#button_connect].onAction
	@FXML
	public void connect(ActionEvent event) {
		appStarter = new Kickstarter(textfield_server.getText(), Integer.parseInt(textfield_port.getText()), image_currentLight, radio_walk.isSelected());
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
	

    @FXML
    void switchRadioStandard(ActionEvent event) {
    	radio_walk.setSelected(false);
    	radio_standard.setSelected(true);
    }
    
    @FXML
    void switchRadioWalk(ActionEvent event) {
    	radio_walk.setSelected(true);
    	radio_standard.setSelected(false);
    }
}

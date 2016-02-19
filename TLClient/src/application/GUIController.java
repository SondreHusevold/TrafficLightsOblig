package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;

import javafx.scene.image.ImageView;

/*!
 * JavaFX controller to control the Scene Builder created user interface. 
 */

public class GUIController {
	@FXML
	private TextField textfield_server;
	@FXML
	public Button button_connect;
	@FXML
	private TextField textfield_port;
	@FXML
	public Button button_disconnect;
	@FXML
	public ImageView image_currentLight;
    @FXML
    private RadioButton radio_standard;
    @FXML
    private RadioButton radio_walk;
	
	private Kickstarter appStarter;

	/*!
	 * Button event when the button_connect is pressed.
	 * 
	 * Connects the client by creating the bridge between the Traffic Light and the GUI and sending user interface
	 * related information from the host field, the port field and type fields. While also sending the imageview further down the classes.
	 */
	@FXML
	public void connect(ActionEvent event) {
		appStarter = new Kickstarter(textfield_server.getText(), Integer.parseInt(textfield_port.getText()), radio_walk.isSelected(), image_currentLight);
		appStarter.connect();
		button_connect.setDisable(true);
		button_disconnect.setDisable(false);
		radio_standard.setDisable(true);
		radio_walk.setDisable(true);
		System.out.println(button_connect.toString());
	}
	
	/*!
	 * Button event when the button_disconnect is pressed.
	 * 
	 * Disconnects the client by stopping the threads and nulling the bridge killing off the client's connection completely.
	 * Also reenables the connect button.
	 */
	@FXML
	public void disconnect(ActionEvent event) {
		disconnect();
	}
	
	public void disconnect() {
		if(appStarter != null){
			appStarter.disconnect();
		}
		appStarter = null;
		button_connect.setDisable(false);
		button_disconnect.setDisable(true);
		radio_standard.setDisable(false);
		radio_walk.setDisable(false);
	}
	
	public Button getConnect(){
		return button_connect;
	}
	
	/*!
	 * Method to switch from Walk radio button to the Standard radio button.
	 */
    @FXML
    void switchRadioStandard(ActionEvent event) {
    	radio_walk.setSelected(false);
    	radio_standard.setSelected(true);
    }
    
	/*!
	 * Method to switch from Standard radio button to the Walk radio button.
	 */
    @FXML
    void switchRadioWalk(ActionEvent event) {
    	radio_walk.setSelected(true);
    	radio_standard.setSelected(false);
    }
}

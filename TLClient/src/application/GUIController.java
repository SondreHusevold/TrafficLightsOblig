package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
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
    
    // Static wrapper workaround for an ugly bug with the user interface where the connection buttons won't stop being enabled after being disconnected
    // forcefully by the server. This problem is also exists if button.setDisabled() is ran through Platform.RunLater and/or loading the GUIController through an FXML Loader. 
	// This half-assed solution solves it by allowing the traffic light to forcefully change the buttons statically.
    public static Button sbutton_connect, sbutton_disconnect;
    public static RadioButton sradio_standard, sradio_walk;
    
	/*!
	 * Button event when the button_connect is pressed.
	 * 
	 * Connects the client by creating the bridge between the Traffic Light and the GUI and sending user interface
	 * related information from the host field, the port field and type fields. While also sending the imageview further down the classes.
	 */
	@FXML
	public void connect(ActionEvent event) {
		
		// Setting workaround buttons here so that they work after disconnecting.
		sbutton_connect = button_connect;
		sbutton_disconnect = button_disconnect;
		sradio_standard = radio_standard;
		sradio_walk = radio_walk;
		
		int port;
		try{
			port = Integer.parseInt(textfield_port.getText());
		}
		catch(NumberFormatException e){
			new Alert(AlertType.ERROR, "Port invalid. Falling back on 5555...", ButtonType.OK).showAndWait();
			port = 5555;
		}
		
		appStarter = new Kickstarter(textfield_server.getText(), port, radio_walk.isSelected(), image_currentLight);
		appStarter.connect();
		changeConnectionButtons(true);
	}
	
	/*!
	 * Button event when the button_disconnect is pressed.
	 * 
	 * Disconnects the client by stopping the threads and nulling the bridge killing off the client's connection completely.
	 * Also reenables the connect button by using the workaround.
	 */
	@FXML
	public void disconnect(ActionEvent event) {
		disconnect();
	}
	
	public void disconnect() {
		changeConnectionButtons(false);
		if(appStarter != null){
			appStarter.disconnect();
		}
		appStarter = null;
	}
	
	public static void changeConnectionButtons(boolean connect){
		if(sbutton_connect != null && connect){
			sbutton_connect.setDisable(true);
			sbutton_disconnect.setDisable(false);
			sradio_standard.setDisable(true);
			sradio_walk.setDisable(true);
		}
		else if(sbutton_connect != null){
			sbutton_connect.setDisable(false);
			sbutton_disconnect.setDisable(true);
			sradio_standard.setDisable(false);
			sradio_walk.setDisable(false);
		}
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

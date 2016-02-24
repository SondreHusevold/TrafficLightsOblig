package server;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

import javafx.event.ActionEvent;

/*!
 * JavaFX controller. Controls all the buttons, labels, sliders, textareas and such.
 * 
 * Most of the main stuff is contained within the "Kickstarter" main thread instead of this one. This will ensure that the GUIController won't need
 * to listen to any sockets, create any clients or something similar.
 */

public class GUIController {
	@FXML
    public ListView<String> list_clientList;
	@FXML
	public ImageView view_trafficLight1, view_trafficLight2;
	@FXML
	private TextField textfield_port;
	@FXML
	private Slider yellowSlider, greenSlider, redSlider;
	@FXML
	private Label label_redSliderValue, label_yellowSliderValue, label_greenSliderValue;
	@FXML
	private Button button_start, button_stop, button_move_up, button_move_down, button_ApplyChanges, button_applyClient,
					button_trafficLight1, button_trafficLight2, button_trafficLight3, button_trafficLight4,
					button_walking_1, button_walking_2, button_walking_3, button_walking_4, button_walking_5, button_walking_6,
					button_walking_7, button_walking_8;
	@FXML
	public TextArea log;
	@FXML
	private CheckBox checkbox_sync_colors;
	
	final int red = 0, yellow = 1, green = 2;
	boolean checkboxSyncMessage = false;
	private Kickstarter appStarter;			// Main class to separate the GUI controller and the actual server application.
	Thread mainThread;						// Thread where the kickstarter runs.
	
	/*!
	 * Event triggered by button_start.
	 * 
	 * This will create a new thread which will make appStarter a new Kickstarter object and send arguments.
	 * It will then start the thread, and disable the start button, while enabling the stop button.
	 */
	@FXML
	public void launch(ActionEvent event) {
		int port;
		try{
			port = Integer.parseInt(textfield_port.getText());
		}
		catch(NumberFormatException e){
			new Alert(AlertType.ERROR, "Port invalid. Falling back on 5555...", ButtonType.OK).showAndWait();
			port = 5555;
		}
		
		log.appendText("Server started at port " + port + "\n");
		try{
			mainThread = new Thread(appStarter = new Kickstarter(list_clientList, port, log));
			mainThread.start();
			button_stop.setDisable(false);
			button_start.setDisable(true);
		} 
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*!
	 * Event triggered by button_stop.
	 * 
	 * See stop() for details.
	 */
	public void stop(ActionEvent event){
		stop();
	}
	
	/*!
	 * Kills the appStarter and its thread. Reenables the start button.
	 */
	public void stop(){
		if(appStarter != null){
			appStarter.kill();
			appStarter = null;
		}
		
		if(mainThread != null){
			try {
				mainThread.join();
				mainThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		button_start.setDisable(false);
		button_stop.setDisable(true);
		log.appendText("Server stopped.");
	}
	
	/*!
	 * Changes the label where the yellow slider is to show its value.
	 */
	@FXML
	public void changeYellowSliderValue(MouseEvent event) {
		int value = (int) yellowSlider.getValue();
		label_yellowSliderValue.setText("(" + value + ")");
		button_ApplyChanges.setDisable(false);
	}
	
	/*!
	 * Changes the label where the green slider is to show its value.
	 */
	@FXML
	public void changeGreenSliderValue(MouseEvent event) {
		int value = (int) greenSlider.getValue();
		label_greenSliderValue.setText("(" + value + ")");
		if(checkbox_sync_colors.isSelected()){
			redSlider.setValue(value);
			label_redSliderValue.setText("(" + value + ")");
		}
		button_ApplyChanges.setDisable(false);
	}
	
	/*!
	 * Changes the label where the red slider is to show its value.
	 */
	@FXML
	public void changeRedSliderValue(MouseEvent event) {
		int value = (int) redSlider.getValue();
		label_redSliderValue.setText("(" + value + ")");
		if(checkbox_sync_colors.isSelected()){
			greenSlider.setValue(value);
			label_greenSliderValue.setText("(" + value + ")");
		}
		button_ApplyChanges.setDisable(false);
	}
	
	/*!
	 * Applies the colors by getting the frequency from the sliders and sending it to the clients and writes to the log.
	 */
	@FXML
	public void applyColors(ActionEvent event) {
		if(appStarter != null){
			int r = (int) redSlider.getValue();
			int y = (int) yellowSlider.getValue();
			int g = (int) greenSlider.getValue();
			log.appendText("Server: > Setting new frequencies on all clients.\nServer: > Red: " + r + "\nServer: > Yellow: " + y + "\nServer: > Green:" + g + "\n");
			appStarter.setFrequency(red, r);
			appStarter.setFrequency(yellow, y);
			appStarter.setFrequency(green, g);
		}
		button_ApplyChanges.setDisable(true);
	}
	
	/*!
	 * Shows the warning for desynchronizing the red and green colors and disables it from appearing again during that run.
	 */
	@FXML
	public void sync_warning(ActionEvent event) {
		if(!checkboxSyncMessage){
			new Alert(AlertType.WARNING, "Deselecting this will make it possible to change the red and green bar independently.\n\n"
				+ " Doing so will make it hard for the traffic lights to synchronize properly.", ButtonType.OK).showAndWait();
			checkboxSyncMessage = true;
		}
	}
	
	/*!
	 * This is used for all the buttons on the map.
	 * 
	 * It will first check what button was pressed, whether it's a normal traffic light and then a walking sign button since they are of
	 * different icons and sizes. It also sets the button's icon, and sends arguments like what client is selected to the assignLight 
	 * method.
	 */
	@FXML
	public void assignLight(ActionEvent event){
		Button[] buttonArray = {button_trafficLight1, button_trafficLight2, button_trafficLight3, button_trafficLight4};
 		if(list_clientList.getSelectionModel() != null){
 			for(int i = 0; i < buttonArray.length; i++) {
 				if(event.getSource().equals(buttonArray[i])){
 					if(appStarter.assignLight(buttonArray[i], list_clientList.getSelectionModel().getSelectedIndex(), i, false)){
 						buttonArray[i].setStyle("-fx-graphic: url('/server/button_used.png'); -fx-background-color: transparent;");
 						return;
 					}
 					else{
  					   new Alert(AlertType.ERROR, "The selected client is not a traffic signal and cannot be assigned to this spot.", ButtonType.OK).showAndWait();
  					   return;
 					}
 				}
			}
		}
 		
 		Button[] walkingButtonArray = {button_walking_3, button_walking_1, button_walking_4, button_walking_2, button_walking_7, button_walking_5,
				button_walking_8, button_walking_6};
 		if(list_clientList.getSelectionModel() != null){
 			for(int i = 0; i < walkingButtonArray.length; i++) {
 				if(event.getSource().equals(walkingButtonArray[i])){
 					if(appStarter.assignLight(walkingButtonArray[i], list_clientList.getSelectionModel().getSelectedIndex(), i, true)){
 						walkingButtonArray[i].setStyle("-fx-graphic: url('/server/button_walk.png'); -fx-background-color: transparent;");
 						return;
 					}
 					else{
 					   new Alert(AlertType.ERROR, "The selected client is not a walking sign and cannot be assigned to this spot.", ButtonType.OK).showAndWait();
 					   return;
 					}
 						
 				}
			}
		}
	}
}

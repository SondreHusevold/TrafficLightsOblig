package server;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

/*!
 * JavaFX controller. Controls all the buttons, labels, sliders, textareas and such.
 */

public class GUIController {
	@FXML
    private ListView<String> list_clientList;
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
		log.appendText("Server started at port " + Integer.parseInt(textfield_port.getText()) + "\n");
		mainThread = new Thread(appStarter = new Kickstarter(list_clientList, Integer.parseInt(textfield_port.getText()), log));
		mainThread.start();
		button_stop.setDisable(false);
		button_start.setDisable(true);
	}
	
	/*!
	 * Kills the appStarter and its thread. Reenables the start button.
	 */
	public void stop(ActionEvent event){
		appStarter.kill();
		appStarter = null;
		mainThread = null;
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
	 * Event triggered by button_ApplyChanges
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
	
	@FXML
	public void sync_warning(ActionEvent event) {
		if(!checkboxSyncMessage){
			new Alert(AlertType.WARNING, "Deselecting this will make it possible to change the red and green bar independently.\n\n"
				+ " Doing so will make it hard for the traffic lights to synchronize properly.", ButtonType.OK).showAndWait();
			checkboxSyncMessage = true;
		}
	}
	
	@FXML
	public void applyToClient(ActionEvent event){
		System.out.println(list_clientList.getSelectionModel().getSelectedIndex());
	}
	
	@FXML
	public void assignWalkingLight(ActionEvent event){
		
	}
	
	// BEDRE IF TESTING
	
	@FXML
	public void assignLight(ActionEvent event){
		Button[] buttonArray = {button_trafficLight1, button_trafficLight2, button_trafficLight3, button_trafficLight4};
 		if(list_clientList.getSelectionModel() != null){
 			for(int i = 0; i < buttonArray.length; i++) {
 				if(event.getSource().equals(buttonArray[i])){
 					if(appStarter.assignLight(list_clientList.getSelectionModel().getSelectedIndex(), i)){
 						buttonArray[i].setStyle("-fx-graphic: url('/server/button_used.png'); -fx-background-color: transparent;");
 						return;
 					}
 						
 				}
			}
		}
 		
 		Button[] walkingButtonArray = {button_walking_2, button_walking_4, button_walking_1, button_walking_3, button_walking_5, button_walking_6,
				button_walking_7, button_walking_8};
 		if(list_clientList.getSelectionModel() != null){
 			for(int i = 0; i < walkingButtonArray.length; i++) {
 				if(event.getSource().equals(walkingButtonArray[i])){
 					if(appStarter.assignLight(list_clientList.getSelectionModel().getSelectedIndex(), i)){
 						walkingButtonArray[i].setStyle("-fx-graphic: url('/server/button_walk.png'); -fx-background-color: transparent;");
 						return;
 					}
 						
 				}
			}
		}
	}
}

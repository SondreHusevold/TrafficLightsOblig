package server;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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
	private Slider yellowSlider;
	@FXML
	private Slider greenSlider;
	@FXML
	private Slider redSlider;
	@FXML
	private Label label_redSliderValue;
	@FXML
	private Label label_yellowSliderValue;
	@FXML
	private Label label_greenSliderValue;
	@FXML
	private Button button_start;
	@FXML
	private Button button_stop;
	@FXML
	private Button button_ApplyChanges;
	@FXML
	public TextArea log;
	
	final int red = 0, yellow = 1, green = 2;

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
		button_ApplyChanges.setDisable(false);
	}
	
	/*!
	 * Changes the label where the red slider is to show its value.
	 */
	@FXML
	public void changeRedSliderValue(MouseEvent event) {
		int value = (int) redSlider.getValue();
		label_redSliderValue.setText("(" + value + ")");
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
}

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

	private Kickstarter appStarter;
	Thread mainThread;
	
	// Event Listener on Button[#button_begin].onAction
	@FXML
	public void launch(ActionEvent event) {
		mainThread = new Thread(appStarter = new Kickstarter(list_clientList, Integer.parseInt(textfield_port.getText()), log));
		mainThread.start();
		button_stop.setDisable(false);
		button_start.setDisable(true);
		log("Server started at port " + Integer.parseInt(textfield_port.getText()));
	}
	
	public void stop(ActionEvent event){
		appStarter.kill();
		appStarter = null;
		mainThread = null;
		button_start.setDisable(false);
		button_stop.setDisable(true);
		log("Server stopped.");
	}
	
	// Event Listener on Slider[#yellowSlider].onDragDetected
	@FXML
	public void changeYellowSliderValue(MouseEvent event) {
		int value = (int) yellowSlider.getValue();
		label_yellowSliderValue.setText("(" + value + ")");
		button_ApplyChanges.setDisable(false);
	}
	
	// Event Listener on Slider[#greenSlider].onDragDetected
	@FXML
	public void changeGreenSliderValue(MouseEvent event) {
		int value = (int) greenSlider.getValue();
		label_greenSliderValue.setText("(" + value + ")");
		button_ApplyChanges.setDisable(false);
	}
	
	// Event Listener on Slider[#redSlider].onDragDetected
	@FXML
	public void changeRedSliderValue(MouseEvent event) {
		int value = (int) redSlider.getValue();
		label_redSliderValue.setText("(" + value + ")");
		button_ApplyChanges.setDisable(false);
	}
	
	// Event Listener on Button[#button_ApplyChanges].onAction
	@FXML
	public void applyColors(ActionEvent event) {
		button_ApplyChanges.setDisable(true);
	}
	
	public void log(String t){
		log.appendText(t + "\n");
	}
}

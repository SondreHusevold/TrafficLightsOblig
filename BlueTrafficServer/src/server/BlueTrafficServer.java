package server;
	
import java.io.*;
import java.net.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/*!
 * Main class. Initializes the user interface and its controller. Doesn't do much else.
 */

public class BlueTrafficServer extends Application {
	
	 private Stage primaryStage;
	 private BorderPane rootLayout;
    
	@Override
	public void start(Stage primaryStage) {
		try {
			 this.primaryStage = primaryStage;
			 this.primaryStage.setTitle("Traffic Light Server");
			 initRootLayout();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	 public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(BlueTrafficServer.class.getResource("GUI.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}

package server;
	
import java.io.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;

/*!
 * This is the main class and it basically doesn't do anything except setting up the window using JavaFX.
 * 
 * Most of the important stuff is done from the Kickstarter class instead of this one.
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
            primaryStage.setOnCloseRequest(event -> {
            	GUIController controller = loader.getController();
            	controller.stop();
            });
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

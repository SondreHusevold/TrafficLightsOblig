package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

/*!
 * This is the main class and it basically doesn't do anything except setting up the window using JavaFX.
 * 
 * Most of the important stuff is done from the Kickstarter class instead of this one.
 */

public class TLClient extends Application {
	 private Stage primaryStage;
	 private BorderPane rootLayout;
    
	@Override
	public void start(Stage primaryStage) {
		try {
			 this.primaryStage = primaryStage;
			 this.primaryStage.setTitle("Traffic Light Client");
			 initRootLayout();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*!
	 * Sets up the GUI using JavaFX.
	 */
	 public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TLClient.class.getResource("GUI.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> {
            	GUIController controller = loader.getController();
            	controller.disconnect();
            });
     
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}

package application;

import backend.*;
import javafx.scene.image.ImageView;

/*!
 * This class is meant as a bridge between the GUI Controller and the Traffic Light.
 * The reason for this being the Main class (TLClient) is mainly used to initialize the GUI and its JavaFX controller,
 * thus in order to separate the GUI controller and what actually starts the client backend, 
 * this acts like the main method to kickstart the client instead of handling client objects inside the GUI controller. 
 */

public class Kickstarter {

	TrafficLight light;
	int port;
	String host;
	
	/*!
	 * Gets the hostname, port and traffic light type as arguments from the GUI controller.
	 * The ImageView from the center of the GUI is also sent through here in order for the traffic light
	 * to get access to this and change it when the lights change.
	 */
	
	public Kickstarter(String hostname, int _port, boolean walk, ImageView view){
		if(walk)
			light = new TrafficLight(5, 10, view);
		else
			light = new TrafficLight(view);
		host=hostname;
		port = _port;
	}
	
	/*!
	 * Connects to the traffic light using the hostname and the port acquired from the user interface.
	 */
	public void connect(){
		light.connect(host, port);
	}
	
	/*!
	 * Disconnects the traffic light by 
	 */
	public void disconnect(){
		light.disconnect();
	}
}

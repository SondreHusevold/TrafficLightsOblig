package application;

import backend.*;
import javafx.scene.image.ImageView;

public class Kickstarter {

	TrafficLight light;
	int port;
	String host;
	
	public Kickstarter(String hostname, int _port, ImageView view, boolean walk){
		if(walk)
			light = new TrafficLight(5, 10, view);
		else
			light = new TrafficLight(view);
		host=hostname;
		port = _port;
	}
	
	public void connect(){
		light.connect(host, port);
	}
	
	public void disconnect(){
		light.disconnect();
	}
}

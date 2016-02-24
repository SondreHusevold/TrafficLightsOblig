package backend;

import javafx.scene.image.Image;

/*!
 *  This class contains all the information about the light. This simplifies the Traffic Light structure by
 *  encapsulating all light information inside a single class that the traffic light manipulates.
 *  
 *  There are usually three of these inside the traffic light - red, yellow and green.
 */

public class Light {

	private boolean enabled = false;		// Check whether or not the light is lit up.
	private int frequency;					// Light switching frequency in seconds.
	private String name;					// Name of the light - Red, yellow or green.
	private Image image;					// The light's image representation.
	
	/*!
	 * The default constructor used in the traffic light class. 
	 * 
	 * Has the light's name, frequency and the image representation as parameters. 
	 */
	
	public Light(String _name, int freq, String imageName){
		image = new Image(imageName);
		name = _name;
		frequency = freq;
	}
	
	/*!
	 * Gets the image for this light.
	 */
	public Image getImage(){
		return image;
	}
	
	/*!
	 * Gets the name for this light.
	 */
	public String getName(){
		return name;
	}
	
	/*!
	 * Sets the frequency in seconds for this light.
	 */
	public void frequency(int newFreq){
		frequency = newFreq;
	}
	
	/*!
	 * Gets the frequency in seconds for this light.
	 */
	public int getFrequency(){
		return frequency;
	}
	
	
	/*!
	 * Sets whether or not the light is lit up.
	 */
	public void enabled(boolean bool){
		enabled = bool;
	}
	
	/*!
	 * Gets whether or not the light is lit up.
	 */
	public boolean isEnabled(){
		return enabled;
	}
}

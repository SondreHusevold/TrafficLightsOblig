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
	 * Never actually used, but there in case.
	 * 
	 * Might be removed at the end of project.
	 */
	public Light(String imageName){
		image = new Image(imageName);
		frequency = 10;
		name = "Default";
	}
	
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
	 * Used for yellow light in walking signals since yellow is never shown.
	 */
	
	public void name(String newName, String imageName){
		name = newName;
		image = new Image(imageName);
	}
	
	public Image getImage(){
		return image;
	}
	
	public String getName(){
		return name;
	}
	
	public void frequency(int newFreq){
		System.out.println("CHANGED FREQUENCY OF " + getName() + " to " + newFreq);
		frequency = newFreq;
	}
	
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

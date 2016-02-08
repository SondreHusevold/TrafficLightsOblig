package backend;

import javafx.scene.image.Image;

public class Light {

	private boolean enabled = false;
	private int frequency;
	private String name;
	private Image image;
	
	/*!
	 * Default light. 
	 */
	public Light(String imageName){
		image = new Image(imageName);
		frequency = 10;
		name = "Default";
	}
	
	public Light(String _name, int freq, String imageName){
		image = new Image(imageName);
		name = _name;
		frequency = freq;
	}
	
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
		frequency = newFreq;
	}
	
	public int getFrequency(){
		return frequency;
	}
	
	public void enabled(boolean bool){
		enabled = bool;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
}

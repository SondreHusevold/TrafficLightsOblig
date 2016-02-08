package backend;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.*;

import javax.swing.JOptionPane;

import javafx.scene.image.ImageView;

public class TrafficLight {

	final int red = 0, yellow = 1, green = 2;
	
	private int port;
	private String hostName;
	private Socket clientSocket;
	public ImageView view;
	
	Light[] lightSwitches;
	boolean direction;			// Forward or backwards - Red - Yellow - Green, Green - Yellow - Red.
	ScheduledExecutorService scheduler;
	ScheduledFuture<?> futureOfSchedulers;
	
	/*!
	 * Sets the light to default vaules:
	 * 
	 * Red: 10 seconds
	 * Yellow: 2 seconds.
	 * Green: 15 seconds.
	 */
	
	public TrafficLight(ImageView _view){
		view = _view;
		lightSwitches = new Light[3];
		lightSwitches[red] = new Light("Red", 5, "/backend/Red.png");
		lightSwitches[yellow] = new Light("Yellow", 2, "/backend/Yellow.png");
		lightSwitches[green] = new Light("Green", 10, "/backend/Green.png");
	}
	
	/*!
	 *  Specific light switching frequency. 
	 */
	
	public TrafficLight(int redF, int yellowF, int greenF, ImageView _view){
		view = _view;
		lightSwitches = new Light[3];
		lightSwitches[red] = new Light("Red", redF, "images/Red.png");
		lightSwitches[yellow] = new Light("Yellow", yellowF, "images/Yellow.png");
		lightSwitches[green] = new Light("Green", greenF, "images/Green.png");
	}
	
	/*!
	 * Walking sign
	 */
	public TrafficLight(int redF, int greenF, ImageView _view){
		view = _view;
		lightSwitches = new Light[3];
		lightSwitches[red] = new Light("Red", 5, "images/Walk Red.png");
		lightSwitches[green] = new Light("Green", 10, "images/Walk Green.png");
	}
	
	/*!
	 * Sets light frequency based on incoming parameters.
	 */
	
	public void setLightFrequency(int colour, int seconds){
		lightSwitches[colour].frequency(seconds);
	}
	
	public int getLightFrequency(int colour){
		return lightSwitches[colour].getFrequency();
	}
		
	public int getActiveColour(){
		for(int i = 0; i < lightSwitches.length; i++){
			if(lightSwitches[i].isEnabled()){
				return i;
			}
		}
		System.out.println("ERROR: NO LIGHTS ARE ENABLED... RE-ENABLING RED.");
		lightSwitches[red].enabled(true);
		return red;
	}
	
	public String log(){
		for(int i = 0; i < lightSwitches.length; i++){
			if(lightSwitches[i].isEnabled()){
				return ("Traffic Light: " + lightSwitches[i].getName());
			}
		}
		return "Error";
	}
	
	public Callable switchColour(){
		return new Callable(){
			public Object call() throws Exception {
			    	int colour = getActiveColour();
			    	
			    	// Switch color direction
			    	if(colour == red){
			    		direction = true;
			    	}
			    	else if(colour == green){
			    		direction = false;
			    	}
			    	
			    	// Switch to next light.
			    	lightSwitches[colour].enabled(false);
			    	if(direction){
			    		lightSwitches[colour+1].enabled(true);
			    	}
			    	else
			    		lightSwitches[colour-1].enabled(true);
					return colour;
			}
		};
	}
	
	public void changeImage(){
		view.setImage(lightSwitches[getActiveColour()].getImage());
	}
	
	public void scheduleNext(){
		futureOfSchedulers =
			    scheduler.schedule(switchColour(), getLightFrequency(getActiveColour()), TimeUnit.SECONDS);
	}
	
	public void connect(String _hostName, int _port){
		try {
			port = _port;
			hostName = _hostName;
			clientSocket = new Socket(hostName, port);
			System.out.println("Connecting...");
			run();
		} 
		catch (IOException e){
			
			// Asks to reconnect if it failed to connect to the host.
			int choice = JOptionPane.showOptionDialog(null, ("Failed to connect to " + hostName + "\n\nReconnect?"), "Failed to connect to host.",
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, JOptionPane.YES_OPTION);
			
			if(choice == JOptionPane.YES_OPTION)
				connect(hostName, port);
			else
				System.exit(1);
		}
	}
	
	public void disconnect(){
		futureOfSchedulers.cancel(true);
		scheduler.shutdown();
	}
	
	public void run(){
		
		// Initialize scheduler, setup direction and ports.
		scheduler = Executors.newScheduledThreadPool(3);
		direction = true;
		lightSwitches[red].enabled(true);

		scheduleNext();
		
		Thread t = new Thread(new Runnable() {
	         public void run()
	         {
	        	 try(
        		  	  PrintWriter out =
        		                new PrintWriter(clientSocket.getOutputStream(), true);
        		        // Stream reader from the socket
        		        BufferedReader in =
        		                new BufferedReader(
        		                        new InputStreamReader(clientSocket.getInputStream()));
        		     ) {
        				while(!futureOfSchedulers.isCancelled()){
        					if(futureOfSchedulers.isDone()){
        						changeImage();
        						scheduleNext();
        					}
        				}
        		    } 
        		    catch (UnknownHostException e) {
        		        System.err.println("ERROR: COULD NOT FIND HOST: " + hostName);
        		        System.exit(1);
        		    } 
        		    catch (IOException e) {
        		        System.err.println("ERROR: CANNOT GAIN I/O PRIVILEGE ON HOST: " +
        		                hostName);
        		        System.exit(1);
        		    }
	        	 System.out.println("Done running.");
	         }
		});
		t.start();
	}
}

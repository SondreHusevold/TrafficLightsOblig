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

	final int red = 0, yellow = 1, green = 2;	// Makes it easy to call the lights in the array.
	
	private int port;
	private String hostName;
	private Socket clientSocket;
	public ImageView view;
	
	Light[] lightSwitches;						// Array which contains the three lights which is easily callable by using the finals above.
	boolean direction;							// Forward or backwards - Red - Yellow - Green, Green - Yellow - Red.
	ScheduledExecutorService scheduler;			// A "task". 
	ScheduledFuture<?> futureOfSchedulers;		// A queue for schedulers.
	
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
		lightSwitches[red] = new Light("Red", 5, "/images/Red.png");
		lightSwitches[yellow] = new Light("Yellow", 2, "/images/Yellow.png");
		lightSwitches[green] = new Light("Green", 10, "/images/Green.png");
	}
	
	/*!
	 *  Specific light switching frequency. 
	 */
	
	public TrafficLight(int redF, int yellowF, int greenF, ImageView _view){
		view = _view;
		lightSwitches = new Light[3];
		lightSwitches[red] = new Light("Red", redF, "/images/Red.png");
		lightSwitches[yellow] = new Light("Yellow", yellowF, "/images/Yellow.png");
		lightSwitches[green] = new Light("Green", greenF, "/images/Green.png");
	}
	
	/*!
	 * Walking sign
	 */
	public TrafficLight(int redF, int greenF, ImageView _view){
		view = _view;
		lightSwitches = new Light[3];
		lightSwitches[red] = new Light("Red", redF, "/images/Walk Red.png");
		lightSwitches[yellow] = new Light("Yellow", 0, "/images/Walk Red.png");
		lightSwitches[green] = new Light("Green", greenF, "/images/Walk Green.png");
	}
	
	/*!
	 * Sets light frequency based on incoming arguments.
	 */
	
	public void setLightFrequency(int colour, int seconds){
		lightSwitches[colour].frequency(seconds);
	}
	
	public int getLightFrequency(int colour){
		return lightSwitches[colour].getFrequency();
	}
	
	/*!
	 * Goes through the lights and looks for which one is lit at this point.
	 */
	
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
	
	/*
	 * Callable method that the scheduler runs when it reaches its time.
	 * 
	 * Will gather the current active colour and set the direction of the next light.
	 * If it reaches red, it'll change direction and go downwards. If it reaches green, it'll go upwards.
	 * 
	 * It'll also change to the next light by disabling the current light, and enabling the next light in line according to the direction.
	 */
	
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
	
	/*!
	 * Switches the image shown on the client's user interface. 
	 */
	
	public void changeImage(){
		view.setImage(lightSwitches[getActiveColour()].getImage());
	}
	
	/*!
	 * Schedules the next colour switch by getting the light's frequency.
	 */
	
	public void scheduleNext(){
		futureOfSchedulers =
			    scheduler.schedule(switchColour(), getLightFrequency(getActiveColour()), TimeUnit.SECONDS);
	}
	
	
	/*!
	 * Connects to the server by using the clientSocket. Will start the traffic light immediately after connection succeeded.
	 */
	public void connect(String _hostName, int _port){
		try {
			port = _port;
			hostName = _hostName;
			clientSocket = new Socket(hostName, port);
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
	
	/*!
	 * Shuts down the scheduler and the scheduler queue.
	 */
	public void disconnect(){
		futureOfSchedulers.cancel(true);
		scheduler.shutdown();
	}
	
	/*!
	 * Main stuff where all the magic happens.
	 */
	public void run(){
		
		// Initialize scheduler, setup direction and ports.
		scheduler = Executors.newScheduledThreadPool(3);
		direction = true;									// Sets the direction downwards 
		lightSwitches[red].enabled(true);					// Enables the red light automatically

		scheduleNext();										// Schedules the red light for change.
		changeImage();										// Changes the image to represent what happens above
		
		// Create a new thread, starts to listen for input and output from the PrintWriter and bufferedreader.
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
        				while(!futureOfSchedulers.isCancelled()){   // As long as the scheduler queue isn't cancelled
        					if(futureOfSchedulers.isDone()){		// If the scheduler has completed its task of changing the colour
        						changeImage();						// Change the image
        						scheduleNext();						// Schedule the next colour change.
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
		// This will start the thread above. Required, otherwise the thread will never start running the Runnable code.
		t.start();
	}
}

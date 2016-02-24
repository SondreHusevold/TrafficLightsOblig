package backend;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.*;
import application.GUIController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;

/*!
 * The traffic light class is meant as the actual traffic light or a walking sign.
 * 
 * It consists of three lights - Red, Yellow and Green and is where the client connects with the host.
 * It is here the scheduling is done for the client, and it is here where the server and client communicates.
 * This is also where the images are changed, the frequencies are set and input/output is interpreted. 
 */

public class TrafficLight {

	final int red = 0, yellow = 1, green = 2;	// Makes it easy to call the lights in the array.
	
	private int port;
	private String hostName;
	private Socket clientSocket;
	public ImageView view;
	public boolean quit, walk;
	private PrintWriter out;
	private BufferedReader in;
	public Thread tlightThread;
	
	Light[] lightSwitches;						// Array which contains the three lights which is easily callable by using the finals above.
	boolean direction;							// Forward or backwards - Red - Yellow - Green, Green - Yellow - Red.
	ScheduledExecutorService scheduler;			// A "task". 
	ScheduledFuture<?> futureOfSchedulers;		// A queue for schedulers.
	
	/*!
	 * Sets the light to default values:
	 * 
	 * Red: 5 seconds
	 * Yellow: 2 seconds.
	 * Green: 5 seconds.
	 */
	
	public TrafficLight(ImageView _view, boolean walk_){
		view = _view;
		lightSwitches = new Light[3];
		walk = walk_;
		
		// Walking Sign
		if(walk){
			lightSwitches[red] = new Light("Red", 5, "/images/Walk Red.png");
			lightSwitches[yellow] = new Light("Yellow", 2, "/images/Walk Red.png");
			lightSwitches[green] = new Light("Green", 5, "/images/Walk Green.png");
		}
		else{
			lightSwitches[red] = new Light("Red", 5, "/images/Red.png");
			lightSwitches[yellow] = new Light("Yellow", 2, "/images/Yellow.png");
			lightSwitches[green] = new Light("Green", 5, "/images/Green.png");
		}
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
	
	public boolean getWalk(){
		return walk;
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
	
	public void changeImage(String image){
		view.setImage(new Image(image));
	}
	
	/*!
	 * Schedules the next colour switch by getting the light's frequency.
	 */
	
	public void scheduleNext(){
		if(!scheduler.isShutdown() && !quit){
			futureOfSchedulers =
			    scheduler.schedule(switchColour(), getLightFrequency(getActiveColour()), TimeUnit.SECONDS);
		}
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
			Alert alrt = new Alert(AlertType.ERROR, "Check if server is running.\n\nTry reconnecting?", ButtonType.YES, ButtonType.NO);
			alrt.setTitle("Unable to connect to server!");
			alrt.setHeaderText("Could not connect to server.");
			Optional<ButtonType> result = alrt.showAndWait();
			if(result.get() == ButtonType.YES)
				connect(hostName, port);
			else	
				System.exit(1);			// Simply exists the system. Easier than resetting everything back.
		}
	}
	
	/*!
	 * Shuts down the scheduler and the scheduler queue.
	 */
	public void disconnect(){
		try {
			if(!scheduler.isShutdown()){
				futureOfSchedulers.cancel(true);
				scheduler.shutdown();
				while (!scheduler.awaitTermination(10, TimeUnit.MINUTES));
				quit = true;
				changeImage("/images/Off.png");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*!
	 * Initializes the scheduler all over again. Because once it is shut down it is unusable until re-initialized.
	 * This is done when synchronizing and when connecting.
	 */
	public void initializeScheduler(int colour){
		quit = false;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		if(colour == red)
			direction = true;									// Sets the direction downwards
		else if(colour == green)
			direction = false;
		
		for(int i = 0; i < lightSwitches.length; i++){
			lightSwitches[i].enabled(false);
		}
		
		lightSwitches[colour].enabled(true);				// Enables the proper light automatically
	}
	
	/*!
	 * Main stuff where all the magic happens.
	 */
	public void run(){
		
		// Initialize scheduler, setup direction and ports.
		initializeScheduler(red);
		scheduleNext();										// Schedules the red light for change.
		changeImage();										// Changes the image to represent what happens above
		
		// Create a new thread, starts to listen for input and output from the PrintWriter and bufferedreader.
		tlightThread = new Thread(new Runnable(){
			 @Override
	         public void run()
	         {
	        	 try {
	                 	String receivedText;
	                 	out = new PrintWriter(clientSocket.getOutputStream(), true);
	                 	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	                 	
	                 	if(walk)																 // Tells the server what type of client this is. Walk or normal traffic light.
	                 		send("walk");
        				while(!futureOfSchedulers.isCancelled() && !quit && !scheduler.isShutdown()){
        					
        					if(in.ready()){															// Check for incoming message.
        						receivedText = in.readLine();										// Read IV
        						String encryptedText = in.readLine();								// Read encrypted text
        						String decrypted = Crypto.decrypt(encryptedText, receivedText);		// Decrypt.
        						
        						doCommand(decrypted);												// Sets in motion the decrypted command.
        					}
        					
        					if(!(scheduler.isShutdown()) && futureOfSchedulers.isDone()){		// If the scheduler has completed its task of changing the colour
        						scheduleNext();													// Schedule the next colour change.
        						changeImage();													// Change the image
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
	        	 catch (RejectedExecutionException e){
	        		 // This will happen when the client is forcefully disconnected while doing a task.
	        		 // Therefore, the exception will be catched and no action will be done. This is common.
	        	 }
	        	 
	        	// Sending and closing buffers are done here to prevent system from failing to disconnect 
	        	// if the scheduler was interrupted abruptly and got a RejectedExecutionException 
	        	// which would then jump over the kill command and the closing of the buffers.
 				send("kill");
 				
 				try {
					in.close();
	 				out.close();
	 				FXMLLoader fxmlLoader = new FXMLLoader();
	 				BorderPane p = fxmlLoader.load(getClass().getResource("/application/GUI.fxml").openStream());
	 				GUIController controller = (GUIController) fxmlLoader.getController();
	 	            controller.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
 				
	         }
		});
		// This will start the thread above. Required, otherwise the thread will never start running the Runnable code.
		tlightThread.start();
	}
	
    /*!
     * Sends a string to the server.
     */
    public void send(String s){
		String IV = Crypto.getRandomIV();								// Gets IV
		String message = IV + "\n" + Crypto.encrypt(s, IV); 			// Creates the message by including IV, newline and encrypted text.
    	out.println(message);											// Sends encrypted message.
    }
    
	/*!
	 * Completes commands.
	 * 
	 * Checks the command from a decrypted input. This is sent from the run method.
	 * It has three command possibilities:
	 * * "kill" (server asks this client to shut down).
	 * * "C***" (color synchronization. See comments below.)
	 * * "sync" (stops client, waits for server response to synchronize with other clients).
	 * 
	 */
	
	public void doCommand(String decrypted) throws IOException{
		if(decrypted.startsWith("kill")){
			quit = true;
			changeImage("/images/Off.png");
		}
		else if(decrypted.startsWith("C")){
			
			// Typical command will look like this: C015
			// C (command that tells client that this is a color frequency command) 
			// 0 (final int red as declared at the top.)
			// 15 (frequency in seconds)
			int colour = Character.getNumericValue(decrypted.charAt(1));	
			lightSwitches[colour].frequency(Integer.parseInt(decrypted.substring(2)));
		}
		else if(decrypted.startsWith("sync")){
			disconnect();
			initializeScheduler(Character.getNumericValue(decrypted.charAt(decrypted.length()-1)));
			if(walk)
				changeImage("/images/Walk Off.png");
			else
				changeImage("/images/Off.png");
			String receivedText, encryptedText;
			while((receivedText = in.readLine()) != null){
				encryptedText = in.readLine();
				decrypted = Crypto.decrypt(encryptedText, receivedText);
				if(decrypted.startsWith("restart")){
					break;
				}
			}
		}
	}
}

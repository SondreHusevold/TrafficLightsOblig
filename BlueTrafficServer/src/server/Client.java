package server;

import java.net.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.io.*;

/*!
 * A unique thread for a client. This thread is listening on the socket for input or output from the client.
 */

public class Client extends Thread {
	
	private Socket cnctSocket;						// Socket the client is on.
    private InetAddress clntAddr;					// Client's IP address.
    private PrintWriter out;						// Output to client.
    private BufferedReader in;						// Input from client.
    Button currentButton;							// Button it has been assigned.
    private int pos;								// What position it has, 0 or 1. Changing this will change its synchronization.
    private boolean walk = false;					// Whether or not it is a walking signal.

    /*!
     * Default constructor. Sets the socket and the client's IP.
     */
    public Client(Socket _socket) {
        this.cnctSocket = _socket;
        currentButton = null;
        pos = 0;
        clntAddr = _socket.getInetAddress();
    }
    
    /*!
     * Gets the IP of the client.
     */
    public String getIP(){
    	return clntAddr.getHostAddress();
    }

    /*!
     * Gets the position of the client.
     */
    public int getPos(){
    	return pos;
    }
    
    /*!
     * Gets the walk signal boolean of the client.
     */
    public boolean getWalk(){
    	return walk;
    }
    
    /*!
     * Sets the position of the client.
     */
    public void setPos(Button b, int i){
    	if(currentButton != null){
			if(!walk)
				currentButton.setStyle("-fx-graphic: url('/server/button_unused.png'); -fx-background-color: transparent;");
			else
				currentButton.setStyle("-fx-graphic: url('/server/button_unused_walk.png'); -fx-background-color: transparent;");
	}
    	pos = i;
    	currentButton = b;
    }

    /*!
     * This is the thread's running task and is what is constantly running to check for input.
     * 
     * This will initialize the out and in buffers and wait for any new input until the socket is closed forcefully.
     * If certain input is detected like kill and walk, it'll apply certain commands to either end the method completely, or
     * set the walk flag.
     * 
     * In order to see the commands, it'll need to use the Crypto class to decrypt the information located in the incoming input.
     */
    public void run() {
        try {
        	// Initializes output and input to/from the server.
        	out = new PrintWriter(cnctSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cnctSocket.getInputStream()));
            
            // Initialize IV string. Listens on input and sets IV if it receives information.
            String IV;
            boolean keepUp = true;
            Kickstarter.log("Client [" + getIP() + "]: > Connected to the server.");
            
            // Reads from the socket until the thread is stopped or otherwise killed.
            // First reads the IV, then decrypts the next encrypted text.
            while (((IV = in.readLine())!=null && keepUp)) {
        		String decryptedInput = Crypto.decrypt(in.readLine(), IV);
            
            	if(decryptedInput.endsWith("kill")){
            		keepUp = false;
            	}
            	else if(decryptedInput.endsWith("walk"))
            		walk = true;
            	else{
            		// This usually doesn't happen, but if the client sends something that isn't a command, it'll simply send the output to the log.
            		Kickstarter.log("Client [" + getIP() + "]: > " + decryptedInput);
            	}
            }

            // Close the connection socket
            cnctSocket.close();
            disconnect();
        } 
        catch (IOException e) {
            System.out.println("Exception occurred when trying to communicate with the client " + clntAddr.getHostAddress());
            System.out.println(e.getMessage());
        }
    }
    
    /*!
     * Sends a string to the client by encrypting it first and then sending it through the out buffer as long as the output isn't closed.
     */
    public void send(String s){
		String IV = Crypto.getRandomIV();								// Gets IV
		String message = IV + "\n" + Crypto.encrypt(s, IV); 			// Creates the message by including IV, newline and encrypted text.
    	out.println(message);											// Sends encrypted message.
    }
    
    /*!
     * Disconnects the client from the server.
     * 
     * * Closes the input/ouput with the server.
     * * Removes its own IP from the list of clients in the user interface. 
     * * Also sets the main thread to join (remove) this client's thread before removing it from the clientList.
     * 
     */
    public void disconnect(){
        // Close the BufferedReader and PrintWriter connection to the server.
    	try{
	    	out.close();
	    	in.close();
    	} 
    	catch(IOException e){
    		System.out.println("Got error while closing buffer.");
    	}
    	
    	ListView<String> list = Kickstarter.list_clientList;
    	Kickstarter.applicableList.remove(getIP());
    	
    	// Set the button as "unused" so that it perfectly clear that the position is ready.
    	if(currentButton != null){
    		Platform.runLater(() -> {
				if(!walk){
					currentButton.setStyle("-fx-graphic: url('/server/button_unused.png'); -fx-background-color: transparent;");
				}
				else{
					currentButton.setStyle("-fx-graphic: url('/server/button_unused_walk.png'); -fx-background-color: transparent;");
				}
    		});
    	}
    	
    	// Run the following on the application thread.
		Platform.runLater(() -> {
			list.setItems(FXCollections.observableArrayList(Kickstarter.applicableList));
			try {
				this.join();
				Kickstarter.clientList.remove(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		Kickstarter.log("Client [" + getIP() + "]: > Disconnected from the server.");
    }
}

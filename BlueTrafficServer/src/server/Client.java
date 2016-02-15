package server;

/*!
 * 
 */

import java.net.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.*;

/*!
 * A unique thread for a client. This thread is listening on the socket for input or output from the client.
 */

public class Client extends Thread {
	
	private Socket cnctSocket;						// Socket the client is on.
    private InetAddress clntAddr;					// Client's IP address.
    private TextArea log;							// Logging area from the user interface for appending.
    private  PrintWriter out;
    private  BufferedReader in;
    
    /*!
     * Default constructor. Sets the socket and the client's IP.
     */
    public Client(Socket _socket) {
        this.cnctSocket = _socket;
        clntAddr = _socket.getInetAddress();
    }
    
    public String getIP(){
    	return clntAddr.getHostAddress();
    }

    public void run() {
        try {
        	// Initializes output and input to/from the server.
        	out = new PrintWriter(cnctSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cnctSocket.getInputStream()));
            
            // Initialize IV string. Listens on input and sets IV if it receives information.
            String IV;
            boolean keepUp = true;
            Kickstarter.log("Client [" + clntAddr.getHostAddress() + "]: > Connected to the server.");
            
            // Reads from the socket until the thread is stopped or otherwise killed.
            while (((IV = in.readLine())!=null && keepUp)) {
        		String decryptedInput = Crypto.decrypt(in.readLine(), IV);
            
            	if(decryptedInput.endsWith("kill")){
            		keepUp = false;
            	}
            	else{
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
     * Sends a string to the client.
     */
    public void send(String s){
		String IV = Crypto.getRandomIV();								// Gets IV
		String message = IV + "\n" + Crypto.encrypt(s, IV); 		// Creates the message by including IV, newline and encrypted text.
    	out.println(message);											// Sends encrypted message.
    }
    
    /*!
     * Disconnects the client from the server.
     * 
     * - Closes the input/ouput with the server.
     * - Removes its own IP from the list of clients in the user interface. 
     * - Also sets the main thread to join (remove) this client's thread before removing it from the clientList.
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
		
		Kickstarter.log("Client [" + getIP() + "]: > Disconnceted from the server.");
    }
}

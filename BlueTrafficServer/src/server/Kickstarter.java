package server;

import java.awt.SecondaryLoop;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class Kickstarter implements Runnable {

	public static TextArea log;							// Static wrapper for the log.
	public static ListView<String> list_clientList;		// List of clients shown on the right hand side of the user interface. Needs to be SET.
	public static List<Client> clientList;				// List of clients currently connected. Makes it easy to tell commands to all clients connected to the server.
	public static List<String> applicableList;			// Is applied to the ListView.
	public boolean isRunning;							// Stops the server if set to false.
	private int port;
	
	/*!
	 * Default constructor and main method. 
	 * 
	 * Will set all parameters to their respective variables. Will also create a listener on the list of clients.
	 */
	public Kickstarter(ListView<String> _list_clientList, int argPort, TextArea logging){
		log = logging;
		port = argPort;
		isRunning = true;
		list_clientList = _list_clientList;
		clientList = new ArrayList<Client>();
		applicableList = new ArrayList<String>();
	}
	
	/*!
	 * Gets the list of clients.
	 * 
	 * Unused?
	 */
	public List<Client> getClientList() {
		return clientList;
	}

	/*!
	 * Where the magic happens.
	 * 
	 * As long as the "isRunning" boolean is true, the server will listen on the port for clients who want to respond.
	 * Once the server accepts a client, it'll add it to the clientList where the listener will pick it up and add it to the list of clients.
	 * From here, the client will have its own thread where it can communicate with the server.
	 * 
	 */
	@Override
	public void run() {
		try ( ServerSocket serverSocket = new ServerSocket(port); ) {
		   while (isRunning) {
		       // create and start a new Client thread for each connected client
			   Client clnt = new Client(serverSocket.accept());
			   clientList.add(clnt);
			   clnt.start();
			   
			   // Adds client to the client list on the right.
			   applicableList.add(clnt.getIP());
			   // Required to avoid a thread exception with JavaFX when several clients connect.
			   // Then sets the applicableList to the ListView on the right hand side.
			   Platform.runLater(() -> {
				   list_clientList.setItems(FXCollections.observableArrayList(applicableList));			
			   });
		   }
		} 
		catch (IOException e) {
			System.out.println("Exception occurred when trying to listen on port "
					+ port + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
	/*!
	 *  Logs by appending to the log textarea in the application.
	 *  
	 *  Is static here instead of GUIController due to JavaFX generated variables should not and cannot be static.
	 */
	public static void log(String t){
		log.appendText(t + "\n");
	}
	
	/*!
	 * Sends the new light frequencies to all lights.
	 */
	public void setFrequency(int color, int frequency){
		 String message = "C" + color + frequency;
		 for (Client client : clientList) {
			 client.send(message);
	     }
	}
	
	/*!
	 * Kills all client threads by killing the "isRunning" part of run().
	 * 
	 * This is used to tell all clients that the server is shutting down and will no longer respond.
	 */
   public void kill() {
       isRunning = false;
       for (Client client : clientList) {
    	   client.send("kill");									// Sends the stop command to all clients.
       }
       applicableList.clear();									// Clears the client list as server has stopped.
		Platform.runLater(() -> {
			list_clientList.setItems(FXCollections.observableArrayList(applicableList));
		});
		
   }
	
}

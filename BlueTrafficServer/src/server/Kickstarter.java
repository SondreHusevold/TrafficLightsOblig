package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

/*!
 * This class is meant as a main method thread where lists, sockets and other information is initialized and taken care of.
 * It is basically a bridge between the GUIController and the respective clients. This ensures that the GUIController won't need
 * to have the list fields, nor need to create new clients or listen to the socket for new incoming clients.
 * 
 * This class also synchronizes all clients, and will send a kill command to them if the server is abruptly stopped.
 */

public class Kickstarter implements Runnable {

	public static TextArea log;							// Static wrapper for the log.
	public static ListView<String> list_clientList;		// List of clients shown on the right hand side of the user interface. Needs to be SET.
	public static List<Client> clientList;				// List of clients currently connected. Makes it easy to tell commands to all clients connected to the server.
	public static List<String> applicableList;			// Is applied to the ListView.
	public boolean isRunning;							// Stops the server if set to false.
	private int port;	
	ServerSocket serverSocket;
	
	/*!
	 * Default constructor and main method. 
	 * 
	 * Will set all parameters to their respective variables. Will also create a listener on the list of clients.
	 */
	public Kickstarter(ListView<String> _list_clientList, int argPort, TextArea _log) throws IOException{
		log = _log;
		port = argPort;
		isRunning = true;
		list_clientList = _list_clientList;
		clientList = new ArrayList<Client>();
		applicableList = new ArrayList<String>();
	}

	/*!
	 * As long as the "isRunning" boolean is true, the server will listen on the port for clients who want to respond.
	 * Once the server accepts a client, it'll add it to the clientList where the listener will pick it up and add it to the list of clients.
	 * From here, the client will have its own thread where it can communicate with the server.
	 * 
	 */
	@Override
	public void run() {
		try {
		   serverSocket = new ServerSocket(port);
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
				   synchronizeClients();
				   list_clientList.setItems(FXCollections.observableArrayList(applicableList));			
			   });
		   }
		} 
		catch (IOException e) {
			// If the socket is already in use
			if(e.getMessage().contains("already")){
				Platform.runLater(()->{
					Alert alrt = new Alert(AlertType.ERROR, "The specified socket is already in use and the server is unable to connect to clients. "
							+ "\n\nPlease stop the server and choose another socket, or close the program using the socket.", ButtonType.OK);
					alrt.setTitle("Unable to start server!");
					alrt.setHeaderText("Unable to start with socket " + port + ".");
					alrt.showAndWait();
				});
			}
			
			// There will be an IO exception if the server is stopped as it is listening for a new connection.
			// This is done on purpose in order to make it jump out of the run method and thus close off the thread.
		}
	}
	
	/*!
	 *  Logs by appending to the log textarea in the application.
	 *  
	 *  Is static here instead of GUIController due to JavaFX generated variables should not and cannot be static.
	 */
	public static void log(String t){
		Platform.runLater(() -> {
			log.appendText(t + "\n");
		});
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
	 * Synchronizes all the clients by sending each client a 'sync0' or 'sync2' command. The numbers at the end of these commands
	 * corresponds to which lane they're supposed to synchronize with. 
	 * 
	 * The thread will then sleep for around a second to ensure that all the clients are done cancelling their jobs and ready to start over again.
	 * After that second has passed, it'll send all the clients a 'restart' command and they'll all end their loops and begin anew simultaneously. 
	 */
	@SuppressWarnings("static-access")
	public void synchronizeClients(){
		for(Client client : clientList){
			if(client.getPos() % 2 == 0)
				client.send("sync0");		// Red is known as 0 in the TrafficLight class.
			else
				client.send("sync2");		// Green is known as 2 in the TrafficLight class.
		}
		try {
			Thread.currentThread().sleep(1500);		// Have the server wait 1,5 seconds to ensure that each client is ready to restart their schedulers.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(Client client : clientList){
			client.send("restart");					// Sends a "GO!" command to all the clients simultaneously. 
		}
		log("Server: > Clients synchronized.");
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
		try {
			if(serverSocket != null)
				serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
   }
   
   /*!
    * This will active once a button is pressed on the map tab.
    * 
    * It will assign a map position for the client and its button. It will then log it and synchronize all the clients to ensure they are all
    * on the same page. This will thus change their synchronization depending on where they stand on the map.
    */
   public boolean assignLight(Button b, int list, int pos, boolean walk){
	   if(list < 0){
			Platform.runLater(()->{
				Alert alrt = new Alert(AlertType.ERROR, "Please choose a client from the list on the right hand side.", ButtonType.OK);
				alrt.setTitle("You have not chosen a client.");
				alrt.setHeaderText("Cannot assign client.");
				alrt.showAndWait();
			});
			return false;
	   }
	   if(clientList.get(list).getWalk() != walk)
		   return false;
	   clientList.get(list).setPos(b, pos);
	   log("Change position of client: " + clientList.get(list).getIP() + " to another position.");
	   synchronizeClients();
	   return true;
   }
	
}

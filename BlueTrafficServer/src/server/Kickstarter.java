package server;

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

	public TextArea log;
	public ListView<String> list_clientList;		// List of clients shown on the right hand side of the user interface.
	public ObservableList<Client> clientList;		// List to show inside the ListView. ListView requires an "ObservableList".
	public boolean isRunning;						// Stops the server if set to false.
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
		clientList = FXCollections.observableList(new ArrayList<Client>());
		clientList.addListener(new ListChangeListener<Client>() {
	      @Override
	      public void onChanged(ListChangeListener.Change change) {
	    	List<String> listOfClients = new ArrayList<String>();
	    	for (int i = 0; i < clientList.size(); i++) {
				listOfClients.add(clientList.get(i).clntAddr.getHostAddress());
			}
    		list_clientList.setItems(FXCollections.observableList(listOfClients));
	      }
	    });
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
		// TODO Auto-generated method stub
		
		try ( ServerSocket serverSocket = new ServerSocket(port); ) {
		   while (isRunning) {
		       // create and start a new ClientServer thread for each connected client
			   Client clnt = new Client(serverSocket.accept(), log);
			   clnt.start();
			   
			   // Required to avoid a thread exception with JavaFX when several clients connect.
			   Platform.runLater(() -> {
				   // Adds client to the client list on the right.
				   clientList.add(clnt);
			   });
		       log("New client connected: " + clnt.clntAddr.getHostAddress());	// Logs the IP address of the client into the serverlog.
		       
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
	 */
	public void log(String t){
		log.appendText(t + "\n");
	}
	
	/*!
	 * Kills all threads by killing the "isRunning" part of run().
	 */
   public void kill() {
       isRunning = false;
       clientList = null;
   }
	
}

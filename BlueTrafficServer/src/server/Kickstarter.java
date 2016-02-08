package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javafx.collections.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class Kickstarter implements Runnable {

	public TextArea log;
	public ListView<String> list_clientList;
	public ObservableList<Server> clientList;
	public boolean isRunning;
	private int port;
	
	public Kickstarter(ListView<String> _list_clientList, int argPort, TextArea logging){
		log = logging;
		port = argPort;
		isRunning = true;
		list_clientList = _list_clientList;
		clientList = FXCollections.observableList(new ArrayList<Server>());
		clientList.addListener(new ListChangeListener<Server>() {
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
	
	public List<Server> getClientList() {
		return clientList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try ( ServerSocket serverSocket = new ServerSocket(port); ) {
		   while (isRunning) {
		       // create and start a new ClientServer thread for each connected client
			   Server server = new Server(serverSocket.accept(), log);
			   server.start();
		       clientList.add(server);
		       log("New client connected: " + server.clntAddr.getHostAddress());
		       
		   }
		} 
		catch (IOException e) {
			System.out.println("Exception occurred when trying to listen on port "
					+ port + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
	public void log(String t){
		log.appendText(t + "\n");
	}
	
   public void kill() {
       isRunning = false;
   }
	
}

package server;

/*!
 * 
 */

import java.net.*;

import javafx.scene.control.TextArea;

import java.io.*;

/*!
 * A unique thread for a client. This thread is listening on the socket for input or output from the client.
 */

public class Client extends Thread {
	
	Socket cnctSocket;						// Socket the client is on.
    InetAddress clntAddr;					// Client's IP address.
    TextArea log;							// Logging area from the user interface for appending.

    public Client(Socket _socket, TextArea logging) {
    	log = logging;
        this.cnctSocket = _socket;
        clntAddr = _socket.getInetAddress();
    }

    public void run() {
        try (
            // Create server socket with the given port number
        	// This is the output. Using "out" will send information to the client.
            PrintWriter out =
                    new PrintWriter(cnctSocket.getOutputStream(), true);
            // Input is sent through here.
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(cnctSocket.getInputStream()));
        ) {
            String receivedText;
            
            // Reads from the socket until the thread is stopped or otherwise killed.
            while (((receivedText = in.readLine())!=null)) {
                log("Client [" + clntAddr.getHostAddress() + "]: > " + receivedText);
            }

            // Close the connection socket
            cnctSocket.close();

        } catch (IOException e) {
            System.out.println("Exception occurred when trying to communicate with the client " + clntAddr.getHostAddress());
            System.out.println(e.getMessage());
        }
    }
    
    /*!
     * Logs to the user interface text area.
     */
    public void log(String t){
    	
    	log.appendText(t + "\n");
    }
}

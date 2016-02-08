package server;

/*!
 * 
 */

import java.net.*;

import javafx.scene.control.TextArea;

import java.io.*;

public class Server extends Thread {
	
	Socket cnctSocket;
    InetAddress clntAddr;
    TextArea log;

    public Server(Socket _socket, TextArea logging) {
    	log = logging;
        this.cnctSocket = _socket;
        clntAddr = _socket.getInetAddress();
    }

    public void run() {
        try (
            // Create server socket with the given port number
            PrintWriter out =
                    new PrintWriter(cnctSocket.getOutputStream(), true);
            // Stream reader from the connection socket
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(cnctSocket.getInputStream()));
        ) {
            String receivedText;
            
            // read from the connection socket
            while (((receivedText = in.readLine())!=null)) {
                log("Client [" + clntAddr.getHostAddress() + "]: > " + receivedText);
            }

            // close the connection socket
            cnctSocket.close();

        } catch (IOException e) {
            System.out.println("Exception occurred when trying to communicate with the client " + clntAddr.getHostAddress());
            System.out.println(e.getMessage());
        }
    }
    
    public void log(String t){
    	log.appendText(t + "\n");
    }
}

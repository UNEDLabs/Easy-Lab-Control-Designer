package org.colos.roboticsLabs.robots.utils.connections;
/**
 * @author Almudena Ruiz
 *
 */
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketClient {
  
	private Socket clientSocket;
	private DataOutputStream outToServer;
	private String mIP; 
	private int mportNumber;
	
	/**
	 * Opens a socket
	 * 
	 * @param hostIP the IP address
	 * @param portNumber the port number
	 * @param user the user name
	 * @param password the password
	 * @return a socket
	 * @throws Exception
	 */
	public Socket openSocket(String hostIP, int portNumber, String user, String password) throws Exception {
		mIP = hostIP;
		mportNumber = portNumber;
		clientSocket = new Socket(mIP, mportNumber);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		return clientSocket;
	}

	/**
	 * Closes the socket
	 * @throws Exception
	 */

	public void closeSocket() throws Exception {
		clientSocket.close();
		System.out.println("The connection is completed");
	}
	
	/**
	 * Sets the values to the Robot through the socket
	 * @param values
	 * @throws Exception
	 */

	public void writeSocket(String values) throws Exception {
		outToServer.writeBytes(values);
	}
}

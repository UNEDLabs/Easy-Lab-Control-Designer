/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Connection of a Ejs Client
 * This class manages a connection from a client Ejs. It is called from the Server Ejs 
 * which is listening to the clients
 */ 

public class ConnectionEJS {
	
	//Connection state
	private boolean ok = true;	
	
	//Connection socket
	private Socket sock;
	
	//User identificator
	private String identificator;
	
	//Variable to get the type of a connection (Student or Teacher)
	private boolean student = true;
	
	//Variable which manages the chalk assignment of a connection
	private boolean chalk = false;
	
	//Streams in, out of the socket
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	
	/**
	* Constructor of a connection
	* @param con Socket connection of the connection
	*/
	public ConnectionEJS(Socket con){
		this.sock = con;
		try {
			
			//Get the output of the socket
			//this.out = new ObjectOutputStream(new BufferedOutputStream(this.sock.getOutputStream()));
			this.out= new ObjectOutputStream(this.sock.getOutputStream());
			this.out.flush();	//Clean the socket
		} catch (IOException e) {
			System.err.println((QuestionStudentTool.spanish)?"Error en la apertura del buffer":"Error in the Stream opening "+e.getMessage());
			this.ok = false;
		}
		try {
			
			//Get the input of the socket
			this.in = new ObjectInputStream(this.sock.getInputStream());
		} catch (IOException e) {
			System.err.println((QuestionStudentTool.spanish)?"Error en el buffer de entrada":"Error in the input Stream "+e.getMessage());
			this.ok = false;
		}
		if(this.ok){
			//If the connection is ok, extract the socket information
			try {
				DataSocket ds = (DataSocket)this.in.readObject();
				if(ds.getSignal().equals("ID")){
					this.identificator = ds.getIdentificator();	//User
					this.ok = true;	//The data have been extracted correctly
				}
				else this.ok = false; //The data have been extracted incorrectly
				
			}catch (IOException e) {
				System.err.println((QuestionStudentTool.spanish)?"Error en el objeto lector":"Error in the Object reading of the Stream "+e.getMessage());
				this.ok = false;
			}catch (ClassNotFoundException e) {
				System.err.println((QuestionStudentTool.spanish)?"Error de ClassNotFoundException en el buffer":"Error ClassNotFoundException of the Stream "+e.getMessage());
				this.ok = false;
			} 
		}
	}
	
	
	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Close of a connection
	*/
	protected void closeConnection(){
		try {
			this.out.flush();
			this.out.close();
			this.in.close();
			this.sock.close();
			this.ok = false;	//The connection is not ok
		} catch (IOException e) {
			System.out.println((QuestionStudentTool.spanish)?"Error IOException en el cierre de la conexión ":"Error IOException in the closing of the connection "+e.getMessage());
		}
		
	}
	
	/**
	* Get the state of a connection
	* @return boolean True if is a active connection, false otherwise
	* true si la conexión es correcta, false si no lo es.
	*/
	protected boolean activeConnection(){
		return this.ok;
	}
	
	
	/**
	* Get the user identificator of a connection
	* @return String User
	*/
	protected String user(){
		return this.identificator;
	}
	
	
	/**
	* Get the type of a connection
	* @return boolean
	*/
	protected boolean isStudent(){
		return this.student;
	}
	
	
	/**
	* Get the IP of the connection
	* @return InetAdress
	*/
	protected InetAddress IAddress(){
		return this.sock.getInetAddress();
	}
	
	
	/**
	* Return the socket of a connection Ejs
	* @return Socket
	*/
	protected Socket connectionSocket(){
		return this.sock;
	}
	
	
	/**
	* Return the ObjectInputStream of connection
	* @return ObjectInputStream
	*/
	protected ObjectInputStream connectionInput(){
		return this.in;
	}
	
	
	/**
	* Return the ObjectInputStream of connection
	* @return ObjectInputStream
	*/
	protected ObjectOutputStream connectionOutput(){
		return this.out;
	}
	
	
	/**
	* Give the chalk to a connection
	*/
	protected void giveChalk(){
		chalk = true;
	}
	
	/**
	* Remove the chalk of a connection
	*/
	protected void removeChalk(){
		chalk = false;
	}
	
	
	/**
	* Return if this connection have the chalk
	* @return boolean
	*/
	protected boolean getChalk(){
		return chalk;
	}
	// ---------------------------
	// End Protected Methods
	// ---------------------------
	
}

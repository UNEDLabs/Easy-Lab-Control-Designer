/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.net.*;
import java.util.Vector;
import java.io.*;

import javax.swing.JOptionPane;

import org.colos.ejs.library.Animation;
import org.opensourcephysics.display.OSPRuntime;


/**
* Thread of the student applet
*/

public class Receiver extends Thread {
	
	
	private boolean active = true; //status of the socket
	private Socket serverSock;//socket of the server
	private InetAddress serverIP;//server direction

	//Objects in and out
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	//Buffer of the Client
	private Vector<DataSocket> Buffer;
	private String identity;
	private QuestionStudentTool tool;//Para escribir sobre la herramienta
	
	//Management of session
	protected static String labelEnd = "FinishSession";
	protected static String labelID = "ID";
	protected static String labelNotEntry = "Can not entry";
	protected static String labelEntry = "Can entry";
	
	//Options Chalk
	protected static String labelGiveChalk = "GiveChalk";
	protected static String labelGiveRequestChalk = "GiveChalk_OK";
	protected static String labelRemoveChalk = "RemoveChalk";
	protected static String labelRemoveRequestChalk = "RemoveChalk_OK";
	
	//Simulation
	protected static String labelPlay = "play";
	protected static String labelPause = "pause";
	protected static String labelReset = "reset";
	protected static String labelVar = "variable";
	protected static String labelInitialize = "initialize";
	protected static String labelUpdate = "update";
	protected static String labelUpdateAfterModelAction = "updateAfterModelAction";
	protected static String labelExperiment = "experiment";
	
	//Simulation Request
	protected static String labelStepRequest = "step_OK";
	
	//Simulation Get Experiments
	protected static String labelExperimentCol = "expCol";
	
	//Thread to get UDP message
	private ReceiverUDP recUDP = null;
	
	
	
//	/**
//	* Constructor without parameters.
//	*/
//	public Receiver (ThreadGroup group){
//	   super(group,"Receiver EJS");
//	}
		
//	private Receiver() {};
	
	/**
	* Constructor with parameters
	* @param vectorDataSocket Vector Object buffer
	* @param name String Identity of the student
	* @param tool QuestionStudentTool Client Interface
	*/
	public Receiver(ThreadGroup group, Vector<DataSocket> vectorDataSocket, String name, QuestionStudentTool tool){
    super(group,"Receiver EJS");
		this.Buffer = vectorDataSocket;
		this.identity = name;
		this.tool = tool;
	}
	
	 /**
	  * Constructor with parameters
	  * @param vectorDataSocket Vector Object buffer
	  * @param name String Identity of the student
	  * @param tool QuestionStudentTool Client Interface
	  */
	  public Receiver(Vector<DataSocket> vectorDataSocket, String name, QuestionStudentTool tool){
	    this.Buffer = vectorDataSocket;
	    this.identity = name;
	    this.tool = tool;
	  }
	
	
	/**
	* Connection with the socket server
	* @param serverIPString String IP Direction of the master
	* @param port int Port of the socket connection
	*/
	protected boolean connect(String serverIPString, int port){
		
		try {
			
			//Open the server socket
			this.serverIP = InetAddress.getByName(serverIPString);
			//this.serverSock = new Socket(this.serverIP, port); //CJB 030308
			//InetSocketAddress p = new InetSocketAddress(port);
			
			//this.serverSock = new Socket(this.serverIP,port,p.this.getIPLocalReceiver(),port);
			//this.serverSock = new Socket(this.serverIP,port,p.getAddress(),port); //Cambio 12/04/2011**
			this.serverSock = new Socket(this.serverIP,port); 
			//System.out.println("Ver puerto abierto "+this.serverSock.getLocalPort());
			
			if(!this.serverSock.isConnected()){
				System.out.println((QuestionStudentTool.spanish)?"Socket no conectado":"Socket not connected");
				return false;
			}
			
			//Access to the buffers
			this.inputStream(this.serverSock);
			this.outStream(this.serverSock);
	
			//Send the identity of the client
			DataSocket aux = new DataSocket(identity,null,Receiver.labelID,null);
			this.output.writeObject(aux);
			System.out.println((QuestionStudentTool.spanish)?"Enviando objeto identificación...":"Sending the identification object...");
			
			//Creation and Open the UDP socket
			recUDP = OSPRuntime.appletMode ? new ReceiverUDP(this.serverIP,port,this.serverSock.getLocalPort(),this.tool) : new ReceiverUDP(Animation.getThreadGroup(), this.serverIP,port,this.serverSock.getLocalPort(),this.tool);
			recUDP.connect();
		
			this.active = true;
			
			return true;
		} catch (UnknownHostException e) {
			
			tool.setText((QuestionStudentTool.spanish)?"Error en la conexión con el servidor":"Error in the server connection");
			if(tool.isThereInterface()){
				tool.acceptButton.setEnabled(true);
				tool.cancelButton.setEnabled(false);
				tool.userField.setEditable(true);
			}
			tool.sim.connectControls();
			return false;
		} catch (IOException e) {
			
			tool.setText((QuestionStudentTool.spanish)?"Error en los buffers":"Error in the buffers");
			if(tool.isThereInterface()){
				tool.acceptButton.setEnabled(true);
				tool.cancelButton.setEnabled(false);
				tool.userField.setEditable(true);
			}
			tool.sim.connectControls();
			return false;
		}
	}
	

	
	/**
	* Method to get the input socket
	* @param server Socket
	*/
	private void inputStream(Socket server){
		try {
			this.input = new ObjectInputStream(new BufferedInputStream(server.getInputStream()));
			
			//this.input = new ObjectInputStream(server.getInputStream());//Carlos 141107
		} catch (IOException e) {
			tool.setText((QuestionStudentTool.spanish)?"Error IOException intentando abrir el InputStream":"Error IOException trying open the InputStream");
		}
	}

	
	/**
	* Method to get the output socket
	* @param server Socket
	*/
	private void outStream(Socket server){
		try {
			this.output = new ObjectOutputStream(server.getOutputStream());
			output.flush();
		} catch (IOException e) {
			tool.setText((QuestionStudentTool.spanish)?"Error IOException intentando abrir el OutputStream":"Error IOException trying open the OutputStream");
		}
	}
	
	
	/**
	* Disconnection from the teacher
	*/
	protected void disconnectTeacher(){
		 tool.setText((QuestionStudentTool.spanish)?"Desconectada la sesión del teacher":"Disconnect session from the teacher");
		 if(tool.isThereInterface()){
			 tool.acceptButton.setEnabled(true);
			 tool.cancelButton.setEnabled(false);
			 tool.userField.setEditable(true);
		 }
		 //Chalk
		 if(tool.sim.getChalk())
			tool.sim.setChalk(false);
		 //Chalk
		 tool.sim.connectControls();
	}

	
	/**
	* Disconnection from the student. Stop the Thread
	*/
	protected void disconnect(){
		
		//Disconnect the thread
		this.active = false;
		
		//Chalk
		if(tool.sim.getChalk()) tool.sim.setChalk(false);
		
		//Send the finish of the client
		DataSocket aux = new DataSocket(null,null,"Finish",null);
		
		try {
			this.output.writeObject(aux);
		} catch (IOException e1) {e1.printStackTrace();}
		
		try {
			this.output.close();
			this.input.close();
		} catch (IOException e) {
			tool.setText((QuestionStudentTool.spanish)?"Error IOException en los buffers":"Error IOException close or open the OutputStream/InputStream");
		}
		try {
			this.serverSock.close();
		} catch (IOException e) { 
			tool.setText((QuestionStudentTool.spanish)?"Error IOException cerrando el socket":"Error IOException closing the socket");
		}
		
		//Delete all the objects of the buffer
		Buffer.removeAllElements();	
	}
	
	
	/**
	* Method run. Overload of Thread run method. Get the signals of master
	*/
	public void run(){
		recUDP.start();
		while(this.active){
			DataSocket aux;
			try {
				aux = (DataSocket) this.input.readObject();
				
				//Check the messages send from the master
				if(aux.getSignal().equals(Receiver.labelEnd)){
					this.active = false;
					disconnectTeacher();
					tool.stopTimer();
					JOptionPane.showMessageDialog(null, (QuestionStudentTool.spanish)?"El profesor ha cerrado la sesión":"The teacher has closed the session", (QuestionStudentTool.spanish)?"Información":"Information", JOptionPane.INFORMATION_MESSAGE);
				}
				else if(aux.getSignal().equals(Receiver.labelNotEntry)){
					this.active = false;
					disconnectTeacher();
					tool.setText((QuestionStudentTool.spanish)?"Hay muchos clientes":"There are a lot of clients");
				}
				else if(aux.getSignal().equals(Receiver.labelEntry)){
					tool.setText((QuestionStudentTool.spanish)?"Conectado":"Connected");
				}
				else if(aux.getSignal().equals(Receiver.labelPlay)){
					tool.sim.setIsPlaying(true);
				}
				else if(aux.getSignal().equals(Receiver.labelPause)){
					tool.sim.pause();
				}
				else if(aux.getSignal().equals(Receiver.labelReset)){
					tool.sim.reset();
					tool.sim.disconnectControls();
				}
				else if(aux.getSignal().equals(Receiver.labelVar)){
					tool.sim.setVariable(aux.getObject().toString(), aux.value);
					tool.sim.update();
				}
				
				/**STEP**/
				//else if(aux.getSignal().equals(Receiver.labelStep)){
					//Carlos 03/12/07***He cambiado de orden las órdenes
				//	tool.sim.step();
				//	this.output.writeObject(new DataSocket(null,null,Receiver.labelStepRequest,null));
				//}
				
				else if(aux.getSignal().equals(Receiver.labelInitialize)){
					tool.sim.initialize();
				}
				else if(aux.getSignal().equals(Receiver.labelUpdateAfterModelAction)){
					tool.sim.setVariables((String)aux.getObject());
					tool.sim.update();
					//System.out.println("Update en el student");
				}
				else if(aux.getSignal().equals(Receiver.labelUpdate)){
					tool.sim.setVariables((String)aux.getObject());
					tool.sim.update();
				}
				else if(aux.getSignal().equals(Receiver.labelExperiment)){
					tool.sim.runExperiment((String)aux.getObject());
				}
				
				else if(aux.getSignal().equals(Receiver.labelGiveChalk)){
					this.output.writeObject(new DataSocket(null,null,Receiver.labelGiveRequestChalk,null));
					tool.sim.setChalk(true);
					tool.sim.connectControls();
					tool.setText((QuestionStudentTool.spanish)?"Tiza asignada":"Chalk assigned");		
				}
				else if(aux.getSignal().equals(Receiver.labelRemoveChalk)){
					this.output.writeObject(new DataSocket(null,null,Receiver.labelRemoveRequestChalk,null));
					this.recUDP.getSocketUDP().send(this.recUDP.getDatagramPacketUDPEndChalk());
					tool.sim.setChalk(false);
					tool.sim.disconnectControls();
					tool.setText((QuestionStudentTool.spanish)?"Tiza quitada":"Chalk removed");
				}
				
				else if(aux.getSignal().equals(Receiver.labelExperimentCol)){
					tool.sim.setExperimentCol((String)aux.getObject());
				}
				Thread.yield();
			
			} catch (IOException e) {
				tool.setText((QuestionStudentTool.spanish)?"Error IOException leyendo el objeto":"Error IOException reading the object");
				tool.setStateConnection(false);
				this.active = false;
				if(tool.sim.getChalk()) tool.sim.setChalk(false);
				JOptionPane.showMessageDialog(null, (QuestionStudentTool.spanish)?"El profesor ha cerrado temporalmente la sesión":"The teacher has closed temporarily the session", (QuestionStudentTool.spanish)?"Información":"Information", JOptionPane.INFORMATION_MESSAGE);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				tool.setStateConnection(false);
				this.active = false;
				if(tool.sim.getChalk()) tool.sim.setChalk(false);
				tool.sim.connectControls();
				JOptionPane.showMessageDialog(null, (QuestionStudentTool.spanish)?"El profesor ha cerrado temporalmente la sesión":"The teacher has closed temporarily the session", (QuestionStudentTool.spanish)?"Información":"Information", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		tool.setText((QuestionStudentTool.spanish)?"Cliente finalizado":"Client finished");
	}
	
	
	/**
	* Finish the client. Call at method disconnect.
	*/
	protected void finishClient(){
		disconnect();	//Esta función ya realiza la liberación de todos los recursos, menos la memoria.
		recUDP.disconnect();
	}
	
	
	/**
	* Get the object in the buffer
	*/
	protected DataSocket extractObject(){
		DataSocket exit;
		//Return the first object in the buffer
		if(this.Buffer.size() > 0){
			exit = this.Buffer.remove(0);
		}
		else
			return null;
		
		return exit;
	}

	
	/**
	* Get the object DataSocket in the buffer
	*/
	protected Vector<DataSocket> extractVectorDataSocket(){
		Vector<DataSocket> VectorDataSocket;
		if(this.Buffer.size() > 0){
			VectorDataSocket= Buffer;
			Buffer = new Vector<DataSocket>();
		}
		else return null;
		
		return VectorDataSocket;
	}
	
	
	// ---------------------------
	// Protected Getters of the class
	// ---------------------------
	/**
	* Interface to get the output socket
	*/
	protected ObjectOutputStream getOutput(){
		return output;
	}
	
	
	/**
	* Interface to get the input socket
	*/
	protected ObjectInputStream getInput(){
		return input;
	}
	
	
	/**
	* Interface to get the socket UDP
	*/
	protected DatagramSocket getUDP(){
		return this.recUDP.getSocketUDP();
	}
	
	
	/**
	* Interface to get if the step is done
	*/
	protected boolean getStepOk(){
		return this.recUDP.stepOkTeacher;
	}
	
	
	/**
	* Interface to set if the step is done
	*/
	protected void setStepOk(boolean value){
		this.recUDP.stepOkTeacher = false;
	}
	
	
	/**
	* Interface to get the DatagramSocket of the Chalk
	*/
	protected DatagramPacket getDataUDPChalk(){
		return this.recUDP.getDatagramPacketUDPChalk();
	}
	
	
	/**
	* Interface to get the DatagramSocket of Socket UDP
	*/
	protected DatagramPacket getDataUDPChalkReceive(){
		return this.recUDP.getDatagramPacketUDPChalkReceive();
	}
	
	
	protected void setActive(boolean _state){
		this.active = _state;
	}
	
	protected boolean getActive(){
		return this.active;
	}
	// ---------------------------
	// End Protected Getters of the class
	// ---------------------------
	

	
}

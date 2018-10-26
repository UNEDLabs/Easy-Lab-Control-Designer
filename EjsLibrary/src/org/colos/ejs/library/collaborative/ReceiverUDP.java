/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
* Thread UDP of the student applet
*/

public class ReceiverUDP extends Thread {
	
	
	private boolean active; //status of the socket
	
	//Data Server
	private InetAddress serverIP; //server direction
	private int portServer; //port of socket
	
	//Port openned in the UDP socket. It pass from TCP socket
	private int portOpenned;
	
	//Parameters of UDP Protocol
	private DatagramSocket socketUDP;
	private byte[] bufferUDPReceive;
	private byte[] bufferUDPSendChalk;
	private byte[] bufferUDPSend;
	private byte[] bufferUDPEndChalk;
	private DatagramPacket stepSend;
	private DatagramPacket stepReceive;
	private DatagramPacket stepReceiveChalk;
	private DatagramPacket stepSendChalk;
	private DatagramPacket endChalk;
	
	//To manage the simulation and the interface
	private QuestionStudentTool tool;
	
	//Constants
	private final String STEP = "step";
	private final String STEP_OK = "stepOK";
	private final String STEP_T = "steT";
	private final String END_CHALK = "endC";
	
	//StepOk of teacher
	protected boolean stepOkTeacher = false;
	
	
//	/**
//	* Constructor without parameters.
//	*/
//	public ReceiverUDP(ThreadGroup group){
//    super(group,"Receiver UDP EJS");
//		//Initialization
//		initialize();
//		
//		//Default values
//		try {this.serverIP = InetAddress.getByName("127.0.0.1");} catch (UnknownHostException e) {e.printStackTrace();}
//		this.portServer = 50000;
//		this.portOpenned = 50001;
//	}
	
	
	/**
	* Constructor with parameters
	* @param serverIP InetAddress IP address of the server
	* @param portServer int Server port
	* @param portOpenned int Port openned for the student
	* @param tool QuestionStudentTool Tool to manage the simulation
	*/
	public ReceiverUDP(ThreadGroup group, InetAddress serverIP, int portServer,int portOpenned,QuestionStudentTool tool){
    super(group,"Receiver UDP EJS");

		//Initialization
		initialize();
		
		//Update the main values
		this.serverIP = serverIP;
		this.portServer = portServer;
		this.portOpenned = portOpenned;
		this.tool = tool;
		
		//Configure the message to send
		bufferUDPSend = this.STEP_OK.getBytes();
		bufferUDPSendChalk = this.STEP.getBytes();
		bufferUDPEndChalk = this.END_CHALK.getBytes();
		stepSend = new DatagramPacket(bufferUDPSend,bufferUDPSend.length,this.serverIP,this.portServer);
		stepSendChalk = new DatagramPacket(bufferUDPSendChalk,bufferUDPSendChalk.length,this.serverIP,this.portServer);
		endChalk = new DatagramPacket(bufferUDPEndChalk,bufferUDPEndChalk.length,this.serverIP,this.portServer);
	}
	
  /**
  * Constructor with parameters
  * @param serverIP InetAddress IP address of the server
  * @param portServer int Server port
  * @param portOpenned int Port openned for the student
  * @param tool QuestionStudentTool Tool to manage the simulation
  */
  public ReceiverUDP(InetAddress serverIP, int portServer,int portOpenned,QuestionStudentTool tool){

    //Initialization
    initialize();
    
    //Update the main values
    this.serverIP = serverIP;
    this.portServer = portServer;
    this.portOpenned = portOpenned;
    this.tool = tool;
    
    //Configure the message to send
    bufferUDPSend = this.STEP_OK.getBytes();
    bufferUDPSendChalk = this.STEP.getBytes();
    bufferUDPEndChalk = this.END_CHALK.getBytes();
    stepSend = new DatagramPacket(bufferUDPSend,bufferUDPSend.length,this.serverIP,this.portServer);
    stepSendChalk = new DatagramPacket(bufferUDPSendChalk,bufferUDPSendChalk.length,this.serverIP,this.portServer);
    endChalk = new DatagramPacket(bufferUDPEndChalk,bufferUDPEndChalk.length,this.serverIP,this.portServer);
  }
  
	// ---------------------------
	// Private Methods
	// ---------------------------
	/**
	* Initialization of variables
	*/
	private void initialize(){
		this.active = true;
		this.socketUDP = null;
		this.bufferUDPReceive = new byte[4];//Step, SteT
		this.bufferUDPSendChalk = new byte[4];//Step chalk
		this.bufferUDPSend = new byte[6];//StepOK
		this.bufferUDPEndChalk = new byte[4];//EndC
		this.stepSend = null;
		
		//Configure the message to receive (buffer)
		this.stepReceive = new DatagramPacket(bufferUDPReceive,bufferUDPReceive.length);
		
	}
	// ---------------------------
	// End Private Methods
	// ---------------------------
	
	
	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Connect the server UDP
	*/
	protected void connect(){

		try {
			this.socketUDP = new DatagramSocket(this.portOpenned); //Cambio 12/04/2011**
			//this.socketUDP = new DatagramSocket();
		}catch (SocketException e) {e.printStackTrace();tool.setText((QuestionStudentTool.spanish)?"Error abriendo el canal UDP":"Error openning UDP tunnel");} 
	}
	
	/**
	* Discconnect the server UDP
	*/
	protected void disconnect(){
		this.active = false;
		socketUDP.close();
		tool.setText((QuestionStudentTool.spanish)?"Cliente UDP terminado":"Client UDP finished");
	}
	
	
	/**
	* Get the socket UDP
	* @return DatagramPacket
	*/
	protected synchronized DatagramSocket getSocketUDP(){
		return this.socketUDP;
	}
	
	
	/**
	* Get the DatagramPacket
	* @return DatagramPacket
	*/
	protected DatagramPacket getDatagramPacketUDPChalk(){
		return this.stepSendChalk;
	}
	
	
	/**
	* Get the DatagramPacket
	* @return DatagramPacket 
	*/
	protected DatagramPacket getDatagramPacketUDPChalkReceive(){
		return this.stepReceiveChalk;
	}
	
	protected DatagramPacket getDatagramPacketUDPEndChalk(){
		return this.endChalk;
	}
	// ---------------------------
	// End Protected Methods
	// ---------------------------
	
	
	/**
	* Method run. Overload of Thread run method. Get the signals of master
	*/
	public void run(){
		String temp;
		while(this.active){
			try {
				//Receive a data
				this.socketUDP.receive(stepReceive);
			
				//Get the data
				temp = new String(stepReceive.getData(),0,stepReceive.getData().length);

				//Management of step
				if(temp.equals(this.STEP)){
					tool.sim.step();
					this.socketUDP.send(this.stepSend);//Comento 19/02/08***
				}
				
				else if(temp.equals(this.STEP_T)){
					this.stepOkTeacher = true;
					//System.out.println("Recibo del teacher "+temp);
				}
				Thread.yield();
			}catch (IOException e){
				e.printStackTrace();
				this.active = false;
				tool.setText((QuestionStudentTool.spanish)?"Error en el Thread UDP":"Error in the UDP Thread");
			}
		}
	}
	
}

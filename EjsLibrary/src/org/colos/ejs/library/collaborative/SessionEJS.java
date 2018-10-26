/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.colos.ejs.library.Animation;
import org.opensourcephysics.display.OSPRuntime;


/**
 * Session of a virtual class. This class manages all the connections which are connected to the master
 * In this file, there are four classes embedded:
 * - SessionEJS: main control of the virtual class. She manage the Connection Vector, 
 * - SessionHandler: class which remove the died threads in the session
 * - Sender: thread class which manages the sending of the objects
 * - ReadInputThread: thread class which manages the objects sent from the student 
 */

public class SessionEJS implements Runnable{
	
	
	/**PUBLIC**/
	//Number of session
	public int session;
	
	/**PROTECTED**/
	//Label to finish the session
	protected static String endSession = "FinishSession";
	
	//Counters
	protected final static int MAXCUSTOMER=70;	
	protected int counter = 0;
	
	/**PRIVATE**/
	private Vector<ConnectionEJS> myConnections;//Vector of connections
	private Vector<DataSocket> buffer;//Buffer to send at the clients
	private Vector<ReadInputThread> childrens;//Vector of client threads
	private Sender sender;//Sender of the buffer's objects 
	private sessionHandler administrator=null;//Handler of the sessionEJS
	
	//To control de view
	private ListStudentsTree myList;
	
	//Chalk management
	private boolean isThereChalk = false;
	private int numberChalk = -1;
	
	//Server UDP
	private boolean activeServerUDP = false;
	private Thread serverUDP = null;
	//Constants get the UDP client
	private final String STEP = "step";
	private final String STEPT ="steT";
	private final String END_CHALK ="endC";
	
	
	//--------------
	//UDP Management
	//--------------
	protected DatagramSocket socketUDP = null;
	protected byte[] bufferUDPReceive = new byte[6];//stepOK
	protected byte[] bufferUDPReceiveChalk = new byte[4]; //step 
	protected byte[] bufferUDPSendChalk = new byte[4]; //steT
	protected byte[] bufferUDPSend = new byte[4];//step
	//protected DatagramPacket[] stepSend = new DatagramPacket[this.MAXCUSTOMER];
	
	//Carlos 17/12/2007***
	protected ArrayList<DatagramPacket> stepSendList = new ArrayList<DatagramPacket>();
	//Carlos 17/12/2007***
	
	protected DatagramPacket stepReceive = new DatagramPacket(bufferUDPReceive,bufferUDPReceive.length);
	protected DatagramPacket stepReceiveChalk = new DatagramPacket(bufferUDPReceiveChalk,bufferUDPReceiveChalk.length);
	protected DatagramPacket stepSendChalk = null;
	//--------------
	//UDP Management
	//--------------

	
	
	/**
	* Constructor without parameters.
	*/
	public SessionEJS(){}
	
	
	/**
	* Constructor with parameters
	* @param tree ListStudentsTree Tree Interface
	*/
	public SessionEJS(ListStudentsTree tree){
		
		//Assign the tool
		this.myList = tree;
		tree.txtArea.setText("");
		tree.txtArea.setText((ConfTeacherTool.spanish)?"Creando una nueva sesión...":"Creating a new session...");
		
		//Assign memory
		this.myConnections = new Vector<ConnectionEJS>();
		this.buffer = new Vector<DataSocket>();
		this.childrens = new Vector<ReadInputThread>();
		
		//Create and run the sender
		this.sender = OSPRuntime.appletMode ? new Sender(this.myConnections, this.buffer) : new Sender(Animation.getThreadGroup(),this.myConnections, this.buffer);
		this.sender.start();
		
		
		//Create socketUDP. Open the DatagramSocket in the same port of the server
		try {
			socketUDP = new DatagramSocket(tree.port);
			bufferUDPSend = this.STEP.getBytes();
			bufferUDPSendChalk = this.STEPT.getBytes();
			//System.out.println("Creo el socket UDP y mensaje");
		}catch (SocketException e) {e.printStackTrace();}
	}

	/**
	* Creation of a new connection in the virtual class
	* @param aux ConnectionEJS This object contains all the information of the student connected
	*/
	public synchronized void newConnectionSession(ConnectionEJS aux){
		
		System.out.println((ConfTeacherTool.spanish)?"Añadiendo un nuevo socket cliente...":"Adding a new socket client...");
		
		//There are not connections
		if(this.myConnections.size()==0)
		{
			//Add a new connection passed
			this.myConnections.add(aux);
			
			//Creation of the sesionHandler
			if(this.administrator==null)
			{
				this.administrator = OSPRuntime.appletMode ? new sessionHandler(this, this.childrens, this.myConnections) : new sessionHandler(Animation.getThreadGroup(),this, this.childrens, this.myConnections);
				this.administrator.setDaemon(true);
				this.administrator.start();
			}
		}
		else this.myConnections.add(aux);
		
		//Add to the view a new Node
		//CJB110308
//		Si nos da problemas podemos poner this.myList.port en esta sentencia, porque realmente está esperando en el mismo puerto
		String _args[] = {aux.IAddress().getHostName(),Integer.toString(aux.connectionSocket().getPort()),aux.user()};
		myList.addNode(aux.user(), _args);
		//CJB110308
		
		/**UDP**/
		//Creation of DatagramPackets with the port and IP of client. Server UDP
		//DatagramPacket auxPacket = new DatagramPacket(bufferUDPSend,bufferUDPSend.length,aux.IAddress(),aux.connectionSocket().getPort());
		//CJB110308
		//Si nos da problemas podemos poner this.myList.port en esta sentencia, porque realmente está esperando en el mismo puerto
		DatagramPacket auxPacket = new DatagramPacket(bufferUDPSend,bufferUDPSend.length,aux.IAddress(),aux.connectionSocket().getPort());
		//CJB110308
		stepSendList.add(counter,auxPacket);
		
		//stepSend[counter] = new DatagramPacket(bufferUDPSend,bufferUDPSend.length,aux.IAddress(),aux.connectionSocket().getPort());
		//System.out.println("Creating UDP step "+stepSend[counter].getPort()+" "+stepSend[counter].getAddress().getHostAddress());
		
		
		/**READ_INPUT_THREAD**/
		//Creation of a new reader and to throw it
		ReadInputThread readerAux = OSPRuntime.appletMode ? new ReadInputThread(buffer,aux,this) : new ReadInputThread(Animation.getThreadGroup(),buffer,aux,this);

		//readerAux.setPriority(Thread.MIN_PRIORITY);
		readerAux.start();
		
		//Add to the vector of readers
		this.childrens.add(readerAux);
		readerAux = null;
		
		//Addtion at the counter
		counter++;
		
		//Transfering the current state of the simulation
		try{
			aux.connectionOutput().writeObject(new DataSocket(null,myList.sim.getVariables(),"updateAfterModelAction",null));
		}catch (IOException e) {}
		
		//New client in the list
		String w1 = (ConfTeacherTool.spanish)?"Nuevo cliente número ":"New client number ";
		String w2 = (ConfTeacherTool.spanish)?" y nombre ":" and name ";
		myList.setText(w1+counter+w2+aux.user());
	}

		
	/**
	* Close a connection with a user identification (String ident) from the master
	* @param ident String User identification to close it
	*/
	public synchronized void closeConnectionSession(String ident){
	
		
		//For of the connections
		for(int i = 0; i < this.myConnections.size(); i++){
			if(this.myConnections.elementAt(i).user().equals(ident)){
				
				//Get the connection
				System.out.println((ConfTeacherTool.spanish)?"Cerrando la conexión...":"Closing the connection...");
				ConnectionEJS aux = this.myConnections.elementAt(i);
				
				//Send the "finish" label
				try {
					aux.connectionOutput().writeObject(new DataSocket(null,null,"FinishSession",null));
				} catch (IOException e) {}
				
				//Closing the student connection and removing the chalk if it exists
				aux.closeConnection();
				if(aux.getChalk())
				{
					aux.removeChalk();
					this.myList.sim.connectControls();
				}
				
				stepSendList.remove(i);
								
				//The sessionHandler does the rest
				this.childrens.elementAt(i).stopReaderActive();
		
				//To exit of the for
				i = this.myConnections.size();
			}
		}
	}
	
	
	/**
	* Give the connection to a student
	* @param ident String User identification to give the chalk
	*/
	public synchronized void giveChalkConnection(String ident){
	
		System.out.println((ConfTeacherTool.spanish)?"Dando la tiza a un estudiante... ":"Giving the chalk at a connection...");
		
		//For of the connections
		for(int i = 0; i < this.myConnections.size(); i++){
			if(this.myConnections.elementAt(i).user().equals(ident)){
				
				//Get the connectionEJS and get the connection number
				ConnectionEJS aux = this.myConnections.elementAt(i);
				numberChalk = i;
				
				//Send the "GiveChalk" message to the student
				try {
					aux.connectionOutput().writeObject(new DataSocket(null,null,"GiveChalk",null));
				} catch (IOException e) {System.err.println((ConfTeacherTool.spanish)?"Error en la tiza ":"Error in chalk "+e.getMessage());}
				
				//Set the boolean variable chalk to true in the class ConnectionEJS
				aux.giveChalk();
				isThereChalk = true;
				this.myList.sim.setIsThereChalk(true);
				
				/**UDP Management**/
				//Data "steT" with the chalk
				stepSendChalk = new DatagramPacket(bufferUDPSendChalk,bufferUDPSendChalk.length,aux.IAddress(),aux.connectionSocket().getPort());
				if(serverUDP==null){
					serverUDP = OSPRuntime.appletMode ? new Thread(this) : new Thread(Animation.getThreadGroup(),this);
					this.activeServerUDP = true;
					/*try {
						this.socketUDP.setSoTimeout(1);
					} catch (SocketException e) {e.printStackTrace();}*/
					serverUDP.start();
				}
				/**UDP Management**/
				
				//To exit of the for
				i = this.myConnections.size();
			}
		}
	
	}
	
	 
	/**
	* Remove the connection to a student
	* @param ident String User identification to remove the chalk
	*/
	public synchronized void removeChalkConnection(String ident){
			
			System.out.println((ConfTeacherTool.spanish)?"Quitando la tiza a un estudiante...":"Removing the chalk of a connection...");
			
			//For of the connections
			for(int i = 0; i < this.myConnections.size(); i++){
				if(this.myConnections.elementAt(i).user().equals(ident)){

					//Get the connectionEJS 
					ConnectionEJS aux = this.myConnections.elementAt(i);
					
					//Send the "RemoveChalk" message to the student
					try {
						aux.connectionOutput().writeObject(new DataSocket(null,null,"RemoveChalk",null));
					} catch (IOException e) {System.err.println((ConfTeacherTool.spanish)?"Error borrando la tiza ":"Error removing the chalk "+e.getMessage());}
					
					//Remove the chalk in the master
					numberChalk = -1;
					aux.removeChalk();
					isThereChalk = false;
					this.myList.sim.setIsThereChalk(false);
					
					/**UDP Management**/
					this.activeServerUDP = false;
					serverUDP=null;
					/**UDP Management**/
					
					//To exit of the for
					i = this.myConnections.size();
				}
			}
	}
	
	
	/**
	* Close all the session. All the students will be disconnected
	*/
	public synchronized void closeSession(){
		
		//Print messages
		this.getMyList().setText((ConfTeacherTool.spanish)?"Cerrando la sesión virtual...":"Clossing the session...");
		System.out.println((ConfTeacherTool.spanish)?"Parando los threads...":"Stoping the threads...");
		ConnectionEJS aux;
		
		//ConnectionEJS object
		int j =this.myConnections.size();
		
		for(int i = 0; i <j ; i++){
			
			//Get the connection
			aux = this.myConnections.elementAt(i);
			
			//Send the "finish" label from the master
			try {
				aux.connectionOutput().writeObject(new DataSocket(null,null,"FinishSession",null));
			} catch (IOException e) {}
			
			//Closing the student connection and removing the chalk if it exist
			aux.closeConnection();
			
			//Chalk
			if(aux.getChalk())
			{
				aux.removeChalk();
				this.myList.sim.connectControls();
			}
			
			//The sessionHandler does the rest
			this.childrens.elementAt(i).stopReaderActive();
			this.getMyList().removeNode(aux.user());
		}
		
		//Closing the sender and the administrator
		if (this.sender != null) sender.stopSender();
		if (this.administrator!=null) administrator.stopSession();

		//Closing the buffer
		buffer.removeAllElements();
	
	}
	
	
	
	// ---------------------------
    // Public Interface
	// ---------------------------
	/**
	* Add a DataSocket object to the buffer
	* @param paquete DataSocket A DataSocket object
	*/
	public void addBuffer(DataSocket paquete){
		this.buffer.add(paquete);
	}
	
	
	/**
	* Get the interface List
	* @return ListStudentsTree The tree interface
	*/
	public ListStudentsTree getMyList() {
		return myList;
	}
	
	
	/**
	* Get the DataSocket object of the children
	* @return DataSocket 
	*/
	public DataSocket getObjectChildren(int index){
		return this.childrens.elementAt(index).extractObject();
	}
	
	
	/**
	* Get the size of the students connected (childrens)
	* @return int Size of children Vector
	*/
	public int getSizeChildren(){
		return this.childrens.size();
	}
	
	
	/**
	* Get if there is chalk
	* @return boolean 
	*/
	public boolean getIsThereChalk(){
		return isThereChalk;
	}
	
	
	/**
	* Get the ObjectOutputStream of the chalk student (out socket chanel)
	* @return ObjectOutputStream 
	*/
	public ObjectOutputStream getOutChalk(){
		return this.myConnections.elementAt(numberChalk).connectionOutput();
	}
	
	
	/**
	* Get the ObjectOutputStream of the chalk student (out socket chanel)
	* @return ObjectOutputStream 
	*/
	public ObjectOutputStream getOutputChildren(int i){
		return this.myConnections.elementAt(i).connectionOutput();
	}
	
	
	/**
	* Get the value of the boolean chalk variable of a ConnectionEJS
	* @return boolean 
	*/
	public boolean getChildrenChalk(int index){
		return this.myConnections.elementAt(index).getChalk();
	}
	
	
	/**
	* Get the number of the chalk
	* @return int Number of the chalk 
	*/
	public int getNumberChalk(){
		return this.numberChalk;
	}
	// ---------------------------
    // End Public Interface
	// ---------------------------
	
	
	/**
	* Method run. Overload of Thread run method. Get the signals of master
	*/
	public void run(){
		System.out.println((ConfTeacherTool.spanish)?"Comienzo de la escucha":"Start of the client's listening");
		String temp;
		while(this.activeServerUDP)
		{
			try {
				//Receive a data
				this.socketUDP.receive(stepReceiveChalk);
		
				//Get the data
				temp = new String(stepReceiveChalk.getData(),0,stepReceiveChalk.getData().length);

				//Management of step
				if(temp.equals(this.STEP))
				{
					//System.out.println("Recibo del chalk "+temp);
					myList.sim.step();
				}
				else if(temp.equals(this.END_CHALK))
				{
					this.activeServerUDP = false;
				}
				
				Thread.yield();
			}catch (IOException e){}
		}
		//System.out.println("Salgo del serverUDP");
		try {
			this.socketUDP.setSoTimeout(0);
		} catch (SocketException e) {e.printStackTrace();}
	}
}
	
	



	
/*****************************SESSION HANDLER***************************/
/**
 * The task of this class is to keep clean the session of died threads
 * It cleans threads which are not using at the moment
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es)
 * sleep(250)
 */

class sessionHandler extends Thread{

	private SessionEJS parent; //Parent session. Virtual class management
		
	private Vector<ReadInputThread> readers; //Readers Vector
	private Vector<ConnectionEJS> connections; //ConnectionEJS Vector
	private boolean activeAdmin = true; //Boolean variable to stop the thread
	
		
	/**
	* Constructor. Updating of internal variables
	* @param superior SessionEJS Virtual class session
	* @param readers Vector Readers Vector of the session
	* @param clientes Vector Student connections (ConnectionEJS objects)
	*/
	public sessionHandler(ThreadGroup group, SessionEJS superior, Vector<ReadInputThread> readers, Vector<ConnectionEJS> clients){
    super(group,"Session handler EJS");
		System.out.println((ConfTeacherTool.spanish)?"Creando una nueva sesión virtual...":"Creating a new session Administrator...");
		this.parent = superior;
		this.readers = readers;
		this.connections = clients;
	}
		
  /**
  * Constructor. Updating of internal variables
  * @param superior SessionEJS Virtual class session
  * @param readers Vector Readers Vector of the session
  * @param clientes Vector Student connections (ConnectionEJS objects)
  */
  public sessionHandler(SessionEJS superior, Vector<ReadInputThread> readers, Vector<ConnectionEJS> clients){
    System.out.println((ConfTeacherTool.spanish)?"Creando una nueva sesión virtual...":"Creating a new session Administrator...");
    this.parent = superior;
    this.readers = readers;
    this.connections = clients;
  }
	
	/**
	* Method run. Overload of Thread run method. Clean the session
	*/
	public void run(){
		while (this.activeAdmin){
			for( int i = readers.size()-1; i>=0; i--){
				if(!(this.readers.elementAt(i).isActive()) && !(this.connections.elementAt(i).getChalk())){
					this.parent.getMyList().setText((ConfTeacherTool.spanish)?"Cliente desconectado":"Client disconnected "+this.connections.elementAt(i).user());
					this.readers.elementAt(i).stopReader();
					this.readers.remove(i);
					this.parent.getMyList().removeNode(this.connections.remove(i).user());
					parent.counter--;
				}
			}
			try {
				sleep(250);	//Sleep the thread
			} catch (InterruptedException e) {
				System.err.println("Sleep interrumped "+e.getMessage());
			}
		}
		this.connections.removeAllElements();
		this.readers.removeAllElements();
		this.parent.getMyList().setText((ConfTeacherTool.spanish)?"Sesión finalizada":"Session finished");
		this.parent.getMyList().sim.setTeacherSim(false);
		return;
	}
	
	/**
	* Stop the session handler
	*/
	public void stopSession(){
		this.activeAdmin=false;
	}
}




	
/********************************SENDER*******************************/
/**
 * Sender. Send the objects in the buffer to all the student connections
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es)
 * Thread.yield();
 */
class Sender extends Thread{
		
	private boolean active = true;//Active the thread
	private Vector<ConnectionEJS> connections;//Connections of the clients
	private Vector<DataSocket> buf;//Buffer of sending objects

	
	
	/**
	* Constructor
	* @param connects Vector Vector of student connections
	* @param buffers Vector Vector of object sent
	*/
	Sender(ThreadGroup group, Vector<ConnectionEJS> connects, Vector<DataSocket> buffers){
    super(group,"Sender EJS");
		System.out.println((ConfTeacherTool.spanish)?"Creando un nuevo servidor de envío...":"Creating a new sender...");
		this.connections = connects;
		this.buf = buffers;
		this.setPriority(MAX_PRIORITY);
		
	}
		
	 /**
	  * Constructor
	  * @param connects Vector Vector of student connections
	  * @param buffers Vector Vector of object sent
	  */
	  Sender(Vector<ConnectionEJS> connects, Vector<DataSocket> buffers){
	    System.out.println((ConfTeacherTool.spanish)?"Creando un nuevo servidor de envío...":"Creating a new sender...");
	    this.connections = connects;
	    this.buf = buffers;
	    this.setPriority(MAX_PRIORITY);
	    
	  }
	    
	/**
	* Method run. Overload of Thread run method. Send the object embedded in the buffer
	*/
	public void run(){
		while(this.active){
			while(buf.size() > 0){
				int clientes = connections.size()-1;
				DataSocket envio = buf.remove(0);
				for(int i = clientes; i >= 0; i--){
					if(!connections.elementAt(i).getChalk()){
						try {
							connections.elementAt(i).connectionOutput().writeObject(envio);
							} catch (IOException e) {
								System.err.println((ConfTeacherTool.spanish)?"Error en la escritura del socket":"Error in the socket writing "+e.getMessage());
							}
						}
					}
			}
			Thread.yield();
		}
		System.out.println((ConfTeacherTool.spanish)?"Servidor de envío parado":"Sender stoped");
		return;
	}
		
	
	/**
	* Stop the sender
	*/
	public void stopSender(){
		System.out.println((ConfTeacherTool.spanish)?"Parando el envío...":"Stoping sender...");
		this.active=false;
	}
}








/***************************************READ_INPUT_THREAD***********************************/
/**
 * It creates a new thread for one each connection accepted. 
 * Its function is embedded the objects in the buffer which arrives from the student
 * This class has an important task in the chalk assigment because receives all the messages 
 * from student chalk to execute in the master 
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es)
 * Thread.yield();
 */
class ReadInputThread extends Thread{
		

	private boolean active = true;//Check if the thread is running
	private ConnectionEJS connection;//Student connection (ConnectionEJS object)
	private ObjectInputStream input; //InputStream of student thread
	private Vector<DataSocket> buffer; //Object DataSocket buffer
	private SessionEJS parent; //Parent session
	
	
	
	/**
	* Constructor of a new Read Input Thread. Reader of a connection
	* @param out Vector 
	* @param con ConnectionEJS Stundent connection (Object ConnectionEJS)
	* @param parent SessionEJS Virtual class session
	*/
	public ReadInputThread(ThreadGroup group, Vector<DataSocket> out, ConnectionEJS con,SessionEJS parent){
    super(group,"Read input EJS");
		this.buffer = new Vector<DataSocket>();
		this.input = con.connectionInput();
		this.connection = con;
		this.parent = parent;
	}
  
  /**
  * Constructor of a new Read Input Thread. Reader of a connection
  * @param out Vector 
  * @param con ConnectionEJS Stundent connection (Object ConnectionEJS)
  * @param parent SessionEJS Virtual class session
  */
  public ReadInputThread(Vector<DataSocket> out, ConnectionEJS con,SessionEJS parent){
    this.buffer = new Vector<DataSocket>();
    this.input = con.connectionInput();
    this.connection = con;
    this.parent = parent;
  }
    		
	
	/**
	* Stop the reader
	*/
	public void stopReaderActive(){
		this.active = false;
	}
	
	
	/**
	* Stop and close the reader
	*/
	public void stopReader(){
		this.active = false;
		
		try {this.input.close();
		} catch (IOException e) {System.err.println("Error IOException in ObjectInputStream "+e.getMessage());}
	}
		
	
	/**
	* Check if the thread is running
	* @return boolean True: it is running, False: it is not running
	*/
	public boolean isActive(){
		return this.active;
	}
	
	
	/**
	* Method run. Overload of Thread run method. Read the messages of a student
	*/
	public void run(){
		while(active){
			DataSocket auxiliar;
			try {
				
				//Read the object in the socket. Read until it exists one
				auxiliar = (DataSocket) this.input.readObject();
				
				/***Management of the signal received***/
				if(auxiliar.getSignal().equals("Finish")){
					if(this.connection.getChalk()){
						this.parent.getMyList().sim.connectControls();
						this.parent.getMyList().sim.setIsThereChalk(false);
						this.connection.removeChalk();
						this.parent.getMyList().activeTheTree();
					}
					this.active = false;
				}
				else if(auxiliar.getSignal().equals("GiveChalk_OK")){
					parent.getMyList().sim.disconnectControls();
				}
				else if(auxiliar.getSignal().equals("RemoveChalk_OK")){
					parent.getMyList().sim.connectControls();
				}
				else if(auxiliar.getSignal().equals("play")){
					parent.getMyList().sim.play();
				}
				else if(auxiliar.getSignal().equals("pause")){
					parent.getMyList().sim.pause();
				}
				else if(auxiliar.getSignal().equals("reset")){
					parent.getMyList().sim.reset();
					parent.getMyList().sim.disconnectControls();
				}
				else if (auxiliar.getSignal().equals("variable")){
					parent.getMyList().sim.setVariable(auxiliar.getObject().toString(), auxiliar.value);
					parent.getMyList().sim.update();
					parent.getMyList().sim.apply(auxiliar.getObject().toString());
				}
				else if(auxiliar.getSignal().equals("initialize")){
					parent.getMyList().sim.initialize();
				}
				else if(auxiliar.getSignal().equals("updateAfterModelAction")){
					parent.getMyList().sim.setVariables((String)auxiliar.getObject());
					parent.getMyList().sim.updateAfterModelAction();
				}
				else if(auxiliar.getSignal().equals("experiment")){
					parent.getMyList().sim.runExperiment((String)auxiliar.getObject());
				}

				//Save the object in the buffer
				else this.buffer.add(auxiliar);
	
				Thread.yield();
			}
			//Exception Management
			catch (SocketException e) {
				System.err.println((ConfTeacherTool.spanish)?"ERROR en SocketException ":"ERROR in SocketException "+e.getMessage());
				this.active = false;
			} catch (IOException e) {
				System.err.println((ConfTeacherTool.spanish)?"ERROR en IOException. Conexión cerrada ":"ERROR in IOException. Connection Closed "+e.getMessage());
				this.active = false;
			} catch (ClassNotFoundException e) {
				System.err.println((ConfTeacherTool.spanish)?"ERROR en excepción ClasNotFound":"ERROR ClasNotFound exception "+e.getMessage());
				this.active=false;
			}
		}
		if(this.connection.getChalk()){
			this.parent.getMyList().sim.connectControls();
			this.parent.getMyList().sim.setIsThereChalk(false);
			this.connection.removeChalk();
			this.parent.getMyList().activeTheTree();
			JOptionPane.showMessageDialog(null, (ConfTeacherTool.spanish)?"Estudiante desconectado\n Tiza devuelta":"Student disconnected\n Chalk returned", (ConfTeacherTool.spanish)?"Información":"Information", JOptionPane.INFORMATION_MESSAGE);
		}
		System.out.println((ConfTeacherTool.spanish)?"Saliendo del lector...":"Getting out of the reader...");
		return;
	}
	
	
	/**
	* Get the object in the buffer
	* @return DataSocket
	*/
	public DataSocket extractObject(){
		DataSocket exit;
		if(this.buffer.size() > 0){
			exit = this.buffer.remove(0);
			System.out.println("Ver tamaño "+this.buffer.size());
		}else
			return null;
	
		return exit;
	}
}


	



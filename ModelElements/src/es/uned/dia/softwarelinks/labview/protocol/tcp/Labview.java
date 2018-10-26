/*
 * Clase Jil (Java Internet LabVIEW).
 * API para controlar VIs remotos desde java.
 */
package es.uned.dia.softwarelinks.labview.protocol.tcp;

import java.net.*;
import java.util.List;
import java.util.Vector;
import java.io.*;

import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.awt.event.*;

/**
 * Java library to use with JIL server 
 * @author Héctor Vargas Oyarzún.- (UNED)
 */
public class Labview{

    private Socket jilTCP;
    private DataInputStream bufferInputTCP;
    private DataOutputStream bufferOutputTCP;
    private int SERVICE_PORT;
    private String SERVICE_IP;
    private boolean modeAS = true;
    private boolean asynchronousSimulation = false;
    private String lastErrorMsg;
    private boolean connected = false;
    private String indicadores = "";
    private String controles = "";
    private String SEPARATOR = ",";
    private String viFile;
    private boolean isLoaded = false;
    private boolean isRunning = false;
    
    private CircularByteBuffer cbbs; // buffer de envio
    private CircularByteBuffer cbbr; // buffer de recepcion
    private Conversion conversion = new Conversion();
    private Sender sender = null;
//    private Receiver receiver = null;
    
    private double timeSlot = 0;
    private double leftTime = 0;
    private Timer timer = null;
    private Timer timerKeepAlive = null;    

    static private List metadataIndicators, metadataControls;
    
    // DataType
    enum DataType { 
    	UNDEFINED(0), INT(2), FLOAT(6), DOUBLE(7), BOOLEAN(8), STRING(9);
    	private int id;
    	private String type;
    	private DataType(int id) { 
			this.id = id;
    		switch(id) {
    		case 2: type = "int"; break;
    		case 6:	type = "float"; break;
    		case 7: type = "double"; break;
    		case 8:	type = "boolean"; break;
    		case 9:	type = "String"; break;
    		}
    	}
    	
    	public static DataType getDataType(int id) {
    		DataType result;
    		switch(id) {
    			case 2: result = INT; break;
    			case 7: result = DOUBLE; break;
    			case 8:	result = BOOLEAN; break;
    			case 9:	result = STRING; break;
    			default: result = UNDEFINED; break;
    		}
    		return result; 
   		}
    	
    	public static DataType getDataType(String id) {
    		DataType result = UNDEFINED;
    		if(id.compareTo("int")==0) result = INT;
    		else if(id.compareTo("double")==0) result = DOUBLE;
    		else if(id.compareTo("String")==0) result = STRING;

    		return result; 
   		}

    	public int getId() { return id; }
    	
    	public String getType() { return type; }
    };

    /**
     * This class implements all the java methods to control LabVIEW applications remotely 
     * @param serverAddress A string describing the URL of the JiL Server. Example: "labview:localhost:2055"
     */
    public Labview(String serverAddress){

      connected=false;
      String sPort = serverAddress.substring(serverAddress.lastIndexOf(':') + 1,serverAddress.lastIndexOf('>'));
      String sAddress = serverAddress.substring(9, serverAddress.lastIndexOf(':'));

      SERVICE_IP = sAddress;
      try {
        SERVICE_PORT = Integer.parseInt(sPort);
      }catch (NumberFormatException nfe) {
        System.out.println("Error in Port number:" + nfe);
      }
      
      viFile = serverAddress.trim();
      viFile = viFile.substring(viFile.lastIndexOf('>')+1);

      if (viFile.length() == 0) return;
      
    }
    
    /**
     * Connects to JIL Server.  
     */
    public synchronized void connect() {
      if (!connected){
        try {
          jilTCP = new java.net.Socket();
          jilTCP.connect(new InetSocketAddress(SERVICE_IP, SERVICE_PORT), 4000);
          bufferInputTCP = new DataInputStream(new BufferedInputStream(jilTCP.getInputStream()));
          bufferOutputTCP = new DataOutputStream(new BufferedOutputStream(jilTCP.getOutputStream()));
          jilTCP.setSoTimeout(4000);
          jilTCP.setTcpNoDelay(true);
          cbbs = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
          cbbr = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
          sender = new Sender(cbbs, bufferOutputTCP);
          sender.setPriority(Thread.MIN_PRIORITY);
          sender.start();
          connected = true;
          System.out.println("connect() method message: Successful connection.");
        }catch (IOException ioe) {
          System.err.println("connect() method message: IOException = " + ioe.getMessage());
        }catch (Exception e) {
          System.err.println("connect() method message: Exception = " + e.getMessage());
        } 
      }
    }
    
    /**
     * Checks whether user has a valid booking. 
     * @param user A string indicating the username.
     * @param password A string indicating the password.
     * @return Returns the result of the authentication attempt. 0: username or password does not exist. 1: username and password exist but he/she attempts to connect out of timeslot. 2: username, password and timeslot are ok.
     */
    public synchronized int authenticate(String user, String password){
        int result = 0;
        if (connected){
            if (!isLoaded){
                try{
                    bufferOutputTCP.writeInt(13); // write cmd to authenticate method in LabVIEW
                    bufferOutputTCP.writeInt(user.length()); // write size of the user
                    bufferOutputTCP.write(user.getBytes()); // write the user
                    bufferOutputTCP.writeInt(password.length()); // write size of the password
                    bufferOutputTCP.write(password.getBytes()); // write the password
                    bufferOutputTCP.flush();
                    result = bufferInputTCP.readInt();
                    if (result == 2){
                        timeSlot = (double)bufferInputTCP.readFloat();
                        leftTime = (double)bufferInputTCP.readFloat();
                        
                        timer = new Timer (1000, new ActionListener (){// cada 1 seg se dispara este timer
                            public void actionPerformed(ActionEvent e){
                                // Aquí el código que queramos ejecutar.
                                if (leftTime <= 0){timer.stop();}
                                leftTime = leftTime - 1;// resta un segundo al tiempo que queda
                             }
                        });
                        timer.start();

                        timerKeepAlive = new Timer (5000, new ActionListener (){// cada 5 seg envía clientKeepAlive cmd
                            public void actionPerformed(ActionEvent e){
                                // Aquí el código que queramos ejecutar.
                                if (cbbs.getAvailable()==0){
                                    try{
                                        bufferOutputTCP.writeInt(14);
                                        bufferOutputTCP.flush();
                                        //System.out.println("heartbeat sent...");
                                    }catch(IOException ioe){
                                        disconnect();
                                        System.out.println("error timerKeepAlive :"+ioe.getMessage());
                                    }
                                }
                             }
                        });
                        timerKeepAlive.start();
                        
                    }
                }catch(IOException ioe){
                    System.out.println("authenticate() method message: "+ioe.getMessage());
                }                
            }else{
                System.out.println("authenticate() method message: The remote VI is already loaded.");
            }
        }else{
            System.out.println("authenticate() method message: You are not connected yet.");
        }
        return result;
    }
    
    /**
     * Gets time slot reserved. 
     * @return A double value in seconds.
     */
    public synchronized double getTimeSlot(){       
        return timeSlot;
    }
    
    /**
     * Gets the left time from timeslot.
     * @return A double value in seconds.
     */
    public synchronized double getLeftTime(){      
        return leftTime;
    }
    
    /**
     * Disconnects from JIL Server.
     */            
    public synchronized void disconnect(){
      if (connected){        
          synchronized(cbbs){
              try {
                  cbbs.getOutputStream().write(conversion.toByta((int)3));
                  bufferInputTCP.close();
                  bufferOutputTCP.close();
                  jilTCP.close();
                  bufferInputTCP = null;
                  bufferOutputTCP = null;
                  jilTCP = null;          
                  connected = false;
                  isLoaded = false;
                  isRunning = false;
                  //receiver.stopReceiver();
                  //sender.stopSender();
                  timer.stop();
                  timerKeepAlive.stop();
                  System.out.println("disconnect() method message: Successful disconnection.");
              }catch (Exception e) {
                  System.out.println("disconnect() method message: Exception = " + e.getMessage());
              }
          }
      }
    }

    /**
     * Opens the target VI specified by viPath string and it reports the variables to exchange by initCmd.
     * @param viPath Corresponds to the target VI relative location referenced from apps folder of JIL server. Example of viPath: "sum/sum.vi"
     * @param initCmd Corresponds to the list of variables (controls and indicators) that you want to exchange with the target VI.
     * After name of the variable you must add "(con)" if the variable is a control or "(ind)" if it is a indicator.
     * Example of initCmd: "a(con),b(con),stop(con),c(ind)". If you are using hardware in the loop, always you must inform the variable
     * stop(con), that is, a control labelled stop in the target VI to finish its execution of a safe way.
     * 
     * @return If TRUE, target VI has been loaded in memory. If FALSE, target VI hasn't loaded (Check the arguments).
     */
    public synchronized boolean openVI(String viPath, String initCmd){      
      if (connected){          
        if (!isLoaded){
          try {
            cleanInputBuffer(true);
            bufferOutputTCP.writeInt(4); // write cmd to openVI method in LabVIEW
            bufferOutputTCP.writeInt(viPath.length()); // write the size of the relative path to the VI
            bufferOutputTCP.write(viPath.getBytes()); // write the relative path
            bufferOutputTCP.flush(); // send data
            boolean pathOK = bufferInputTCP.readBoolean(); // read result from server side
            if (pathOK){              
              System.out.println("openVI() method message: The path of the remote VI is OK.");
              isLoaded = bufferInputTCP.readBoolean();
              if (isLoaded){                
                if (exchangeFormat(initCmd)){
                  System.out.println("openVI() method message: The remote VI has been loaded in memory.");
                  System.out.println("openVI() method message: Exchange variables have been reported.");
                }else{
                  closeVI();
                }                
              }else{
                System.out.println("openVI() method message: The remote VI has errors and cannot opened. Please check errors.");  
              }
            }else{
              System.out.println("openVI() method message: The path of the remote VI is wrong.");              
            }          
          }
          catch (Exception e) {
            System.out.println("openVI() method message: Exception = " + e.getMessage());
          }
        }else{
          System.out.println("openVI() method message: The remote VI is already loaded.");  
        }
      }else{
          System.out.println("openVI() method message: You are not connected yet.");
      }      
      return isLoaded;
    }
    
    /**
     * Closes a refnum associated to the open target VI.
     */
    public synchronized void closeVI(){
      if (connected){
        if (isLoaded){
          if (!isRunning){
            try {                
              cleanInputBuffer(true);
              bufferOutputTCP.writeInt(12);
              bufferOutputTCP.flush();              
              if (bufferInputTCP.readBoolean()){
                isLoaded = false;                
                System.out.println("closeVI() method message: The remote VI has been closed"); 
              }else{
                System.out.println("closeVI() method message: Error closing the remote VI"); 
              }          
            }catch (Exception e) {
              System.out.println("closeVI() method message: Exception = " + e.getMessage());
            }                               
          }else{
            System.out.println("closeVI() method message: The remote VI is running. You need to stop the remote VI before of closing it."); 
          }              
        }else{
          System.out.println("closeVI() method message: The remote VI is not loaded.");
        }
      }else{
        System.out.println("closeVI() method message: You are not connected yet.");
      }    
    }
    
    /**
     * Runs the target VI (this method is similar to the Run button on the toolbar of LabVIEW).
     * @param waituntildone Specifies whether to wait until the VI completes execution before the Invoke Node continues executing.   
     */
    public synchronized void runVI(boolean waituntildone){
      if (connected){
        if (isLoaded){
          if (!isRunning){
            try {
              cleanInputBuffer(true);  
              bufferOutputTCP.writeInt(6);
              bufferOutputTCP.writeBoolean(waituntildone); 
              bufferOutputTCP.flush();
              isRunning = bufferInputTCP.readBoolean();
              if (isRunning) {
                if (!waituntildone){
                  if (modeAS){
                    asynchronousSimulation = true;
                    bufferOutputTCP.writeInt(8);
                    bufferOutputTCP.flush();
                    System.out.println("runVI() method message: Running remote VI asynchronously.");
                  }else{
                    asynchronousSimulation = false;
                    bufferOutputTCP.writeInt(7);
                    bufferOutputTCP.flush();
                    System.out.println("runVI() method message: Running remote VI synchronously.");
                  }
                  //receiver = new Receiver(cbbr, bufferInputTCP);
                  //receiver.start();
                }else{
                  isRunning = false;  
                }
              }else{
                System.out.println("runVI() method message: Errors in the execution of the remote VI.");
              }
            }catch (Exception e) {
              System.out.println("runVI() method message: Exception = " + e.getMessage());
            }              
          }else{
            System.out.println("runVI() method message: The remote VI is already running.");  
          }
        }else{
          System.out.println("runVI() method message: The remote VI is not loaded.");  
        }
      }else{
          System.out.println("runVI() method message: You are not connected yet.");
      }
    }
    
    /**
     * Stops the execution of the target VI  (this method is similar to the Abort Execution button on the toolbar of LabVIEW). 
     */
    public synchronized void stopVI(){
      if (connected){
        if (isLoaded){
            try {
              asynchronousSimulation = false;
              bufferOutputTCP.writeInt(1);
              bufferOutputTCP.flush();
              delay(500);
              cleanInputBuffer(true);
              isRunning = false;
              System.out.println("stopVI() method message: The remote VI has been stopped");           
            }catch (Exception e) {
              System.out.println("stopVI() method message: Exception = " + e.getMessage());
            }
        }else{
          System.out.println("stopVI() method message: The remote VI is not loaded.");
        }
      }else{
        System.out.println("stopVI() method message: You are not connected yet.");
      }        
    }
    
    /**
     * Reports whether is connected with JIL server.
     * @return A boolean indicating whether is connected.
     */
    public synchronized boolean isConnected(){      
      return connected;
    }
    
    /**
     * Reports whether target VI is running.
     * @return A boolean indicating whether target VI is running.
     */
    public synchronized boolean isRunning(){
      return isRunning;
    }
    
    /**
     * Gets the TCP socket that communicates to low-level with JIL Server.
     * @return A java.net.Socket class used as communication channel.
     */
    public Socket getSocket(){
      return jilTCP;
    } 
    
    /**
     * Gets the number of the bytes availables in the input stream buffer.
     * @return A int value indicating bytes availables.
     */
    public int getDataAvailable(){
      int disponible = 0;
      try{
        disponible = bufferInputTCP.available();
      }catch(IOException ioe){
        System.out.println("error getDataAvailable :"+ioe.getMessage());
      }catch(Exception e){
        System.out.println("error getDataAvailable :"+e.getMessage());
      }

      return disponible;  
    }
    
    private int getDataAvailableCB(){
      int disponible = 0;
      try{
        disponible = cbbs.getAvailable();    
      }catch(Exception e){
        System.out.println("error getDataAvailable :"+e.getMessage());  
      }
        
      return disponible;          
    }
    
    private synchronized boolean exchangeFormat(String initCommand){
      boolean result = false;      
      if (connected){
        if (isLoaded){
          if (!isRunning){
            try {
              String externalVars = initCommand;
              indicadores = "";
              controles = "";
              if (externalVars != null) {
                externalVars = externalVars.trim();
                if (externalVars.length() <= 0){
                  return false;
                }
                int pos;
                do {
                  pos = externalVars.indexOf(",");
                  String variable;
                  if (pos < 0) {
                    variable = externalVars;
                  }else {
                    variable = externalVars.substring(0, pos);
                    externalVars = externalVars.substring(pos + SEPARATOR.length());
                  }
                  if (variable.endsWith("(ind)")) {
                    variable = variable.substring(0, variable.length() - 5); // 5 porque es el largo de (ind)
                    indicadores = indicadores.concat(variable + SEPARATOR);
                  }else if (variable.endsWith("(con)")) {
                    variable = variable.substring(0, variable.length() - 5); // 5 porque es el largo de (con)
                    controles = controles.concat(variable + SEPARATOR);
                  }
                }while (pos >= 0);
                if (indicadores.endsWith(SEPARATOR)) {
                  indicadores = indicadores.substring(0,indicadores.length() - SEPARATOR.length());
                }
                if (controles.endsWith(SEPARATOR)) {
                  controles = controles.substring(0,controles.length() - SEPARATOR.length());
                }
                cleanInputBuffer(true);
                bufferOutputTCP.writeInt(10); // write cmd to identApp method in LabVIEW
                bufferOutputTCP.writeInt(indicadores.length()); // write the size of the string indicadores
                bufferOutputTCP.write(indicadores.getBytes()); // write the string indicadores in bytes
                bufferOutputTCP.writeInt(controles.length()); // write the size of the string controles
                bufferOutputTCP.write(controles.getBytes()); // write the string controles in bytes
                bufferOutputTCP.flush(); // send data
                result = bufferInputTCP.readBoolean();
              }
            }catch (Exception e) {
              lastErrorMsg = e.getMessage();
              System.out.println("exchangeFormat() method message: Exception = " + e.getMessage());
            }  
          }else{
            System.out.println("exchangeFormat() method message: The remote VI is running. You need to stop the VI remote before.");  
          }            
        }else{
          System.out.println("exchangeFormat() method message: The remote VI is not loaded.");  
        }
      }else{
          System.out.println("exchangeFormat() method message: You are not connected yet.");
      }
      return result;
    }
    
    private synchronized void cleanInputBuffer(boolean _remove){
      //System.out.println("haltUpdate of EjsRemoteLabview");
      if (connected){
        //remove old data
        if (_remove) {
          try {
            int buffsizein = bufferInputTCP.available();
            while (buffsizein > 0) {
              bufferInputTCP.skip(buffsizein);
              buffsizein = bufferInputTCP.available();              
            }
          }catch (Exception e) {
            System.out.println("cleanInputBuffer() method message: Exception = " + e.getMessage());
          }
        }
      }else{
        System.out.println("cleanInputBuffer() method message: You are not connected yet.");
      }
    }
    
    private void delay(int mseconds) {
        try {       
            Thread.sleep(mseconds);
        }
        catch (InterruptedException e) {       
            System.out.println("Delay interrupted!");
        }
    }

  /*
   ************************************
   * INICIO METODOS SETTERS Y GETTERS *
   ************************************
  */
    
  public void setValue(String _name, boolean _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
          try {
              if (!asynchronousSimulation){
                bufferOutputTCP.writeInt(30); // cmd to set a boolean in Run Mode
                bufferOutputTCP.writeInt(_name.length()); // write size of the name
                bufferOutputTCP.write(_name.getBytes()); // write the name
                bufferOutputTCP.writeBoolean(_value); // write the data
                bufferOutputTCP.flush();
              }else{
                //synchronized(cbbs){
                  byte[] left = concat(conversion.toByta((int)30), conversion.toByta(_name.length()));
                  byte[] right = concat(_name.getBytes(), conversion.toByta(_value));            
                  cbbs.getOutputStream().write(concat(left, right));// Escribimos en el buffer circular
                  //cbbs.notify(); 
                //}
              }           
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();        
            System.out.println("setValue() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("setValue() method message: Exception = " + e.getMessage());
            disconnect();
          }                   
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");}
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public void setValue(String _name, int _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
          try {
            if (!asynchronousSimulation){
                bufferOutputTCP.writeInt(31); // cmd to set a boolean in Run Mode
                bufferOutputTCP.writeInt(_name.length()); // write size of the name
                bufferOutputTCP.write(_name.getBytes()); // write the name
                bufferOutputTCP.writeInt(_value); // write the data
                bufferOutputTCP.flush();
                
            }else{
              //synchronized(cbbs){                 
                byte[] left = concat(conversion.toByta((int)31), conversion.toByta(_name.length()));
                byte[] right = concat(_name.getBytes(), conversion.toByta(_value));            
                cbbs.getOutputStream().write(concat(left, right));// Escribimos en el buffer circular
                //cbbs.notify();                   
              //}
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();        
            System.out.println("setValue() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("setValue() method message: Exception = " + e.getMessage());
            disconnect();
          }                    
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public void setValue(String _name, float _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){          
          try {
            if (!asynchronousSimulation){
                bufferOutputTCP.writeInt(34); // cmd to set a boolean in Run Mode
                bufferOutputTCP.writeInt(_name.length()); // write size of the name
                bufferOutputTCP.write(_name.getBytes()); // write the name
                bufferOutputTCP.writeFloat(_value); // write the data
                bufferOutputTCP.flush();                
            }else{
                //synchronized(cbbs){
                    byte[] left = concat(conversion.toByta((int)34), conversion.toByta(_name.length()));
                    byte[] right = concat(_name.getBytes(), conversion.toByta(_value));            
                    cbbs.getOutputStream().write(concat(left, right));// Escribimos en el buffer circular
                    //cbbs.notify();
                //}
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();        
            System.out.println("setValue() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("setValue() method message: Exception = " + e.getMessage());
            disconnect();
          }      
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, double _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(32); // write cmd to setDouble method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeDouble(_value); // write the double
                  bufferOutputTCP.flush(); // send data                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)32), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value));            
                        cbbs.getOutputStream().write(concat(left, right));// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();        
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                        
    
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, String _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                    bufferOutputTCP.writeInt(33); // write cmd to setString method in LabVIEW
                    bufferOutputTCP.writeInt(_name.length()); // write size of the _name
                    bufferOutputTCP.write(_name.getBytes()); // write the _name
                    bufferOutputTCP.writeInt(_value.length()); // write size of the _value
                    bufferOutputTCP.write(_value.getBytes()); // write the _value
                    bufferOutputTCP.flush();                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)33), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length()));
                        byte[] join = concat(left, right);       
                        cbbs.getOutputStream().write(concat(join, _value.getBytes()));// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }              

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, boolean[] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(40); // write cmd to setDoubleArray method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeInt(_value.length); // write the length of the array
                  for (int i = 0; i < _value.length; i++) {
                    bufferOutputTCP.writeBoolean(_value[i]); // write every element of the array in the buffer
                  }
                  bufferOutputTCP.flush(); // send data                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)40), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        for (int i = 0; i < _value.length; i++) {
                          join = concat(join, conversion.toByta(_value[i]));
                        }
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }              

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, int[] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(41); // write cmd to setIntArray method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeInt(_value.length); // write the length of the array
                  for (int i = 0; i < _value.length; i++) {
                    bufferOutputTCP.writeInt(_value[i]); // write every element of the array in the buffer
                  }
                  bufferOutputTCP.flush(); // send data                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)41), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        for (int i = 0; i < _value.length; i++) {
                          join = concat(join, conversion.toByta(_value[i]));
                        }                  
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }              

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, float[] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(44); // write cmd to setDoubleArray method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeInt(_value.length); // write the length of the array
                  for (int i = 0; i < _value.length; i++) {
                    bufferOutputTCP.writeFloat(_value[i]); // write every element of the array in the buffer
                  }
                  bufferOutputTCP.flush(); // send data                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)44), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        for (int i = 0; i < _value.length; i++) {
                          join = concat(join, conversion.toByta(_value[i]));
                        }                  
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }              

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, double[] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(42); // write cmd to setDoubleArray method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeInt(_value.length); // write the length of the array
                  for (int i = 0; i < _value.length; i++) {
                    bufferOutputTCP.writeDouble(_value[i]); // write every element of the array in the buffer
                  }
                  bufferOutputTCP.flush(); // send data                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)42), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        for (int i = 0; i < _value.length; i++) {
                          join = concat(join, conversion.toByta(_value[i]));
                        }                  
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }             
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, String[] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(43); // write cmd to setDoubleArray method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name
                  bufferOutputTCP.writeInt(_value.length); // write the length of the array
                  for (int i = 0; i < _value.length; i++) {
                    bufferOutputTCP.writeInt(_value[i].length()); // write every element of the array in the buffer
                    bufferOutputTCP.write(_value[i].getBytes()); // write every element of the array in the buffer
                  }
                  bufferOutputTCP.flush(); // send data                   
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)43), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        for (int i = 0; i < _value.length; i++) {
                          join = concat(join, conversion.toByta(_value[i].length()));
                          join = concat(join, _value[i].getBytes());
                        }                  
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}                                  
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }              

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, boolean[][] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(50); // write cmd to setDoubleArray2D method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name

                  bufferOutputTCP.writeInt(_value.length); // write rows number in the matrix
                  bufferOutputTCP.writeInt(_value[0].length); // write columns number in the matrix
                  for (int i = 0; i < _value.length; i++) {
                    for (int j = 0; j < _value[0].length; j++) {
                      bufferOutputTCP.writeBoolean(_value[i][j]);
                    }
                  }
                  bufferOutputTCP.flush();                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)50), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        join = concat(join, conversion.toByta(_value[0].length));                
                        for (int i = 0; i < _value.length; i++) {
                            for (int j = 0; j < _value[0].length; j++) {
                                join = concat(join, conversion.toByta(_value[i][j]));
                            }
                        }    
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                            

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, int[][] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(51); // write cmd to setDoubleArray2D method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name

                  bufferOutputTCP.writeInt(_value.length); // write rows number in the matrix
                  bufferOutputTCP.writeInt(_value[0].length); // write columns number in the matrix
                  for (int i = 0; i < _value.length; i++) {
                    for (int j = 0; j < _value[0].length; j++) {
                      bufferOutputTCP.writeInt(_value[i][j]);
                    }
                  }
                  bufferOutputTCP.flush();           
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)51), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        join = concat(join, conversion.toByta(_value[0].length));                
                        for (int i = 0; i < _value.length; i++) {
                            for (int j = 0; j < _value[0].length; j++) {
                                join = concat(join, conversion.toByta(_value[i][j]));
                            }
                        }    
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                            
        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, float[][] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){  
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(54); // write cmd to setDoubleArray2D method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name

                  bufferOutputTCP.writeInt(_value.length); // write rows number in the matrix
                  bufferOutputTCP.writeInt(_value[0].length); // write columns number in the matrix
                  for (int i = 0; i < _value.length; i++) {
                    for (int j = 0; j < _value[0].length; j++) {
                      bufferOutputTCP.writeFloat(_value[i][j]);
                    }
                  }
                  bufferOutputTCP.flush();                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)54), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        join = concat(join, conversion.toByta(_value[0].length));                
                        for (int i = 0; i < _value.length; i++) {
                            for (int j = 0; j < _value[0].length; j++) {
                                join = concat(join, conversion.toByta(_value[i][j]));
                            }
                        }    
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                            

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, double[][] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(52); // write cmd to setDoubleArray2D method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name

                  bufferOutputTCP.writeInt(_value.length); // write rows number in the matrix
                  bufferOutputTCP.writeInt(_value[0].length); // write columns number in the matrix
                  for (int i = 0; i < _value.length; i++) {
                    for (int j = 0; j < _value[0].length; j++) {
                      bufferOutputTCP.writeDouble(_value[i][j]);
                    }
                  }
                  bufferOutputTCP.flush();                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)52), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        join = concat(join, conversion.toByta(_value[0].length));                
                        for (int i = 0; i < _value.length; i++) {
                            for (int j = 0; j < _value[0].length; j++) {
                                join = concat(join, conversion.toByta(_value[i][j]));
                            }
                        }    
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //} 
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                            

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  public synchronized void setValue(String _name, String[][] _value){
    if (connected){
      if (isLoaded){
        if (controles.length()>0){
              try {
                if (!asynchronousSimulation){
                  bufferOutputTCP.writeInt(53); // write cmd to setDoubleArray2D method in LabVIEW
                  bufferOutputTCP.writeInt(_name.length()); // write size of the name
                  bufferOutputTCP.write(_name.getBytes()); // write the name

                  bufferOutputTCP.writeInt(_value.length); // write rows number in the matrix
                  bufferOutputTCP.writeInt(_value[0].length); // write columns number in the matrix
                  for (int i = 0; i < _value.length; i++) {
                    for (int j = 0; j < _value[0].length; j++) {
                      bufferOutputTCP.writeInt(_value[i][j].length());
                      bufferOutputTCP.write(_value[i][j].getBytes());
                    }
                  }
                  bufferOutputTCP.flush();                    
                }else{
                    //synchronized(cbbs){
                        byte[] left = concat(conversion.toByta((int)53), conversion.toByta(_name.length()));
                        byte[] right = concat(_name.getBytes(), conversion.toByta(_value.length));
                        byte[] join = concat(left, right);
                        join = concat(join, conversion.toByta(_value[0].length));                
                        for (int i = 0; i < _value.length; i++) {
                            for (int j = 0; j < _value[0].length; j++) {
                                join = concat(join, conversion.toByta(_value[i][j].length()));
                                join = concat(join, _value[i][j].getBytes());
                            }
                        }              
                        cbbs.getOutputStream().write(join);// Escribimos en el buffer circular
                        //cbbs.notify();
                    //}
                }
              }catch (IOException io) {
                lastErrorMsg = io.getMessage();
                System.out.println("setValue() method message: IOException = " + io.getMessage());
                disconnect();
              }catch (Exception e) {
                lastErrorMsg = e.getMessage();
                System.out.println("setValue() method message: Exception = " + e.getMessage());
                disconnect();
              }                            

        }else{System.out.println("setValue() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("setValue() method message: The remote VI is not loaded.");}
    }else{System.out.println("setValue() method message: You are not connected yet.");}
  }

  /**
   * Change the address of the server
   * 
   * @param address The address of the server
   * @return true if the address is updated, false otherwise
   */
  public boolean setServerAddress(String serverAddress) {
	  if(connected) return false;

	  boolean changed = true;
      String sPort = serverAddress.substring(serverAddress.lastIndexOf(':') + 1,serverAddress.lastIndexOf('>'));
      String sAddress = serverAddress.substring(9, serverAddress.lastIndexOf(':'));

      SERVICE_IP = sAddress;
      try {
        SERVICE_PORT = Integer.parseInt(sPort);
      }catch (NumberFormatException nfe) {
        System.out.println("Error in Port number:" + nfe);
        changed = false;
      }
      
      viFile = serverAddress.trim();
      viFile = viFile.substring(viFile.lastIndexOf('>')+1);

      if (viFile.length() == 0) changed = false;
      
      return changed;
  }
  
 /************************************************************************
 *************************************************************************
 *************************************************************************/

  public synchronized boolean getBoolean(String _variable){
    boolean valueBoolean = false;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            //System.out.println("variable: " + _variable + " has been received");
            if (asynchronousSimulation){                
                return getBooleanAS();                
            }
            
            if (modeAS) {
              cleanInputBuffer(true);
            }
            bufferOutputTCP.writeInt(60);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            valueBoolean = bufferInputTCP.readBoolean();
            System.out.println("Pasando");
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBoolean() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("getBoolean() method message: Exception = " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBoolean() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBoolean() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBoolean() method message: You are not connected yet.");}
    
    return valueBoolean;
  }
  private synchronized boolean getBooleanAS(){
    boolean valueBoolean = false;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            valueBoolean = bufferInputTCP.readBoolean();
            /*
            byte[] data = new byte[1];
            if (cbbr.getAvailable()>0){
            cbbr.getInputStream().read(data);
            }
            valueBoolean = conversion.toBoolean(data);
            */
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBooleanAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host getBooleanAS: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBooleanAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBooleanAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBooleanAS() method message: You are not connected yet.");}
    
    return valueBoolean;
  }

  public synchronized int getInt(String _variable){
    int valueInt = 0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation){
                return getIntAS();
            }

            if (modeAS) {
              cleanInputBuffer(true);
            }

            bufferOutputTCP.writeInt(61);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            valueInt = bufferInputTCP.readInt();
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getInt() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getInt() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getInt() method message: The remote VI is not loaded.");}
    }else{System.out.println("getInt() method message: You are not connected yet.");}
    
    return valueInt;
  }
  private synchronized int getIntAS(){
    //System.out.println("debería haber recibido un entero");
    int valueInt = 0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            valueInt = bufferInputTCP.readInt();
/*
            byte[] data = new byte[4];
            cbbr.getInputStream().read(data);
            valueInt = conversion.toInt(data);
*/              
              
              
              
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getIntAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host getIntAS: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getIntAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getIntAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getIntAS() method message: You are not connected yet.");}
    
    return valueInt;
  }

  public synchronized float getFloat(String _variable){
    float valueFloat = 0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation){
                return getFloatAS();
            }

            if (modeAS) {
              cleanInputBuffer(true);
            }

            bufferOutputTCP.writeInt(64);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            valueFloat = bufferInputTCP.readFloat();
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShort() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShort() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShort() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShort() method message: You are not connected yet.");}
    
    return valueFloat;
  }
  private synchronized float getFloatAS(){
    //System.out.println("debería haber recibido un entero");
    float valueFloat = 0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {            
            valueFloat = bufferInputTCP.readFloat();
/*
            byte[] data = new byte[4];
            cbbr.getInputStream().read(data);
            valueFloat = conversion.toFloat(data);            
*/            
            
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShortAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShortAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShortAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShortAS() method message: You are not connected yet.");}
    
    return valueFloat;
  }

  public synchronized double getDouble(String _variable){
    double valueDouble = 0.0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation){
                return getDoubleAS();
            }

            if (modeAS) {
              cleanInputBuffer(true);
            }

            bufferOutputTCP.writeInt(62);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            valueDouble = bufferInputTCP.readDouble();
            //System.out.println("getDouble() asincronicamente");
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getDouble() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDouble() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDouble() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDouble() method message: You are not connected yet.");}
    
    return valueDouble;
  }
  private synchronized double getDoubleAS(){
    double valueDouble = 0.0;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try{
            valueDouble = bufferInputTCP.readDouble();
/*              
            byte[] data = new byte[8];
            cbbr.getInputStream().read(data);
            valueDouble = conversion.toDouble(data);
*/              
              
            //System.out.println("getDouble() asincronicamente");
          }catch (IOException io){
            lastErrorMsg = io.getMessage();
            System.out.println("getDoubleAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e){
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host getDoubleAS: "+e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDoubleAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDoubleAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDoubleAS() method message: You are not connected yet.");}
    
    return valueDouble;
  }

  public synchronized String getString(String _variable){ // Strongly inspired in JMatLink
    String charArray = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getStringAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }
            bufferOutputTCP.writeInt(63);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            int size = bufferInputTCP.readInt();
            byte[] entrada = new byte[size];
            bufferInputTCP.read(entrada, 0, size);
            charArray = new String(entrada);
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getString() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getString() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getString() method message: The remote VI is not loaded.");}
    }else{System.out.println("getString() method message: You are not connected yet.");}
    
    return (charArray);
  }
  private synchronized String getStringAS(){
    String charArray = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
/*              
            byte[] data = new byte[4];            
            cbbr.getInputStream().read(data);
            byte[] _data = new byte[conversion.toInt(data)];
            cbbr.getInputStream().read(_data);
            charArray = new String(_data);             
*/              
            int size = bufferInputTCP.readInt();
            byte[] entrada = new byte[size];
            bufferInputTCP.readFully(entrada, 0, size);
            charArray = new String(entrada);
            
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getStringAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host getStringAS: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getStringAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getStringAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getStringAS() method message: You are not connected yet.");}
    
    return (charArray);
  }

  public synchronized boolean[] getBooleanArray (String _variable){
    boolean[] vectorBoolean = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getBooleanArrayAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }

            int _dim1;
            bufferOutputTCP.writeInt(70);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            vectorBoolean = new boolean[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorBoolean[i] = bufferInputTCP.readBoolean();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBooleanArray() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBooleanArray() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBooleanArray() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBooleanArray() method message: You are not connected yet.");}
    
    return vectorBoolean;
  }
  private synchronized boolean[] getBooleanArrayAS (){
    boolean[] vectorBoolean = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1;
            _dim1 = bufferInputTCP.readInt();
            vectorBoolean = new boolean[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorBoolean[i] = bufferInputTCP.readBoolean();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBooleanArrayAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBooleanArrayAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBooleanArrayAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBooleanArrayAS() method message: You are not connected yet.");}
    
    return vectorBoolean;
  }

  public synchronized int[] getIntArray (String _variable){
    int[] vectorInt = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getIntArrayAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }

            int _dim1;
            bufferOutputTCP.writeInt(71);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            vectorInt = new int[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorInt[i] = bufferInputTCP.readInt();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getIntArray() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getIntArray() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getIntArray() method message: The remote VI is not loaded.");}
    }else{System.out.println("getIntArray() method message: You are not connected yet.");}
    
    return vectorInt;
  }
  private synchronized int[] getIntArrayAS (){
    int[] vectorInt = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1;
            _dim1 = bufferInputTCP.readInt();
            vectorInt = new int[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorInt[i] = bufferInputTCP.readInt();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getIntArrayAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getIntArrayAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getIntArrayAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getIntArrayAS() method message: You are not connected yet.");}
    
    return vectorInt;
  }

  public synchronized float[] getFloatArray (String _variable){
    float[] vectorFloat = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getFloatArrayAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }

            int _dim1;
            bufferOutputTCP.writeInt(74);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            vectorFloat = new float[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorFloat[i] = bufferInputTCP.readFloat();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShortArray() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShortArray() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShortArray() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShortArray() method message: You are not connected yet.");}
    
    return vectorFloat;
  }
  private synchronized float[] getFloatArrayAS (){
    float[] vectorFloat = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1;
            _dim1 = bufferInputTCP.readInt();
            vectorFloat = new float[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorFloat[i] = bufferInputTCP.readFloat();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShortArrayAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShortArrayAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShortArrayAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShortArrayAS() method message: You are not connected yet.");}
    
    return vectorFloat;
  }

  public synchronized double[] getDoubleArray (String _variable){
    double[] vectorDouble = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getDoubleArrayAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }

            int _dim1;
            bufferOutputTCP.writeInt(72);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            vectorDouble = new double[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorDouble[i] = bufferInputTCP.readDouble();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getDoubleArray() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDoubleArray() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDoubleArray() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDoubleArray() method message: You are not connected yet.");}
    
    return vectorDouble;
  }
  private synchronized double[] getDoubleArrayAS (){
    double[] vectorDouble = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1;
            _dim1 = bufferInputTCP.readInt();
            vectorDouble = new double[_dim1];
            for (int i = 0; i < _dim1; i++) {
              vectorDouble[i] = bufferInputTCP.readDouble();
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getDoubleArrayAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDoubleArrayAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDoubleArrayAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDoubleArrayAS() method message: You are not connected yet.");}
    
    return vectorDouble;
  }

  public synchronized String[] getStringArray (String _variable){
    String[] vectorString = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getStringArrayAS();

            if (modeAS) {
              cleanInputBuffer(true);
            }

            int _dim1;
            bufferOutputTCP.writeInt(73);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            vectorString = new String[_dim1];
            for (int i = 0; i < _dim1; i++) {
              int size = bufferInputTCP.readInt();
              byte[] entrada = new byte[size];
              bufferInputTCP.read(entrada, 0, size);
              String charArray = new String(entrada);
              vectorString[i] = charArray;
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getStringArray() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getStringArray() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getStringArray() method message: The remote VI is not loaded.");}
    }else{System.out.println("getStringArray() method message: You are not connected yet.");}
    
    return vectorString;
  }
  private synchronized String[] getStringArrayAS (){
    String[] vectorString = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1;
            _dim1 = bufferInputTCP.readInt();
            vectorString = new String[_dim1];
            for (int i = 0; i < _dim1; i++) {
              int size = bufferInputTCP.readInt();
              byte[] entrada = new byte[size];
              bufferInputTCP.read(entrada, 0, size);
              String charArray = new String(entrada);
              vectorString[i] = charArray;
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getStringArrayAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getStringArrayAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getStringArrayAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getStringArrayAS() method message: You are not connected yet.");}
    
    return vectorString;
  }

  public synchronized boolean[][] getBooleanArray2D (String _variable){
    boolean[][] arrayBoolean = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getBooleanArray2DAS();
            if (modeAS) {
              cleanInputBuffer(true);
            }
            int _dim1, _dim2;

            bufferOutputTCP.writeInt(80);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayBoolean = new boolean[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayBoolean[i][j] = bufferInputTCP.readBoolean();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBooleanArray2D() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBooleanArray2D() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBooleanArray2D() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBooleanArray2D() method message: You are not connected yet.");}
    
    return arrayBoolean;
  }
  private synchronized boolean[][] getBooleanArray2DAS (){
    boolean[][] arrayBoolean = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1, _dim2;

            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayBoolean = new boolean[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayBoolean[i][j] = bufferInputTCP.readBoolean();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getBooleanArray2DAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getBooleanArray2DAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getBooleanArray2DAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getBooleanArray2DAS() method message: You are not connected yet.");}
    
    return arrayBoolean;
  }

  public synchronized int[][] getIntArray2D (String _variable){
    int[][] arrayInt = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getIntArray2DAS();
            if (modeAS) {
              cleanInputBuffer(true);
            }
            int _dim1, _dim2;

            bufferOutputTCP.writeInt(81);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayInt = new int[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayInt[i][j] = bufferInputTCP.readInt();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getIntArray2D() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getIntArray2D() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getIntArray2D() method message: The remote VI is not loaded.");}
    }else{System.out.println("getIntArray2D() method message: You are not connected yet.");}
    
    return arrayInt;
  }
  private synchronized int[][] getIntArray2DAS (){
    int[][] arrayInt = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1, _dim2;

            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayInt = new int[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayInt[i][j] = bufferInputTCP.readInt();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getIntArray2DAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getIntArray2DAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getIntArray2DAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getIntArray2DAS() method message: You are not connected yet.");}
    
    return arrayInt;
  }

  public synchronized float[][] getFloatArray2D (String _variable){
    float[][] arrayFloat = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getFloatArray2DAS();
            if (modeAS) {
              cleanInputBuffer(true);
            }
            int _dim1, _dim2;

            bufferOutputTCP.writeInt(84);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayFloat = new float[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayFloat[i][j] = bufferInputTCP.readFloat();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShortArray2D() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShortArray2D() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShortArray2D() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShortArray2D() method message: You are not connected yet.");}
    
    return arrayFloat;
  }
  private synchronized float[][] getFloatArray2DAS (){
    float[][] arrayFloat = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1, _dim2;

            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayFloat = new float[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayFloat[i][j] = bufferInputTCP.readFloat();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getShortArray2DAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getShortArray2DAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getShortArray2DAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getShortArray2DAS() method message: You are not connected yet.");}
    
    return arrayFloat;
  }

  public synchronized double[][] getDoubleArray2D (String _variable){
    double[][] arrayDouble = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getDoubleArray2DAS();
            if (modeAS) {
              cleanInputBuffer(true);
            }
            int _dim1, _dim2;

            bufferOutputTCP.writeInt(82);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayDouble = new double[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayDouble[i][j] = bufferInputTCP.readDouble();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getDoubleArray2D() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDoubleArray2D() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDoubleArray2D() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDoubleArray2D() method message: You are not connected yet.");}
    
    return(arrayDouble);
  }
  private synchronized double[][] getDoubleArray2DAS (){
    double[][] arrayDouble = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1, _dim2;

            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayDouble = new double[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                arrayDouble[i][j] = bufferInputTCP.readDouble();
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getDoubleArray2DAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getDoubleArray2DAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getDoubleArray2DAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getDoubleArray2DAS() method message: You are not connected yet.");}
    
    return(arrayDouble);
  }

  public synchronized String[][] getStringArray2D (String _variable){
    String[][] arrayString = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            if (asynchronousSimulation)return getStringArray2DAS();
            if (modeAS) {
              cleanInputBuffer(true);
            }
            int _dim1, _dim2;

            bufferOutputTCP.writeInt(83);
            bufferOutputTCP.writeInt(_variable.length());
            bufferOutputTCP.write(_variable.getBytes());
            bufferOutputTCP.flush();
            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayString = new String[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                int size = bufferInputTCP.readInt();
                byte[] entrada = new byte[size];
                bufferInputTCP.read(entrada, 0, size);
                String charArray = new String(entrada);
                arrayString[i][j] = charArray;
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getStringArray2D() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getStringArray2D() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getStringArray2D() method message: The remote VI is not loaded.");}
    }else{System.out.println("getStringArray2D() method message: You are not connected yet.");}
    
    return arrayString;
  }
  private synchronized String[][] getStringArray2DAS (){
    String[][] arrayString = null;
    
    if (connected){
      if (isLoaded){
        if (indicadores.length()>0){
          try {
            int _dim1, _dim2;

            _dim1 = bufferInputTCP.readInt();
            _dim2 = bufferInputTCP.readInt();
            arrayString = new String[_dim1][_dim2];
            for (int i = 0; i < _dim1; i++) {
              for (int j = 0; j < _dim2; j++) {
                int size = bufferInputTCP.readInt();
                byte[] entrada = new byte[size];
                bufferInputTCP.read(entrada, 0, size);
                String charArray = new String(entrada);
                arrayString[i][j] = charArray;
              }
            }
          }catch (IOException io) {
            lastErrorMsg = io.getMessage();
            System.out.println("getStringArray2DAS() method message: IOException = " + io.getMessage());
            disconnect();
          }catch (Exception e) {
            lastErrorMsg = e.getMessage();
            System.out.println("No connection to host: " + e.getMessage());
            disconnect();
          }            
        }else{System.out.println("getStringArray2DAS() method message: You haven't got used exchangeFormat method or there aren't controls to write.");} 
      }else{System.out.println("getStringArray2DAS() method message: The remote VI is not loaded.");}
    }else{System.out.println("getStringArray2DAS() method message: You are not connected yet.");}
    
    return arrayString;
  }

  public synchronized void getMetadata(){
      metadataIndicators = null;
      metadataControls = null;
      if (connected){
        try{
          bufferOutputTCP.writeInt(5);// write cmd to getMetaData method in LabVIEW
          bufferOutputTCP.flush();
          int result = bufferInputTCP.readInt();
          if (result == 1){
            metadataIndicators = getXML();
            metadataControls = getXML();
          }else{
            System.out.println("Error getting variables of getMetaData of BrowserForRemoteLabview");
          }
        }catch(IOException io){
          System.out.println("IOException in getMetaData"+io.getMessage());
        }
      }     
    }
 
  public synchronized List getXML(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;	

		try {
			db = dbf.newDocumentBuilder();			
		} catch (ParserConfigurationException e1) {
			System.out.println("XML parser configuration error.");
		} 

      List metadata = new Vector();
      try{
        int sizeXML = bufferInputTCP.readInt(); // read the size of the XML file
        byte[] data = new byte[sizeXML];
        bufferInputTCP.readFully(data, 0, sizeXML); // read XML file

        System.out.println("XML received, its size is: " + sizeXML +" bytes");

        // Parse the XML String
		InputSource is = new InputSource(new StringReader(new String(data)));
		Document doc = db.parse(is);
		NodeList lista = doc.getElementsByTagName("Cluster");
		int size = lista.getLength();
		for (int i=0; i<size; i++) {
			List cluster = unflatFromXMLCluster(lista.item(i));			
			if(cluster != null) metadata.add(cluster); 
		}
      }catch(IOException io){
        System.out.println("IOException in getXML"+io.getMessage());
      } catch (SAXException e) {
        System.out.println("SAX: "+e.getMessage());
      }
      return metadata;
    }
  
  private List<String> unflatFromXMLCluster(Node clusterNode) {
	  if(clusterNode.getNodeName() != "Cluster") return null;

	  List<String> result = new Vector<String>();
	  NodeList childs = clusterNode.getChildNodes();
	  int n = childs.getLength();

	  String varname = "", vartype = "";
	  for(int i=0; i<n; i++) {
		  String name = "", val = "", tag = childs.item(i).getNodeName();
		  
		  if(tag == "String" || tag == "U16") {			  
			  NodeList nodes = childs.item(i).getChildNodes();
			  int size = nodes.getLength();
			  for(int j=0; j<size; j++) {
				  String nodeName = nodes.item(j).getNodeName();
				  if(nodeName == "Name") name = nodes.item(j).getTextContent();
				  else if(nodeName == "Val") val = nodes.item(j).getTextContent();
			  }

			  if(name.compareTo("name") == 0) varname = val;
			  else if(name.compareTo("DataType") == 0) vartype = val;
		  }
	  }

	  if(varname.isEmpty() || vartype.isEmpty()) {
		  result = null;
	  } else {
		  DataType type = DataType.getDataType(Integer.parseInt(vartype));
		  result.add(varname);
		  result.add(type.getType());
	  }
	  
   	  return result;
  }
  
  public List getIndicators() {
	  return metadataIndicators;
  }

  public List getControls() {
	  return metadataControls;
  }
  
/*
 *******************************
 * FIN METODOS SETTER Y GETTER *
 *******************************
*/
    private byte[] concat(byte[] A, byte[] B) {
        byte[] C= new byte[A.length+B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);

        return C;
    }
    
/*
 *******************************
 * JIL Wrapper *
 *******************************
 */
    
    // Send and receive all the controls and indicators to and from LabVIEW.
    // Returns true when the data is exchanged succesfully, false otherwise
    public boolean step() {
    	boolean result = false;
    	
    	if (isConnected() && isRunning()) {
    		result = true;
    		setValues();
    	    getValues();
   	    }
    	
    	return result; 
    }

    // Open the VI 
    public boolean openVI() { return false; };
    
    // Obtain the values of the indicators from LabVIEW
    public void getValues() {
    	System.out.println("Warning: the method getValues() is empty");
    }
    
    // Send the values of the controls to LabVIEW
    public void setValues() {
    	System.out.println("Warning: the method setValues() is empty");
    }

}
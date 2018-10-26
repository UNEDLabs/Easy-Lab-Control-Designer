package org.colos.ejs.model_elements.input_output;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opensourcephysics.controls.OSPLog;

/**
 * Encapsulates access to a WebSocket server
 * @author Francisco Esquembre
 * @version 1.0, October 2015
 *
 */
public class WebSocketUtil {
  
  private int mPortNumber;
  private int mMustWait = 30;
  protected WebSocketServer mCommSocket;
  protected java.util.Hashtable<WebSocket, String> mConnectionsTable = new java.util.Hashtable<WebSocket, String>();

  static public int getFreePort() {
    int minport = 8800;
    int maxport = 8900;
    int port = minport;
    while (port<maxport) {
      try {
        ServerSocket commSocket = new ServerSocket();
        commSocket.bind(new InetSocketAddress(port));
        commSocket.close();
        return port;
      } catch (IOException e) { }
      port++;
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) { }
    }
    return -1;
  }
  
  public WebSocketServer getServer() {
    return mCommSocket;
  }
  
  /**
   * Standard constructor to be called by the simulation
   * Connection must be started explicitly by calling start()
   */
  public WebSocketUtil() { }
    /**
   * Standard constructor to be called by the simulation
   * @param _model
   */
  public WebSocketUtil(int _portNumber) {
    start(_portNumber);
  }

  public boolean isConnected() {
    if (mCommSocket==null || mCommSocket.connections().isEmpty()) return false;
    return true;
  }
  
  public int getPortnumber() { return mPortNumber; }
  
  public void setWaitTime(int seconds) { mMustWait = seconds; }

  public void start(int _portNumber) {
    if (mCommSocket!=null) return; // Already running
    if (_portNumber<0) mPortNumber = getFreePort();
    else mPortNumber = _portNumber;
    System.out.println ("Starting server on port "+mPortNumber);
    mConnectionsTable.clear();
    mCommSocket = new WebSocketServer(new InetSocketAddress(mPortNumber)) {
      public void onMessage(WebSocket conn, String message) { 
        OSPLog.finest("Websocket "+ mPortNumber+ ": Input received: "+ message);
//        String id;
//        try { id = conn.getRemoteSocketAddress().getAddress().getHostAddress(); } 
//        catch (Exception exc) {
//          exc.printStackTrace();
//          id = "Unknown client";
//        }
        processInput(conn,message);
      }
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
        OSPLog.fine("Connected to "+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
        System.err.println("Connected to "+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
        System.err.println("Client : "+ conn);
        mConnectionsTable.put(conn,""+conn.hashCode());
        onConnectionOpened(conn);
        sendStatus();
      }
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        OSPLog.fine("Connection closed by "+conn+ ": Reason = "+reason+ ": Remote = "+remote);
        System.err.println("Connection closed by "+conn+ ": Reason = "+reason + ": Remote = "+remote);
        onConnectionClosed(conn);
        mConnectionsTable.remove(conn);
        if (mCommSocket.connections().isEmpty()) { // Exit if no view connects in MAX_WAIT seconds 
          mConnectionsTable.clear();
          new Thread(new Runnable() {
            public void run() { waitForConnection(); }
          }).start();
        }
      }
      public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error connection "+ conn);
        ex.printStackTrace();
        if (conn==null && mCommSocket.connections().isEmpty()) { 
          OSPLog.fine("General communication error!");
        }
        else {
          OSPLog.fine("Communication error with "+conn); //.getRemoteSocketAddress().getAddress().getHostAddress());
        }
//          ex.printStackTrace();
      }
    };
    mCommSocket.start();
    waitForConnection();
  }
  
  public boolean sendStatus() { return true; }

  public void stop() {
    try { 
      mCommSocket.stop(); 
      mCommSocket = null;
    } 
    catch (IOException e) { e.printStackTrace(); }
  }
  
  synchronized public void setID (WebSocket conn, String anID) {
    mConnectionsTable.put(conn, anID);
    System.err.println("ID of "+conn +" is now : "+anID);
  }
  
  synchronized public String[] getConnectionsIDs () {
    int n = mConnectionsTable.size();
    if (n<=0) return new String[0];
    String[] idArray = new String[n];
    java.util.Enumeration<String> elements = mConnectionsTable.elements();
    for (int i=0; i<n; i++) {
      String anId = elements.nextElement(); 
      idArray[i] = anId;
    }
    return idArray;
  }
  

  synchronized public boolean sendMessage (String message) {
    if (!isConnected()) return false;
    for (WebSocket conn : mCommSocket.connections()) conn.send(message);
    return true;    
  }

  public void waitForConnection() {
    try { Thread.sleep(1000); } catch (InterruptedException e1) { e1.printStackTrace(); }
    int counter=0;
    String ipStr = "localhost:"+mCommSocket.getPort();
    try {
      java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
      ipStr = ip.getHostAddress()+":"+mCommSocket.getPort();
    } catch (Exception exc) { exc.printStackTrace(); }

    if (mMustWait<=0)System.err.println("Waiting for connection at " +ipStr+" forever...");
    while (mCommSocket.connections().isEmpty()) {
      try {
        counter++;
        if (mMustWait>0 && counter>mMustWait) {
          System.err.println("No view connected in "+mMustWait+" seconds. Exiting!");
          mCommSocket.stop(); 
          System.exit(1);
        }
        if (mMustWait>0) System.err.println("Waiting for connection at " +ipStr+" for "+counter+"/"+mMustWait+" seconds...");
        Thread.sleep(1000);
      } catch (Exception e) { e.printStackTrace(); }
    }
    System.err.println("Connection established!");
  }
  
  
  // backwards compatibility
  public void processInput (WebSocket socket, String input) { processInput (input); }

  public void processInput (String input) { System.out.println("WebSocketServer : Input received = "+input); }

  public void onConnectionOpened (WebSocket conn) { }
  public void onConnectionClosed (WebSocket conn) { }

}

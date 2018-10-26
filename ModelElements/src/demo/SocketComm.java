package demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.colos.ejs.library.Model;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public abstract class SocketComm extends WebSocketServer {
  private Model mModel;
  private boolean mConnected = false;

	public SocketComm(Model model, int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
		mModel = model;
	}

	abstract public String getOutputData();
  abstract public void processInputData(String input);
	
  public void release(){
    mConnected = false;
    try {
      super.stop();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
	public void connect() {
    super.start();
	  mConnected = true;
	  Thread thread = new Thread() {
	    public void run() {
//	      int counter = 0;
	      while ( mConnected ) {
//	        if (counter==0)  System.out.println( "ModelServer is now listening on "+getAddress()+" port: " + getPort() );
//	        counter = (counter+1) % 500; 

	        String output = getOutputData().trim();
	        if (output.length()>0) {
	          Set<WebSocket> con = connections();
	          synchronized ( con ) {
	            for( WebSocket c : con ) {            
	              System.out.println( c + ": out = " + output);
	              c.send(output);
	            }
	          }
	        }
	        try{ Thread.sleep(10); } catch(Exception e){};
	      }
	    }
	  };
	  System.out.println( "ModelServer started on "+getAddress()+" port: " + getPort() );
	  thread.start();
	  
	}
	
	
	@Override
	public void onMessage(WebSocket conn, String message) {
	  message = message.trim();
		System.out.println( conn + ": input : <" + message+">" );
		if (message.equals("_play")) {
		  System.out.println("Should play now!");
		  mModel._play();
		}
		else if (message.equals("_pause")) mModel._pause();
    else if (message.equals("_reset")) mModel._reset();
    else if (message.equals("_initialize")) mModel._initialize();
    else if (message.equals("_step")) mModel._step();
		else processInputData(message);
	}

  @Override
  public void onOpen( WebSocket conn, ClientHandshake handshake ) {
    System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " client for the model!" );
  }

  @Override
  public void onClose( WebSocket conn, int code, String reason, boolean remote ) {    
    System.out.println( conn + " has left the model!" );
  }

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
	}
	
	
	
}

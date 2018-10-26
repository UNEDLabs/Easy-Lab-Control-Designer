package es.uned.dia.softwarelinks.labview.protocol;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;


/**
 * Class to interact with a JIL Server
 * @author Jesús Chacón <jcsombria@gmail.com> 
 */
public class LabviewSession {	
    private enum State { IDLE, CONNECTED, OPENED, RUNNING };
    private State state = State.IDLE;
    private LowLevelProtocol protocol;
    
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
	public LabviewSession(String url, LowLevelProtocol protocol) throws MalformedURLException {
		this.protocol = protocol;
		protocol.setServerAddress(url);
	}
	
    /**
     * Create a new Labview Object 
     * @param url The url of the server
     */
	public LabviewSession(URL url, LowLevelProtocol protocol) {
		this.protocol = protocol;
    	protocol.setServerAddress(url);
	}
	
    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
	public void setServerUrl(String url, LowLevelProtocol protocol) throws MalformedURLException {
		this.protocol = protocol;
		protocol.setServerAddress(url);
	}

    /**
     * Set the <i>url</i> of the server 
     * @param url The url of the server
     */
	public void setServerUrl(URL url, LowLevelProtocol protocol) {
		this.protocol = protocol;		
	}
	
    /**
     * Open a connection with the server 
     * @return		<i>true</i> if the object is connected with the server as a result of the call, <i>false</i> otherwise.   	
     */
    public boolean connect() {
    	if(state != State.IDLE) return false;
		if(protocol.connect()) {
			state = State.CONNECTED;
			return true;
		}

		return false;
    }
    
    public boolean authenticate() {
    	return true;
    }

    /**
     * Open a new VI 
     * @param name The path to the VI
     */
    public boolean openVI(String name) {
    	if(state != State.CONNECTED) return false;   	
		if(protocol.openVI(name)) {				
			state = State.OPENED;
			return true;
		}
		return false;
    }

    /**
     * Run the opened VI
     * @returns true if the VI is running as a result of the call 
     */
    public boolean runVI() {
    	if(state != State.OPENED) return false;   	
		if(protocol.runVI()) {
			state = State.RUNNING;
			return true;
		}
		return false;
    }
    
    /**
     * Stop the running VI
     */
    public boolean stopVI() {
    	if(state != State.RUNNING) return false;   	
		if(protocol.stopVI()) {
			state = State.OPENED;
			return true;
		}
		return false;
    }
    
    /**
     * Close the opened VI
     */
    public boolean closeVI() {
    	switch(state) {
    	case IDLE:
    	case CONNECTED:
    		return false;
    	case RUNNING:
    		stopVI();
    	case OPENED:
   		default:
    	}   		
		if(protocol.closeVI()) {
			state = State.CONNECTED;
			return true;
		}
		return false;
    }
    
    /**
     * Sync the VI
     */
    public boolean syncVI() {
    	switch(state) {
    	case IDLE:
    	case CONNECTED:
    		return false;
    	case RUNNING:
    	case OPENED:
    		break;
   		default:
    	}    	
    	if(protocol.syncVI()) {
			return true;
    	}
		return false;
    }   
    
    /**
     * Disconnect from the server
     */
    public boolean disconnect() {
    	if(state == State.RUNNING) {
    		stopVI();
    	}
    	if(state == State.OPENED) {
    		closeVI();
    	}
    	if(state != State.CONNECTED) {
    		return false;
    	}

    	if(protocol.disconnect()) {
			state = State.IDLE;
			return true;
		}
		return false;
    }
    
    public boolean isConnected() {
    	return (state != State.IDLE);
    }

    public boolean isRunning() {
    	return (state == State.RUNNING);
    }
    
    public boolean isOpened() {
    	return (state == State.OPENED) || (state == State.RUNNING);
    }

    public HashMap<String,String> getControls() {
    	return protocol.getControls();
    }

    public HashMap<String,String> getIndicators() {
    	return protocol.getIndicators();
    }
    
    public LowLevelProtocol getProtocol() {
    	return protocol;
    }
}

package es.uned.dia.softwarelinks.labview.protocol;

import java.net.URL;
import java.util.HashMap;

/**
* Interface to implement the high-level communication protocol
*/
public interface HighLevelProtocol {
	// Execution control methods
	public boolean connect();
	public void start();
    public boolean step();
	public void stop();
	public boolean disconnect();
    
	public boolean setServerAddress(String url);
	public boolean setServerAddress(URL url);

	// Getter & Setter methods for the types boolean, int, float, double and String
	public void getValuesLater();
	public void setValuesLater();
	public void getValues();
	public void setValues();
	
	public HashMap<String,String> getControls();
	public HashMap<String,String> getIndicators();
}

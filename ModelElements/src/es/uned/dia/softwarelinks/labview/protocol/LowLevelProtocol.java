package es.uned.dia.softwarelinks.labview.protocol;

import java.net.URL;
import java.util.HashMap;

/**
* Interface to implement the low-level communication protocol
*/
public interface LowLevelProtocol {
	// Connection methods
	public boolean setServerAddress(String url);
	public boolean setServerAddress(URL url);
	public boolean connect();
	public boolean disconnect();

	// Execution control methods
	public boolean openVI(String pathToVI);
	public boolean runVI();
	public boolean stopVI();
	public boolean closeVI();
	public boolean syncVI();
	
	// Status methods
    public boolean isConnected();
    public boolean isRunning();
    public boolean isOpened();

	// Getter & Setter methods for the types boolean, int, float, double and String
	public boolean getBoolean(String name);
	public int getInt(String name);
	public float getFloat(String name);
	public double getDouble(String name);
	public String getString(String name);
	public void setValue(String name, boolean value);
	public void setValue(String name, int value);
	public void setValue(String name, float value);
	public void setValue(String name, double value);
	public void setValue(String name, String value);
	public HashMap<String,String> getControls();
	public HashMap<String,String> getIndicators();
}

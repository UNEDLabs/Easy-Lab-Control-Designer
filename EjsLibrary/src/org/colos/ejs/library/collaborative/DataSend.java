/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Type of data sent through the socket
 */

public class DataSend implements Serializable{
	
	//Serial Version ID
	private static final long serialVersionUID = 1L;
	private String host;
	private String IPDir;
	private String IPLocal_Public;
	private String port;
	private String user;
	private String password;
	private String pack;
	private String mainFrame;
	private String dim;
	private String codebase;
	private int nparams;
	private ArrayList<String> listFields = new ArrayList<String>();
	
	
	/**
	* Constructor without parameters. Default values of a data sent
	*/
	public DataSend(){
		host = "http://localhost:8080";
		IPDir = "127.0.0.1";
		IPLocal_Public="local";
		port = "50001";
		user = "default";
		password = "rooter";
		pack = "Unnamed";
		mainFrame = "Frame";
		dim = "600,400";
		codebase = "";
		nparams = 9;
		listFields = null;
	}
	
	
	/**
	* Constructor with parameters
	* @param _args ArrayList Values to form a object DataSend 
	*/
	public DataSend(ArrayList<String> _args, boolean moodle)
	{
		host = _args.get(0);
		IPDir =_args.get(1);
		IPLocal_Public = _args.get(2);
		port = _args.get(3);
		if(!moodle){
			user = _args.get(4);
			password = _args.get(5);
			pack = _args.get(6);
			mainFrame = _args.get(7);
			dim = _args.get(8);
			nparams = 9;
		}
		else{
			pack = _args.get(4);
			mainFrame = _args.get(5);
			codebase = _args.get(6);
			dim = _args.get(7);
			nparams = 8;
		}
		listFields = _args;
	}
	
	
	/**
	* Constructor with parameters
	* @param hostP String Host of public server
	* @param IPDirP String IP Direction of master
	* @param IPLocal_PublicP String Signal "local" or "public" to use a IP local or public in the connection
	* @param userP String User who generates the HTML page
	* @param passwordP String Password to do that
	* @param packP String Applet package
	* @param mainFrameP String Main Frame of the applet
	* @param dimP String Dimension of the applet
	*/
	public DataSend(String hostP, String IPDirP,String IPLocal_PublicP,String portP, String userP, String passwordP,String packP, String mainFrameP, String dimP){
		host = hostP;
		IPDir = IPDirP;
		IPLocal_Public = IPLocal_PublicP;
		port = portP;
		user = userP;
		password = passwordP;
		pack = packP;
		mainFrame = mainFrameP;
		dim = dimP;
	}
	
	
	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Pass the class components to a String array
	* @return String[] String array of arguments
	*/
	protected String[] DataSendtoString(){
		
		String[] _args = new String[this.nparams];
		if(this.nparams==9){
			_args[0] = this.host;
			_args[1] = this.IPDir;
			_args[2] = this.IPLocal_Public;
			_args[3] = this.port;
			_args[4] = this.user;
			_args[5] = this.password;
			_args[6] = this.pack;
			_args[7] = this.mainFrame;
			_args[8] = this.dim;
		}
		else{
			_args[0] = this.host;
			_args[1] = this.IPDir;
			_args[2] = this.IPLocal_Public;
			_args[3] = this.port;
			_args[4] = this.pack;
			_args[5] = this.mainFrame;
			_args[6] = this.codebase;
			_args[7] = this.dim;
		}
		return _args;
	}

	
	/**
	* Get the host
	* @return String Host address
	*/
	protected String getHost() {
		return host;
	}
	
	
	/**
	* Get the IP address
	* @return String IP address of the local PC
	*/
	protected String getIPLocal_Public(){
		return IPLocal_Public;
	}
	
	
	/**
	* Get the array of parameters
	* @return ArrayList ArrayList of parameters
	*/
	protected ArrayList<String> getListFields(){
		return listFields;
	}
	// ---------------------------
	// End Protected Methods
	// ---------------------------

}

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

public class DataSocket implements Serializable {
	
	//Serial version ID
	private static final long serialVersionUID = 1L;
	
	//Private members
	private String identificator;
	private ArrayList<Object> myArrayList;
	private String signal;
	
	//Protected value
	protected String value;
	
	
	/**
	* Constructor without parameters
	*/
	public DataSocket(){}
	
	
	/**
	* Constructor with parameters
	* @param id String Identification of student-teacher
	* @param _object Object Object embedded in the message
	* @param sig String Signal of message
	* @param val String
	*/
	public DataSocket(String id, Object _object,String sig, String val){
		identificator = id;
		signal = sig;
		value = val;
		myArrayList = new ArrayList<Object>();
		myArrayList.add(_object);
	}
	
	
	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	protected void setIdentificator(String id){
		identificator = id;
	}
	
	protected String getIdentificator(){
		return identificator;
	}
	
	protected Object getObject(){
		return myArrayList.get(0);
	}
	
	protected ArrayList<Object> getList(){
		return myArrayList;
	}
	
	protected void setSignal(String sig){
		signal = sig;
	}
	
	protected String getSignal(){
		return signal;
	}
	// ---------------------------
	// End Protected Methods
	// ---------------------------

}

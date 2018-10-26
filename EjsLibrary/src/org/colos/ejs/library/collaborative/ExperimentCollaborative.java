/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import org.colos.ejs.library.Experiment;

/**
 * Expermient collaborative
 */

public class ExperimentCollaborative extends Experiment {
	
	//Private member experment. To get a experiment given
	@SuppressWarnings("unused")
	private Experiment exp;
	private boolean alive;
	
	
	/**
	* Constructor with parameters
	* @param exp Experiment Experiment that it is running in the master
	*/
	public ExperimentCollaborative (Experiment exp){
		super(null,null);
		this.exp = exp;
	}
	
	
	/**
	* Set the experiment in the exp member
	* @param exp Experiment Experiment that it is running in the master
	*/
	public void setExperiment (Experiment exp){
		this.exp = exp;
		_thread = exp._thread;
	}
	
	
	/**
	* Check if it is running a experiment in the master
	* @return boolean True, is running. False is not running
	*/
	public boolean isAliveThread(){
		if(_thread==null) return false;
		alive = _thread.isAlive();
		return alive;
	}
	
	
	/**
	* Overload the run Thread method
	*/
	public void run() {}

}

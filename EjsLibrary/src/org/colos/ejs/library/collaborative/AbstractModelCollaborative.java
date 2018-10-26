/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import org.colos.ejs.library.Model;


/**
 * AbstractModel which contains a simulation collaborative
 */

public abstract class AbstractModelCollaborative extends Model  {
	
	abstract public SimulationCollaborative getSimulationCollaborative();
	
	  //	---- Control of the Thread ----
	  private _ControlSimulationExperimentClass _cSE = new _ControlSimulationExperimentClass();
	  private class _ControlSimulationExperimentClass {
	    public synchronized void _controlForSimulation() {
	      try { wait(); }
	      catch (Exception _exc) {}
	    }
	    public synchronized void _controlForExperiment(){
	      notify();
	    }
	  }
	  //---- End of Control of the Thread ----
	  
	  
	  public void _play() { getSimulationCollaborative().play(); }

	  public void _playAndWait(){ _play(); _cSE._controlForSimulation(); }

	  public void _pause() { getSimulationCollaborative().pause(); _cSE._controlForExperiment(); }

	  public void _step() { _pause(); getSimulationCollaborative().step(); }
	  
	  public void _reset() {
//		  _external.reset();
		  getSimulationCollaborative().reset();
	  }

	  public void _initialize() { 
//	    _external.reset(); 
	    getSimulationCollaborative().initialize(); 
	  }
}

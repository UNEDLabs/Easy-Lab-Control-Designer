/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import org.opensourcephysics.display.OSPRuntime;

/**
 * A base interface for user experiments
 */

public abstract class Experiment implements Runnable {
  String _name,_description;
  public Thread _thread=null; //CJB for collaborative (chage protected for public)
  boolean _shouldStop=false;

  public Experiment (String _aName, String _aDescription) {
    _name = _aName;
    _description = _aDescription;
  }

  public void _runExperiment () {
//    System.out.println (this+" : Starting the thread");
    if (_thread!=null) _stopExperiment();
    _thread = OSPRuntime.appletMode ? new Thread(this) : new Thread(Animation.getThreadGroup(),this);
    _thread.setPriority(Thread.NORM_PRIORITY);
    _shouldStop = false;
    _thread.start();
  }

  public void _abortExperiment() {
//    System.out.println (this+" : Stopping the thread");
    _shouldStop = true;
    if (_thread!=null) _thread.interrupt();
    _thread = null;
  }

  public boolean _stopExperiment () { return _shouldStop; }

  public String _getName ( ) { return _name; }

  public String _getDescription ( ) { return _description; }

  abstract public void run ();

} // End of class



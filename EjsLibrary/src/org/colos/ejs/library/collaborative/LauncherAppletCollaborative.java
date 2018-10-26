/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import org.colos.ejs.library.LauncherApplet;


/**
 * Applet collaborative
 */

public class LauncherAppletCollaborative extends LauncherApplet {
	
	//New simulation collaborative class
	public SimulationCollaborative _simulation = null;
	public AbstractModelCollaborative _model;
	
	//Redefinition of methods with SimulationCollaborative
	public void _play() { _simulation.play(); }
	public void _pause() { _simulation.pause(); }
	public void _step() { _simulation.step(); }
	public void _setFPS(int _fps) { _simulation.setFPS(_fps); }
	public void _setDelay(int _delay) { _simulation.setDelay(_delay); }
	
	//More redefined collaborative methods
	public boolean _isPlaying() { return _simulation.isPlaying(); }
	public boolean _isPaused()  { return _simulation.isPaused(); }

	//More redefined collaborative methods
	public void _setParentComponent (String _parent) { _simulation.setParentComponent (_parent); }
	public boolean _saveImage (String _filename, String _element) { return _simulation.saveImage (_filename, _element); }
	public boolean _saveState (String _filename) { return _simulation.saveState (_filename); }
	public boolean _saveVariables (String _filename, String _varList) { return _simulation.saveVariables (_filename,_varList); }
	public boolean _saveVariables (String _filename, java.util.ArrayList<String> _varList) { return _simulation.saveVariables (_filename,_varList); }
	public boolean _saveText (String _filename, String _text) { return _simulation.saveText (_filename, _text); }
	public boolean _saveText (String _filename, StringBuffer _text) { return _simulation.saveText (_filename, _text); }
	public boolean _readState (String _filename) { return _simulation.readState (_filename); }
	public boolean _readVariables (String _filename,String _varList) { return _simulation.readVariables (_filename,(java.net.URL) null,_varList); }
	public boolean _readVariables (String _filename, java.util.ArrayList<String> _varList) { return _simulation.readVariables (_filename,(java.net.URL) null,_varList); }
	public String  _readText (String _filename) { return _simulation.readText (_filename); }
	public boolean _setVariables (String _command, String _delim, String _arrayDelim) { return _simulation.setVariables (_command,_delim,_arrayDelim); }
	public boolean _setVariables (String _command) { return _simulation.setVariables (_command); }
	public String _getVariables (String _varName) { return _simulation.getVariable (_varName); }
	
}

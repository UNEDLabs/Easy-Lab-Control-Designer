/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.server;

import java.awt.Color;
import java.awt.Component;
import java.util.Hashtable;

import org.colos.ejs.library.ConfigurableElement;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.View;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.Value;

/**
 * A base interface for the graphical user interface of a simulation
 */

public class DummyView implements View {

  private Simulation mSimulation;
  private Hashtable<String, DummyViewElement> mElements = new Hashtable<String, DummyViewElement>();

  public DummyView(Simulation simulation) {
    mSimulation = simulation;
  }

  /**
   * Adds an element to the view
   * 
   * @param name
   * @return
   */
  public DummyViewElement addElement(DummyViewElement element, String name) {
    mElements.put(name, element);
    return element;
  }

  // ---------------------------------------
  // Implementation of View
  // ----------------------------------------

  public void reset() { }

  public void initialize() { }

  public void read() { }

  public void read(String _variable) {}

  public void finalUpdate() { }

  public void update() { }
  
  public void collectData() { }

  public void onExit() { }

  public void setUpdateSimulation(boolean _value) { }

  public java.awt.Component getComponent(String _name) { return null; }

  public ControlElement getElement(String _name) { return null; }

  public ConfigurableElement getConfigurableElement(String _name) {
    if (_name == null)
      return null;
    return mElements.get(_name);
  }

  public void blockVariable(String variable) { }

  // --------- Setting different types of values ------

  public void setValue(String _name, boolean _value) { }

  public void setValue(String _name, int _value) { }

  public void setValue(String _name, double _value) { }

  public void setValue(String _name, String _value) { }

  public void setValue(String _name, Object _value) { }

  // --------- Getting different types of values ------

  public Value getValue(String _name) { return null; }

  public boolean getBoolean(String _name) { return false; }

  public int getInt(String _name) { return 0; }

  public double getDouble(String _name) { return 0; }

  public String getString(String _name) { return ""; }

  public Object getObject(String _name) { return null; }

  // ------------------------------------------
  // println
  // ------------------------------------------

  public void println(String s) { }

  public void println() { }

  public void print(String s) { }

  public void clearMessages() { }

  public java.util.Vector<ControlElement> getElements() { return null; }

  // ---------------------------------------------------
  // Public Utilities
  // ---------------------------------------------------

  static private Color[] colorTable = null;

  static public Color[] getPhaseColorTable() {
    if (colorTable == null) {
      colorTable = new Color[256];
      for (int i = 0; i < 256; i++) {
        double val = Math.abs(Math.sin(Math.PI * i / 255));
        int b = (int) (255 * val * val);
        val = Math.abs(Math.sin(Math.PI * i / 255 + Math.PI / 3));
        int g = (int) (255 * val * val * Math.sqrt(val));
        val = Math.abs(Math.sin(Math.PI * i / 255 + 2 * Math.PI / 3));
        int r = (int) (255 * val * val);
        colorTable[i] = new Color(r, g, b);
      }
    }
    return colorTable;
  }

  /**
   * Converts a phase angle in the range [-Pi,Pi] to hue, saturation, and
   * brightness.
   * 
   * @param phi
   *          phase angle
   * @return the HSB color
   */
  static public Color phaseToColor(double phi) {
    int index = (int) (127.5 * (1 + phi / Math.PI));
    index = index % 255;
    return getPhaseColorTable()[index];
  }

  /**
   * Shows a message dialog
   * 
   * @param _panel
   * @param _title
   * @param _message
   */
  public void alert(String _panel, String _title, String _message) {
    alert(getComponent(_panel), _title, _message);
  }

  public void alert(Component _parent, String _title, String _message) {
    System.err.println("ALERT: " + _title);
    System.err.println("message: " + _message);
  }

  public void setParentComponent(String _parent) {
    mSimulation.setParentComponent(_parent);
  }

  public void setUpdateView(boolean _update) {
    mSimulation.setUpdateView(_update);
  }

  /**
   * Resets all view elements to their initial state
   */
  public void resetElements() {
    reset();
    initialize();
  }

  public void clearData() { 
    clearElements();
  }

  public void resetTraces() { 
    clearElements();
  }

  /**
   * Clear all view elements from previous data
   */
  public void clearElements() {
    initialize();
  }

  /**
   * Formats a double according to the given pattern
   * 
   * @param _value
   * @param _pattern
   * @return
   */
  public String format(double _value, String _pattern) {
    return new java.text.DecimalFormat(_pattern).format(_value);
  }

} // End of class


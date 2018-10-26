/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.util.ArrayList;
import java.util.List;

import org.colos.ejs.library.control.ControlElement;

/**
 * A base interface for the graphical user interface of a simulation
 */

public class MultipleView implements View {
  private List<View> mViews;

  public MultipleView(View... views) {
    mViews = new ArrayList<View>();
    for (View view: views) mViews.add(view);
  }

  public View getFirstView() { return mViews.get(0); }

  /**
   * Clearing any previous data
   */
  public void reset() {
    for (View view: mViews) view.reset();
  }

  /**
   * A softer reset. Calling reset makes initialize unnecessary
   */
  public void initialize() {
    for (View view: mViews) view.initialize();
  }

  /**
   * A softer reset. Calling reset makes initialize unnecessary
   */
  public void clearData() {
    for (View view: mViews) view.clearData();
  }

  /**
   * A softer reset. Calling reset makes initialize unnecessary
   */
  public void resetTraces() {
    for (View view: mViews) view.resetTraces();
  }

  /**
   * Read current data
   */
  public void read() {
    for (View view: mViews) view.read();
  }

  /**
   * Read a single variable
   */
  public void read(String variable) {
    for (View view: mViews) view.read(variable);
  }

  /**
   * Accept data sent and display it
   */
  public void update() {
    for (View view: mViews) view.update();
  }

  /**
   * Does the final update which makes the drawing complete.
   * Typically used by drawing panels for rendering
   */
  public void finalUpdate() {
    for (View view: mViews) view.finalUpdate();
  }

  /**
   * Accept data sent but do not graphic work
   */
  public void collectData() {
    for (View view: mViews) view.collectData();
  }

  /**
   * Clean-up when you exit
   */
  public void onExit() {
    for (View view: mViews) view.onExit();
  }

  /**
   * Whether the view must inform any simulation to update
   * whenever it changes because of user interaction
   * @param _value boolean
   */
  public void setUpdateSimulation (boolean value) {
    for (View view: mViews) view.setUpdateSimulation(value);
  }

  /**
   * Get a graphical object
   * @param _name A keyword that identifies the graphical object that
   * must be retrieved. Typically its name.
   * @return The graphical component
   */
  public java.awt.Component getComponent (String _name) {
    for (View view: mViews) {
      java.awt.Component component = view.getComponent(_name);
      if (component!=null) return component;
    }
    return null;
  }

  /**
   * Get a ControlElement by name
   * @param _name A keyword that identifies the control element that
   * must be retrieved. Typically its name.
   * @return The ControlElement
   */
  public ControlElement getElement (String _name) {
    for (View view: mViews) {
      ControlElement element = view.getElement(_name);
      if (element!=null) return element;
    }
    return null;
  }

  /**
   * Get a ConfigurableElement by name
   * @param _name A keyword that identifies the control element that
   * must be retrieved. Typically its name.
   * @return The ControlElement
   */
  public ConfigurableElement getConfigurableElement (String _name) {
    for (View view: mViews) {
      ConfigurableElement element = view.getConfigurableElement(_name);
      if (element!=null) return element;
    }
    return null;
  }

  /**
   * Protects a view variable from being updated
   * @param variable
   */
  public void blockVariable(String variable) {
    for (View view: mViews) view.blockVariable(variable);
  }

  // ---------------------------------------
  // Methods originally in org.opensourcephysics.controls.Control
  // ----------------------------------------

  /**
   * Prints a string in the control's message area followed by a CR and LF.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void println(String s) {
    for (View view: mViews) view.println(s);
  }

  /**
   * Prints a blank line in the control's message area.  GUI controls will usually display
   * messages in a non-editable text area.
   */
  public void println() {
    for (View view: mViews) view.println();
  }

  /**
   * Prints a string in the control's message area.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void print(String s) {
    for (View view: mViews) view.print(s);
  }

  /**
   * Clears all text from the control's message area.
   */
  public void clearMessages() {
    for (View view: mViews) view.clearMessages();
  }

  //CJB for collaborative
  /**
   * Get the Vector elementList of the simulation controls
   */
  public java.util.Vector<ControlElement> getElements() {
    java.util.Vector<ControlElement> elements = new java.util.Vector<ControlElement>();
    for (View view: mViews) {
      java.util.Vector<ControlElement> viewElements = view.getElements();
      if (viewElements!=null) elements.addAll(viewElements);
    }
    return elements;
  }

  //CJB for collaborative

} // End of class


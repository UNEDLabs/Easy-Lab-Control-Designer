/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import org.colos.ejs.library.control.ControlElement;

/**
 * A base interface for the graphical user interface of a simulation
 */

public interface View {

 /**
  * Clearing any previous data
  */
  public void reset();

 /**
  * A softer reset. Calling reset makes initialize unnecessary
  */
  public void initialize();

  /**
   * A softer reset. Calling reset makes initialize unnecessary
   */
   public void clearData();

   /**
    * A softer reset. Calling reset makes initialize unnecessary
    */
    public void resetTraces();
    
 /**
  * Read current data
  */
  public void read();

 /**
  * Read a single variable
  */
  public void read(String _variable);

 /**
  * Accept data sent and display it
  */
  public void update();

  /**
   * Does the final update which makes the drawing complete.
   * Typically used by drawing panels for rendering
   */
   public void finalUpdate();

  /**
   * Accept data sent but do not graphic work
   */
   public void collectData();

  /**
   * Clean-up when you exit
   */
   public void onExit();

  /**
   * Whether the view must inform any simulation to update
   * whenever it changes because of user interaction
   * @param _value boolean
   */
  public void setUpdateSimulation (boolean _value);

  /**
   * Get a graphical object
   * @param _name A keyword that identifies the graphical object that
   * must be retrieved. Typically its name.
   * @return The graphical component
   */
   public java.awt.Component getComponent (String _name);

   /**
    * Get a ControlElement by name
    * @param _name A keyword that identifies the control element that
    * must be retrieved. Typically its name.
    * @return The ControlElement
    */
   public ControlElement getElement (String _name);
   
   /**
    * Get a ConfigurableElement by name
    * @param _name A keyword that identifies the control element that
    * must be retrieved. Typically its name.
    * @return The ControlElement
    */
   public ConfigurableElement getConfigurableElement (String _name);
   
   /**
    * Protects a view variable from being updated
    * @param variable
    */
   public void blockVariable(String variable);

   // ---------------------------------------
   // Methods originally in org.opensourcephysics.controls.Control
   // ----------------------------------------
   
   /**
    * Prints a string in the control's message area followed by a CR and LF.
    * GUI controls will usually display messages in a non-editable text area.
    *
    * @param s
    */
   public void println(String s);

   /**
    * Prints a blank line in the control's message area.  GUI controls will usually display
    * messages in a non-editable text area.
    */
   public void println();

   /**
    * Prints a string in the control's message area.
    * GUI controls will usually display messages in a non-editable text area.
    *
    * @param s
    */
   public void print(String s);

   /**
    * Clears all text from the control's message area.
    */
   public void clearMessages();

   //CJB for collaborative
   /**
    * Get the Vector elementList of the simulation controls
    */
   public java.util.Vector<ControlElement> getElements();
   //CJB for collaborative
   
} // End of class


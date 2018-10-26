/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

/**
 * An interface for RadioButtons
 */
public interface RadioButtonInterface {

  public void reportChanges();

  public int getVariableIndex();

  public void setControlParent (ControlContainer _aParent);

}
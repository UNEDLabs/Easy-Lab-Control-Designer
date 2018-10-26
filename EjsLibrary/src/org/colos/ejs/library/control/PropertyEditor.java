/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

/**
 * An interface to get the editor of properties in Ejs
 */
public interface PropertyEditor {

  public java.util.List<javax.swing.text.JTextComponent> getFieldList ();

  public boolean isReading ();
  
  public String getName();

  public void displayErrorOnProperty (String property, boolean error);
  
  public Object evaluateExpression(String expression);

} // End of class


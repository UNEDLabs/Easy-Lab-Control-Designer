/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

/**

 * A base class for functions.
 * Subclasses must overwrite the methods
 */

public class _EjsConstants  {

  static public final String VERSION = "5.3";
  static public final String VERSION_DATE = "171106";
  static public final String WEB_SITE = "http://www.um.es/fem/Ejs";
  
  // Convenience values for mouse events (Can be removed???)
  static public final int LEFT_MOUSE_BUTTON = 1;
  static public final int MIDDLE_MOUSE_BUTTON = 2;
  static public final int RIGHT_MOUSE_BUTTON = 3;

  static public void main(String[] args) {
    javax.swing.JOptionPane.showMessageDialog(null, "Library file of Easy Java Simulations\nVersion "+VERSION+", build "+VERSION_DATE+"\n"+WEB_SITE);
    System.exit(0);
  }

} // End of class



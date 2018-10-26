/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import javax.swing.JScrollPane;

/**
 * A configurable panel. It has no internal value, nor can trigger
 * any action.
 */
public class ControlScrollPanel extends ControlPanel {

  protected JScrollPane scrollPanel;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    java.awt.Component panel = super.createVisual();
    scrollPanel = new JScrollPane(panel);
    return panel;
  }

  public java.awt.Component getComponent () { return scrollPanel; }

} // End of class

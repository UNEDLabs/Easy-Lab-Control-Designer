/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForArrowStyle extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"ARROW", "SEGMENT", "BOX", "TRIANGLE", "RHOMBUS"};
    prefix = "ArrowStyle";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();
    if (_type.toLowerCase().indexOf("newarrowstyle")>=0) {
      buttons[options.length-2].setVisible(true);
      buttons[options.length-1].setVisible(true);
    }
    else {
      buttons[options.length-2].setVisible(false);
      buttons[options.length-1].setVisible(false);
    }
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}
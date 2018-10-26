/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForBorderPosition extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"TOP", "ABOVE_TOP", "BELOW_TOP", "BOTTOM", "ABOVE_BOTTOM", "BELOW_BOTTOM"};
    prefix = "BorderPosition";
    optionsPanel.setLayout(new java.awt.GridLayout(2,3,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

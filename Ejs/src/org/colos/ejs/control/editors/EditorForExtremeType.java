/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForExtremeType extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"PLAIN",        "CIRCLE",        "DIAMOND",        "SQUARE",       "ARROW",
                            "LINE",  "FILLED_CIRCLE", "FILLED_DIAMOND", "FILLED_SQUARE", "FILLED_ARROW"};
    prefix = "ExtremeType";
    optionsPanel.setLayout(new java.awt.GridLayout(2,5,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

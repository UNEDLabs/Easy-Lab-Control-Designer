/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForBorderType extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"EMPTY", "LOWERED_BEVEL", "RAISED_BEVEL", "LOWERED_ETCHED", "RAISED_ETCHED",
                            "LINE",  "ROUNDED_LINE",  "MATTE",        "TITLED",         "ROUNDED_TITLED"};
    prefix = "BorderType";
    optionsPanel.setLayout(new java.awt.GridLayout(2,5,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

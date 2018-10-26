/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForSymbolType extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"CIRCLE_1", "CIRCLE_2", "CIRCLE_3", "CIRCLE_4", "CIRCLE_5",
                            "DIAMOND_1", "DIAMOND_2", "DIAMOND_3", "RECTANGLE_1", "RECTANGLE_2"};
    prefix = "SymbolType";
    optionsPanel.setLayout(new java.awt.GridLayout(2,5,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

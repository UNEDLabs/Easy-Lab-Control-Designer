/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForCartesianAxisType extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"LINEAR", "LOG10"};
    prefix = "CartesianAxisType";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();
    // TODO: This one has no images, check
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}
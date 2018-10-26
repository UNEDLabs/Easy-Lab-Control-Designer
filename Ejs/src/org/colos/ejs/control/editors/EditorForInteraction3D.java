/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForInteraction3D extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"ENABLED_NONE", "ENABLED_ANY", "ENABLED_X", "ENABLED_Y", "ENABLED_Z", "ENABLED_XY", "ENABLED_XZ", "ENABLED_YZ", "ENABLED_NO_MOVE"};
    prefix = "Interaction3D";
    optionsPanel.setLayout(new java.awt.GridLayout(2,4,0,0));
    resetButtons();

    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

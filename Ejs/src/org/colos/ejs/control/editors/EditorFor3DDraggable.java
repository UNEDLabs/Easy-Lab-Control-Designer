/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorFor3DDraggable extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    return edit(null, returnField);
  }
  
  public static String edit (java.util.List<TwoStrings> list, javax.swing.JTextField returnField) {
    options = new String[] {"NONE", "ANY", "AZIMUTH", "ALTITUDE"};
    prefix = "3DDraggable";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();
    return EditorMultiuse.edit  (returnField);
  }

}

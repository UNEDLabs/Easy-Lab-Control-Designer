/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForMarkerShape extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"NONE", "ELLIPSE", "RECTANGLE", "ROUND_RECTANGLE", "WHEEL"};
    prefix = "MarkerShape";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();

    // Backwards compatibility
    String value = returnField.getText().trim().toLowerCase();
    if      (value.equals("circle"))        returnField.setText("ELLIPSE");
    else if (value.equals("square"))        returnField.setText("RECTANGLE");
    else if (value.equals("square"))        returnField.setText("RECTANGLE");
    else if (value.equals("filled_circle")) returnField.setText("ELLIPSE");
    else if (value.equals("filled_square")) returnField.setText("RECTANGLE");
    else if (value.equals("no_marker"))     returnField.setText("NONE");
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

  public static String edit (java.util.List<TwoStrings> list, javax.swing.JTextField returnField) {
    options = new String[] {"NONE", "ELLIPSE", "RECTANGLE", "ROUND_RECTANGLE", "WHEEL"};
    prefix = "MarkerShape";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();
    return EditorMultiuse.edit  (returnField);
  }
}

/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForTraceMarkerShape extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"ELLIPSE", "RECTANGLE", "ROUND_RECTANGLE", "BAR", "POST", "AREA", "NONE"};
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
    else if (value.equals("bar"))           returnField.setText("BAR");
    else if (value.equals("post"))          returnField.setText("POST");
    else if (value.equals("area"))          returnField.setText("AREA");
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

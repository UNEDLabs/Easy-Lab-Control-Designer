/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForElementPosition extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"CENTERED", "NORTH", "SOUTH", "EAST", "WEST",
                                        "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST"};
    prefix = "ElementPosition";
    optionsPanel.setLayout(new java.awt.GridLayout(2,5,0,0));
    resetButtons();

    // Backwards compatibility
    String value = returnField.getText().trim().toLowerCase();
    if      (value.equals("hor_centered"))      returnField.setText("NORTH");
    else if (value.equals("hor_centered_down")) returnField.setText("NORTH");
    else if (value.equals("hor_centered_up"))   returnField.setText("SOUTH");
    else if (value.equals("ver_centered"))        returnField.setText("WEST");
    else if (value.equals("ver_centered_right"))  returnField.setText("WEST");
    else if (value.equals("ver_centered_left"))   returnField.setText("EAST");
    else if (value.equals("lower_left"))   returnField.setText("SOUTH_WEST");
    else if (value.equals("upper_left"))   returnField.setText("NORTH_WEST");

    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

  public static String edit (java.util.List<TwoStrings> list, javax.swing.JTextField returnField) {
    options = new String[] {"CENTERED", "NORTH", "SOUTH", "EAST", "WEST",
        "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST"};
    prefix = "ElementPosition";
    optionsPanel.setLayout(new java.awt.GridLayout(2,5,0,0));
    resetButtons();
    return EditorMultiuse.edit  (returnField);
  }
  
}

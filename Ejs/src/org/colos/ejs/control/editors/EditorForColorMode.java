/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import org.opensourcephysics.display2d.ColorMapper;

public class EditorForColorMode extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"SPECTRUM", "GRAYSCALE","DUALSHADE", "RED", "GREEN", "BLUE", "BLACK", "WIREFRAME", "NORENDER", "REDBLUESHADE"};
    prefix = "ColorMode";
    optionsPanel.setLayout(new java.awt.GridLayout(2,7));
    resetButtons();
    for (int i=0, m = options.length; i<m; i++) {
      buttons[i].setEnabled(true);
//      buttons[i].setVisible(true);
    }
    if (_type.toLowerCase().indexOf("plotmode")>=0) ; // all are visible
    else if (_type.toLowerCase().indexOf("colormode")>=0) {
      buttons[ColorMapper.NORENDER].setEnabled(false);
//      buttons[options.length-1].setVisible(false);
    }
    else { // VectorColorMode
      buttons[ColorMapper.DUALSHADE].setEnabled(false);
      buttons[ColorMapper.REDBLUE_SHADE].setEnabled(false);
      buttons[ColorMapper.WIREFRAME].setEnabled(false);
      buttons[ColorMapper.NORENDER].setEnabled(false);
//      for (int i=6; i<options.length; i++) buttons[i].setVisible(false);
    }
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}

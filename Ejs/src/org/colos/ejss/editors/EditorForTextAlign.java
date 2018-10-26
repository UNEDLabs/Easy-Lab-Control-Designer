/**
 * The editor package contains editor for EjsS properties
 * @author F. Esquembre (http://fem.um.es).
 * @version March 2013
 */

package org.colos.ejss.editors;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForTextAlign extends EditorMultiuse {

  public static String edit (java.util.List<TwoStrings> optionsList, javax.swing.JTextField returnField) {
    sIconPrefix = "Alignment";
    sOptionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    return EditorMultiuse.edit (optionsList, returnField);
  }

}
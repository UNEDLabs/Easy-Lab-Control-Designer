/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejss.editors;

import java.util.StringTokenizer;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForColor3D {

  public static String edit (java.util.List<TwoStrings> optionsList, javax.swing.JTextField returnField) {
    String value = EditorForColor.editWithoutAlpha (optionsList, returnField);
    if (value==null) return null;
    for (TwoStrings entry : optionsList) if (entry.getFirstString().equals(value)) return entry.getSecondString();
    // Use only the RGB values
    if (value.startsWith("rgba")) {
      try {
        StringTokenizer tkn = new StringTokenizer(value.substring(4),"(), ");
        return "rgb("+tkn.nextToken()+","+tkn.nextToken()+","+tkn.nextToken()+")";
      }
      catch (Exception exc) { return "rgba("+value+")"; }
    }
    return value;
  }

}

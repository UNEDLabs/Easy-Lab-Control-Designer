/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForMeshDataType extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"MESH_2D", "MESH_3D", "SCALAR_2D_FIELD", "SCALAR_3D_FIELD", "VECTOR_2D_FIELD", "VECTOR_3D_FIELD"};
    prefix = "MeshDataType"; 
    optionsPanel.setLayout(new java.awt.GridLayout(3,2,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

  public static String edit (java.util.List<TwoStrings> list, javax.swing.JTextField returnField) {
    options = new String[] {"MESH_2D", "MESH_3D", "SCALAR_2D_FIELD", "SCALAR_3D_FIELD", "VECTOR_2D_FIELD", "VECTOR_3D_FIELD"};
    prefix = "MeshDataType"; 
    optionsPanel.setLayout(new java.awt.GridLayout(3,2,0,0));
    resetButtons();
    return EditorMultiuse.edit  (returnField);
  }

}

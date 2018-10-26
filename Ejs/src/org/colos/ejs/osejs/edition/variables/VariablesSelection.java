/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.variables;

import java.awt.datatransfer.*;


public class VariablesSelection implements Transferable {

  static public final DataFlavor variablesFlavor = new DataFlavor("ejs/variables;class=java.lang.String","Ejs model variables");

  private String selection;

  public VariablesSelection (String _selection) { selection = _selection; }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { variablesFlavor};
  }

  public boolean isDataFlavorSupported (DataFlavor _flavor) {
    return _flavor.equals(variablesFlavor);
  }

  public Object getTransferData (DataFlavor _flavor) throws UnsupportedFlavorException {
    if (_flavor.equals(variablesFlavor)) return selection;
    throw new UnsupportedFlavorException(_flavor);
  }

} // end of class

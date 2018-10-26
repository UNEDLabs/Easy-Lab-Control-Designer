/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.html_view;

import java.awt.datatransfer.*;

public class Selection implements Transferable {

  static public final DataFlavor viewFlavor = new DataFlavor("ejs/htmlview;class=java.lang.String","Ejs html view");

  private String selection;

  public Selection (String _selection) { 
    selection = _selection; 
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { viewFlavor, DataFlavor.stringFlavor };
  }

  public boolean isDataFlavorSupported (DataFlavor _flavor) {
    return _flavor.equals(viewFlavor) || _flavor.equals(DataFlavor.stringFlavor);
  }

  public Object getTransferData (DataFlavor _flavor) throws UnsupportedFlavorException {
    if (_flavor.equals(viewFlavor)) return selection;
    if (_flavor.equals(DataFlavor.stringFlavor)) return selection;
    throw new UnsupportedFlavorException(_flavor);
  }

} // end of class

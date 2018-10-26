/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.variables;

import java.awt.datatransfer.*;

public class ModelElementTransferable implements Transferable {

  static public final DataFlavor modelElementFlavor = new DataFlavor("ejs/model_element;class=org.colos.ejs.model_elements.ModelElement","EJS model element");

  private ModelElementInformation selection;

  /**
   * Constructor that makes a copy of the element info (because it might change after the selection is done)
   * @param _elementInfo
   * @param _position
   */
  public ModelElementTransferable (ModelElementInformation _elementInfo) {
    selection = new ModelElementInformation(_elementInfo.getElement(),_elementInfo.getJarFile());
    selection.setName(_elementInfo.getName());
    selection.setPosition(_elementInfo.getPosition());
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { modelElementFlavor };
  }

  public boolean isDataFlavorSupported (DataFlavor _flavor) {
    return _flavor.equals(modelElementFlavor);
  }

  public Object getTransferData (DataFlavor _flavor) throws UnsupportedFlavorException {
    if (_flavor.equals(modelElementFlavor)) return selection;
    throw new UnsupportedFlavorException(_flavor);
  }

} // end of class

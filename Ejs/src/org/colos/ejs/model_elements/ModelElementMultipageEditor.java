/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.model_elements;

import org.colos.ejs.model_elements.ModelElementsCollection;


/**
 * An Editor that has more than one page, each of them labeled by a different String
 */
public interface ModelElementMultipageEditor {

  public void showPanel(ModelElementsCollection collection, String elementName, String keyword);

}


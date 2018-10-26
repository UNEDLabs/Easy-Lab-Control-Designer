/**
 * The html package contains generic tools to view and edit HTML pages
 * Copyright (c) August 2010 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * @version August 2010
 */

package org.colos.ejs.osejs.edition.html;

import java.awt.Component;
import javax.swing.text.JTextComponent;

/**
 * An interface for a component that displays amn HTML document
 * @author Paco
 *
 */

interface HtmlComponent {
  
  public Component getComponent ();
  
  public JTextComponent getTextComponent();

}

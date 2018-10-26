/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

public class ControlEditor extends TabbedEditor {

  public ControlEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super(_ejs,Editor.CODE_EDITOR, "Control");
  }

} // end of class

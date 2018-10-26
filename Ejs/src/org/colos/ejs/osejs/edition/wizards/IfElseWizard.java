/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.wizards;

public class IfElseWizard implements Wizard {

  static final org.colos.ejs.osejs.utils.ResourceUtil res = new org.colos.ejs.osejs.utils.ResourceUtil("Resources");

  public String getCode () {
    return "if ( /* "+res.getString("CodeWizard.WriteConditionHere")+" */ ) {\n"+
           "  /* "+ res.getString("CodeWizard.WriteCodeHere")+" */\n"+
           "}\n"+
           "else {\n"+
           "  /* "+ res.getString("CodeWizard.WriteAltCodeHere")+" */\n"+
           "}\n";
  }

} // end of class


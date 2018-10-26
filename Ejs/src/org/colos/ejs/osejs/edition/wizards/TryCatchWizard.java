/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.wizards;

public class TryCatchWizard implements Wizard {

  static final org.colos.ejs.osejs.utils.ResourceUtil res = new org.colos.ejs.osejs.utils.ResourceUtil("Resources");

  public String getCode () {
    return "try {\n"+
           "  /* "+ res.getString("CodeWizard.WriteCodeHere")+" */\n"+
           "} catch (java.lang.Exception _exc) {\n"+
           "  /* "+ res.getString("CodeWizard.WriteAltCodeHere")+" */\n"+
           "}\n";
  }

} // end of class


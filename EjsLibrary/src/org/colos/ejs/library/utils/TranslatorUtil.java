/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.colos.ejs.library.Simulation;

/**
 * A simple map between properties and values
 */

public class TranslatorUtil  {
  protected Map<String,String> defaultProperties = new HashMap<String,String>();
  
  // -----------------------------------
  // Basic operations
  //------------------------------------

  /**
   * Provides a locale selector component (if any)
   */
  public javax.swing.JComponent getSelectorComponent(Simulation _simulation) { return null; }

  /**
   * Adds a String to the properties
   */
  public final void addString(String _keyword, String _value) { defaultProperties.put(_keyword, _value); }
  
  /**
   * Looks for the translation of the given keyword. If not found in any locale, then the _defautl is provided.
   * @param _keyword
   * @param _default
   * @return
   */
  public final String translateString (String _keyword, String _default) {
    boolean hadQuotes=false;
    if (_default.startsWith("\"") && _default.endsWith("\"")) {
      hadQuotes = true;
      _default = _default.substring(1,_default.length()-1);
    }
    String value = getValueOf(_keyword);
    if (value==null) value = _default;
    if (hadQuotes) value = "\"" + value + "\"";
    return value;
  }
  
  /**
   * Same as translateString(_property,_property);
   * @param _property
   * @return
   */
  public final String translateString (String _property) { return translateString(_property,_property); }
  
  // -----------------------------------
  // To be overwritten
  //------------------------------------
  
  /**
   * Protected method used by the final translateString method
   * @param _keyword
   * @return
   */
  protected String getValueOf(String _keyword) { return defaultProperties.get(_keyword); }

  public void addTranslation(String _language) { }
  
  public void setLocaleItem (LocaleItem _item) { }
  
  public void addToMenu (JMenu _menu, Simulation _simulation) { }

  public void refreshMenu() {}
  
} // End of class



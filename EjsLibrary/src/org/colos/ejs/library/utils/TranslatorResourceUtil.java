/**
 * The resource package contains utils and definitions for
 * multilingual use of the whole project
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.colos.ejs.library.Simulation;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.ToolsRes;

public class TranslatorResourceUtil extends TranslatorUtil {
  private String classname;
  private Simulation simulation;
  private Map<LocaleItem,Map<String,String>> translations = new HashMap<LocaleItem,Map<String,String>>();
  private Map<String,String> currentTranslation;
  private JMenu langMenu=null;
  private LocaleSelector localeSelector; 

  public TranslatorResourceUtil (String _classname) {
    classname = _classname.replace('.', '/');
    currentTranslation = defaultProperties;
    readPropertiesFile(defaultProperties,classname);
    translations.put(LocaleItem.getDefaultItem(),defaultProperties);
  }
  
  // -----------------------------------
  // Methods overwritten
  //------------------------------------

  @Override
  public javax.swing.JComponent getSelectorComponent(Simulation _simulation) {
    this.simulation = _simulation;
    if (localeSelector==null) {
      localeSelector = new LocaleSelector(simulation);
      localeSelector.setEditable(false);
      for (LocaleItem item : translations.keySet()) localeSelector.addLocaleItem(item);
    }
    return localeSelector.getComponent();
  }
  
  @Override
  protected String getValueOf(String _keyword) { 
    String value = currentTranslation.get(_keyword);
    if (value==null) value = defaultProperties.get(_keyword);
    return value; 
  }
  
  /**
   * Reads and adds the resources for the given LocaleItem
   * @param _item
   */
  @Override
  public void addTranslation(String _language) {
    LocaleItem item = LocaleItem.getLocaleItem(_language);
    if (item!=null) {
      Map<String,String> translation = translations.get(item);
      if (translation!=null) return;
      translation = new HashMap<String,String>();
      readPropertiesFile(translation,classname+"_"+item.getKeyword()); 
      translations.put(item, translation); // Added even if the file is not there (there may be an HTML translation)
    }
    else System.out.println ("Warning! Ignored invalid locale "+_language);
  }
    
  @Override
  public void setLocaleItem (LocaleItem _item) {
    currentTranslation = translations.get(_item);
//    if (currentTranslation==null && _item.getKeyword().length()>2) { // Try going down from "es ES" to "es", if available
//      System.out.println ("Trying for "+_item.getKeyword().substring(0,2));
//      LocaleItem plainLangItem = LocaleItem.getLocaleItem(_item.getKeyword().substring(0,2));
//      currentTranslation = translations.get(plainLangItem);
//      if (currentTranslation==null) {
//        currentTranslation = new HashMap<String,String>();
//        if (readPropertiesFile(currentTranslation,classname+"_"+plainLangItem.getKeyword())) {
//          translations.put(plainLangItem,currentTranslation);
//        }
//      }
//    }
    if (currentTranslation==null) currentTranslation = defaultProperties;
    if (localeSelector!=null) localeSelector.refreshGUI(_item);
  }

  @Override
  public void addToMenu (JMenu _menu, Simulation _simulation) {
    this.simulation = _simulation;
    // Change language menu
    langMenu = new JMenu(ToolsRes.getString("TranslatorTool.Label.Description"));
    _menu.add(langMenu);
  }


  @Override
  public void refreshMenu() {
    if (langMenu==null) return;
    langMenu.removeAll();
    List<LocaleItem> localeItems = new ArrayList<LocaleItem>(translations.keySet());
    Collections.sort(localeItems);
    for (LocaleItem localeItem : localeItems) langMenu.add(new MyMenuItem(localeItem));
  }

  // ------------------------------------
  // Utility methods and classes
  //-------------------------------------
  
  /**
   * Reads a properties file
   * @param _translation
   * @param _filename
   * @return true if successful
   */
  static private boolean readPropertiesFile(Map<String,String> _translation, String _filename) {
//    System.out.println ("Reading file "+_filename+".properties for translation "+_translation);
    Resource resourceFile = ResourceLoader.getResource(_filename+".properties"); //$NON-NLS-1$ //$NON-NLS-2$
    if (resourceFile==null) {
      System.out.println ("Warning! Ignored unavailable resource file : "+_filename+".properties.");
      return false;
    }
    try {
      InputStream stream = resourceFile.openInputStream();
      BufferedReader input = new BufferedReader(new InputStreamReader(stream, LocaleItem.getCharset()));
      // read properties line by line and put entries into the map
      String next = input.readLine();
      while (next!=null) {
        int i = next.indexOf("="); //$NON-NLS-1$
        if (i>-1) {
          String key = next.substring(0, i);
          String val = next.substring(i+1);
          _translation.put(key, val);
        }
        next = input.readLine();
      }
      return true;
    }
    catch(IOException ex) {
      System.out.println ("Warning! Error reading resource file "+_filename+".");
      return false;
    }
  }

  private class MyMenuItem extends JMenuItem {
    LocaleItem item;
    
    MyMenuItem(LocaleItem _item) {
      super(_item.toString());
      item = _item;
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          simulation.setLocaleItem(item);
        }
      });
    }
  }

} // end of class

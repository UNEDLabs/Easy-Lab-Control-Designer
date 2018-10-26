/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.macos;
 
import javax.swing.*;

import org.colos.ejs.osejs.Osejs;
//import org.colos.ejs.osejs.utils.ResourceUtil;
//import org.opensourcephysics.tools.ResourceLoader;

import java.awt.Desktop;

//import com.apple.eawt.Application;

import java.awt.desktop.*;

public class MacOSXHandlerForEjsJava9 implements AboutHandler, QuitHandler, PreferencesHandler {
//  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  // --------------------------------------------
  // Main constructor
  // --------------------------------------------

  private Osejs mEjs;
  
  public MacOSXHandlerForEjsJava9 (Osejs ejs) {
    mEjs = ejs;
    Desktop app = Desktop.getDesktop();
    app.setAboutHandler(this);
    app.setQuitHandler(this);
//    app.setDockIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.EjsIcon")));
    app.setPreferencesHandler(this);
  }
  
  public void handleAbout (AboutEvent e)  
  {
    JOptionPane.showMessageDialog(null,"Visit Easy Java Simulations wiki at\nhttp://www.um.es/fem/EjsWiki","Easy Java Simulations", JOptionPane.ERROR_MESSAGE); 
  }

  @Override
  public void handleQuitRequestWith(QuitEvent arg0, QuitResponse response) {
    if (mEjs.quit()) response.cancelQuit();
    response.performQuit();
  }

  @Override
  public void handlePreferences(PreferencesEvent arg0) {
    mEjs.editPreferences();
  }

} // end of class


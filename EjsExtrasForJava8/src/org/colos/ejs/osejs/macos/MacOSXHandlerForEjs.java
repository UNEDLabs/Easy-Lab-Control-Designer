/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.macos;
 
import javax.swing.*;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.opensourcephysics.tools.ResourceLoader;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitResponse;

public class MacOSXHandlerForEjs implements AboutHandler, QuitHandler, PreferencesHandler {
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  // --------------------------------------------
  // Main constructor
  // --------------------------------------------

  private Osejs mEjs;
  
  public MacOSXHandlerForEjs (Osejs ejs) {
    mEjs = ejs;
    Application app = Application.getApplication();
    app.setAboutHandler(this);
    app.setQuitHandler(this);
    app.setDockIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.EjsIcon")));
    app.setPreferencesHandler(this);
  }
  
  public void handleAbout (AppEvent.AboutEvent e)  
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


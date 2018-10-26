/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.macos;

import java.io.*;

import javax.swing.*;

import org.colos.ejs.osejs.EjsConsole;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.opensourcephysics.tools.ResourceLoader;

import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.QuitResponse;

public class MacOSXHandler implements AboutHandler, OpenFilesHandler, QuitHandler {
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  // --------------------------------------------
  // Main constructor
  // --------------------------------------------

  private EjsConsole console;
  
  public MacOSXHandler (EjsConsole _console) {
    console = _console;
    Application app = Application.getApplication();
    app.setOpenFileHandler(this);
    app.setAboutHandler(this);
    app.setQuitHandler(this);
    app.setDockIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.ConsoleIcon")));

  }
  
  public void handleAbout (AppEvent.AboutEvent e)  
  {
    JOptionPane.showMessageDialog(null,"Visit Easy Java Simulations wiki at\nhttp://www.um.es/fem/EjsWiki", "EJS Console",JOptionPane.ERROR_MESSAGE); 
  }
  
  public void openFiles(OpenFilesEvent _event) {
    java.util.List<File> files = _event.getFiles();
    for (File file : files) console.runEjs(file.getAbsolutePath());
  }

  @Override
  public void handleQuitRequestWith(QuitEvent arg0, QuitResponse response) {
    console.quit();
    response.cancelQuit();
  }

} // end of class


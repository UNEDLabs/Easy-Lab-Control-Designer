/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.macos;

import java.io.*;

import javax.swing.*;

import org.colos.ejs.osejs.EjsConsole;
//import org.colos.ejs.osejs.utils.ResourceUtil;
//import org.opensourcephysics.tools.ResourceLoader;

import java.awt.Desktop;
import java.awt.desktop.*;

public class MacOSXHandlerJava9 implements AboutHandler, OpenFilesHandler, QuitHandler {
//  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  // --------------------------------------------
  // Main constructor
  // --------------------------------------------

  private EjsConsole console;
  
  public MacOSXHandlerJava9 (EjsConsole _console) {
    console = _console;
    Desktop app = Desktop.getDesktop();
    app.setOpenFileHandler(this);
    app.setAboutHandler(this);
    app.setQuitHandler(this);
//    app.setDockIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.ConsoleIcon")));

  }
  
  public void handleAbout (AboutEvent e)  
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


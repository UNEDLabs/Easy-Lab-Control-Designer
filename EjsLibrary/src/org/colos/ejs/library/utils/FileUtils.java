package org.colos.ejs.library.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FileUtils {

  //-------------------------------------
  // Look and feel
  //-------------------------------------

  static public void updateComponentTreeUI(Component c) {
    if (org.opensourcephysics.display.OSPRuntime.isMac()) { // Only if it not the Aqua l&f
      if (UIManager.getLookAndFeel().toString().toLowerCase().indexOf("aqua")<0) SwingUtilities.updateComponentTreeUI(c);  
    }
    else SwingUtilities.updateComponentTreeUI(c);
  }
  
  /**
   * Returns the name (without the extension) of a given file
   */
  static public String getPlainName (File _file) {
    String name = _file.getName();
    int index = name.lastIndexOf('.');
    if (index>=0) name = name.substring(0,index);
    return name;
  }
  
  /**
   * Gets the path of a file in standard form.
   * If it is a directory, the path ends in "/"
   */
  static public String getPath (File _file) {
    String path;
    try { path = _file.getCanonicalPath(); }
    catch (Exception exc) { path = _file.getAbsolutePath(); }
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      path = path.replace('\\','/');
      // Sometimes the system provides c:, sometimes C:\
      int a = path.indexOf(':');
      if (a>0) path = path.substring(0,a).toUpperCase()+path.substring(a);
    }
    if (_file.isDirectory() && !path.endsWith("/")) path = path + "/";
    return path;
  }

  /**
   * Corrects a URL by changing spaces to "%20"
   * @param _urlStr String
   * @return String
   */
  static public String correctUrlString (String _urlStr) {
    String noSpaces = ""; // Replace spaces by "%20"
    java.util.StringTokenizer tkn = new java.util.StringTokenizer (_urlStr, " ",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals(" ")) noSpaces += "%20";
      //else if (token.equals("&")) aux += "%26";
      else noSpaces += token;
    }
    String noAnds = ""; // Replace "&" by "%26"
    tkn = new java.util.StringTokenizer (noSpaces, "&",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals("&")) noAnds += "%26";
      else noAnds += token;
    }
    return noAnds;
  }

}

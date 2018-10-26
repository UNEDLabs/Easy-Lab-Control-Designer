package org.colos.ejs;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class PackageEjsConsoleOnly {

  /**
   * @param args
   */
  public static void main(String[] args) {
    PackageEjs.processCommand (PackageEjs.EJS_CONSOLE); // This one always after EJS_LIBRARY
    System.exit(0);
  }

  
}

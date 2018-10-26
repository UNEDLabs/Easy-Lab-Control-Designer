package org.colos.ejs;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class PackageEjsMainOnly {

  /**
   * @param args
   */
  public static void main(String[] args) {
    PackageEjs.processCommand (PackageEjs.EJS); // This one always after EJS_LIBRARY
    System.exit(0);
  }

  
}

package org.colos.ejs;

/**
 * This class packages the Compile tools from the JDK needed by EJS to compile simulations under the JRE
 * @author Francisco Esquembre
 *
 */
public class PackageComSun {
  /**
   * @param args
   */
  public static void main(String[] args) {
    PackageEjs.processCommand (PackageEjs.COM_SUN); // But copy first tools.jar from a JDK into libraries
    System.exit(0);
  }

}

/**
 * Part of the GSVideo library: http://gsvideo.sourceforge.net/
 * Copyright (c) 2008-11 Andres Colubri 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */

package codeanticode.gsvideo;

class GSLibraryPath {
  
  String get() { // Added by Paco
    boolean isEJSApp = "true".equals(System.getProperty("osp_ejs"));
    if (isEJSApp) { // Running from EJS as a separate program
      String path = System.getProperty("user.dir");
      if (!path.endsWith("/")) path += "/";
      return path += "../export";
    }
    // Try if under EJS
    String path = System.getProperty("home");
    if (path==null) return ".";
    if (!path.endsWith("/")) path += "/";
    return path + "export"; // under EJS
  }

  /*
  String get() { // Added by Paco
    String path = ""; // getPath();
    if (path.isEmpty()) {
      
      boolean isEJSApp = "true".equals(System.getProperty("osp_ejs"));
      if (isEJSApp) { // Running from EJS as a separate program
        path = System.getProperty("user.dir");
        if (!path.endsWith("/")) path += "/";
//        if (! (new java.io.File("gstreamer").exists() ) ) 
        path += "../export";
      }
      else { // Try if under EJS
        path = System.getProperty("home");
        if (path!=null) {
          if (!path.endsWith("/")) path += "/";
          path += "export"; // under EJS
        }
        else path = "."; // must be next to me
      }
    }
    else {
//      path = System.getProperty("user.dir");
      if (!path.endsWith("/")) path += "/";
//      path += "../export";
      path += "_utils";
    }
    return path;
  }

  /*
  // This method returns the folder inside which the gstreamer library folder
  // should be located.
  private String getPath() {
    URL url = this.getClass().getResource("GSLibraryPath.class");
    if (url != null) {
      // Convert URL to string, taking care of spaces represented by the "%20"
      // string.
      String path = url.toString().replace("%20", " ");
      int n0 = path.indexOf('/');

      int n1 = -1;
      if (Platform.isWindows()) {
        n1 = path.indexOf("/lib/ARElement.jar"); // location of GSVideo.jar in
                                               // exported apps.
        if (n1 == -1)
          n1 = path.indexOf("/ARElement.jar"); // location of GSVideo.jar in
                                             // library folder.

        // In Windows, path string starts with "jar file/C:/..."
        // so the substring up to the first / is removed.
        n0++;
      } else if (Platform.isMac()) {
        // In Mac, getting the index of GSVideo.jar is enough in the case of sketches running from the PDE
        // as well as exported applications.
//        n1 = path.indexOf("GSVideo.jar"); 
        n1 = path.indexOf("ARElement.jar");
      } else if (Platform.isLinux()) {
        // TODO: what's up?
      }

      if ((-1 < n0) && (-1 < n1)) {
        return path.substring(n0, n1);
      } else {
        return "";
      }
    }
    return "";
  }
  */
  
}
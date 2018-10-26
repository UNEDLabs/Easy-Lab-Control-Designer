/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.awt.Component;
import java.awt.image.BufferedImage;


/**
 * A dummy video tool
 */

public class VideoUtil {

  public boolean isFullClass() { return false; }

  public void takeSnapshot(Component _component) {}

  public boolean startVideoTool (org.colos.ejs.library.View _view, String _element) { return false; }
  
  public void captureVideoImage() {}
  
  public boolean writeGIF(java.io.OutputStream out, BufferedImage bi) { return false; }


} // End of class



package org.colos.ejs.library.utils;

import java.awt.Image;
import java.util.EventListener;

/**
 * An interface for an object that receives a new image whenever the web cam provides it
 * @author Francisco Esquembre May 2012
 *
 */
public interface WebVideoListener extends EventListener {
  public void imageChanged(Image image);
  public void connectionError(String error);
}

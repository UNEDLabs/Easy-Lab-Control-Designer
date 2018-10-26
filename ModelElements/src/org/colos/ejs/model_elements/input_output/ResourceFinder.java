package org.colos.ejs.model_elements.input_output;

import java.applet.AudioClip;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Reader;

import javax.swing.ImageIcon;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * Encapsulates access to OSP's ResourceLoader
 * @author Francisco Esquembre
 * @version 1.0, April 2012
 *
 */
public class ResourceFinder {
  private Model model;
  private String filename;

  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public ResourceFinder(Model _model, String _filename) {
    this.model = _model;
    setFilename(_filename);
  }

  /**
   * Sets the filename to read to a constant String (such as "./myFile.txt")
   * or links it to a String model variable (such as "%myStringVariable%")
   * @return
   */
  public void setFilename(String _filename) {
    this.filename = _filename;
  }

  /**
   * Returns the file name to read
   * @return
   */
  public String getFilename() {
    return ModelElementsUtilities.getValue(model,filename);
  }

  // --- Convenience methods ---
  
  public Resource getResource() {
    return ResourceLoader.getResource(getFilename());
  }
  
  public InputStream openInputStream() {
    return ResourceLoader.openInputStream(getFilename());
  }

  public Reader openReader() {
    return ResourceLoader.openReader(getFilename());
  }

  public String getString() {
    return ResourceLoader.getString(getFilename());
  }

  public ImageIcon getIcon() {
    return AbstractModelElement.createImageIcon(getFilename());
  }

  public Image getImage() {
    return ResourceLoader.getImage(getFilename());
  }

  public BufferedImage getBufferedImage() {
    return ResourceLoader.getBufferedImage(getFilename());
  }

  public AudioClip getAudioClip() {
    return ResourceLoader.getAudioClip(getFilename());
  }

  // --- Extra Convenience methods ---
  
  public Resource getResource(String path) {
    return ResourceLoader.getResource(path);
  }
  
  public InputStream openInputStream(String path) {
    return ResourceLoader.openInputStream(path);
  }

  public Reader openReader(String path) {
    return ResourceLoader.openReader(path);
  }

  public String getString(String path) {
    return ResourceLoader.getString(path);
  }

  public ImageIcon getIcon(String path) {
    return AbstractModelElement.createImageIcon(path);
  }

  public Image getImage(String path) {
    return ResourceLoader.getImage(path);
  }

  public BufferedImage getBufferedImage(String path) {
    return ResourceLoader.getBufferedImage(path);
  }

  public AudioClip getAudioClip(String path) {
    return ResourceLoader.getAudioClip(path);
  }


}

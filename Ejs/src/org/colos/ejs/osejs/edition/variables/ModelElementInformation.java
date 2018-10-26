package org.colos.ejs.osejs.edition.variables;

import java.io.File;

import org.colos.ejs.model_elements.ModelElement;

/**
 * Stores information about a model element
 * @author Paco
 *
 */
public class ModelElementInformation {

  private ModelElement element;
  private String name;
  private int position=-1; // the position in the list of added elements (-1 if is an addition)
  private File jarFile; // The jar file where this model element is defined (can be null if the jar file is in the default classpath)

  public ModelElementInformation (ModelElement _element, File _jarFile) { 
    element = _element;
    jarFile = _jarFile;
  }
  
  public ModelElement getElement() { return element; }

  public void setName (String _name) { name = _name; }
  
  public String getName () { return name; }
  
  public void setPosition(int _pos) { position = _pos; }

  public int getPosition() { return position; }
  
  public File getJarFile() { return jarFile; }
  
}

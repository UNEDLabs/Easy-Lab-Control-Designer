package org.colos.ejs.model_elements;

import java.awt.Component;

/**
 * An interface for a class which provides edition services for a model element  
 * @author Paco
 *
 */
public interface ModelElementsCollection {

  /**
   * Lets the user choose a file (of the given extension, which may be null) and return its 
   * name relative to EJS simulation directory (if this is the case) or to the EJS source directory.
   * @param _parentComponent the parent component for the file chooser dialog
   * @param _title the title for the file chooser dialog
   * @param _description the description of desired files 
   * @param _extension the comma-separated list of desired extensions for the file
   * @return
   */
  public String chooseFilename(Component _parentComponent, String _title, String _description, String _extensions);

  /**
   * Lets the user choose a file and return its name relative to EJS simulation directory (if this is the case) or to the EJS source directory.
   * @param _parentComponent the parent component for the variable chooser dialog
   * @param _typesAllowed a '|'-separated list of accepted types, such as "String" or "int|double"
   * @param _currentValue the current value, if any
   * @return
   */
  public String chooseVariable(Component _parentComponent, String _typesAllowed, String _currentValue); 
    
  
  public String chooseViewElement(Component _parentComponent, Class<?> _typesAllowed, String _currentValue);

  /**
   * Tells the collection that there was a change in one element
   */
  public void reportChange(ModelElement _element);
  
  /**
   * Provides access to the EJS object
   */
  public org.colos.ejs.osejs.Osejs getEJS();

}

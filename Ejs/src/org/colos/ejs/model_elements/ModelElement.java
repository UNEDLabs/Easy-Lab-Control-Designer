package org.colos.ejs.model_elements;

/**
 * This interface declares the API for a clas that can be used by EJS to:
 * 1.- Instantiate an object of a class
 * 2.- Edit the properties of that class
 * 3.- Include the code required to use this object in a simpler way in a generated simulation
 * 
 * An implementing object should separate the implementation of this interface from the library 
 * object that will go into the simulation, in order not to add unnecessary weight to it.
 * @author Francisco Esquembre
 * @version 1.0 August 2010
 *
 */
public interface ModelElement {

  /**
   * Returns the icon used by EJS as logo for the element
   * @return
   */
  public javax.swing.ImageIcon getImageIcon();
  
  /**
   * Returns the generic name for all objects of this class. Will be displayed by EJS.
   * @return
   */
  public String getGenericName();
  
  /**
   * Returns the fully qualified constructor name. Used to declare the variable
   * @return
   */
  public String getConstructorName();
  
  /**
   * Returns the code required to construct and initialize the element. May span more than one line.
   * This code will be called when resetting the simulation, after all basic variables have been reset 
   * to their initial values (as specified in the table of variables), and before the code of the 
   * initialization pages of EJS.
   * Sample minimal code is : _name + " = new " + getConstructorName() + "();"
   * @param _name the name of the element as decided by EJS
   * @return
   */
  public String getInitializationCode(String _name);
  
  /**
   * Returns the code required (if any) to quit the element in an orderly way, 
   * when the simulation frees resources before quitting. 
   * May be empty "" and may span more than one line.
   * @param _name the name of the element as decided by EJS
   * @return
   */
  public String getDestructionCode(String _name);
  
  /**
   * Returns a semicolon ';' separated list of imports to be added to the source code
   * May be empty "" or null if the element requires no imports
   * @return
   */
  public String getImportStatements();

  /**
   * Returns a semicolon ';' separated list of resources (files) required by the element at run-time
   * May be empty "" or null if the element requires no resources
   * @return
   */
  public String getResourcesRequired();
  
  /**
   * Returns a semicolon ';' separated list of resources (files) required by minijar to package the element into the simulation's jar 
   * This is typically empty "" or null if the element requires no resources other than class files, that minijar can discover by itself.
   * But you may need to specify other files that minijar cannot discover, such as GIF files.
   * The use of minijar wildcards is allowed.
   * @return
   */
  public String getPackageList();

  /**
   * Returns optional information to display following the name of the element.
   * EJS uses it on the list of model elements
   * @return
   */
  public String getDisplayInfo();
  
  /**
   * Returns the configuration of the element in XML form. Will be used by EJS to save the element to file
   * @return
   */
  public String savetoXML();
  
  /**
   * Reads the configuration of the element from XML form (as created by saveToXML())
   * @return
   */
  public void readfromXML(String _inputXML);
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  /**
   * Returns a short description of the element.
   * Displayed by EJS when you linger on the element
   * @return
   */
  public String getTooltip();
  
  /**
   * The element has been destroyed. Clean the screen.
   */
  public void clear();
  
  /**
   * Used whenever EJS changes the font for code editors
   * @param font
   */
  public void setFont(java.awt.Font font);
  
  /**
   * Used by EJS to display the help for the element's class
   * @param parentComponent the parent component to locate the editor relative to
   */
  public void showHelp(java.awt.Component parentComponent);

  /**
   * Used by EJS to display the editor of the particular element
   * @param parentComponent the parent component to locate the editor relative to
   * @param _name the name of the element as decided by EJS
   * @param list The model element list you can ask for information
   */
  public void showEditor(String _name, java.awt.Component parentComponent, ModelElementsCollection list);
  
  /**
   * Used by EJS whenever the model variables change
   * @param _name the name of the element as decided by EJS
   */
  public void refreshEditor(String _name);
  
  /**
   * Searches the string in the element's editor.
   * @param info String Information passed to the editor. Just pass it down to the ModelElementSearch created
   * @param searchString String The string to search for
   * @param mode int The search mode (org.colos.ejs.osejs.edition.SearchResult.CASE_SENSITIVE or org.colos.ejs.osejs.edition.SearchResult.CASE_INSENSITIVE)
   * @param name String The name given to this element
   * @param collection ModelElementsCollection The model element collection you can ask for information
   * @return List A list of ModelElementSearch objects. Null if not found, or not implemented.
   */
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String elementName, ModelElementsCollection collection);

}

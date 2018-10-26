/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) July 2005 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

/**
 * The interface that a Moodle connection will need.
 */

public interface MoodleLink {

// ----------------------------------------
// Utilities
// ----------------------------------------

  /**
   * Whether there is a valid connection to a server.
   * @return boolean
   */
  public boolean isConnected ();

  /**
   * Sets the parent component of any subsequent message window such as
   * a JOptionPane.
   * @param _component java.awt.Component
   */
  public void setParentComponent (java.awt.Component _component);

  /**
   * Sets the name label for any message window
   * @param _label String
   */
  public void setNameLabel (String _label);

  /**
   * Sets the annotation label for any message window
   * @param _label String
   */
  public void setAnnotationLabel (String _label);

// ----------------------------------------
// Saving
// ----------------------------------------

  /**
   * Saves a binary data to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _data byte[]
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveBinary (String _filename, String _annotation, byte[] _data);
  
  /**
   * Saves an image to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _image Image
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveImage (String _filename, String _annotation, java.awt.Image _image);
  
  /**
   * Saves a text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _text String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveText (String _filename, String _annotation, String _text);

  /**
   * Saves a XML text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _xml String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveXML (String _filename, String _annotation, String _xml);

// ----------------------------------------
// Reading
// ----------------------------------------

  public byte[] readBinary (String _ext);

  public String readText (String _filename);

  public String readText (String _filename, String _type);

  public String readXML (String _filename);

  public java.awt.Image readImage (String _ext);

} // End of class

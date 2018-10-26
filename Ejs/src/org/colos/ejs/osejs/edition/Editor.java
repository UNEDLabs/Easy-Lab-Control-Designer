/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejss.xml.SimulationXML;

public interface Editor {
  static public final int GENERATE_CODE         = 1;
  static public final int GENERATE_DECLARATION  = 2;
  static public final int GENERATE_DESTRUCTION  = 3;
  //static public final int GENERATE_INFO         = 4;
  static public final int GENERATE_ON_EXIT      = 5;
  static public final int GENERATE_SOURCECODE   = 6;
  static public final int GENERATE_CHANGE_LOCALE= 7;
  static public final int GENERATE_PLAINCODE   = 8;

  static public final int GENERATE_VIEW_RESET               = 10;
  static public final int GENERATE_VIEW_INITIALIZE          = 11;
  static public final int GENERATE_VIEW_UPDATE              = 12;
  static public final int GENERATE_VIEW_LISTENERS           = 13;
  static public final int GENERATE_VIEW_READ                = 14;
  static public final int GENERATE_VIEW_READ_ONE            = 15;
//  static public final int GENERATE_RESET_CONTROL            = 16;
//  static public final int GENERATE_UPDATE_CONTROL           = 17;
  static public final int GENERATE_VIEW_UPDATE_BOOLEANS     = 16;
  static public final int GENERATE_VIEW_BLOCK_VARIABLES     = 17;
  static public final int GENERATE_VIEW_EXPRESSIONS         = 18; // 030418

  static public final int GENERATE_READ_STATE               = 20;
  static public final int GENERATE_SAVE_STATE               = 21;
  static public final int GENERATE_SIMULATION_STATE         = 22;
  static public final int GENERATE_RESET_SOLVER             = 23;
  static public final int GENERATE_AUTOMATIC_RESET_SOLVER   = 24;
  
  static public final int GENERATE_CAPTURE_WINDOW           = 30;
  static public final int GENERATE_OWNER_FRAME              = 31;
  static public final int GENERATE_MAIN_WINDOW              = 32;
  static public final int GENERATE_WINDOW_LIST              = 33;

  static public final int GENERATE_LIST_VARIABLES           = 41;
  static public final int GENERATE_LIST_ACTIONS             = 42;
  static public final int GENERATE_JSON_VARIABLES_PUBLIC    = 43;
  static public final int GENERATE_JSON_VARIABLES_IN        = 44;
  static public final int GENERATE_JSON_VARIABLES_OUT       = 45;
  static public final int GENERATE_JSON_ACTIONS             = 46;
  
  //static public final int GENERATE_JARS_NEEDED              = 50;
  static public final int GENERATE_RESOURCES_NEEDED         = 51;
  static public final int GENERATE_IMPORT_STATEMENTS        = 52;
  static public final int GENERATE_RESOURCES_NEEDED_BY_PACKAGE = 53;
  
  static public final int GENERATE_ENABLED_CONDITION = 70;
  static public final int GENERATE_CHANGE_ENABLED_CONDITION = 71;
  static public final int GENERATE_RESET_ENABLED_CONDITION = 72;
  static public final int GENERATE_ENABLED_MEMORY = 73;

  static public final int GENERATE_SERVER_DECLARATION  = 80;
  static public final int GENERATE_SERVER_CODE  = 81;
  static public final int GENERATE_SERVER_DUMMY_DECLARATION  = 82;
  static public final int GENERATE_SERVER_DUMMY_CODE  = 83;

  
  static public final String CODE_EDITOR      = "CODE_EDITOR";
  static public final String DESCRIPTION_EDITOR  = "DESCRIPTION_EDITOR";
  static public final String VARIABLE_EDITOR  = "VARIABLE_EDITOR";
  static public final String EVOLUTION_EDITOR = "EVOLUTION_EDITOR";
  static public final String ODE_EDITOR       = "ODE_EDITOR";
  static public final String HTML_EDITOR      = "HTML_EDITOR";
  static public final String HTML_EXTERNAL_EDITOR = "HTML_EXTERNAL_EDITOR";
  static public final String LIBRARY_EDITOR   = "LIBRARY_EDITOR";
  static public final String LIBRARY_EXTERNAL_EDITOR   = "LIBRARY_EXTERNAL_EDITOR";
  static public final String ACTION_EDITOR    = "ACTION_EDITOR";
  static public final String EVENT_EDITOR     = "EVENT_EDITOR";
  static public final String ERROR_EDITOR     = "ERROR_EDITOR";
  static public final String DISCONTINUITY_EDITOR     = "DISCONTINUITY_EDITOR";
  static public final String SCHEDULED_CONDITION_EDITOR = "SCHEDULED_CONDITION_EDITOR";
  static public final String SCHEDULED_EVENT_EDITOR     = "SCHEDULED_EVENT_EDITOR";
  static public final String ONEEXPERIMENT_EDITOR    = "ONEEXPERIMENT_EDITOR";
  static public final String HTML_VIEW_EDITOR    = "HTML_VIEW_EDITOR";
//  static public final String SIMINFO_EDITOR   = "SIMULATION_INFO";

 /**
  * Sets the name of the editor
  */
  public void setName (String _name);

 /**
  * Gets the name of the editor
  */
  public String getName();

 /**
  * Clears and initializes the editor
  */
  public void clear ();

 /**
  * Gets the visible panel of the editor
  */
  public java.awt.Component getComponent ();

 /**
  * Sets the color for the different parts of the editor
  */
  public void setColor (java.awt.Color _color);

 /**
  * Sets the font for the different parts of the editor
  */
  public void setFont (java.awt.Font _font);

  /**
   * Sets the font for the different parts of the editor
   */
   public void setZoomLevel (int level);

 /**
  * Whether the content of this editor has changed or not
  */
  public boolean isChanged ();

 /**
  * Sets the changed state of the content of this editor
  */
  public void setChanged (boolean _changed);

 /**
  * Whether the content of this editor is valid for edition and generation
  */
  public boolean isActive ();

 /**
  * Sets the active state of the content of this editor
  */
  public void setActive (boolean active);

  /**
   * Whether the content of this editor is valid for edition and generation
   */
   public boolean isInternal ();

  /**
   * Sets the active state of the content of this editor
   */
   public void setInternal (boolean internal);

  /**
   * Fills an EjsS XML structure
   * @param _simXML
   */
  public void fillSimulationXML(SimulationXML _simXML);

 /**
  * Generates code to be used in a Java file.
  * Inactive pages return null.
  * @param _type must be one of the class constants
  */
  public StringBuffer generateCode (int _type, String _info);

 /**
  * A string that will save the content
  * @see readString(String)
  */
  public StringBuffer saveStringBuffer ();

 /**
  * Reads the content from a String
  * @see saveString()
  *
  */
  public void readString (String _input);

  /**
   * Searches the string in the editor.
   * @param _searchString String The string to search for
   * @param _mode int The search mode (CASE_SENSITIVE or CASE_INSENSITIVE)
   * @return AbstractList A list of SearchResult objects
   */
  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode);

} // end of interface Editor

/*
 public void setName (String _name);
 public String getName();
 public void clear ();
 public java.awt.Component getComponent ();
 public void setColor (java.awt.Color _color);
 public void setFont (java.awt.Font _font);
 public boolean isChanged ();
 public void setChanged (boolean _changed);
 public boolean isActive ();
 public void setActive (boolean active);
 public StringBuffer generateCode (int _type, String _info);
 public StringBuffer saveStringBuffer ();
 public void readString (String _input);
*/


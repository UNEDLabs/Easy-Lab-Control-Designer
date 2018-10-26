/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) January 2008 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import javax.swing.text.JTextComponent;

/**
 * Provides information about the result of a search
 * @author F. Esquembre
 * Last revision: Jan 2008
 */
public class SearchResult {
  static public final int CASE_SENSITIVE = 1;
  static public final int CASE_INSENSITIVE = 2;
  static public final int WHOLE_WORD = 4;
  static public final int SEARCH_DESCRIPTION = 1;
  static public final int SEARCH_MODEL = 2;
  static public final int SEARCH_VIEW = 4;
  static public final int SEARCH_EXPERIMENTS = 8;
  static public final int SEARCH_HTML_VIEW = 16;

  protected String information, textFound; // The textual information to show to the user
  protected JTextComponent containerTextComponent; // The container where the search string was found
  protected int caretPosition, lineNumber;

  public SearchResult (String anInformation, String aText, JTextComponent aComponent, int aLineNumber, int aCaretPosition) {
    information = anInformation;
    textFound = aText;
    containerTextComponent = aComponent;
    lineNumber = aLineNumber;
    caretPosition = aCaretPosition;
  }

  public String toString () {
    return information+"("+lineNumber+"): "+textFound;
  }

  public void show () {
    if (containerTextComponent!=null) {
      containerTextComponent.requestFocusInWindow();
      containerTextComponent.setCaretPosition(caretPosition);
    }
  }

//  public JTextComponent getTextComponent() { return containerTextComponent; }
  
  public String getTextFound() { return textFound; }
  
  public int getLineNumber() { return lineNumber; }
  
  public int getCaretPosition() { return caretPosition; }

} // end of class SearchResult

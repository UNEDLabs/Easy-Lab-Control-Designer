package org.colos.ejs.osejs.edition.ode_editor;

/**
 * Holds information about a Variable in an EquationEditor
 * @author Paco
 *
 */
class EquationVariable {
  private boolean isArray; // true if the variable is an array
  private boolean followedByDerivative; // true if the variable is followed by its derivative in the table
  private int rowNumber; // The row of th etable where this variable appears
  private String name; // The plain name in case of arrays
  private String stateString;
  private String rateString;


  EquationVariable (String _name, int _rowNumber, boolean _isArray, String _state, String _rate) {
    this.name = _name;
    this.rowNumber = _rowNumber;
    this.isArray = _isArray;
    this.stateString = _state;
    if (_rate.endsWith(";")) _rate = _rate.substring(0,_rate.length()-1);
    this.rateString = _rate;

    this.followedByDerivative = false;
  }
  
  String getName() { return name; }
  
  String getStateString() { return stateString; }
  
  String getRateString() { return rateString; }
  
  int getRowNumber() { return rowNumber; }
  
  boolean isArray() { return isArray; }
  
  boolean isFollowedByDerivative () { return followedByDerivative; }
  
  void setFollowedByDerivative (boolean _followed) { this.followedByDerivative = _followed; }
  
}


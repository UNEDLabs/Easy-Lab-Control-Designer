// Copied literally from org.opensourcephysics.numerics for the sake of independence

package org.colos.ejs.library.control.value;

/**
 * Indicates that an error occured in parser operation, and the operation
 * could not be completed. Used internally in <code>Parser</code> class.
 *
 * @see Parser
 */

public final class ParserException extends Exception {
  static public final int SYNTAX_ERROR = -1;
  private int errorcode;

  /**
   * The constructor of <code>ParserException</code>.
   *
   * @param code the error code
   */

  public ParserException(int code) {
    super();
    errorcode = code;
  }

  public ParserException(String msg) {
    super(msg);
    errorcode = SYNTAX_ERROR;  // a generic  syntax error
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */

  public int getErrorCode() {
    return errorcode;
  }
}

/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

public class Resolution {
  static final public int DIVISIONS  = 0;
  static final public int MAX_LENGTH = 1;

  /**
   * The type of the resolution. Either DIVISIONS or MAX_LENGTH.
   *   <ul>
   *     <li>DIVISIONS implies that the Drawable3D must be divided in pieces according to the integer values provided.</li>
   *     <li>MAX_LENGTH provides a maximum length for the individual pieces of the Drawable3D.</li>
   *   </ul>
   *   It is, however, up to the Drawable3D to respect or not these indications.
   */
  protected int type=DIVISIONS;
  /**
   * The maximum length for the individual pieces of the Drawable3D
   */
  protected double maxLength = 1.0;
  /**
   * The value of the first resolution division. The actual meaning depends on the Drawable3D.
   */
  protected int n1=1;
  /**
   * The value of the second resolution division. The actual meaning depends on the Drawable3D.
   */
  protected int n2=1;
  /**
   * The value of the thrid resolution division. The actual meaning depends on the Drawable3D.
   */
  protected int n3=1;

  /**
   * A factory method for a resolution based on the maximum size of each piece
   * @param The maximum size.
   */
  static public Resolution createDivisions (double max) {
    Resolution res = new Resolution (0);
    res.type = MAX_LENGTH;
    res.maxLength = max;
    return res;
  }

  /**
   * A constructor for a resolution of type DIVISIONS.
   */
  public Resolution (int _n1) {
    type = DIVISIONS;
    this.n1 = _n1;
  }

  /**
   * A constructor for a resolution of type DIVISIONS.
   */
  public Resolution (int _n1, int _n2) {
    type = DIVISIONS;
    this.n1 = _n1;
    this.n2 = _n2;
  }

  /**
   * A constructor for a resolution of type DIVISIONS.
   */
  public Resolution (int _n1, int _n2, int _n3) {
    type = DIVISIONS;
    this.n1 = _n1;
    this.n2 = _n2;
    this.n3 = _n3;
  }

}



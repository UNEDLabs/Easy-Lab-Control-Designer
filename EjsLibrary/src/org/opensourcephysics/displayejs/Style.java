/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;

/**
 * A class that holds all the information possibly required by a Drawable3D
 * when the moment comes to display itself on the screen. Actual Drawables3D
 * may use or not all the information provided.
 */
public class Style {
  static final public int CENTERED    =  0;
  static final public int NORTH       =  1;
  static final public int SOUTH       =  2;
  static final public int EAST        =  3;
  static final public int WEST        =  4;
  static final public int NORTH_EAST  =  5;
  static final public int NORTH_WEST  =  6;
  static final public int SOUTH_EAST  =  7;
  static final public int SOUTH_WEST  =  8;

  static final public float[] DASHED_STROKE = {5,5};

  /**
   * The drawable to which it belongs. This is needed to report to it any change that
   * implies a call to needsToProject or needsToRecompute.
   */
  private Drawable3D drawable=null;

  /**
   * Indicates if the drawable should displace itself from the drawing point.
   * Standard values are provided as static class members. Default is CENTERED.
   */
  int position=CENTERED;
  /**
   * The (anti-clock wise) angle to rotate the drawable on the screen.
   */
  double angle=0.0, cosAngle=1.0, sinAngle=0.0;
  /**
   * The color used to display the lines in the drawable.
   */
  Color edgeColor=Color.black;
  /**
   * The Stroke to use for the lines in the drawable.
   */
  Stroke edgeStroke=new BasicStroke();
  /**
   * The color or pattern to use when filling areas of the drawable.
   */
  Paint fillPattern=Color.blue;
  /**
   * The font to use for the texts in the drawable.
   */
  Font font=null;
  /**
   * The object to display. It is up to the drawable to use this object
   * properly. For instance, InteractiveText uses toString(),
   * InteractiveImage will typecast it into an Image.
   */
  Object displayObject=null;

  public Style (Drawable3D _drawable) { this.drawable = _drawable; }

  public Style (Style _style) {
    drawable = _style.drawable;
    position = _style.position;
    angle = _style.angle; cosAngle = _style.cosAngle; sinAngle = _style.sinAngle;
    edgeColor = _style.edgeColor;
    edgeStroke = _style.edgeStroke;
    fillPattern = _style.fillPattern;
    font = _style.font;
    displayObject = _style.displayObject;
  }

  public void copyFrom (Style _style) {
    drawable = _style.drawable;
    position = _style.position;
    angle = _style.angle; cosAngle = _style.cosAngle; sinAngle = _style.sinAngle;
    edgeColor = _style.edgeColor;
    edgeStroke = _style.edgeStroke;
    fillPattern = _style.fillPattern;
    font = _style.font;
    displayObject = _style.displayObject;
  }

  public void setPosition (int _position) { this.position = _position; drawable.needsToProject(null); }
  public int getPosition () { return this.position; }

  public void setAngle (double _angle) { 
    this.angle = _angle; 
    cosAngle = Math.cos(angle); 
    sinAngle = Math.sin(angle); 
    drawable.needsToProject(null); 
  }
  public double getAngle () { return this.angle; }

  public void setEdgeColor (Color _color) { this.edgeColor = _color; }
  public Color getEdgeColor () { return this.edgeColor; }

  public void setEdgeStroke (Stroke _stroke) { this.edgeStroke = _stroke; }
  public Stroke getEdgeStroke () { return this.edgeStroke; }

  public void setFillPattern (Paint _pattern) { this.fillPattern = _pattern; }
  public Paint getFillPattern () { return this.fillPattern; }

  public void setFont (Font _font) { this.font = _font; drawable.needsToProject(null); }
  public Font getFont () { return this.font; }

  public void setDisplayObject (Object _object) { 
    this.displayObject = _object;
    drawable.needsToProject(null); 
  }
  public Object getDisplayObject () { return this.displayObject; }

}

/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.automaticcontrol;

import org.opensourcephysics.displayejs.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * This is an abstract class for a drawable consisting of two poligons and two texts.
 * The first polygon can be filled or hollow responding to a boolean variable.
 * The first text is typically a numeric value which (if enabled) can be modified by dragging the mouse.
 * The user can show/hide the first text.
 * The whole set can be rotated.
 *
 * Subclasses should implement the setGroupData() method which sets the data
 * for the poligons and the position of the texts.
 * This class provides a rotateData() method which rotates the data.
 *
 * @author Francisco Esquembre (fem@um.es)
 * @version 1.0
 */
public abstract class PoligonsAndTexts extends GroupDrawable implements InteractionListener {
  // Configuration variables
  protected boolean filled = false;
  protected int type = 0;
  protected double angle = 0.0, value = 0.0, increment = 0.1;
  protected java.awt.Paint fillPattern = null;
  protected java.text.DecimalFormat format= new java.text.DecimalFormat("0.000");

  // Implementation variables
  protected boolean hasChanged = false, showText=true, showText2=true;
  protected InteractivePoligon poligon, poligon2;
  protected InteractiveText text, text2;
  protected double textX = 0.0, textY = 0.0;
  protected double text2X = 0.0, text2Y = 0.0;
  protected double[][] data=null, data2=null;
  private double origX = 0, origY = 0, newX=0, newY=0;

  public PoligonsAndTexts() {
    super.add(poligon  = new InteractivePoligon());
    super.add(poligon2 = new InteractivePoligon());
    super.add(text  = new InteractiveText());
    super.add(text2 = new InteractiveText());
    poligon.setClosed(true);
    poligon.getStyle().setFillPattern(null);
    poligon2.setClosed(true);
    poligon2.getStyle().setFillPattern(null);
    text.getStyle().setFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,11));
    text2.getStyle().setFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,11));
    setXYZ(0.0, 0.0, 0.0);
    setSizeXYZ(1.0, 1.0, 1.0);

    setFilled (filled);
    setValue (value);
    setEnabled (true);
    setMovable(false);
    addListener(this);

    setGroupData();
    hasChanged = true;
  }

// -------------------------------------
// Configure the poligons
// -------------------------------------

  public void setType(int _type) {
    if (_type != type) {
      type = _type;
      setGroupData();
    }
  }

  public void setAngle(double _angle) {
    if (_angle != angle) {
      angle = _angle;
      setGroupData();
    }
  }

  public void setFilled(boolean _filled) {
    filled = _filled;
    if (_filled) poligon.getStyle().setFillPattern(fillPattern);
    else poligon.getStyle().setFillPattern(null);
  }

  public void setMovable(boolean _movable) {
    poligon.setEnabled(InteractiveElement.TARGET_POSITION, _movable);
    poligon2.setEnabled(InteractiveElement.TARGET_POSITION, _movable);
  }

  public void setLineColor(java.awt.Color _color) {
    poligon.getStyle().setEdgeColor(_color);
    poligon2.getStyle().setEdgeColor(_color);
  }

  public void setLineStroke(java.awt.Stroke _stroke) {
    poligon.getStyle().setEdgeStroke(_stroke);
    poligon2.getStyle().setEdgeStroke(_stroke);
  }

  public void setFillColor(java.awt.Paint _fill) {
    fillPattern = _fill;
    if (filled) poligon.getStyle().setFillPattern(fillPattern);
  }

  public void setFillColor2(java.awt.Paint _fill) {
    poligon2.getStyle().setFillPattern(_fill);
  }

  public void setVisible (boolean _visible) {
    super.setVisible(_visible);
    if (_visible) {
      text.setVisible(showText);
      text2.setVisible(showText2);
    }
  }

// ---------------------------------------
//   Configure the texts
// ---------------------------------------

  public InteractiveElement getText() { return text; }

  public void setShowText (boolean _show) {
    text.setVisible(showText=_show);
  }

  public void setShowSecondText (boolean _show) {
    text2.setVisible(showText2=_show);
  }

  public void setText (String _str) {
    text.getStyle().setDisplayObject(_str);
  }

  public void setText2 (String _str) {
    text2.getStyle().setDisplayObject(_str);
  }

  public void setTextColor(java.awt.Color _color) {
    text.getStyle().setEdgeColor(_color);
    text2.getStyle().setEdgeColor(_color);
  }

  public void setTextFont(java.awt.Font _font) {
    text.getStyle().setFont(_font);
    text2.getStyle().setFont(_font);
  }

// ---------------------------------------
//   Configure and use the value
// ---------------------------------------

  public void setEnabled(boolean _enabled) {
    text.setEnabled(InteractiveElement.TARGET_POSITION, _enabled);
  }
  public void setValue(double _val) {
    if (value == _val)return;
    value = _val;
    text.getStyle().setDisplayObject(format.format(value));
  }

  public double getValue() { return value; }

  public void setValueIncrement(double _val) { increment = _val; }

  public double getValueIncrement() { return increment; }

  public void setValueFormat(java.text.DecimalFormat _format) {
    if (format == _format)return;
    format = _format;
    text.getStyle().setDisplayObject(format.format(value));
  }

  public void interactionPerformed(InteractionEvent _event) {
    if ( _event.getTarget() instanceof InteractionTargetGroupDrawableElement &&
         ((InteractionTargetGroupDrawableElement)_event.getTarget()).getElement()==text) {
      switch (_event.getID()) {
        case InteractionEvent.MOUSE_PRESSED : origX = newX = getX(); origY = newY = getY(); break;
        case InteractionEvent.MOUSE_DRAGGED :
          double thisX = this.getX(), thisY = this.getY();
          if (Math.abs(thisX-newX)>Math.abs(thisY-newY)) value += ((thisX-newX)/getRadius()/getSizeX())*increment;
          else value += ((thisY-newY)/getRadius()/getSizeY())*increment;
//          if (value<0) value = 0;
//          else if (value>1) value = 1;
          setXY(origX, origY);
          newX = thisX; newY = thisY;
          text.getStyle().setDisplayObject(format.format(value));
          break;
      }
    }
  }

// --------------------------------
// Private methods
// --------------------------------

  /**
   * Must provide an estimate of the size of the element in any directions.
   * Used internally to change the value when dragging the mouse.
   * @return double
   */
  abstract protected double getRadius();

  /**
   * Must set the data for the poligons and the position of the texts
   */
  abstract protected void setGroupData();

  protected void rotateData () {
    double a = Math.cos(angle), b = Math.sin(angle),auxX;
    if (data!=null) for (int i = 0, n = data.length; i < n; i++) {
      auxX = a * data[i][0] - b * data[i][1];
      data[i][1] = b * data[i][0] + a * data[i][1];
      data[i][0] = auxX;
    }
    if (data2!=null) for (int i = 0, n = data2.length; i < n; i++) {
      auxX = a * data2[i][0] - b * data2[i][1];
      data2[i][1] = b * data2[i][0] + a * data2[i][1];
      data2[i][0] = auxX;
    }
    auxX = a * textX - b * textY;
    textY = b * textX + a * textY;
    textX = auxX;
    auxX = a * text2X - b * text2Y;
    text2Y = b * text2X + a * text2Y;
    text2X = auxX;
  }

}

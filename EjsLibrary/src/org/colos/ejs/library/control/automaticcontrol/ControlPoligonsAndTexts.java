/*
 * The control.automaticcontrol package contains subclasses of
 * control.ControlElement that are sueful for Automatic Control
 * Copyright (c) December 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.displayejs.ControlDrawingPanel3D;
import org.colos.ejs.library.control.displayejs.ControlGroupDrawable;
import org.colos.ejs.library.control.displayejs.ControlInteractiveElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.automaticcontrol.PoligonsAndTexts;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.displayejs.InteractionListener;
import org.opensourcephysics.displayejs.InteractionEvent;
import org.opensourcephysics.displayejs.InteractionTargetGroupDrawableElement;
import java.awt.BasicStroke;
import org.opensourcephysics.displayejs.Style;

/**
 * A generic PoligonsAndTexts
 */
public abstract class ControlPoligonsAndTexts extends ControlGroupDrawable implements InteractionListener {
  static protected final int PAT_ADDED = 20;
  static protected final int VALUE = 12;
  static protected final int FILLCOLOR2 = 6;


  private DoubleValue value;
  private double lineWidth=Double.NaN;
  protected PoligonsAndTexts pat;

  public ControlPoligonsAndTexts () {
    super ();
    pat = (PoligonsAndTexts) myGroupDrawable;
    value = new DoubleValue (pat.getValue());
    pat.addListener(this);
  }

  protected abstract Drawable createDrawable ();

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      // For the poligons
      infoList.add ("type");
      infoList.add ("angle");
      infoList.add ("lineColor");
      infoList.add ("lineStroke");
      infoList.add ("filled");
      infoList.add ("fillColor");
      infoList.add ("fillColor2");
      // Texts
      infoList.add ("text");
      infoList.add ("text2");
      infoList.add ("showText");
      infoList.add ("textFont");
      infoList.add ("textColor");
      // Value
      infoList.add ("value");
      infoList.add ("valueFormat");
      infoList.add ("valueIncrement");
      // Interaction
      infoList.add ("movable");
      infoList.add ("enabled");
      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("angle")) return "rotationAngle";
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    if (_property.equals("action"))      return "releaseAction";
    if (_property.equals("movable"))     return "enabledPosition";
    if (_property.equals("filled"))          return "drawingFill";
    if (_property.equals("valueFormat"))     return "format";
    if (_property.equals("textFont"))     return "font";
    if (_property.equals("type"))     return "style";
    if (_property.equals("lineStroke"))     return "stroke";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("type"))         return "int";
    if (_property.equals("angle"))        return "int|double";
    if (_property.equals("lineColor"))    return "Color|Object";
    if (_property.equals("lineStroke"))   return "int|double|Object";
    if (_property.equals("filled"))       return "boolean";
    if (_property.equals("fillColor"))    return "Color|Object";
    if (_property.equals("fillColor2"))   return "Color|Object";

    if (_property.equals("text"))         return "String TRANSLATABLE";
    if (_property.equals("text2"))        return "String TRANSLATABLE";
    if (_property.equals("showText"))     return "boolean";
    if (_property.equals("textFont"))     return "Font|Object";
    if (_property.equals("textColor"))    return "Color|Object";

    if (_property.equals("value"))          return "int|double";
    if (_property.equals("valueFormat"))    return "Format|Object|String TRANSLATABLE";
    if (_property.equals("valueIncrement")) return "int|double";

    if (_property.equals("movable"))      return "boolean";
    if (_property.equals("enabled"))      return "boolean";
    if (_property.equals("pressaction"))  return "Action CONSTANT";
    if (_property.equals("dragaction"))   return "Action CONSTANT";
    if (_property.equals("action"))       return "Action CONSTANT";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : pat.setType(_value.getInteger()); break;
      case 1 :
        if (_value instanceof IntegerValue)
             pat.setAngle(_value.getInteger()*ControlDrawingPanel3D.TO_RAD);
        else pat.setAngle(_value.getDouble());
        break;
      case 2 : pat.setLineColor((java.awt.Color)_value.getObject()); break;
      case 3 :
        if (_value.getObject() instanceof java.awt.Stroke) pat.setLineStroke((java.awt.Stroke) _value.getObject());
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          if (lineWidth<0) pat.setLineStroke(new java.awt.BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0) );
          else pat.setLineStroke(new java.awt.BasicStroke((float) lineWidth));
        }
        break;
      case 4 : pat.setFilled(_value.getBoolean()); break;
      case 5 : pat.setFillColor((java.awt.Color)_value.getObject()); break;
      case FILLCOLOR2 : pat.setFillColor2((java.awt.Color)_value.getObject()); break;

      case  7 : pat.setText(_value.getString()); break;
      case  8 : pat.setText2(_value.getString()); break;
      case  9 : pat.setShowText(_value.getBoolean()); break;
      case 10 : pat.setTextFont((java.awt.Font) _value.getObject());  break;
      case 11 : pat.setTextColor((java.awt.Color) _value.getObject());  break;

      case VALUE : pat.setValue(value.value=_value.getDouble()); break;
      case 13 :
        if (_value.getObject() instanceof java.text.DecimalFormat) pat.setValueFormat((java.text.DecimalFormat) _value.getObject());
        else pat.setValueFormat((java.text.DecimalFormat) ConstantParser.formatConstant(_value.getString()).getObject());
        break;
      case 14 : pat.setValueIncrement(_value.getDouble()); break;

      case 15 : pat.setMovable(_value.getBoolean()); break;
      case 16 : pat.setEnabled(_value.getBoolean()); break;
      case 17 : // pressaction
        removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlInteractiveElement.ACTION_PRESS,_value.getString());
        return;
      case 18 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        return;
      case 19 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        return;

      default: super.setValue(_index-PAT_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : pat.setType(0); break;
      case 1 : pat.setAngle(0.0); break;
      case 2 : pat.setLineColor(java.awt.Color.BLACK); break;
      case 3 : pat.setLineStroke(new java.awt.BasicStroke()); break;
      case 4 : pat.setFilled(false); break;
      case 5 : pat.setFillColor(null); break;
      case FILLCOLOR2 : pat.setFillColor2(null); break;

      case  7 : pat.setText(""); break;
      case  8 : pat.setText2(""); break;
      case  9 : pat.setShowText(true); break;
      case 10 : pat.setTextFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));  break;
      case 11 : pat.setTextColor(java.awt.Color.BLACK);  break;

      case VALUE : pat.setValue(value.value=0.0); break;
      case 13 : pat.setValueFormat(new java.text.DecimalFormat("0.000;-0.000")); break;
      case 14 : pat.setValueIncrement(0.1); break;

      case 15 : pat.setMovable(false); break;
      case 16 : pat.setEnabled(false); break;
      case 17 : removeAction (ControlInteractiveElement.ACTION_PRESS,getProperty("pressaction")); break;
      case 18 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));         break;
      case 19 : removeAction (ControlElement.ACTION,getProperty("action"));                      break;

      default: super.setDefaultValue(_index-PAT_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "0";
      case 2 : return "BLACK";
      case 3 : return "1";
      case 4 : return "false";
      case 5 : return "null";
      case FILLCOLOR2 : return "null";

      case  7 :
      case  8 : return "<none>";
      case  9 : return "true";
      case 10 : return "Dialog,PLAIN,12";
      case 11 : return "BLACK";

      case VALUE : return "0";
      case 13 : return "0.000;-0.000";
      case 14 : return "0.1";

      case 15 :
      case 16 : return "false";
      case 17 :
      case 18 :
      case 19 : return "<no_action>";
      default : return super.getDefaultValueString(_index-PAT_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case  0 : case  1 : case  2 : case  3 :
      case  4 : case  5 : case  FILLCOLOR2 : case  7 :
      case  8 : case  9 : case 10 : case 11 :
                case 13 : case 14 : case 15 :
      case 16 : case 17 : case 18 : case 19 :
        return null;
      case VALUE : return value;
      default: return super.getValue (_index-PAT_ADDED);
    }
  }

// -------------------------
// Interaction
// -------------------------

    static private final int[] posSpot = {POSITION_X+PAT_ADDED,
                                          POSITION_Y+PAT_ADDED,
                                          POSITION_Z+PAT_ADDED};
    int[] getPosSpot ()  { return posSpot; }
    int getValueIndex ()  { return VALUE; }

    @SuppressWarnings("fallthrough")
    public void interactionPerformed(InteractionEvent _event) {
      switch (_event.getID()) {
        case InteractionEvent.MOUSE_EXITED :
          invokeActions (ControlInteractiveElement.ACTION_EXITED);
          break;
        case InteractionEvent.MOUSE_ENTERED :
          invokeActions (ControlInteractiveElement.ACTION_ENTERED);
          break;
        case InteractionEvent.MOUSE_PRESSED :
          if (myParent instanceof ControlDrawingPanel3D) ((ControlDrawingPanel3D) myParent).setSelectedDrawable (this);
          invokeActions (ControlInteractiveElement.ACTION_PRESS);
          // Do not break!
        case InteractionEvent.MOUSE_DRAGGED :
          if (((InteractionTargetGroupDrawableElement)_event.getTarget()).getElement()==pat.getText()) {
            value.value = pat.getValue();
            variableChanged(getValueIndex (), value);
            if (isUnderEjs) setFieldListValue(getValueIndex (), value);
          }
          else {
            posValues[0].value = pat.getX();
            posValues[1].value = pat.getY();
            posValues[2].value = pat.getZ();
            variablesChanged(getPosSpot(), posValues);
            if (isUnderEjs) setFieldListValues(getPosSpot(), posValues);
          }
          break;
        case InteractionEvent.MOUSE_RELEASED :
          if (myParent instanceof ControlDrawingPanel3D) ((ControlDrawingPanel3D) myParent).setSelectedDrawable (null);
          invokeActions (ControlElement.ACTION);
          break;
      }
    } // End of interaction method

} // End of class

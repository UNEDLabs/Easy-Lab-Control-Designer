/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing2d;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing2d.*;
import org.opensourcephysics.drawing2d.interaction.*;
import org.opensourcephysics.tools.ToolForData;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public abstract class ControlSet2D extends ControlElement2D {
  static protected final int SET2D_ADDED = 2;
  
  static protected final int NUM_ELEMENTS = 0;
  static protected final int ELEMENT_SELECTED = 1;

  private Set set;
  protected Element[] elements;
  private int numElements=0;

  protected ObjectValue[] allposValues, allsizesValues;
  protected IntegerValue selectedValue = new IntegerValue(-1);

  protected double[] theXs, theYs;
  protected double[] theSizeXs, theSizeYs;
  protected double[][] allThePos, allTheSizes;
  protected double allScalex = 1.0, allScaley = 1.0, allLineWidth = 1.0;
  protected boolean  numberOfElements_isSet = false;
  private boolean propagatingValue = false;

  protected double defaultElementX, defaultElementY,defaultElementSizeX, defaultElementSizeY;
  protected Color defElementLines;
  protected java.awt.Paint defElementFill;
  
  public ControlSet2D () {
    super ();
    checkNumberOfElements(1,true);
    defaultElementX = elements[0].getX();
    defaultElementY = elements[0].getY();
    defaultElementSizeX = elements[0].getSizeX();
    defaultElementSizeY = elements[0].getSizeY();
    defElementLines = elements[0].getStyle().getLineColor();
    defElementFill = elements[0].getStyle().getFillColor();
  }

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.Set"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    set = new Set();
    return set;
  }

  abstract protected Element createAnElement();

  protected Set getSet() { return set; }
  
  /**
   * This tells how many properties have been added on top of 
   * those of the basic ControlSet2D class
   */
  protected abstract int getPropertiesAddedToSet ();

  protected final int getPropertiesDisplacement () {
    return getPropertiesAddedToSet () + SET2D_ADDED;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
    oldElement.getStyle().copyTo(newElement);

    newElement.setDataObject(oldElement.getDataObject());
    newElement.setPanel(oldElement.getPanel());
    newElement.setName(oldElement.getName());
    newElement.setXY(oldElement.getX(),oldElement.getY());
    newElement.setSizeXY(oldElement.getSizeX(),oldElement.getSizeY());
    newElement.setVisible(oldElement.isVisible());
    newElement.setCanBeMeasured(oldElement.getCanBeMeasured());
    newElement.setTransformation(oldElement.getTransformation());

    newElement.getInteractionTarget(Element.TARGET_POSITION).setEnabled(oldElement.getInteractionTarget(Element.TARGET_POSITION).getEnabled());
    newElement.getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(oldElement.getInteractionTarget(Element.TARGET_POSITION).getAffectsGroup());
    newElement.getInteractionTarget(Element.TARGET_SIZE).setEnabled(oldElement.getInteractionTarget(Element.TARGET_SIZE).getEnabled());
    newElement.getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(oldElement.getInteractionTarget(Element.TARGET_SIZE).getAffectsGroup());
  }

  protected final void checkNumberOfElements(int newNumber, boolean force) {
    if (newNumber<0) newNumber = 0;
    if (numElements==newNumber && !force) return;
    setNumberOfElements (newNumber);
    org.colos.ejs.library.control.EjsControl controlGroup = getGroup();
    if (controlGroup!=null && !controlGroup.isReportingChange() && !propagatingValue) {
      propagatingValue = true; // avoid infinite recursion
      controlGroup.propagateValues(); // Required in case the number of elements is set AFTER other values, such as the position
      propagatingValue = false;
    }
  }
  
  /**
   * Sets the number of elements to the given number.
   * May be overwritten by some elements (see f.i. ControlTrail2D)
   * @param newNumber
   */
  protected int setNumberOfElements(int proposedNumber) {
    // Keep original settings for the new elements
    Element[] oldElements = elements;
    int newNumber = (proposedNumber>0) ? proposedNumber : 1;
    elements = new Element[newNumber];
    for (int i = 0; i < newNumber; i++) {
      elements[i] = createAnElement();
      Element oldElement = null;
      if (i<numElements) oldElement = oldElements[i];
      else if (oldElements!=null) oldElement = oldElements[0];
      if (oldElement!=null) copyAnElement (oldElement,elements[i]);
      elements[i].setName(set.getName()+"["+i+"]");
    }
    // Now rebuild the set
    set.removeAllElements();
    if (proposedNumber>0) for (int i = 0; i < newNumber; i++) set.addElement(elements[i]);

    theXs     = new double[newNumber]; 
    theYs     = new double[newNumber];
    theSizeXs = new double[newNumber]; 
    theSizeYs = new double[newNumber];
    for (int i = 0; i < newNumber; i++) {
      theXs[i]     = elements[i].getX();
      theYs[i]     = elements[i].getY();
      theSizeXs[i] = elements[i].getSizeX(); 
      theSizeYs[i] = elements[i].getSizeY();
      elements[i].addInteractionListener(this);
    }
    allposValues      = new ObjectValue[2];
    allsizesValues    = new ObjectValue[2];
    allposValues[0]   = new ObjectValue(theXs);
    allposValues[1]   = new ObjectValue(theYs);
    allsizesValues[0] = new ObjectValue(theSizeXs);
    allsizesValues[1] = new ObjectValue(theSizeYs); 
    // Ready to go
    numElements = proposedNumber;
    oldElements=null; // Make (double) sure the old elements go to the garbage collector
    return newNumber;
  }

  public Element elementAt (int i) { return elements[i]; }

  public void reset () { // Overwrites default reset
    set.clear();
  }

  public void initialize () { // Overwrites default initialize
    set.initialize();
  }

  public void setName(String name) {
    super.setName(name);
    for (int i=0,n=elements.length; i<n; i++) elements[i].setName(name+"["+i+"]");
  }
  
  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("numberOfElements");
      infoList.add ("elementSelected");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("numberOfElements")) return "int PREVIOUS";
    if (_property.equals("elementSelected"))  return "int";

    if (_property.equals("x"))        return "int|double|double[]";
    if (_property.equals("y"))        return "int|double|double[]";
    if (_property.equals("position")) return "double[][]";
    if (_property.equals("sizeX"))    return "int|double|double[]";
    if (_property.equals("sizeY"))    return "int|double|double[]";
    if (_property.equals("size"))     return "double[][]";
    if (_property.equals("scalex"))   return "int|double";
    if (_property.equals("scaley"))   return "int|double";
    if (_property.equals("transformation")) return "AffineTransform|double|double[]|Object|Object[]"; 

    if (_property.equals("visible"))      return "boolean|boolean[]";
    if (_property.equals("lineColor"))    return "int|int[]|Color|Object|Object[]";
    if (_property.equals("lineWidth"))    return "int|double|double[]";
    if (_property.equals("fillColor"))    return "int|int[]|Color|Object|Object[]";
    if (_property.equals("drawingFill"))  return "boolean|boolean[]";
    if (_property.equals("drawingLines")) return "boolean|boolean[]";

    if (_property.equals("enabledPosition")) return "Interaction2D|int|int[]|boolean|boolean[]";
    if (_property.equals("movesGroup"))      return "boolean|boolean[]";
    if (_property.equals("enabledSize"))     return "Interaction2D|int|int[]|boolean|boolean[]";
    if (_property.equals("resizesGroup"))    return "boolean|boolean[]";
    if (_property.equals("sensitivity"))     return "int|int[]";

    if (_property.equals("pressAction"))     return "Action CONSTANT";
    if (_property.equals("dragAction"))      return "Action CONSTANT";
    if (_property.equals("releaseAction"))   return "Action CONSTANT";
    if (_property.equals("enteredAction"))   return "Action CONSTANT";
    if (_property.equals("exitedAction"))    return "Action CONSTANT";

    if (_property.equals("measured"))        return "boolean|boolean[]";
    if (_property.equals("extraColor"))    return "int|int[]|Color|Object|Object[]";


    return super.getPropertyInfo(_property);
  }

  @Override
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    
    if (_value!=null) {
      boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
      if      (_property.equals("x")) set.setXLabel(useDefaultTitle ? "x" : _value);
      else if (_property.equals("y")) set.setYLabel(useDefaultTitle ? "y" : _value);
    }
    
    return super.setProperty(_property,_value);
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),set));
  }
  
// ------------------------------------------------
// Variables
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case NUM_ELEMENTS : 
          if (_value.getInteger()!=numElements) checkNumberOfElements(_value.getInteger(),true);
          numberOfElements_isSet = true;
          return;
        case ELEMENT_SELECTED : selectedValue.value = _value.getInteger(); return;

      }
      
      // I cannot just call super.setValue() because the actions are different
      _index -= SET2D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : 
          allThePos = null; // Not set using the POSITION property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setX(theXs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<elements.length; i++) elements[i].setX(theXs[i]=val);
          }
          break;
        case POSITION_Y : 
          allThePos = null; // Not set using the POSITION property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setY(theYs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<elements.length; i++) elements[i].setY(theYs[i]=val);
          }
          break;
        case POSITION :
          if (_value.getObject() instanceof double[][]) {
            allThePos = (double[][]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (allThePos.length,false);
            for (int i=0, n=Math.min(elements.length,allThePos.length); i<n; i++) {
              double[] point = allThePos[i];
              elements[i].setXY(theXs[i] = point[0],theYs[i] = point[1]);
            }
          }
          break;
          
        case SIZE_X :
          allTheSizes = null; // Not set using the SIZE property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setSizeX((theSizeXs[i] = val[i])*allScalex);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<elements.length; i++) elements[i].setSizeX((theSizeXs[i]=val)*allScalex);
          }
          break;
        case SIZE_Y : // Not set using the SIZE property
          allTheSizes = null;
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setSizeY((theSizeYs[i] = val[i])*allScaley);
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<elements.length; i++) elements[i].setSizeY((theSizeYs[i]=val)*allScaley);
          }
          break;
        case SIZE :
          if (_value.getObject() instanceof double[][]) {
            allTheSizes = (double[][]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (allTheSizes.length,false);
            for (int i=0, n=Math.min(elements.length,allTheSizes.length); i<n; i++) {
              double[] size = allTheSizes[i];
              elements[i].setSizeXY((theSizeXs[i] = size[0])*allScalex,(theSizeYs[i] = allTheSizes[i][1])*allScaley);
            }
          }
          break;
        case SCALE_X : 
          allScalex = _value.getDouble(); 
          for (int i=0; i<elements.length; i++) elements[i].setSizeX(theSizeXs[i]*allScalex);
          break;
        case SCALE_Y : 
          allScaley = _value.getDouble(); 
          for (int i=0; i<elements.length; i++) elements[i].setSizeY(theSizeYs[i]*allScaley);
          break;

        case TRANSFORMATION :
          if (_value.getObject() instanceof AffineTransform) {
            AffineTransform theTransform = (AffineTransform) _value.getObject();
            for (int i=0; i<elements.length; i++) elements[i].setTransformation(theTransform);
          }
          else if (_value.getObject() instanceof double[]) {
            double[] array = (double[]) _value.getObject();
            for (int i=0,n=Math.min(array.length,elements.length); i<n; i++) elements[i].setTransformation(AffineTransform.getRotateInstance(array[i]));
          }
          else if (_value instanceof DoubleValue || _value instanceof InterpretedValue) {
            AffineTransform theTransform = AffineTransform.getRotateInstance(_value.getDouble());
            for (int i=0; i<elements.length; i++) elements[i].setTransformation(theTransform);
          }
          else if (_value.getObject() instanceof AffineTransform[]) {
            AffineTransform[] arrayTransform = (AffineTransform[]) _value.getObject();
            for (int i=0,n=Math.min(arrayTransform.length,elements.length); i<n; i++) elements[i].setTransformation(arrayTransform[i]);
          }
          else {
            AffineTransform theTransform = decodeAffineTransform(_value.getString());
            for (int i=0; i<elements.length; i++) elements[i].setTransformation(theTransform);
          }
          break;

        case VISIBLE :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setVisible(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].setVisible(val);
          }
          break;
        case LINE_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setLineColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setLineColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setLineColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setLineColor(val);
          }
          break;
        case LINE_WIDTH :
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setLineWidth((float)val[i]);
          }
          else {
            float val = (float) _value.getDouble();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setLineWidth(val);
          }
          break;
        case FILL_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setFillColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setFillColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setFillColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setFillColor(val);
          }
          break;
        case DRAWING_FILL :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setDrawingFill(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setDrawingFill(val);
          }
          break;
        case DRAWING_LINES :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setDrawingLines(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setDrawingLines(val);
          }
          break;

        case ENABLED_POSITION :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val[i]);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val[i]);
          }
          else if (_value instanceof BooleanValue) {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val);
          }
          break;
        case MOVES_GROUP      :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(val);
            }
            break;
        case ENABLED_SIZE     :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val[i]);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val[i]);
          }
          else if (_value instanceof BooleanValue) {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val);
          }
          break;
        case RESIZES_GROUP    :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(val);
            }
            break;

        case SENSITIVITY      : 
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setSensitivity(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<elements.length; i++) elements[i].getStyle().setSensitivity(val);
        }
        break;

        case MEASURED : 
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].setCanBeMeasured(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0; i<elements.length; i++) elements[i].setCanBeMeasured(val);
          }
          break;
        case EXTRA_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setExtraColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setExtraColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) elements[i].getStyle().setExtraColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<elements.length; i++) elements[i].getStyle().setExtraColor(val);
          }
          break;

        default : super.setValue(_index,_value); break;
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case NUM_ELEMENTS : checkNumberOfElements(1,true); numberOfElements_isSet = false; return;
        case ELEMENT_SELECTED : selectedValue.value = -1; return;
      }
      
      // I cannot just call super.setValue() because the actions are different
      _index -= SET2D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : for (int i=0; i<elements.length; i++) elements[i].setX(theXs[i]=defaultElementX); break;
        case POSITION_Y : for (int i=0; i<elements.length; i++) elements[i].setY(theYs[i]=defaultElementY); break;
        case POSITION :   allThePos = null; break;
          
        case SIZE_X : for (int i=0; i<elements.length; i++) elements[i].setSizeX((theSizeXs[i]=defaultElementSizeX)*allScalex); break;
        case SIZE_Y : for (int i=0; i<elements.length; i++) elements[i].setSizeY((theSizeYs[i]=defaultElementSizeY)*allScaley); break;
        case SIZE :   allTheSizes = null; break;

        case SCALE_X : 
          allScalex = 1.0; 
          for (int i=0; i<elements.length; i++) elements[i].setSizeX(theSizeXs[i]*allScalex);
          break;
        case SCALE_Y : 
          allScaley = 1.0; 
          for (int i=0; i<elements.length; i++) elements[i].setSizeY(theSizeYs[i]*allScaley);
          break;

        case TRANSFORMATION : for (int i=0; i<elements.length; i++) elements[i].setTransformation(null); break;

        case VISIBLE : for (int i=0; i<elements.length; i++) elements[i].setVisible(true); break;
        case LINE_COLOR : for (int i=0; i<elements.length; i++) elements[i].getStyle().setLineColor(defElementLines); break;
        case LINE_WIDTH : for (int i=0; i<elements.length; i++) elements[i].getStyle().setLineWidth(1.0f); break;
        case FILL_COLOR : for (int i=0; i<elements.length; i++) elements[i].getStyle().setFillColor(defElementFill); break;
        case DRAWING_FILL  : for (int i=0; i<elements.length; i++) elements[i].getStyle().setDrawingFill(true);  break;
        case DRAWING_LINES : for (int i=0; i<elements.length; i++) elements[i].getStyle().setDrawingLines(true); break;

        case ENABLED_POSITION : for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(false); break;
        case MOVES_GROUP      : for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(false); break;
        case ENABLED_SIZE     : for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(false); break;
        case RESIZES_GROUP    : for (int i=0; i<elements.length; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(false); break;

        case SENSITIVITY : for (int i=0; i<elements.length; i++) elements[i].getStyle().setSensitivity(Style.DEFAULT_SENSITIVITY); break;

        case MEASURED : for (int i=0; i<elements.length; i++) elements[i].setCanBeMeasured(true); break;
        case EXTRA_COLOR : for (int i=0; i<elements.length; i++) elements[i].getStyle().setExtraColor(defExtraColor); break;

        default : super.setDefaultValue(_index); break;
      }

    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case NUM_ELEMENTS : return "1";
        case ELEMENT_SELECTED : return "-1";
      }

      // I cannot just call super.setValue() because the actions are different
      _index -= SET2D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : return Double.toString(defaultElementX);
        case POSITION_Y : return Double.toString(defaultElementY);
        case SIZE_X : return Double.toString(defaultElementSizeX);
        case SIZE_Y : return Double.toString(defaultElementSizeY);
        case LINE_COLOR : return defElementLines.toString();
        case FILL_COLOR : return defElementFill.toString();

        default : return super.getDefaultValueString(_index);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case NUM_ELEMENTS : return null;
        case ELEMENT_SELECTED : return selectedValue;
      }

      // I cannot just call super.setValue() because the actions are different
      _index -= SET2D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : return allposValues[0];
        case POSITION_Y : return allposValues[1];
        case POSITION : return new ObjectValue(allThePos); 
        case SIZE : return new ObjectValue(allTheSizes); 
        case SIZE_X : return allsizesValues[0];
        case SIZE_Y : return allsizesValues[1];
        default: return super.getValue(_index);
      }
    }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  protected void propagatePosition (ControlElement2D origin,int _index) {
    theXs[_index] = elements[_index].getX();
    theYs[_index] = elements[_index].getY();
    if (allThePos!=null) {
      allThePos[_index][0] = theXs[_index];
      allThePos[_index][1] = theYs[_index];
      Value objVal = new ObjectValue(allThePos);
      variableChanged (getFullPositionSpot(),objVal);
      if (this!=origin) origin.variableChanged (origin.getFullPositionSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullPositionSpot(),objVal);
    }
    else {
      //int[] posSpot = getPosSpot();
      //double[] array = (double[])allposValues[0].getObject();
      //System.out.println ("PRopagating "+posSpot[0]+","+posSpot[1]+" to "+array[0]+","+array[1]);
      variablesChanged (getPosSpot(),allposValues);
      if (this!=origin) origin.variablesChanged (origin.getPosSpot(),allposValues);
      if (isUnderEjs) setFieldListValues(getPosSpot(),allposValues);
    }
  }
  
  protected void propagateSize (ControlElement2D origin,int _index) {
    if (allScalex!=0.0) theSizeXs[_index] = elements[_index].getSizeX()/allScalex;
    else             theSizeXs[_index] = elements[_index].getSizeX();
    if (allScaley!=0.0) theSizeYs[_index] = elements[_index].getSizeY()/allScaley;
    else             theSizeYs[_index] = elements[_index].getSizeY();
    if (allTheSizes!=null) {
      allTheSizes[_index][0] = theSizeXs[_index];
      allTheSizes[_index][1] = theSizeYs[_index];
      Value objVal = new ObjectValue(allTheSizes);
      variableChanged (getFullSizeSpot(),objVal);
      if (this!=origin) origin.variableChanged (origin.getFullSizeSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullSizeSpot(),objVal);
    }
    else {
      variablesChanged (getSizeSpot(),allsizesValues);
      if (this!=origin) origin.variablesChanged (origin.getSizeSpot(),allsizesValues);
      if (isUnderEjs) setFieldListValues(getSizeSpot(),allsizesValues);
    }
  }

  protected final void reportMouseMotion (Object _info, int _index) {
    InteractionTarget target = (InteractionTarget) _info;
    //ControlGroup2D gr = getControlGroup();
    ControlSet2D gr = this;
    if (target==elements[_index].getInteractionTarget(Element.TARGET_POSITION)) {
      if (target.getAffectsGroup()) gr.propagatePosition(this);
      else propagatePosition(this,_index);
    }
    else if (target==elements[_index].getInteractionTarget(Element.TARGET_SIZE)) {
      if (target.getAffectsGroup()) gr.propagateSize(this);
      else propagateSize(this,_index);
    }
  }

  public void interactionPerformed(InteractionEvent _event) {
//    System.out.println("Event ID "+_event.getID());
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  :
        selectedValue.value = set.getInteractedIndex(); //getElementInteracted(_event);
//        selectedValue.value = set.indexInGroup((Element) _event.getSource());
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION);
        break;
      case InteractionEvent.MOUSE_EXITED   :
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        break;
      case InteractionEvent.MOUSE_PRESSED  :
        selectedValue.value = set.getInteractedIndex(); //getElementInteracted(_event);
//        selectedValue.value = set.indexInGroup((Element) _event.getSource());
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        invokeActions (ControlSwingElement.ACTION_PRESS);
//        reportMouseMotion (_event.getInfo(),selectedValue.value); This calls On drag unneccessarily
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo(),selectedValue.value);
        //invokeActions (ControlSwingElement.MOUSE_DRAGGED_ACTION);
        break;
      case InteractionEvent.MOUSE_RELEASED :
        invokeActions (ControlElement.ACTION);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        break;
    }
  } // End of interaction method


} // End of class

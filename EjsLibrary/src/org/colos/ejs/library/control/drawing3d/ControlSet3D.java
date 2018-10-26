/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import java.awt.Color;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.Set;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.interaction.InteractionEvent;
import org.opensourcephysics.drawing3d.interaction.InteractionTarget;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;
import org.opensourcephysics.numerics.*;
import org.opensourcephysics.tools.ToolForData;


/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public abstract class ControlSet3D extends ControlElement3D {
  static protected final int SET3D_ADDED = 2;

  static protected final int NUM_ELEMENTS = 0;
  static protected final int ELEMENT_SELECTED = 1;

  protected Set set;
  protected Element[] elements;
  protected int numElements=0;

  protected ObjectValue[] allposValues, allsizesValues;
  protected IntegerValue selectedValue = new IntegerValue(-1);

  protected double[] theXs, theYs, theZs;
  protected double[] theSizeXs, theSizeYs, theSizeZs;
  protected double[][] allThePos, allTheSizes;
  protected double /* scalex = 1.0, scaley = 1.0, scalez = 1.0, */ lineWidth = 1.0;
  protected boolean  numberOfElements_isSet = false;
  private boolean propagatingValue = false;

  protected double defaultElementX, defaultElementY, defaultElementZ;
  protected double defaultElementSizeX, defaultElementSizeY, defaultElementSizeZ;
  protected Color defElementLines;
  protected java.awt.Paint defElementFill;

  public ControlSet3D () {
    super ();
    checkNumberOfElements(1,true);
    defaultElementX = elements[0].getX();
    defaultElementY = elements[0].getY();
    defaultElementSizeX = elements[0].getSizeX();
    defaultElementSizeY = elements[0].getSizeY();
    defElementLines = elements[0].getStyle().getLineColor();
    defElementFill = elements[0].getStyle().getFillColor();
  }

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.Set"; }

  protected Element createElement () {
    set = new Set();
    return set;
  }

  abstract protected Element createAnElement();
  
  /**
   * This tells how many properties have been added on top of 
   * those of the basic ControlSet2D class
   */
  protected abstract int getPropertiesAddedToSet ();

  protected final int getPropertiesDisplacement () {
    return getPropertiesAddedToSet () + SET3D_ADDED;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      oldElement.getStyle().copyTo(newElement.getStyle());
      
      newElement.setDataObject(oldElement.getDataObject());
      if (oldElement.getPanel()!=null) newElement.setPanel(oldElement.getPanel());
      newElement.setName(oldElement.getName());
      newElement.setXYZ(oldElement.getX(),oldElement.getY(),oldElement.getZ());
      newElement.setSizeXYZ(oldElement.getSizeX(),oldElement.getSizeY(),oldElement.getSizeZ());
      newElement.setTransformation(oldElement.getTransformation());
      newElement.addSecondaryTransformations(oldElement.getSecondaryTransformations());
      newElement.setVisible(oldElement.isVisible());
      
      newElement.getInteractionTarget(Element.TARGET_POSITION).setEnabled(oldElement.getInteractionTarget(Element.TARGET_POSITION).getEnabled());
      newElement.getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(oldElement.getInteractionTarget(Element.TARGET_POSITION).getAffectsGroup());
      newElement.getInteractionTarget(Element.TARGET_SIZE).setEnabled(oldElement.getInteractionTarget(Element.TARGET_SIZE).getEnabled());
      newElement.getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(oldElement.getInteractionTarget(Element.TARGET_SIZE).getAffectsGroup());
  }

  protected final void checkNumberOfElements(int newNumber, boolean force) {
    if (numElements==newNumber && !force) return;
    if (newNumber<1) return;
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
  protected void setNumberOfElements(int newNumber) {
    // Keep original settings for the new elements
    Element[] oldElements = elements;
    elements = new Element[newNumber];
    for (int i = 0; i < newNumber; i++) {
      elements[i] = createAnElement();
      Element oldElement = null;
      if (i<numElements) oldElement = oldElements[i];
      else if (oldElements!=null) oldElement = oldElements[0];
      if (oldElement!=null) copyAnElement (oldElement,elements[i]);
    }
    // Now rebuild the set
    set.removeAllElements();
    for (int i = 0; i < newNumber; i++) {
      set.addElement(elements[i]);
      elements[i].setName(set.getName()+"["+i+"]");
    }
    theXs     = new double[newNumber]; 
    theYs     = new double[newNumber];
    theZs     = new double[newNumber];
    theSizeXs = new double[newNumber]; 
    theSizeYs = new double[newNumber];
    theSizeZs = new double[newNumber];
    for (int i = 0; i < newNumber; i++) {
      theXs[i]     = elements[i].getX();
      theYs[i]     = elements[i].getY();
      theZs[i]     = elements[i].getZ();
      theSizeXs[i] = elements[i].getSizeX(); 
      theSizeYs[i] = elements[i].getSizeY();
      theSizeZs[i] = elements[i].getSizeZ();
      elements[i].addInteractionListener(this);
    }
    allposValues      = new ObjectValue[3];
    allsizesValues    = new ObjectValue[3];
    allposValues[0]   = new ObjectValue(theXs);
    allposValues[1]   = new ObjectValue(theYs);
    allposValues[2]   = new ObjectValue(theZs);
    allsizesValues[0] = new ObjectValue(theSizeXs);
    allsizesValues[1] = new ObjectValue(theSizeYs); 
    allsizesValues[2] = new ObjectValue(theSizeZs); 
    // Ready to go
    numElements = newNumber;
    oldElements=null; // Make (double) sure the old elements go to the garbage collector
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
    for (int i=0; i<numElements; i++) elements[i].setName(name+"["+i+"]");
  }
  
  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() { // This eliminates any previous property
      if (infoList==null) {
        infoList = new java.util.ArrayList<String>();
        infoList.add ("numberOfElements");
        infoList.add ("elementSelected");

        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("numberOfElements")) return "int PREVIOUS";
      if (_property.equals("elementSelected"))  return "int";

      if (_property.equals("x"))       return "int|double|double[]";
      if (_property.equals("y"))       return "int|double|double[]";
      if (_property.equals("z"))       return "int|double|double[]";
      if (_property.equals("sizeX"))   return "int|double|double[]";
      if (_property.equals("sizeY"))   return "int|double|double[]";
      if (_property.equals("sizeZ"))   return "int|double|double[]";
      if (_property.equals("transformation"))   return "3DTransformation|double[]|String|Object|Object[]";

      if (_property.equals("visible"))      return "boolean|boolean[]";
      if (_property.equals("lineColor"))    return "int|int[]|Color|Color[]|Object|Object[]";
      if (_property.equals("lineWidth"))    return "int|double|double[]";
      if (_property.equals("fillColor"))    return "int|int[]|Color|Color[]|Object|Object[]";
      if (_property.equals("resolution"))   return "3DResolution|String|Object|Object[]|double";
      if (_property.equals("drawingFill"))  return "boolean|boolean[]";
      if (_property.equals("drawingLines")) return "boolean|boolean[]";

      if (_property.equals("enabledPosition")) return "Interaction3D|int|int[]|boolean|boolean[]";
      if (_property.equals("movesGroup"))      return "boolean|boolean[]";
      if (_property.equals("enabledSize"))     return "Interaction3D|int|int[]|boolean|boolean[]";
      if (_property.equals("resizesGroup"))    return "boolean|boolean[]";
      if (_property.equals("sensitivity"))     return "int|int[]";

      if (_property.equals("position")) return "double[][]";
      if (_property.equals("size"))     return "double[][]";
      if (_property.equals("menuName")) return "String TRANSLATABLE";
      if (_property.equals("elementposition"))return "ArrowPosition|int|int[]";
      if (_property.equals("depthFactor")) return "double|double[]";

      if (_property.equals("measured"))        return "boolean|boolean[]";
      if (_property.equals("extraColor"))    return "int|int[]|Color|Object|Object[]";

      if (_property.equals("numberOfElements")) return "int PREVIOUS";
      if (_property.equals("elementSelected"))  return "int";

      return super.getPropertyInfo(_property);
    }

    public String getPropertyCommonName(String _property) {
      if (_property.equals("size")) return "sizeArray";
      return super.getPropertyCommonName(_property);
    }

    @Override
    public ControlElement setProperty(String _property, String _value) {
      _property = _property.trim();

      if (_value!=null) {
        boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
        if      (_property.equals("x")) set.setXLabel(useDefaultTitle ? "x" : _value);
        else if (_property.equals("y")) set.setYLabel(useDefaultTitle ? "y" : _value);
        else if (_property.equals("z")) set.setYLabel(useDefaultTitle ? "z" : _value);
      }

      return super.setProperty(_property,_value);
    }

    @Override
    public void addMenuEntries () {
      if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
       getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getTopWindow(),set));
    }
    
// ------------------------------------------------
// Variables
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case NUM_ELEMENTS : 
          if (_value.getInteger()!=numElements) checkNumberOfElements(_value.getInteger(),true);
          numberOfElements_isSet = true;
          break;
        case ELEMENT_SELECTED : selectedValue.value = _value.getInteger(); break;
      }
      
      // I cannot just call super.setValue() because the actions are different
      _index -= SET3D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X :
          allThePos = null; // Not set using the POSITION property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theXs.length,val.length); i<n; i++) elements[i].setX(theXs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theXs.length; i<n; i++) elements[i].setX(theXs[i]=val);
          }
          break;
        case POSITION_Y :
          allThePos = null; // Not set using the POSITION property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
               for (int i=0, n=Math.min(theYs.length,val.length); i<n; i++) elements[i].setY(theYs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
             for (int i=0, n=theYs.length; i<n; i++) elements[i].setY(theYs[i]=val);
          }
          break;
        case POSITION_Z :
          allThePos = null; // Not set using the POSITION property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theZs.length,val.length); i<n; i++) elements[i].setZ(theZs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theZs.length; i<n; i++) elements[i].setZ(theZs[i]=val);
          }
          break;
        case POSITION :
          if (_value.getObject() instanceof double[][]) {
            allThePos = (double[][]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (allThePos.length,false);
            for (int i=0, n=Math.min(numElements,allThePos.length); i<n; i++) {
              double[] point = allThePos[i];
              elements[i].setXYZ(theXs[i] = point[0], theYs[i] = point[1], theZs[i] = point[2]);
            }
          }
          break;

        case SIZE_X :
          allTheSizes = null; // Not set using the SIZE property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeXs.length,val.length); i<n; i++) elements[i].setSizeX(theSizeXs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeXs.length; i<n; i++) elements[i].setSizeX(theSizeXs[i]=val);
          }
          break;
        case SIZE_Y :
          allTheSizes = null; // Not set using the SIZE property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeYs.length,val.length); i<n; i++) elements[i].setSizeY(theSizeYs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeYs.length; i<n; i++) elements[i].setSizeY(theSizeYs[i]=val);
          }
          break;
        case SIZE_Z :
          allTheSizes = null; // Not set using the SIZE property
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (numberOfElements_isSet==false) checkNumberOfElements (val.length,false);
            for (int i=0, n=Math.min(theSizeZs.length,val.length); i<n; i++) elements[i].setSizeZ(theSizeZs[i] = val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=theSizeZs.length; i<n; i++) elements[i].setSizeZ(theSizeZs[i]=val);
          }
          break;
        case SIZE :
          if (_value.getObject() instanceof double[][]) {
            allTheSizes = (double[][]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (allTheSizes.length,false);
            for (int i=0, n=Math.min(numElements,allTheSizes.length); i<n; i++) {
              double[] size = allTheSizes[i];
              elements[i].setSizeXYZ(theSizeXs[i] = size[0],theSizeYs[i] = size[1],theSizeZs[i] = size[2]);
            }
          }
          break;

        case TRANSFORMATION :
            if (_value.getObject() instanceof Transformation) {
              Transformation transf = (Transformation) _value.getObject();
              for (int i=0; i<numElements; i++) elements[i].setTransformation(transf);
            }
            else if (_value.getObject() instanceof Object[]) {
              Object[] val = (Object[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].setTransformation((Transformation)val[i]);
            }
            else if (_value.getObject() instanceof double[]) {
              double[] array = (double[]) _value.getObject();
              if (array.length==6) {
                double[] v1 = new double[] { array[0], array[1], array[2] };
                double[] v2 = new double[] { array[3], array[4], array[5] };
                Transformation transf = Matrix3DTransformation.createAlignmentTransformation(v1,v2);
                for (int i=0; i<numElements; i++) elements[i].setTransformation(transf);
              }
              else if (array.length==4) {
                double[] v = new double[] { array[1], array[2], array[3] };
                Transformation transf = Matrix3DTransformation.rotation(array[0],v);
                for (int i=0; i<numElements; i++) elements[i].setTransformation(transf);
              }
            }
            break;

        case VISIBLE :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].setVisible(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0, n=numElements; i<n; i++) elements[i].setVisible(val);
            }
            break;
        case LINE_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<numElements; i++) elements[i].getStyle().setLineColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setLineColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setLineColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<numElements; i++) elements[i].getStyle().setLineColor(val);
          }
          break;
        case LINE_WIDTH :
            if (_value.getObject() instanceof double[]) {
              double[] val = (double[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setLineWidth((float)val[i]);
            }
            else {
              float val = (float) _value.getDouble();
              for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setLineWidth(val);
            }
            break;

        case FILL_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<numElements; i++) elements[i].getStyle().setFillColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setFillColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setFillColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<numElements; i++) elements[i].getStyle().setFillColor(val);
          }
          break;

        case RESOLUTION :
            if (_value.getObject() instanceof Resolution) {
              Resolution res = (Resolution) _value.getObject();
              for (int i=0; i<numElements; i++) elements[i].getStyle().setResolution(res);
            }
            else if (_value.getObject() instanceof Object[]) {
              Object[] val = (Object[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setResolution((Resolution)val[i]);
            }
            else {
              Resolution res = decodeResolution (_value.toString());
              if (res!=null) for (int i=0; i<numElements; i++) elements[i].getStyle().setResolution(res);
            }
            break;

        case DRAWING_FILL :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setDrawingFill(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setDrawingFill(val);
            }
            break;
        case DRAWING_LINES :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setDrawingLines(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setDrawingLines(val);
            }
            break;

        case ENABLED_POSITION :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val[i]);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val[i]);
          }
          else if (_value instanceof BooleanValue) {
            boolean val = _value.getBoolean();
            for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(val);
          }
          break;
        case MOVES_GROUP      :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0, n=numElements; i<n; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(val);
            }
            break;
        case ENABLED_SIZE     :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val[i]);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val[i]);
          }
          else if (_value instanceof BooleanValue) {
            boolean val = _value.getBoolean();
            for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(val);
          }
          break;
        case RESIZES_GROUP    :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0, n=numElements; i<n; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(val);
            }
            break;

        case SENSITIVITY      : 
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setSensitivity(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setSensitivity(val);
          }
          break;

        case OFFSET : 
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) 
              if (elements[i].getStyle().getRelativePosition()!=val[i]) elements[i].getStyle().setRelativePosition(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0, n=numElements; i<n; i++) if (elements[i].getStyle().getRelativePosition()!=val) elements[i].getStyle().setRelativePosition(val);
          }
          break;

        case DEPTH_FACTOR :
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setDepthFactor(val[i]);
          }
          else {
            double val = _value.getDouble();
            for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setDepthFactor(val);
          }
          break;

        case MEASURED : 
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].setCanBeMeasured(val[i]);
          }
          else {
            boolean val = _value.getBoolean();
            for (int i=0, n=numElements; i<n; i++) elements[i].setCanBeMeasured(val);
          }
          break;
        case EXTRA_COLOR :
          if (_value instanceof IntegerValue) {
            Color col = DisplayColors.getLineColor(_value.getInteger());
            for (int i=0; i<numElements; i++) elements[i].getStyle().setExtraColor(col);
          }
          else if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setExtraColor(DisplayColors.getLineColor(val[i]));
          }
          else if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) elements[i].getStyle().setExtraColor((Color)val[i]);
          }
          else if (_value.getObject() instanceof Color) {
            Color val = (Color) _value.getObject();
            for (int i=0; i<numElements; i++) elements[i].getStyle().setExtraColor(val);
          }
          break;

        case COLOR_ORIGIN : 
          for (int i=0; i<numElements; i++) elements[i].setColorOrigin((double[]) _value.getObject()); 
          break;
        case COLOR_DIRECTION : 
          for (int i=0; i<numElements; i++) elements[i].setColorDirection((double[]) _value.getObject()); 
          break;
        case  COLOR_LEVELS : 
          for (int i=0; i<numElements; i++) elements[i].setColorRegions((double[]) _value.getObject()); 
          break;
        case  COLOR_FILLS :
          if (_value.getObject() instanceof int[]) {
            for (int i=0; i<numElements; i++) elements[i].setColorPalette((int[]) _value.getObject());
          }
          else if (_value.getObject() instanceof Color[]) {
            for (int i=0; i<numElements; i++) elements[i].setColorPalette((Color[]) _value.getObject());
          }
          break;
        case  COLOR_BELOW : for (int i=0; i<numElements; i++) elements[i].setColorBelowWhenEqual(_value.getBoolean()); break;

        default : super.setValue(_index,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case NUM_ELEMENTS : setNumberOfElements (1); numberOfElements_isSet = false; break;
        case ELEMENT_SELECTED : selectedValue.value = -1; break;
        default: super.setDefaultValue(_index-SET3D_ADDED); break; //CJB
      }
      
        // I cannot just call super.setValue() because the actions are different
        _index -= SET3D_ADDED; // So that to respect the super's numbering

        switch (_index) {
        case POSITION_X : for (int i=0, n=theXs.length; i<n; i++) elements[i].setX(theXs[i]=defaultX); break;
        case POSITION_Y : for (int i=0, n=theYs.length; i<n; i++) elements[i].setY(theYs[i]=defaultY); break;
        case POSITION_Z : for (int i=0, n=theZs.length; i<n; i++) elements[i].setZ(theZs[i]=defaultZ); break;
        case POSITION : allThePos = null; break;
        
        case SIZE_X : for (int i=0, n=theSizeXs.length; i<n; i++) elements[i].setSizeX(theSizeXs[i]=defaultSizeX); break;
        case SIZE_Y : for (int i=0, n=theSizeYs.length; i<n; i++) elements[i].setSizeY(theSizeYs[i]=defaultSizeY); break;
        case SIZE_Z : for (int i=0, n=theSizeZs.length; i<n; i++) elements[i].setSizeZ(theSizeZs[i]=defaultSizeZ); break;
        case SIZE : allTheSizes = null; break;
        
        case TRANSFORMATION : for (int i=0; i<numElements; i++) elements[i].setTransformation(null); break;

        case VISIBLE : for (int i=0; i<numElements; i++) elements[i].setVisible(true); break;
        case LINE_COLOR : for (int i=0; i<numElements; i++) elements[i].getStyle().setLineColor(defLines); break;
        case LINE_WIDTH : for (int i=0; i<numElements; i++) elements[i].getStyle().setLineWidth(1.0f); break;
        case FILL_COLOR : for (int i=0; i<numElements; i++) elements[i].getStyle().setFillColor(defFill); break;
        case RESOLUTION : for (int i=0; i<numElements; i++) elements[i].getStyle().setResolution(defaultRes); break;

        case DRAWING_FILL : for (int i=0; i<numElements; i++) elements[i].getStyle().setDrawingFill(true); break;
        case DRAWING_LINES : for (int i=0; i<numElements; i++) elements[i].getStyle().setDrawingLines(true); break;

        case ENABLED_POSITION : for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setEnabled(false); break;
        case MOVES_GROUP      : for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_POSITION).setAffectsGroup(false); break;
        case ENABLED_SIZE     : for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setEnabled(false); break;
        case RESIZES_GROUP    : for (int i=0; i<numElements; i++) elements[i].getInteractionTarget(Element.TARGET_SIZE).setAffectsGroup(false); break;
        case SENSITIVITY : for (int i=0; i<numElements; i++) elements[i].getStyle().setSensitivity(Style.DEFAULT_SENSITIVITY); break;

        case OFFSET : for (int i=0; i<numElements; i++) elements[i].getStyle().setRelativePosition(Style.NORTH_EAST); break; 
        case DEPTH_FACTOR : for (int i=0, n=numElements; i<n; i++) elements[i].getStyle().setDepthFactor(1.0); break;
        case MEASURED :  for (int i=0, n=numElements; i<n; i++) elements[i].setCanBeMeasured(true); break;
        case EXTRA_COLOR : for (int i=0; i<numElements; i++) elements[i].getStyle().setExtraColor(defExtraColor); break;

        case COLOR_ORIGIN : 
          double[] colorOrigin = new double[] {0.0,0.0,0.0};
          for (int i=0; i<numElements; i++) elements[i].setColorOrigin(colorOrigin); 
          break;
        case COLOR_DIRECTION : 
          double[] colorDirection = new double[] {1.0,0.0,0.0};
          for (int i=0; i<numElements; i++) elements[i].setColorDirection(colorDirection); 
          break;
        case  COLOR_LEVELS : 
          for (int i=0; i<numElements; i++) elements[i].setColorRegions(null); 
          break;
        case  COLOR_FILLS :
          for (int i=0; i<numElements; i++) elements[i].setColorPalette((Color[])null); 
          break;
        case  COLOR_BELOW : for (int i=0; i<numElements; i++) elements[i].setColorBelowWhenEqual(true); break;

        default : super.setDefaultValue(_index); break;

      }
        if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case NUM_ELEMENTS : return "1";
        case ELEMENT_SELECTED : return "-1";
      }

      // I cannot just call super.setValue() because the actions are different
      _index -= SET3D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : return Double.toString(defaultElementX);
        case POSITION_Y : return Double.toString(defaultElementY);
        case POSITION_Z : return Double.toString(defaultElementZ);
        case SIZE_X : return Double.toString(defaultElementSizeX);
        case SIZE_Y : return Double.toString(defaultElementSizeY);
        case SIZE_Z : return Double.toString(defaultElementSizeZ);
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
      _index -= SET3D_ADDED; // So that to respect the super's numbering
      switch (_index) {
        case POSITION_X : return allposValues[0];
        case POSITION_Y : return allposValues[1];
        case POSITION_Z : return allposValues[2];
        case POSITION : return new ObjectValue(allThePos); 
        case SIZE : return new ObjectValue(allTheSizes); 
        case SIZE_X : return allsizesValues[0];
        case SIZE_Y : return allsizesValues[1];
        case SIZE_Z : return allsizesValues[2];
        default: return super.getValue(_index);
      }
    }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  protected void propagatePosition (ControlElement3D origin,int _index) {
    theXs[_index] = elements[_index].getX();
    theYs[_index] = elements[_index].getY();
    theZs[_index] = elements[_index].getZ();
    if (allThePos!=null) {
      allThePos[_index][0] = theXs[_index];
      allThePos[_index][1] = theYs[_index];
      allThePos[_index][2] = theZs[_index];
      Value objVal = new ObjectValue(allThePos);
      variableChanged (getFullPositionSpot(),objVal);
      if (this!=origin) origin.variableChanged (origin.getFullPositionSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullPositionSpot(),objVal);
    }
    else {
      variablesChanged (getPosSpot(),allposValues);
      if (this!=origin) origin.variablesChanged (origin.getPosSpot(),allposValues);
      if (isUnderEjs) setFieldListValues(getPosSpot(),allposValues);
    }
  }

  protected void propagateSize (ControlElement3D origin,int _index) {
    theSizeXs[_index] = elements[_index].getSizeX();
    theSizeYs[_index] = elements[_index].getSizeY();
    theSizeZs[_index] = elements[_index].getSizeZ();
    if (allTheSizes!=null) {
      allTheSizes[_index][0] = theSizeXs[_index];
      allTheSizes[_index][1] = theSizeYs[_index];
      allTheSizes[_index][2] = theSizeZs[_index];
      Value objVal = new ObjectValue(allTheSizes);
      if (this!=origin) origin.variableChanged (origin.getFullSizeSpot(),objVal);
      if (isUnderEjs) setFieldListValue(getFullSizeSpot(),objVal);
    }
    else {
      variablesChanged (getSizeSpot(),allsizesValues);
      if (this!=origin) origin.variablesChanged (origin.getSizeSpot(),allsizesValues);
      if (isUnderEjs) setFieldListValues(getSizeSpot(),allsizesValues);
    }
  }

  private void reportMouseMotion (Object _info, int _index) {
    InteractionTarget target = (InteractionTarget) _info;
    ControlGroup3D gr = getControlGroup();
    if (target==elements[_index].getInteractionTarget(Element.TARGET_POSITION)) {
      if (target.getAffectsGroup() && gr!=null) gr.propagatePosition(this);
      else propagatePosition(this,_index);
    }
    else if (target==elements[_index].getInteractionTarget(Element.TARGET_SIZE)) {
      if (target.getAffectsGroup()&& gr!=null) gr.propagateSize(this);
      else propagateSize(this,_index);
    }
  }

  protected int getElementInteracted(InteractionEvent _event) {
    Element el = (Element) _event.getSource();
//    System.out.println ("Interacted with element = "+el);
    for (int i=0; i<numElements; i++) if (elements[i]==el) return i;
    return -1;
  }

  int selectedElement = -1;

  public void interactionPerformed(InteractionEvent _event) {
    // System.out.println("Event ID "+_event.getID());
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  :
        selectedValue.value = getElementInteracted(_event);
        variableChanged (ELEMENT_SELECTED+getPropertiesDisplacement(),selectedValue);
        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION);
        break;
      case InteractionEvent.MOUSE_EXITED   :
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableChanged (ELEMENT_SELECTED+getPropertiesDisplacement(),selectedValue);
        break;
      case InteractionEvent.MOUSE_PRESSED  :
        selectedElement = selectedValue.value = getElementInteracted(_event);
        variableChanged (ELEMENT_SELECTED+getPropertiesDisplacement(),selectedValue);
        reportMouseMotion (_event.getInfo(),selectedValue.value);
        invokeActions (ControlSwingElement.ACTION_PRESS);
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo(),selectedElement);
        //invokeActions (ControlSwingElement.MOUSE_DRAGGED_ACTION);
        break;
      case InteractionEvent.MOUSE_RELEASED :
        invokeActions (ControlSwingElement.ACTION);
//        selectedValue.value = -1; This caused problem with delayed actions
        variableChanged (ELEMENT_SELECTED+getPropertiesDisplacement(),selectedValue);
        break;
    }
  } // End of interaction method

} // End of class

/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Color;
import java.awt.Paint;
import java.util.*;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.interaction.InteractionTarget;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: Group</p>
 * <p>Description: A Group is an element that is made of other elements.</p>
 * The group's position, size, visibility and transformation do affect the
 * elements in the group. The group's style doesn't.</p>
 * 
 * <p>Interaction: Groups are enabled by default, although their targets 
 * have no physical location (only plain elements have hot spots).
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class Group extends Element implements org.opensourcephysics.display.Data {
  
  // Implementation variables
  protected ArrayList<Element> elementList = new ArrayList<Element>();
  private int elementInteracted = -1;

  {
    setEnabled (true); // Groups are enabled by default
  }

  // ----------------------------------------
  // Implementation of the element
  // ----------------------------------------

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleGroup(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dGroup(this);
    }
  }

  @Override
  protected void setImplementation(int _implementation) {
    super.setImplementation(_implementation);
    for (Element element : elementList) element.setImplementation(_implementation);
  }
  
  //CJB
  public void setPanel(DrawingPanel3D _panel) {
	  super.setPanel(_panel);
	  for (Element element : elementList){ 
		  if(element.getPanel()!=null)  element.setPanel(_panel);
	  }
  }
  
  public void removePanel(){
	  super.removePanel();
	  for (Element element : elementList) element.removePanel();
  }
  //CJB

  // ------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Adds an Element to this Group.
   * @param element Element
   * @see Element
   */
  public void addElement(Element element) {
    if (!elementList.contains(element)) elementList.add(element);
    element.setGroup(this);
  }

  /**
   * Adds an Element to this Group at the specified location
   * @param element Element
   * @param index int
   * @see Element
   */
  public void addElementAtIndex(int index, Element element) {
    if (!elementList.contains(element)) {
      index = Math.max(index, elementList.size()-1);
      elementList.add(index, element);
    }
    element.setGroup(this);
  }

  /**
   * Adds a collection of elements
   * @param elements
   */
  public void addElements(java.util.Collection<Element> elements) {
    if (elements!=null) for (Element el : elements) addElement(el);
  }

  /**
   * Removes an Element from this Group
   * @param element Element
   * @see Element
   */
  public void removeElement(Element element) {
    element.setGroup(null);
    elementList.remove(element);
  }

  /**
   * Removes all Elements from this Group
   * @see Element
   */
  public void removeAllElements() {
    for (Element element : elementList) element.setGroup(null);
    elementList.clear();
  }

  /**
   * Gets the cloned list of Elements in the group.
   * (Should be synchronized.)
   * @return cloned list
   */
  public synchronized List<Element> getElements() {
    return new ArrayList<Element>(elementList);
  }

  /**
   * Returns the number of child elements
   * @return int
   */
  public int getNumberOfElements() { return elementList.size(); }
  
  /**
   * Gets the elements of the group at a given index.
   * @return the given element (null if the index is not within allowed bounds)
   */
  public Element getElement(int index) {
    try { return elementList.get(index); }
    catch (IndexOutOfBoundsException exc) { return null; }
  }
  
//  /**
//   * Returns the index of an element in the group.
//   * @param _element
//   * @return the index. -1 if the element is not in this group
//   */
//  public int indexInGroup(Element _element) {
//    return elementList.indexOf(_element);
//  }

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------

  @Override
  public void processChanges(int _cummulativeChange) {
    _cummulativeChange |= changeType;
    ImplementingObject impl = getImplementingObject();
    if (impl!=null) { // This may happen when the user switches from Simple3D to Java3D 
      impl.processChanges(changeType, _cummulativeChange);
    }
    for (Element el : elementList) el.processChanges(_cummulativeChange);
    changeType = CHANGE_NONE;
  }

  @Override
  public boolean hasChanged() {
    if (changeType!=CHANGE_NONE) return true;
    for (Element el : elementList) if (el.hasChanged()) return true;
    return false;
  }

  @Override
  public double getDiagonalSize() {
    double max = 0.0;
    for (Element el : elementList) if (el.isVisible()) max = Math.max(max, el.getDiagonalSize());
    return max; 
  }

  @Override
  public void getExtrema(double[] min, double[] max) {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    for (Element el : elementList) {
      if (el.getCanBeMeasured() && el.isVisible()) {
        el.getExtrema(min, max);
        minX = Math.min(Math.min(minX, min[0]), max[0]);
        maxX = Math.max(Math.max(maxX, min[0]), max[0]);
        minY = Math.min(Math.min(minY, min[1]), max[1]);
        maxY = Math.max(Math.max(maxY, min[1]), max[1]);
        minZ = Math.min(Math.min(minZ, min[2]), max[2]);
        maxZ = Math.max(Math.max(maxZ, min[2]), max[2]);
      }
    }
    min[0] = minX;
    max[0] = maxX;
    min[1] = minY;
    max[1] = maxY;
    min[2] = minZ;
    max[2] = maxZ;
  }

  @Override
  public InteractionTarget getTargetHit(int x, int y) {
//    if (!isVisible()) return null;
    if (!isEnabled()) return null;
    List<Element> elList = getElements();
    for (int i=elList.size()-1; i>=0; i--) {
      Element el = elList.get(i);
      if (!el.isVisible()) continue;
      InteractionTarget target = el.getTargetHit(x,y);
      if (target!=null) {
        elementInteracted = i;
        return target;
      }
    }
    elementInteracted = -1;
    return null;
  }

  /**
   * Returns the element last interacted
   * @return int
   */
  public int getInteractedIndex() { return elementInteracted; }

  @Override
  public boolean getCanBeMeasured() { // required by Measurable (in Interactive)
    if (!super.getCanBeMeasured()) return false;
    for (Element el : getElements()) if (el.getCanBeMeasured()) return true;
    return false;
  }
  
  /**
   * Clears all data in child elements of type Trail
   */
  public void clear() {
    for (Element el : getElements()) if (el instanceof MultiTrail) ((MultiTrail) el).clear();
  }
  
  /**
   * Initializes all data in child elements of type Trail
   */
  public void initialize() {
    for (Element el : getElements()) if (el instanceof MultiTrail) ((MultiTrail) el).initialize();
  }

  // ----------------------------------------------------
  // Implementation of Data
  // ----------------------------------------------------

  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();

  public void setID(int id) { datasetID = id; }

  public int getID() { return datasetID; }

  public double[][] getData2D() { return null; }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() {
    for (Element el : elementList) if (el instanceof Data) return ((Data) el).getColumnNames();
    return null; 
  }

  public Color[] getLineColors() { 
//    return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), DisplayColors.getLineColor(2)};
    return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), getStyle().getLineColor()}; 
  }

  public Color[] getFillColors() { 
    Paint fill = getStyle().getFillColor();
    if (fill instanceof Color) return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), (Color) fill };
    return new Color[] { DisplayColors.getLineColor(0), DisplayColors.getLineColor(1), DisplayColors.getLineColor(2)};
  }

  public java.util.List<Data> getDataList() {
    java.util.List<Data> list = new java.util.ArrayList<Data>();
    for (Element el : elementList) if (el instanceof Data) list.add((Data)el);
    return list;
  }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

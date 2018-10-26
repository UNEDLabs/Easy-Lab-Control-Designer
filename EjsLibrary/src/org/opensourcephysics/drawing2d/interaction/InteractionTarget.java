/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d.interaction;

import java.awt.Graphics;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.drawing2d.Element;

/**
 * An InteractionTarget
 */
public class InteractionTarget implements Interactive {
  
  static public final int ENABLED_NONE = 0;
  static public final int ENABLED_ANY = 1;
  static public final int ENABLED_X = 2;
  static public final int ENABLED_Y = 3;
  static public final int ENABLED_NO_MOVE = 4;


  private final Element element;
  private final int type;
  private int enabledType = ENABLED_NONE;
  private boolean affectsGroup = false;
  private String command = null;
  private Object dataObject=null;
    
    /**
     * Constructor for the class
     * @param _element Element
     * @param _type int Either Element.TARGET_POSITION or Element.TARGET_SIZE
     */
    public InteractionTarget(Element _element, int _type) {
      element = _element;
      type = _type;
    }

      /**
     * Returns the Element that contains the target
     * @return Element
     */
    final public Element getElement() {
      return element;
    }

    /**
     * Returns the type of target
     * @return int
     */
    final public int getType() {
      return type;
    }

    /**
     * Enables/Disables the target completely
     * @param value boolean
     */
    final public void setEnabled(boolean value) {
      enabledType = value ? ENABLED_ANY : ENABLED_NONE;
    }

    /**
     * Returns the enabled status of the target
     * @return boolean true if any motion is enabled
     */
    final public boolean isEnabled() {
      return enabledType!=ENABLED_NONE;
    }
    
    /**
     * Sets partially the interaction capability of the target
     * One of:
     * <ul>
     *   <li> ENABLED_NONE: the target is not responsive</li>
     *   <li> ENABLED_ANY: any motion is allowed</li>
     *   <li> ENABLED_X: the target only responds to motion in the X direction</li>
     *   <li> ENABLED_Y: the target only responds to motion in the Y direction</li>
     *   <li> ENABLED_NO_MOVE: the target responds to interaction but does not move</li>
     * </ul>
     * @param value int
     */
    final public void setEnabled(int value) {
      enabledType = value;
    }

    /**
     * Returns the (perhaps partial) interaction capability of the target
     * @return int
     */
    final public int getEnabled() {
      return enabledType;
    }

    /**
     * Returns the action command of this target
     * @return String
     */
    final public String getActionCommand() {
      return command;
    }

    /**
     * Sets the action command for this target
     * @param command String
     */
    final public void setActionCommand(String command) {
      this.command = command;
    }

    /**
     * Whether the interaction with the target affects the top-level group
     * of the element that contains it (instead of only affecting the element).
     * Default is false.
     * @param value boolean
     */
    final public void setAffectsGroup(boolean value) {
      affectsGroup = value;
    }

    /**
     * Whether the target affects the top-level group
     * @return boolean
     */
    final public boolean getAffectsGroup() {
      return affectsGroup;
    }

    /**
     * A place holder for data objects
     * @param _object Object
     */
    final public void setDataObject(Object _object) {
      this.dataObject = _object;
    }

    /**
     * Returns the data object
     * @return Object
     */
    final public Object getDataObject() {
      return this.dataObject;
    }

    // ------------------------------
    // Implementation of Interactive
    // ------------------------------

    public Interactive findInteractive(DrawingPanel panel, int _xpix, int _ypix) { return this; }

    public void setXY(double x, double y) { element.setXY(x, y); }

    public void setX(double x) { element.setX(x); }

    public void setY(double y) { element.setY(y); }

    public double getX() { return element.getX(); }

    public double getY() { return element.getY(); }

    public double getXMin() { return element.getXMin(); }

    public double getXMax() { return element.getXMax(); }

    public double getYMin() { return element.getYMin(); }

    public double getYMax() { return element.getYMax(); }

    public boolean isMeasured() { return element.isMeasured(); }

    public void draw(DrawingPanel panel, Graphics g) { element.draw(panel, g); }

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

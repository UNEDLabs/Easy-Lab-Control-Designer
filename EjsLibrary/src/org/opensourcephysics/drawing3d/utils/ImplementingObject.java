package org.opensourcephysics.drawing3d.utils;

/**
 * Interface for an object that implements an Element under simple3d, Java3D, ...
 * @author Francisco Esquembre
 *
 */
public interface ImplementingObject {

  /**
   * Efectively adds the element to the 3D scene.
   * Required by Java 3D
   */
  public void addToScene();

  /**
   * Efectively removes the element from the 3D scene.
   * Required by Java 3D
   */
  public void removeFromScene();

   /**
   * Process changes of the element
   * @param _change int the changes of this element
   * @param _cumulativeChange int the cumulative changes, i.e. the 
   * changes of the element and those inherited from parent groups
   */
   public void processChanges(int _change, int _cumulativeChange);

   /**
    * Applies any change required by a change in style
    * @param _change int indicates the change that took place
    */
   public void styleChanged(int _change);

 }

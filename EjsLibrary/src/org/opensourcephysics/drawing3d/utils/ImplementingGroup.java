package org.opensourcephysics.drawing3d.utils;

/**
 * Interface for groups under different implementations
 * @author Francisco Esquembre
 *
 */
public interface ImplementingGroup {

  /**
   * Efectively adds the element to the 3D scene.
   * Required by Java 3D
   */
  public void addToScene();
  
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

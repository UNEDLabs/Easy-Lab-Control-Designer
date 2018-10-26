package org.opensourcephysics.drawing3d.utils;

public interface ImplementingPanel extends org.opensourcephysics.display.Renderable {
  
  /**
   * Returns the actual Swing component
   * @return java.awt.Component
   */
   public java.awt.Component getComponent();

  /**
   * Tells the implementing panel that a change makes its current view obsolete.
   * For instance, when the min-max value change.
   * But the update needs not be immediate.
   */
  public void forceRefresh();

 //***************************************** Andres ***************************************************************
 //
 /**
  * Sets the Eye Distance in a stereo view.
  */
 public void setEyeDistance(double d);
 
 // ****************************************************************************************************************
 
 
  /**
   * Updates the panel immediately
   * For instance, when the min-max value change
   */
  public void update();

  /**
   * Sets a flags to redraw as fast as possible.
   * Typically during an interaction
   * @param fast
   */
  public void setFastRedraw(boolean fast);

  /**
   * This will be called by the camera whenever it changes.
   * @see Camera
   */
  public void cameraChanged(int howItChanged);

  /**
   * Shows a message in a yellow text box in the lower right hand corner.
   *
   * @param msg
   */
  public void setMessage(String msg);

  /**
   * Shows a message in a yellow text box.
   * The location must be one of the following:
   * <ul>
   *   <li> BOTTOM_LEFT;
   *   <li> BOTTOM_RIGHT;
   *   <li> TOP_RIGHT;
   *   <li> TOP_LEFT;
   * </ul>
   * @param msg
   * @param location
   */
  public void setMessage(String msg, int location);
  
  
  //Visualization changes (CJB)
  public void visualizationChanged(int hintChanged);
  
 // public void deleteChild(String name);

 }

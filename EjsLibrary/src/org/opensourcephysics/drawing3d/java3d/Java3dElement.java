/*
 * Created on January 29, 2005
 * Complete copy of the OpenGL version
 *  - except for preLinesGL and preFillGL changed to preLinesJ3D and preFillJ3D
 */

package org.opensourcephysics.drawing3d.java3d;

import java.util.ArrayList;

import javax.media.j3d.*;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.opensourcephysics.numerics.*;
import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.drawing3d.utils.*;

/**
* <p>Title: Java3dElement</p>
* <p>Description: The base class for a Java3D implementation of a 3D Element.</p>
* <p>Copyright: Open Source Physics project</p>
* @author Carlos Jara Bravo
* @author Francisco Esquembre
* @version August 2009
* Based on previous work by Glenn Ford
*/
public abstract class Java3dElement implements ImplementingObject {

  static public final int AFFECTS_TRANSFORM = Element.CHANGE_POSITION | Element.CHANGE_SIZE | Element.CHANGE_TRANSFORMATION | 
  												 Element.CHANGE_RESOLUTION | Element.CHANGE_VISIBILITY;
  													
  protected Element element;
  protected boolean primitive;
  private Java3dStyle java3dStyle;

  //Java3d variables
  private BranchGroup branchGroup; 
  private BranchGroup last_branchGroup;
  private TransformGroup transformGroup;
  private Transform3D transform1;
  private Transform3D transform2;
  private Vector3d positionVector = new Vector3d();
  private Vector3d sizeVector = new Vector3d();

  //Variables for setting the secondary transformation
  private Quat4d quatBuffer1 = new Quat4d(0,0,0,1);
  private Vector3d finalTranslation = new Vector3d();
  private ArrayList<Quat4d> listQuat = new ArrayList<Quat4d>();
  private ArrayList<Vector3d> listDirect = new ArrayList<Vector3d>();
  
        
  protected Java3dElement(Element _element) {
    this.element = _element;
    java3dStyle = new Java3dStyle(element.getStyle());
    
    branchGroup = new BranchGroup();
    branchGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
    branchGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
    branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
    
    transform1 = new Transform3D();
    transform2 = new Transform3D();
    transform2.mul(transform1);
    transformGroup = new TransformGroup(transform2);
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    transformGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
    transformGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
    
    element.addChange(AFFECTS_TRANSFORM); 
  }

  // ----------------------------------------
  // Implementation of the ImplementingObject
  // ----------------------------------------
  
  public void addToScene() {
	Group group = element.getGroup();
    if (group!=null){
    	Object obj= group.getImplementingObject();
    	if(obj!=null) ((Java3dElement) group.getImplementingObject()).getTransformGroup().addChild(branchGroup);
    }
    else ((Java3dDrawingPanel3D) element.getPanel().getImplementingPanel()).getTransformGroup().addChild(branchGroup); 
  }

  public void removeFromScene() {
    Group group = element.getGroup();
    if (group!=null) {
      Java3dElement implGroup = (Java3dElement) group.getImplementingObject();
      if (implGroup!=null) implGroup.getTransformGroup().removeChild(branchGroup);
    }
    else {
      Java3dDrawingPanel3D implPanel = (Java3dDrawingPanel3D) element.getPanel().getImplementingPanel();
      if (implPanel!=null) implPanel.getTransformGroup().removeChild(branchGroup);
    }
  }

  public synchronized void processChanges(int _change, int _cummulativeChange) {
	if(element instanceof Set){transformGroup.setTransform(new Transform3D()); return;}
    if ((_change & Element.CHANGE_POSITION) != 0) {
      double[] point = element.getScaledPosition();
      positionVector.x = point[0]; 
      positionVector.y = point[1]; 
      positionVector.z = point[2];
    }
    if ((_change & Element.CHANGE_SIZE) != 0) {
      double[] point = element.getScaledSize();
      sizeVector.x = point[0]; 
      sizeVector.y = point[1]; 
      sizeVector.z = point[2];
    }
    if ((_change & Element.CHANGE_TRANSFORMATION) != 0){  
    	modifyTransform();
    }
    if ((_change & AFFECTS_TRANSFORM) != 0) {	
      //Primitive transformation
      transform1.set(quatBuffer1);
      
      //Secondary transformations
      transform2.setIdentity();
      finalTranslation.set(new double[]{0,0,0});
      for(int i=listQuat.size()-1;i>=0;i--){
    	  Transform3D temp = new Transform3D();
    	  temp.set(listQuat.get(i));
    	  finalTranslation.add(listDirect.get(i));
    	  transform2.mul(temp);
      }
      
      //Final transformation
      transform2.mul(transform1);
      finalTranslation.add(positionVector);
      transform2.setTranslation(finalTranslation);
      transform2.setScale(sizeVector);
      transformGroup.setTransform(transform2);
    }
    if ((_change & Element.CHANGE_VISIBILITY) != 0) {
    	//getAppearance().getRenderingAttributes().setVisible(element.isVisible());
    	makeVisible(element.isVisible());
    }   	
    if(((_change & Element.CHANGE_RESOLUTION) != 0) || ((_change & Element.CHANGE_SHAPE) != 0)){
    	element.styleChanged(Style.CHANGED_TEXTURES);
    }
  }

  public void styleChanged(int _change) {
    switch(_change) {
      case Style.CHANGED_RELATIVE_POSITION: element.addChange(Element.CHANGE_POSITION); break;
      case Style.CHANGED_RESOLUTION: element.addChange(Element.CHANGE_RESOLUTION);  break;
    }
    java3dStyle.applyStyleChange(_change);
  }
  
  /**
   * Used by JAVA 3D groups
   * @param _visible
   */
  protected void makeVisible(boolean _visible) {
    if (element instanceof Group) {
      for (Element elementJ3D : ((Group) this.element).getElements()) {
        ((Java3dElement) elementJ3D.getImplementingObject()).getAppearance().getRenderingAttributes().setVisible(_visible);
      }
    }
    else {
      if (element.getGroup()==null || element.getGroup().isVisible()) {
        getAppearance().getRenderingAttributes().setVisible(_visible);
      }
    }
  }
  
  // ----------------------------------------
  // Java 3D technical stuff
  // ----------------------------------------
  /**
   * @return Returns a boolean which indicates if the 3D object is primitive.
   */
  abstract public boolean isPrimitive();
  
  final protected void addNode(Node _node) {
    if (_node==null) {
      branchGroup.addChild(this.transformGroup);
      return;
    }
    
    //If there is added a node before, clear all
    if (last_branchGroup != null) {
    	last_branchGroup.detach();
    	last_branchGroup.removeAllChildren();
        transformGroup.removeAllChildren();
    }
    transformGroup.addChild(_node); 
    // Put the TransformGroup inside a BranchGroup because 
    // the main branch group may have more than one children
    BranchGroup bg = new BranchGroup();
    bg.setCapability(BranchGroup.ALLOW_DETACH);
    bg.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
    bg.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
    bg.addChild(transformGroup);

    //Put this new BranchGroup in our scene
    branchGroup.addChild(bg);
    
    //Save the last BranchGroup added
    last_branchGroup = bg;
  }

  /**
   * @return Returns the BranchGroup object in which this Element exists.
   */
  final protected BranchGroup getBranchGroup() { return branchGroup; }

  /**
   * @return Returns the TransformGroup object in which this Element exists.
   */
  final protected TransformGroup getTransformGroup() { return transformGroup; }

  /**
   * @return Returns the position of the object.
   */
  final protected Vector3d getPositionVector() { return positionVector; }

  /**
   * @return Returns the size of the object.
   */
  final protected Vector3d getSizeVector() { return sizeVector; }
  
  /**
   * @return Returns the Appearance object in which this Element exists.
   */
  final protected Appearance getAppearance() { return java3dStyle.getAppearance(); }
  
  // --------------------------------------
  // Private methods
  // --------------------------------------

  /**
   * Modify the transform of the transformGroup
   */
  private void modifyTransform () {
    quatBuffer1 = changeTransform(element.getTransformation());
    listQuat.clear(); listDirect.clear();
    for (TransformationWrapper trWrapper : element.getSecondaryTransformations()) {
      if (trWrapper.isEnabled()){
        listQuat.add(changeTransform(trWrapper.getTransformation()));
        listDirect.add(getDirectMovement(trWrapper.getTransformation()));
      }
    }
  }
  
  private Vector3d getDirectMovement(Transformation tr){
	  Vector3d direct = new Vector3d();
	if(tr == null) direct.set(new double[]{0,0,0});
    else if (tr instanceof Quaternion){
      double[] origin = ((Quaternion) tr).getOrigin();
      if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0)
        direct.set(tr.direct(new double[]{0,0,0}));
    }
	else{
		  double[] origin = ((Matrix3DTransformation) tr).getOrigin();
		  if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0)
			  direct.set(tr.direct(new double[]{0,0,0}));
	  }
	  return direct;
  }
  
  private Quat4d changeTransform (Transformation tr){
	 Quat4d quat = new Quat4d();
	 double[] matrixBuffer = new double[16];
	 double[] trPoint = new double[3];
	 Transform3D temp = new Transform3D();
//	 if (tr instanceof TransformationWrapper) {
//	   Transformation wrappedTr = ((TransformationWrapper) tr).getWrappedTransformation();
//	   if (wrappedTr!=null) tr = wrappedTr;
//	 }
	 
	 if (tr==null) quat.set(0,0,0,1); // Unit quaternion
	 else if (tr instanceof Quaternion) {
	      double[] coordsBuffer = ((Quaternion) tr).getCoordinates();
	      System.arraycopy(coordsBuffer,1,trPoint,0,3);
	      element.getPanel().getCamera().map(trPoint);
	      quat.set(trPoint[0], trPoint[1], trPoint[2], coordsBuffer[0]);
	  }
	  else if (tr instanceof Matrix3DTransformation) {
	      ((Matrix3DTransformation) tr).getTransposedFlatMatrix (matrixBuffer);
	      temp.set(matrixBuffer);
	      temp.get(quat);
	      trPoint[0] = quat.x; 
	      trPoint[1] = quat.y; 
	      trPoint[2] = quat.z;  
	      element.getPanel().getCamera().map(trPoint);
	      quat.set(trPoint[0], trPoint[1], trPoint[2], quat.w);
	  }
	  else {
	    System.err.println ("Java3dElement Warning! Ignored transformation of an unknown type : "+tr);
	  }
	  return quat;
  }
}

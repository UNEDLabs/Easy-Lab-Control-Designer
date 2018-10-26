package org.opensourcephysics.drawing3d.java3d;


import java.awt.Font;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementText;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.Text2D;

/**
 * <p>Title: Java3dElementText</p>
 * <p>Description: A text element using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */

public class Java3dElementText extends Java3dElement {

	private OrientedShape3D textOS = null;
	private TransformGroup textTG = null;
	private BranchGroup bg = null;
	private Shape3D shape = null;
	private Transform3D t1=null,t2=null;
	
	private double scale;
	private static final double nearDistance = 3.0;
	private static final double factorDistance = 4.0;
	private static final int preferredFrameSize = 300;
	
	private static java.awt.geom.GeneralPath gp = new java.awt.geom.GeneralPath();
    static{
    	gp.moveTo(0, 0); 
    	gp.lineTo(1.0f, 1.0f);
    }
	
	public Java3dElementText(ElementText _element) {
		super(_element);
		
		textOS = new OrientedShape3D();
		textOS.setCapability(OrientedShape3D.ALLOW_SCALE_WRITE);
		textOS.setCapability(OrientedShape3D.ALLOW_GEOMETRY_WRITE);
		textOS.setCapability(OrientedShape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
		textOS.setCapability(OrientedShape3D.ALLOW_APPEARANCE_WRITE);
		textOS.setRotationPoint(0, 0, 0);
		textOS.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
		textOS.setConstantScaleEnable(true);

		/*localbg = new BranchGroup();
		localbg.setCapability(BranchGroup.ALLOW_DETACH);
		localbg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		localbg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);*/
		
		textTG = new TransformGroup();
		textTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		textTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		textTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		textTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		textTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		
		if(((ElementText)element).getText()!=null) element.addChange(Element.CHANGE_SHAPE);
		
		//textTG.addChild(localbg);
		//addNode(textTG);
	}
	
  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change,_cummulativeChange);
		if ((_change & Element.CHANGE_SIZE) != 0) setOSScale();
		if ((_change & Element.CHANGE_SHAPE) != 0) recreateNode();
		if ((_change & Element.CHANGE_COLOR) != 0) recreateNode();
		if ((_change & Element.CHANGE_PROJECTION) != 0) changeSize(); 
		
	}
	
  
  // -------------------------------------
  // Implementation of Java3dElement
  // -------------------------------------

  @Override
  public boolean isPrimitive() { return false; }

  // -------------------------------------
  // private methods
  // -------------------------------------

  private void setOSScale() {
	if(!((ElementText)element).isTrueSize()) return;
    Vector3d size = getSizeVector();
    size.x = 0.4; size.y = 0.4; size.z = 0.4;
    double max = Math.max(size.x, Math.max(size.y, size.z));
    textOS.setScale(max*20.f);
  }
  /**
   * Recreates the node since it seems the color and font of a Text2D 
   * cannot be changed after instantiation
   */
  private void recreateNode() {
	  Text2D textNode = null;
	  Text3D textNode3d = null;
	  ElementText textElement = (ElementText) element;
      Style style = element.getStyle();
	  if (style.getLineColor()==null) return;
	  String str = textElement.getText();
	  Font font = textElement.getFont();
	  if(font == null || str == null) return;
	  
	  if (textTG!=null && textTG.numChildren()!=0) textTG.removeChild(bg);
	  
	  if(((ElementText)element).isTrueSize()){
		  textNode = new Text2D(str, new Color3f(style.getLineColor()),
		               font.getName(), font.getSize()*2, font.getStyle());
		  getAppearance().setTexture(textNode.getAppearance().getTexture());
		  getAppearance().getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.NICEST);
		
		  //if(textOS.getGeometry()!=null && textOS.numGeometries()!=0) textOS.removeGeometry(0);
		  textOS = new OrientedShape3D();
		  textOS.setCapability(OrientedShape3D.ALLOW_SCALE_WRITE);
		  textOS.setCapability(OrientedShape3D.ALLOW_GEOMETRY_WRITE);
		  textOS.setCapability(OrientedShape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
		  textOS.setCapability(OrientedShape3D.ALLOW_APPEARANCE_WRITE);
		  textOS.setRotationPoint(0, 0, 0);
		  textOS.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
		  textOS.setConstantScaleEnable(true);
		  textOS.setGeometry(textNode.getGeometry());
		  textOS.setAppearance(getAppearance());
		  setOSScale();
	  }
	  else{
		 Font3D font3d = new Font3D(font,  new FontExtrusion(gp));
		 //textNode3d = new Text3D(font3d, textElement.getText(), new Point3f(0.0f,-(float)textElement.getSizeZ()/3.5f,-(float)textElement.getSizeY()/3.0f));
		 textNode3d = new Text3D(font3d, textElement.getText(), new Point3f(0.0f, 0.0f, 0.0f));
		 textNode3d.setAlignment(textElement.getStyle().getRelativePosition());
		 shape = new Shape3D();
		 shape.setGeometry(textNode3d);
		 shape.setAppearance(getAppearance());
	  }
		
	  //if (localbg!=null && localbg.numChildren()!=0) localbg.removeChild(0);
	  bg = new BranchGroup();
	  bg.setCapability(BranchGroup.ALLOW_DETACH);
	  if(((ElementText)element).isTrueSize()) bg.addChild(textOS);
	  else{
		  bg.addChild(shape);
		  t1 = new Transform3D();
		  t2 = new Transform3D();
		  t1.rotX(Math.PI/2);
		  t2.rotY(Math.PI/2);
		  t1.mul(t2);
		  t1.setScale(0.06);
		  textTG.setTransform(t1);
	  }
	  textTG.addChild(bg);
	  addNode(textTG);
  }
  
  protected void updateText(double distanceToScreen) {
		double distance = distanceToScreen;
		if(distance<nearDistance) scale = (float)(factorDistance*distance*preferredFrameSize/element.getPanel().getComponent().getWidth());
		else scale = (float)(factorDistance*distance*preferredFrameSize/element.getPanel().getComponent().getWidth());
		double max = Math.max(element.getSizeX(), Math.max(element.getSizeY(), element.getSizeZ()));
		if(textOS!=null) textOS.setScale(scale*max);
  }
  
  private void changeSize(){
	if(t1==null) return;
	t1.setScale(0.06*Math.max(element.getPanel().getPreferredMaxX(), Math.max(element.getPanel().getPreferredMaxY(), element.getPanel().getPreferredMaxY())));
	textTG.setTransform(t1);
  }
  
  

}

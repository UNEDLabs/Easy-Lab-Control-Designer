/*
 * Created on Jun 13, 2005
 */
package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.*;
import javax.vecmath.*;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.*;

/**
 * <p>Title: Java3dElementArrow</p>
 * <p>Description: An arrow using Java 3D. This arrow sets with a Java3dElementSegment and a Cone primitive object.  
 * The cones' sizes are set to scaled down sizes of segment.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementArrow extends Java3dElementSegment {
	
  /**
  * The proportion of size between the Arrow size and Cone size
  */
  static private final double HEAD_SCALE = 0.075;
  
  private double size = 1.25;//3.5;
  private TransformGroup coneTg;
  private Transform3D coneTransform;

	public Java3dElementArrow(ElementArrow _element) {
		super(_element);
		// Add the cone at the top
		element.getStyle().setDrawingFill(true);
		Cone cone = new Cone(0.5f, 1.0f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, 
								10, 10, getAppearance());
		size = ((ElementArrow)this.element).getHeadSize();
		coneTransform = new Transform3D();
		coneTg = new TransformGroup();
		coneTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		coneTg.setCapability(Group.ALLOW_CHILDREN_WRITE);
		coneTg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		coneTg.setTransform(coneTransform);
		coneTg.addChild(cone);
		
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(coneTg);
		getBranchGroup().addChild(bg);
		resetConePosition();
	}
	
  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    super.processChanges(_change, _cummulativeChange);
    if ((_change & Element.CHANGE_INTERACTION_POINTS)!=0) resetConePosition(); 
    //if ((_change & Element.CHANGE_SIZE)!=0) resetConePosition(); 
  }
  
  public void styleChanged(int _change) {
	  super.styleChanged(_change);
	  switch(_change) {
      	case Style.CHANGED_LINE_WIDTH:
      		size = 1.25;
      		if(element.getStyle().getLineWidth()>1){
      			size  = size + (element.getStyle().getLineWidth()-1)*0.5;
      		}
      		resetConePosition();
      		break;
	  }
  }

  // -------------------------------------
  // Implementation of Java3dElement
  // -------------------------------------

  @Override
  public boolean isPrimitive() { return false; }

  // -------------------------------------
  // private methods
  // -------------------------------------

  /**
   * Places and sizes the cone in the right place
   */
	private void resetConePosition() {
		
		//Original cone is drawn on the K axis so rotate it to the direction of getSize()
		Vector3d v = new Vector3d(0,1,0);
		Vector3d norm = new Vector3d(getSizeVector());
		norm.normalize();
	
		norm.x = (Double.isNaN(norm.x))? 0.0 : norm.x;
		norm.y = (Double.isNaN(norm.y))? 0.0 : norm.y;
		norm.z = (Double.isNaN(norm.z))? 0.0 : norm.z;
		
		AxisAngle4d axisAngle = new AxisAngle4d();
		axisAngle.angle = Math.acos(v.dot(norm));
		
		Vector3d sizeVector = getSizeVector();
		if(sizeVector.x==0.0 && sizeVector.y<0 && sizeVector.z==0.0) sizeVector.z = 0.00001;
		v.cross(v, sizeVector);
		axisAngle.x = v.x;
		axisAngle.y = v.y;
		axisAngle.z = v.z;
		
		//Move the cone to the end of the arrow using what we know about the direction of the arrow
		double coneLength = sizeVector.length()*HEAD_SCALE*size;
		norm.scale(sizeVector.length()*HEAD_SCALE*0.5);
		Vector3d pos = new Vector3d();
		pos.x = getPositionVector().x; pos.y = getPositionVector().y; pos.z = getPositionVector().z;
		/*if(element.getStyle().getRelativePosition()==Style.CENTERED){
			pos.x = pos.x-element.getSizeX()/2;
			pos.y = pos.y-element.getSizeY()/2;
			pos.z = pos.z-element.getSizeZ()/2;
		}
		if(element.getStyle().getRelativePosition()==Style.SOUTH_WEST){
			pos.x = pos.x-element.getSizeX();
			pos.y = pos.y-element.getSizeY();
			pos.z = pos.z-element.getSizeZ();
			axisAngle.x = -axisAngle.x; axisAngle.y = -axisAngle.y; axisAngle.z = -axisAngle.z; 
			axisAngle.angle = Math.PI - axisAngle.angle;  
		}*/
		norm.sub(pos);
		norm.sub(sizeVector, norm);
		
		coneTransform.set(axisAngle);
		coneTransform.setTranslation(norm);
		coneTransform.setScale(coneLength);
		coneTg.setTransform(coneTransform);
	}
	
	
}

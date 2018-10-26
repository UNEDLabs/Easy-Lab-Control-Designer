package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementShape;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

/**
 * <p>Title: Java3dElementShape</p>
 * <p>Description: A particle (box, cone or sphere) using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementShape extends Java3dElement{

	private BranchGroup bg;

	public Java3dElementShape(ElementShape _element){
		super(_element);
		_element.addChange(Element.CHANGE_SHAPE);
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		
		if ((_change & Element.CHANGE_SHAPE) != 0 ) {
			if( ((ElementShape)element).getShapeType()==ElementShape.ELLIPSE || 
					((ElementShape)element).getShapeType()==ElementShape.WHEEL) createEllipsoid();
			if(((ElementShape)element).getShapeType()==ElementShape.RECTANGLE) createBox();
			if(((ElementShape)element).getShapeType()==ElementShape.ROUND_RECTANGLE) createCone();
			if (((ElementShape)element).getShapeType()==ElementShape.NONE) createNone();
		}
	}
	
	private void createEllipsoid(){
		Sphere sphere = new Sphere(0.5f, Primitive.GENERATE_NORMALS | 
			       					       Primitive.GENERATE_TEXTURE_COORDS | 
			       						     Primitive.ENABLE_GEOMETRY_PICKING, 50);
		sphere.setAppearance(getAppearance());
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(sphere);
		addNode(bg);
	}
	
	private void createBox(){
		//box = new ElementBox();
		//Java3dElementBox boxJ3d = new Java3dElementBox(box);
		Box box = new Box(0.5f, 0.5f, 0.5f, Primitive.GENERATE_NORMALS | 
												Primitive.GENERATE_TEXTURE_COORDS |
												  Primitive.ENABLE_GEOMETRY_PICKING, getAppearance());
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		//bg.addChild(boxJ3d.getShape());
		bg.addChild(box);
		addNode(bg);
	}
	
	private void createCone(){
		Cone cone = new Cone(0.5f, 0.75f, Primitive.GENERATE_NORMALS | 
			       							Primitive.GENERATE_TEXTURE_COORDS | 
			       								Primitive.ENABLE_GEOMETRY_PICKING, 50, 50, getAppearance());
		Transform3D t = new Transform3D();
		t.rotX(Math.PI/2);
		TransformGroup tg = new TransformGroup();
		tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
		tg.setTransform(t);
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(cone);
		tg.addChild(bg);
		addNode(tg);
	}
	private void createNone(){
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		addNode(bg);
	}
	
	public boolean isPrimitive() {
		if( ((ElementShape)element).getShapeType()==ElementShape.RECTANGLE) return false;
		return true;
	}
}

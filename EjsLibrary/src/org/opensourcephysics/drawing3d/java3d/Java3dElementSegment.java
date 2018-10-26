package org.opensourcephysics.drawing3d.java3d;


import javax.media.j3d.LineArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import org.opensourcephysics.drawing3d.Element;

/**
 * <p>Title: Java3dElementSegment</p>
 * <p>Description: A segment using Java 3D</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementSegment extends Java3dElement {
	
	LineArray line = null;
	
	public Java3dElementSegment(Element _element) {
		super(_element);

		line = new LineArray(4, LineArray.COORDINATES | LineArray.COLOR_3 | LineArray.TEXTURE_COORDINATE_2);
		line.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
		line.setCapability(LineArray.ALLOW_TEXCOORD_WRITE);

		/*line.setCoordinate(0, new Point3d(0,0,0));
		line.setCoordinate(1, new Point3d(0,0,0));
		line.setCoordinate(2, new Point3d(0,0,0));
		line.setCoordinate(3, new Point3d(0,0,0));*/
			line.setCoordinate(0, new Point3d(0,0,0));
				line.setCoordinate(1, new Point3d(0.5,0.5,0.5));
				line.setCoordinate(2, new Point3d(0.5,0.5,0.5));
				line.setCoordinate(3, new Point3d(1,1,1));
		
		TexCoord2f texCoord[]  = new TexCoord2f[4];
		texCoord[0] =  new TexCoord2f(0.0f, 0.0f);
		texCoord[1] =  new TexCoord2f((float)(1/Math.sqrt(3.0)),(float)(1/Math.sqrt(3.0)));
		texCoord[2] =  new TexCoord2f((float)(1/Math.sqrt(3.0)),(float)(1/Math.sqrt(3.0)));
		texCoord[3] =  new TexCoord2f(1.0f, 1.0f);
		line.setTextureCoordinate(0, 0, texCoord[0]);
		line.setTextureCoordinate(0, 1, texCoord[1]);
		line.setTextureCoordinate(0, 2, texCoord[2]);
		line.setTextureCoordinate(0, 3, texCoord[3]);

		getAppearance().getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		getAppearance().getLineAttributes().setLineAntialiasingEnable(false);
    
		addNode(new Shape3D(line, getAppearance()));
	}
	
  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------
	/*public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
			if(element.getStyle().getRelativePosition()==Style.CENTERED){
 				line.setCoordinate(0, new Point3d(-0.5,-0.5,-0.5));
 				line.setCoordinate(1, new Point3d(0,0,0));
 				line.setCoordinate(2, new Point3d(0,0,0));
 				line.setCoordinate(3, new Point3d(0.5,0.5,0.5));
 			}
 			if(element.getStyle().getRelativePosition()==Style.SOUTH_WEST){
 				line.setCoordinate(0, new Point3d(1,1,1));
 				line.setCoordinate(1, new Point3d(0.5,0.5,0.5));
 				line.setCoordinate(2, new Point3d(0.5,0.5,0.5));
 				line.setCoordinate(3, new Point3d(0,0,0));
 			}
 			if(element.getStyle().getRelativePosition()==Style.NORTH_EAST){
 				line.setCoordinate(0, new Point3d(0,0,0));
 				line.setCoordinate(1, new Point3d(0.5,0.5,0.5));
 				line.setCoordinate(2, new Point3d(0.5,0.5,0.5));
 				line.setCoordinate(3, new Point3d(1,1,1));
 			}
	}*/
	
	
  // -------------------------------------
  // Implementation of Java3dElement
  // -------------------------------------

  @Override
  public boolean isPrimitive() { return false; }
	
	
}

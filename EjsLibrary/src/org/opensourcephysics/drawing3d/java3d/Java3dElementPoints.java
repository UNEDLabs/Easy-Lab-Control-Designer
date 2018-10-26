package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPoints;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: Java3dElementPoints</p>
 * <p>Description: A group of points using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */ 
public class Java3dElementPoints extends Java3dElement {

	//Java 3D variables
	private PointArray points;
	private Shape3D shape;
	private Appearance app = null;
	
	public Java3dElementPoints (ElementPoints _element){
		super(_element);
		//getAppearance().getPointAttributes().setPointSize(1.0f);
		//getAppearance().getPointAttributes().setPointAntialiasingEnable(true);
		app = new Appearance();
		app.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_WRITE);
		PointAttributes pat = new PointAttributes();
		pat.setCapability(PointAttributes.ALLOW_SIZE_WRITE);
		pat.setCapability(PointAttributes.ALLOW_ANTIALIASING_WRITE);
		app.setPointAttributes(pat);
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		
	if ((_change & Element.CHANGE_POSITION) != 0 ) {
      ElementPoints elP = (ElementPoints) element;
      double[][] data = elP.getData();
			int npoints = data.length;
			if(npoints<=0) return;
			points = new PointArray(npoints, PointArray.COORDINATES|PointArray.COLOR_3); 
//			                  | PointArray.ALLOW_VERTEX_ATTR_READ
//			                  |PointArray.ALLOW_VERTEX_ATTR_WRITE);
			points.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
			for(int n = 0; n < npoints;  n++){
				points.setCoordinate(n, new Point3d(data[n][0],data[n][1],data[n][2]));
				points.setColor(n, new Color3f(elP.getPointColor(n)));
			}
			shape = new Shape3D(points, this.app);
			addNode(shape);
		}
		if ((_change & Element.CHANGE_COLOR) != 0) {
			ElementPoints elP = (ElementPoints) element;
			double[][] data = elP.getData();
			int npoints = data.length;
				for(int n = 0; n < npoints;  n++) {
				points.setColor(n, new Color3f(elP.getPointColor(n)));
				
			}
		}
	}
	
	 public void styleChanged(int _change) {
		 super.styleChanged(_change);
		 switch (_change) {
		    case Style.CHANGED_LINE_WIDTH:
		    	ElementPoints elP = (ElementPoints) element;
		    	app.getPointAttributes().setPointSize(elP.getPointWidth(0));
		    	app.getPointAttributes().setPointAntialiasingEnable(true);
			break;
		 }
	 }
		
	@Override
	public boolean isPrimitive() {return false;}

}

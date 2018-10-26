/*
 * Created on March 12, 2006
 */

package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementEllipsoid;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;


/**
 * <p>Title: Java3dElementEllipsoid</p>
 * <p>Description: An ellipsoid with the same size in all dimensions using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementEllipsoid extends Java3dElement  {
	
   //Java3d variables
   private Sphere sphere;
   private TransformGroup tg;
   private BranchGroup bg;
      
   //Configuration
   double[][][] standardSphere;
   private int nr=0, nu=0, nv=0;  
  
	
   public Java3dElementEllipsoid(ElementEllipsoid _element) {
	   super(_element);

	   
	   Transform3D t1 = new Transform3D();
	   t1.rotX(Math.PI/2);
	   tg = new TransformGroup();
	   tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	   tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
	   tg.setTransform(t1);
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		
		if ((_change & Element.CHANGE_RESOLUTION) != 0) {
			if (element.getStyle().getResolution().getType() == Resolution.MAX_LENGTH){
				double maxRadius = Math.max(Math.max(Math.abs(element.getSizeX()), Math.abs(element.getSizeY())), Math.abs(element.getSizeZ()))/2;
		        nr = Math.max((int) Math.round(0.49+maxRadius/element.getStyle().getResolution().getMaxLength()), 1);
		        nu = Math.max((int) Math.round(0.49+Math.abs(((ElementEllipsoid)element).getMaximumAngleU()-((ElementEllipsoid)element).getMinimumAngleU())*Element.TO_RADIANS*maxRadius/element.getStyle().getResolution().getMaxLength()), 1);
		        nv = Math.max((int) Math.round(0.49+Math.abs(((ElementEllipsoid)element).getMaximumAngleV()-((ElementEllipsoid)element).getMinimumAngleV())*Element.TO_RADIANS*maxRadius/element.getStyle().getResolution().getMaxLength()), 1);
			}
			else{
				nr = Math.max(element.getStyle().getResolution().getN1(), 1);
			    nu = Math.max(element.getStyle().getResolution().getN2(), 1);
			    nv = Math.max(element.getStyle().getResolution().getN3(), 1);
			}
			
			if(((ElementEllipsoid) element).checkStandarEllipsoid() && element.getStyle().isDrawingFill()){
				this.primitive = true;
				createPrimitiveEllipsoid(2*Math.max(nu, nv));
			}
			else{
				this.primitive = false;
				createTileEllipsoid();
			}
			styleChanged(Style.CHANGED_TEXTURES);
		}
		
		if ((_change & Element.CHANGE_SHAPE)!= 0 ){
			if(((ElementEllipsoid) element).checkStandarEllipsoid() && element.getStyle().isDrawingFill()) {
				if(primitive) return;
				primitive = true;
				createPrimitiveEllipsoid(2*Math.max(nu, nv));
			}
			else{
				primitive = false;
				createTileEllipsoid();
			}
			styleChanged(Style.CHANGED_TEXTURES);
		}
	}

	
	
	private void createPrimitiveEllipsoid(int _divisions){
		if (sphere!= null || standardSphere!= null) tg.removeChild(bg);
		
		sphere = new Sphere(0.5f, Primitive.GENERATE_NORMALS| 
									Primitive.GENERATE_TEXTURE_COORDS| 
									    Primitive.ENABLE_GEOMETRY_PICKING, _divisions);

		sphere.setAppearance(getAppearance());
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(sphere);
		tg.addChild(bg);
		addNode(tg);
	}
	
	
	private void createTileEllipsoid (){
  
	 standardSphere = ElementEllipsoid.createStandardEllipsoid(nr, nu, nv, 
			 		   ((ElementEllipsoid)element).getMinimumAngleU(), ((ElementEllipsoid)element).getMaximumAngleU(), ((ElementEllipsoid)element).getMinimumAngleV(), ((ElementEllipsoid)element).getMaximumAngleV(),  
				           ((ElementEllipsoid)element).isClosedTop(), ((ElementEllipsoid)element).isClosedBottom(), ((ElementEllipsoid)element).isClosedLeft(), ((ElementEllipsoid)element).isClosedRight());
		int totalN = standardSphere.length;
		int tileSize = standardSphere[0].length;
		Point3d coords[] = new Point3d[totalN*tileSize*2];
		
		for(int n=0; n<totalN; n++){
	 		for(int j=0; j<tileSize; j++){
	 			coords[n*tileSize+j+totalN*tileSize] = new Point3d(standardSphere[n][j][0],standardSphere[n][j][1],standardSphere[n][j][2]);      
	 			if(j==0) coords[n*tileSize+j+3] = new Point3d(standardSphere[n][j][0],standardSphere[n][j][1],standardSphere[n][j][2]);
	 			else if(j==1) coords[n*tileSize+j+1] = new Point3d(standardSphere[n][j][0],standardSphere[n][j][1],standardSphere[n][j][2]);
	 			else if(j==2) coords[n*tileSize+j-1] = new Point3d(standardSphere[n][j][0],standardSphere[n][j][1],standardSphere[n][j][2]);
	 			else coords[n*tileSize+j-3] = new Point3d(standardSphere[n][j][0],standardSphere[n][j][1],standardSphere[n][j][2]);
	 	 	}
		}
		                    
	    GeometryInfo gi =new GeometryInfo (GeometryInfo.QUAD_ARRAY);
		gi.setCoordinates(coords);
			
		NormalGenerator ng = new NormalGenerator();
		ng.setCreaseAngle((float) Math.toRadians(40));		
		ng.generateNormals(gi);
				
		GeometryArray figure = gi.getGeometryArray();
		Shape3D shape = new Shape3D(figure,this.getAppearance());
		
		addNode(shape);
	}
	
	public void styleChanged(int _change) {
		super.styleChanged(_change);
		 if(_change==Style.CHANGED_DRAWING_LINES || _change==Style.CHANGED_DRAWING_FILL){
	        if (element.getStyle().isDrawingLines() && !element.getStyle().isDrawingFill()){
	        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
	        	primitive = false;
	        	createTileEllipsoid();
	        }
	        if(element.getStyle().isDrawingFill() && ((ElementEllipsoid) element).checkStandarEllipsoid()){
	        	if(primitive) return;
	        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
	        	primitive = true;
				createPrimitiveEllipsoid(2*Math.max(nu, nv));
	        }
		 }
	}


	@Override
	public boolean isPrimitive() { return primitive;}
	
}


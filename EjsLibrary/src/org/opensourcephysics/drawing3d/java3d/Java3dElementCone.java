package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementCone;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Primitive;

/**
 * <p>Title: Java3dElementCone</p>
 * <p>Description: A cone using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */ 
public class Java3dElementCone extends Java3dElement {

	  //Java3d variables
	   private Cone cone;
	   private TransformGroup tg;
	   private BranchGroup bg;
	   private Transform3D t,tq;
	   private Quat4d quat;
	      
	   //Configuration
	   double[][][] standardCone;
	   private int nr=0, nu=0, nv=0;  
		
	   public Java3dElementCone(ElementCone _element) {
		   super(_element);
		   t = new Transform3D();
		   t.rotX(Math.PI/2);
		   quat = new Quat4d(element.getPanel().getCamera().getQuatMapping());
		   tq = new Transform3D();
		   tq.set(quat);
		   t.mul(tq);
		   tg = new TransformGroup();
		   tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		   tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
		   tg.setTransform(t);
		}
		
		public void processChanges(int _change, int _cummulativeChange) {
			super.processChanges(_change, _cummulativeChange);
			
			if ((_change & Element.CHANGE_RESOLUTION) != 0) {
				if (element.getStyle().getResolution() == null) return;
				
				int xDivisions, yDivisions;
				if (element.getStyle().getResolution().getType() == Resolution.DIVISIONS) {
					xDivisions = Math.max(element.getStyle().getResolution().getN2(), 1);
					yDivisions = Math.max(element.getStyle().getResolution().getN1(), 1);
				}
				else { 
					double dx = Math.abs(element.getSizeX())/2, dy = Math.abs(element.getSizeY())/2; 
					yDivisions = Math.max((int) Math.round(0.49+Math.max(dx, dy)/element.getStyle().getResolution().getMaxLength()), 1);
					xDivisions = Math.max((int) Math.round(0.49+2*Math.PI*(dx+dy)/element.getStyle().getResolution().getMaxLength()), 3);
				}
				
				nr = nv = yDivisions;
				nu = xDivisions;
				
				if(((ElementCone)element).checkStandarCone() && element.getStyle().isDrawingFill()){
					this.primitive = true;
					createPrimitiveCone();					
				}
				else{
					this.primitive = false; 
					createTileCone();
				}
				styleChanged(Style.CHANGED_TEXTURES);
			}
			
			if ((_change & Element.CHANGE_SHAPE) != 0 ) {
				if(((ElementCone) element).checkStandarCone() && element.getStyle().isDrawingFill()) {
					if(primitive) return;
					primitive = true;
					createPrimitiveCone();
				}
				else{
					primitive = false;
					createTileCone();
				}
				styleChanged(Style.CHANGED_TEXTURES);
			}
		}

		private void createPrimitiveCone(){
			if (cone!= null || standardCone!= null) tg.removeChild(bg);
			
			cone = new Cone(0.5f, 1.0f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, 
							 nu, nr, getAppearance());
			bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(cone);
			
			tg = new TransformGroup();
			tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
			tg.setTransform(t);
			
			tg.addChild(bg);
			addNode(tg);
		}
		
		private void createTileCone (){
	  
			//Compute corners
			double height = ((ElementCone)element).getTruncationHeight()/element.getSizeZ();
		    if(!Double.isNaN(height)) height = Math.min(height, 1.0);
			standardCone = ElementCone.createStandardCone(nr, nu, nv, 
														 ((ElementCone)element).getMinimumAngle(), ((ElementCone)element).getMaximumAngle(), 
														  ((ElementCone)element).isClosedTop(), ((ElementCone)element).isClosedBottom(), 
														 	((ElementCone)element).isClosedLeft(), ((ElementCone)element).isClosedRight(), 
																height);
			int totalN = standardCone.length;
			int tileSize = standardCone[0].length;
			Point3d coords[] = new Point3d[totalN*tileSize*2];
			
			for(int n = 0; n < totalN;  n++){
		 		for(int j=0;j<tileSize;j++){
		 			coords[n*tileSize+j+totalN*tileSize] = new Point3d(standardCone[n][j][0],standardCone[n][j][1],standardCone[n][j][2]);
		 			if(j==0)
			 			coords[n*tileSize+j+3] = new Point3d(standardCone[n][j][0],standardCone[n][j][1],standardCone[n][j][2]);
			 		else if(j==1)
			 			coords[n*tileSize+j+1] = new Point3d(standardCone[n][j][0],standardCone[n][j][1],standardCone[n][j][2]);
			 		else if(j==2)
			 			coords[n*tileSize+j-1] = new Point3d(standardCone[n][j][0],standardCone[n][j][1],standardCone[n][j][2]);
			 		else
			 			coords[n*tileSize+j-3] = new Point3d(standardCone[n][j][0],standardCone[n][j][1],standardCone[n][j][2]);
			 	 }  
			}
			                    
		    GeometryInfo gi =new GeometryInfo (GeometryInfo.QUAD_ARRAY);
			gi.setCoordinates(coords);
				
			NormalGenerator ng = new NormalGenerator();
			ng.setCreaseAngle((float) Math.toRadians(40)); // ángulo de pliegue 		
			ng.generateNormals(gi);
					
			GeometryArray figura = gi.getGeometryArray();
			Shape3D shape = new Shape3D(figura,this.getAppearance());
			
			bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(shape);
			
			tg = new TransformGroup();
			tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
			if(quat.z!=0.0 && quat.w!=0.0){//ZXY and ZYX mappings
				quat.y = -quat.z;
				quat.z = 0.0;
				tq.set(quat);
			}
			tg.setTransform(tq);
			tg.addChild(bg);
			
			addNode(tg);
		}

		public void styleChanged(int _change) {
			super.styleChanged(_change);
			 if(_change==Style.CHANGED_DRAWING_LINES || _change==Style.CHANGED_DRAWING_FILL){
		        if (element.getStyle().isDrawingLines() && !element.getStyle().isDrawingFill()){
		        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
		        	primitive = false;
		        	createTileCone();
		        }
		        if(element.getStyle().isDrawingFill() && ((ElementCone) element).checkStandarCone()){
		        	if(primitive) return;
		        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
		        	primitive = true;
					createPrimitiveCone();
		        }
			 }
		}
		
		@Override
		public boolean isPrimitive() { return primitive;}
}

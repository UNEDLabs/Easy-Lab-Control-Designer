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
import org.opensourcephysics.drawing3d.ElementCylinder;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Primitive;


/**
 * <p>Title: Java3dElementCylinder</p>
 * <p>Description: A cylinder using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementCylinder extends Java3dElement {
	
	   //Java 3D variables
	   private Cylinder cylinder;
	   private TransformGroup tg;
	   private BranchGroup bg;
	   private Transform3D t,tq;
	   private Quat4d quat;
	      
	   //Configuration
	   double[][][] standardCylinder;
	   private int nr=0, nu=0, nv=0;  
	   
	   public Java3dElementCylinder(ElementCylinder _element) {
		   super(_element);
		   t = new Transform3D();
		   t.rotX(-Math.PI/2);
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
				int slices, stacks;
				if (element.getStyle().getResolution().getType() == Resolution.MAX_LENGTH) {
					
					double dx = Math.abs(element.getSizeX())/2;
			        double dy = Math.abs(element.getSizeY())/2;
			        stacks = Math.max((int) Math.round(0.49+Math.max(dx, dy)/element.getStyle().getResolution().getMaxLength()), 1);
			        slices = Math.max((int) Math.round(0.49+Math.abs(((ElementCylinder)element).getMaximumAngle()-((ElementCylinder)element).getMinimumAngle())*Element.TO_RADIANS*(dx+dy)/element.getStyle().getResolution().getMaxLength()), 1);
			        //slices = Math.max((int) Math.round(0.49+Math.abs(element.getSizeZ())/element.getStyle().getResolution().getMaxLength()), 1);
					//slices = (int)Math.floor(Math.min(element.getSizeX(), element.getSizeZ()) / element.getStyle().getResolution().getMaxLength());
					//stacks = (int)Math.floor(element.getSizeY() / element.getStyle().getResolution().getMaxLength());
				}
				else {
					slices = element.getStyle().getResolution().getN2();
					stacks = element.getStyle().getResolution().getN3();
				}
				slices = Math.max(3, slices);
				stacks = Math.max(1, stacks);
				
				nr = nv = stacks;
				nu = slices;
				
				if(((ElementCylinder)element).checkStandarCylinder() && element.getStyle().isDrawingFill()){
					this.primitive = true;
					createPrimitiveCylinder();
				}
				else{
					this.primitive = false;
					createTileCylinder();
				}
				styleChanged(Style.CHANGED_TEXTURES);
			}
			
			if ((_change & Element.CHANGE_SHAPE) != 0 ) {
				if(((ElementCylinder)element).checkStandarCylinder() && element.getStyle().isDrawingFill()) {
					if(primitive) return;
					primitive = true;
					createPrimitiveCylinder();
				}
				else{
					this.primitive = false;
					createTileCylinder();
				}
				styleChanged(Style.CHANGED_TEXTURES);
			}
		}
		
		private void createTileCylinder(){
			standardCylinder = ElementCylinder.createStandardCylinder(nr, nu, nv, 
									((ElementCylinder)element).getMinimumAngle(), ((ElementCylinder)element).getMaximumAngle(), 
									  ((ElementCylinder)element).isClosedTop(), ((ElementCylinder)element).isClosedBottom(),((ElementCylinder)element).isClosedLeft(), ((ElementCylinder)element).isClosedRight());
			int totalN = standardCylinder.length;
			int tileSize = standardCylinder[0].length;
			Point3d coords[] = new Point3d[totalN*tileSize*2];
			for(int n = 0; n < totalN;  n++){
		 		for(int j=0;j<tileSize;j++){
		 			coords[n*tileSize+j+totalN*tileSize] = new Point3d(standardCylinder [n][j][0], standardCylinder[n][j][1], standardCylinder [n][j][2]);
		 			if(j==0)
			 			coords[n*tileSize+j+3] = new Point3d(standardCylinder[n][j][0],standardCylinder[n][j][1],standardCylinder[n][j][2]);
			 		else if(j==1)
			 			coords[n*tileSize+j+1] = new Point3d(standardCylinder[n][j][0],standardCylinder[n][j][1],standardCylinder[n][j][2]);
			 		else if(j==2)
			 			coords[n*tileSize+j-1] = new Point3d(standardCylinder[n][j][0],standardCylinder[n][j][1],standardCylinder[n][j][2]);
			 		else
			 			coords[n*tileSize+j-3] = new Point3d(standardCylinder[n][j][0],standardCylinder[n][j][1],standardCylinder[n][j][2]);
			 	 }  
			}
			                    
		    GeometryInfo gi = new GeometryInfo (GeometryInfo.QUAD_ARRAY);
			gi.setCoordinates(coords);
				
			NormalGenerator ng = new NormalGenerator();
			ng.setCreaseAngle((float) Math.toRadians(40)); 		
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
		
		private void createPrimitiveCylinder(){
			if (cylinder!= null || standardCylinder!= null) tg.removeChild(bg);
	
			cylinder = new Cylinder(0.5f, 1.0f, Primitive.GENERATE_NORMALS | Primitive.ENABLE_GEOMETRY_PICKING |
											     Primitive.GENERATE_TEXTURE_COORDS,  nu, nr, getAppearance());
											      
			bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(cylinder);
			
			tg = new TransformGroup();
			tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
			tg.setTransform(t);
			
			tg.addChild(bg);
			addNode(tg);
		}
		
		public void styleChanged(int _change) {
			super.styleChanged(_change);
			 if(_change==Style.CHANGED_DRAWING_LINES || _change==Style.CHANGED_DRAWING_FILL){
		        if (element.getStyle().isDrawingLines() && !element.getStyle().isDrawingFill()){
		        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
		        	primitive = false;
		        	createTileCylinder();
		        }
		        if(element.getStyle().isDrawingFill() && ((ElementCylinder) element).checkStandarCylinder()){
		        	if(primitive) return;
		        	if(nr==0 && nv==0 && nu==0) processChanges(Element.CHANGE_RESOLUTION,Element.CHANGE_NONE);
		        	primitive = true;
					createPrimitiveCylinder();
		        }
			 }
		}

		@Override
		public boolean isPrimitive() {return primitive;}
}

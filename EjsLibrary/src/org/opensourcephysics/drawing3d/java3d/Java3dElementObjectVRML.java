package org.opensourcephysics.drawing3d.java3d;

import java.util.Vector;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.ViewPlatform;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import loaders.vrml.VrmlLoader;
import loaders.vrml.VrmlScene;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.drawing3d.ElementObjectVRML;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;



/**
 * <p>Title: Java3dElementObjectVRML</p>
 * <p>Description: An element for the VRML97 external graphical file.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementObjectVRML extends Java3dElementObject {
	
	 static private final String WRL = ".wrl"; //$NON-NLS-1$
	 
	 private double scale = 1.0f;
	 private Point3d center = new Point3d(0,0,0);
	 private Point3d min = new Point3d(0,0,0);
	 private Point3d max = new Point3d(0,0,0);
	 private Shape3D shape = null;
	 private boolean changedShape = false;
	 //private BranchGroup bgShape = null;
	 
	 public Java3dElementObjectVRML(ElementObjectVRML _element) {
		 super(_element);
		 getAppearance().getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_LINE);
	 }
	 
	 public void processChanges(int _change, int _cummulativeChange) {
			super.processChanges(_change, _cummulativeChange);
			if ((_change & Element.CHANGE_SHAPE) != 0 ) {
				String file = ((ElementObject)element).getObjectFile().toLowerCase(); 
			    if (file.substring(file.length()-5).contains(WRL)){           
			        if (bg!=null && !changedShape) tg.removeChild(bg);
			        if (shape!=null && changedShape) tg.removeChild(shape);
			        try{
			          Resource resource = ResourceLoader.getResource(((ElementObject)element).getObjectFile());
			          VrmlLoader loader = new VrmlLoader();
			          VrmlScene scene = (VrmlScene) loader.load(resource.getURL());
			          //Set the Node inside DrawingPanel3D
			          bg = new BranchGroup();
			          bg.setCapability(BranchGroup.ALLOW_DETACH);
			          bg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			          bg.addChild(scene.getSceneGroup());
			          scale = getScale(bg);
			          center = getCenter(bg);
			          rX.rotX(ROTATION);
			          rX.rotZ(2.0f*ROTATION);
			          rX.setScale(MAX_SIZE/scale);
			          rX.setTranslation(new Vector3d(center.x/scale, center.y/scale, -center.z/scale));
			          tg.setTransform(rX);
			          tg.addChild(bg);
			          addNode(tg);
			        }catch (Exception exc) { /*exc.printStackTrace();*/System.out.println("VRML file incorrect");}
			    }
			}
			if ((_change & Element.CHANGE_VISIBILITY) != 0) makeVisible(this.element.isVisible());
	 }
	 
	  /**
	   * Used by JAVA 3D groups
	   * @param _visible
	   */
	 @Override
	 protected void makeVisible(boolean _visible) {
	   if(!_visible){if(tg.numChildren()>0) tg.removeChild(bg);}
	   else{ if(tg.numChildren()<1 && bg!=null) tg.addChild(bg);}
	 }
	 
	 /* public void styleChanged(int _change) {
		 if ((_change & Style.CHANGED_FILL_COLOR) != 0) {
			 if(shape==null || changedShape) return;
			 //System.out.println("Ver "+this.element.getStyle().getFillColor());
		 
			 if(element.getStyle().getFillColor()!=null){
				if(bg!=null) tg.removeChild(bg);
				if(element.getStyle().isDrawingLines())
					getAppearance().getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_LINE);
				else
					getAppearance().getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_FILL);
				shape.setAppearance(getAppearance());
				rX.setScale(MAX_SIZE/scale);
				rX.setTranslation(new Vector3d(center.x/scale, center.y/scale, -center.z/scale));
				tg.setTransform(rX);
				if (tg.indexOfChild(bgShape)<0){
					bgShape = new BranchGroup();
					bgShape.setCapability(BranchGroup.ALLOW_DETACH);
					bgShape.setCapability(Group.ALLOW_CHILDREN_EXTEND);
					bgShape.addChild(shape);
					tg.addChild(bgShape);
					addNode(tg);
				}
				changedShape = true;
			}
			else{
				if(shape!=null && changedShape) tg.removeChild(bgShape);
				if(!changedShape) return;
				rX.setScale(MAX_SIZE/scale);
				rX.setTranslation(new Vector3d(center.x/scale, center.y/scale, -center.z/scale));
				tg.setTransform(rX);
				if (tg.indexOfChild(bg)<0){
					tg.addChild(bg);
					addNode(tg);
				}
				changedShape = false;
			}
		 }
 	}*/
	 
	@SuppressWarnings("cast")
	private double getScale(BranchGroup branch){
		  	
	      Vector<Group> Vbg = new Vector<Group>();
	      Vector<Transform3D> Tbg = new Vector<Transform3D>();
	     
	      shape = new Shape3D(); 
	      shape.removeGeometry(0);
	      shape.setPickable(true);
	      shape.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
	      shape.setCapability(Shape3D.ALLOW_PICKABLE_READ);
	      shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
	      shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	      shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	      shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	      
	      max = new Point3d(0,0,0);
	      min = new Point3d(0,0,0);
	      int[] iElements = new int[100];
	      Node node; Group el; 
	      int level=0;
	      
	      Transform3D ident = new Transform3D();
	      ident.setIdentity();
	      
	      Vbg.insertElementAt(branch,0);
	      Tbg.insertElementAt(ident,0);
	      
	      while (level>=0 ){
	          el = (Group)Vbg.get(level);
	          if(iElements[level] < el.numChildren()){
	              node = el.getChild(iElements[level]);
	              iElements[level]++;
	              if(node instanceof Shape3D){
	                  if ((((Shape3D)node).getGeometry()) instanceof GeometryArray) {
	                       Geometry g = getMaximunGeometry((Transform3D)Tbg.get(level),(GeometryArray)((Shape3D)node).getGeometry());
	                       shape.addGeometry(g);
	                  }
	              }
	              else if(node instanceof Link){
	            	  SharedGroup s = ((Link)node).getSharedGroup();
	            	  Object obj = s.getChild(0);
	            	  if(!(obj instanceof Light)){
	            		  Group tempG = (Group)s.getChild(0); Node tempN;
	            		  for(int i=0;i<tempG.numChildren();i++){
	            			  tempN = tempG.getChild(i);
	            			  if(tempN instanceof Shape3D){
	            				  if ((((Shape3D)tempN).getGeometry()) instanceof GeometryArray) {
	            					  Geometry g = getMaximunGeometry((Transform3D)Tbg.get(level),(GeometryArray)((Shape3D)tempN).getGeometry());
	            					  shape.addGeometry(g);
	            				  }
	            			  }
	            		  }
	            	  }
	              }
	              else{
	            	  if(!(node instanceof ViewPlatform)){
	            		  Transform3D auxTrans = new Transform3D((Transform3D)Tbg.get(level));
	            		  Transform3D trans = new Transform3D(); trans.setIdentity();
	            		  level ++;
	                 	  if (node instanceof TransformGroup){
	                 		 ((TransformGroup)node).getTransform(trans);
	                 	  }
	                	  auxTrans.mul(trans);
	                	  Vbg.insertElementAt((Group)node,level);
	                	  Tbg.insertElementAt(auxTrans,level);
	            	  }
	              }
	          }
	          else {
	          	iElements[level]=0;
	            level--;
	          }
	      }
	  	  return getMaxValue();
	  }
	  
	  private double getMaxValue(){
		  double valueX = max.x - min.x;
		  double valueY = max.y - min.y;
		  double valueZ = max.z - min.z;
		  return Math.max(valueX, Math.max(valueY,valueZ));
	  }
	  
	  private GeometryArray getMaximunGeometry(Transform3D t,GeometryArray g){
	      
	      GeometryArray GeomOut;
	      Point3d point1 = new Point3d(); Point3d point2 = new Point3d();
	  	  int flags = GeometryArray.COORDINATES | GeometryArray.ALLOW_COLOR_READ | 
	  				    GeometryArray.ALLOW_COLOR_WRITE | GeometryArray.COLOR_3;
	      int npoints = g.getVertexCount();
	      int multipler = 0;
	      if(npoints%3!=0) multipler = 3-npoints%3;
	      GeomOut = new TriangleArray(npoints+multipler,flags);

	      for (int i=0; i<npoints; i++){
	          g.getCoordinate(i, point1);
	          t.transform(point1,point2);
	          getMaximun(point2);
	          GeomOut.setCoordinate(i,point2);
	      }      
	      return GeomOut;
	  }
	  
	  private void getMaximun(Point3d p){
	      if (p.x>max.x) max.x = p.x;
	      if (p.x<min.x) min.x = p.x;
	      if (p.y>max.y) max.y = p.y;
	      if (p.y<min.y) min.y = p.y;
	      if (p.z>max.z) max.z = p.z;
	      if (p.z<min.z) min.z = p.z;
	  }
	  
	  private Point3d getCenter(BranchGroup branch){
	      BoundingSphere sphere = (BoundingSphere)branch.getBounds();
	      Point3d c = new Point3d();
	      sphere.getCenter(c);
	      return c;
	  }
}

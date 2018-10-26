package org.opensourcephysics.drawing3d.java3d;



import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import loaders.ds3.Ds3Loader;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.drawing3d.ElementObject3DS;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;
import com.sun.j3d.loaders.Scene;

/**
 * <p>Title: Java3dElementObject3DS</p>
 * <p>Description: An element for the 3DS external graphical file.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementObject3DS extends Java3dElementObject {
	
	 //Type of file
	 private static String ThreeDS = ".3ds";
	 
	 public Java3dElementObject3DS(ElementObject3DS _element) {super(_element);}
	 
	 public void processChanges(int _change, int _cummulativeChange) {
			super.processChanges(_change, _cummulativeChange);
			if ((_change & Element.CHANGE_SHAPE) != 0 ) {
				String file = ((ElementObject)element).getObjectFile().toLowerCase(); 
				if (file.substring(file.length()-5).contains(ThreeDS)) {
					try {
					  Resource resource = ResourceLoader.getResource(((ElementObject)element).getObjectFile());
					  com.sun.j3d.loaders.Loader loader3ds;
					  loader3ds = new Ds3Loader();
					  Scene scene;
					  scene = loader3ds.load(resource.getURL());
					  BranchGroup tds = scene.getSceneGroup();
					  if (bg!=null) tg.removeChild(bg);
					  bg = new BranchGroup();
					  bg.setCapability(BranchGroup.ALLOW_DETACH);
					  bg.addChild(tds);
				      rX.rotX(ROTATION);
					    
					  BoundingSphere sceneBounds = (BoundingSphere) tds.getBounds();
					  Point3d center = new Point3d();
					  sceneBounds.getCenter(center);
					  double radius = sceneBounds.getRadius();
					  rX.setScale(MAX_SIZE/radius);
					  Vector3f temp = new Vector3f(-(float)center.x, (float)-center.y, (float)-center.z);
					  Transform3D trasT = new Transform3D();
					  trasT.setTranslation(temp);
					  rX.mul(trasT);
					  tg.setTransform(rX);
					  tg.addChild(bg);
					  addNode(tg);
					}catch (Exception exc) { exc.printStackTrace();}
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
	   else{if(tg.numChildren()<1 && bg!=null) tg.addChild(bg);}
	 }

}

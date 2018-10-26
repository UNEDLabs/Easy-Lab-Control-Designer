package org.opensourcephysics.drawing3d.java3d;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementObject;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

/**
 * <p>Title: Java3dElementObject</p>
 * <p>Description: An element for external graphical files.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementObject extends Java3dElement {

	 static protected final double MAX_SIZE = 1.0f;
	 static protected final double ROTATION = Math.PI/2;
	  
	 //Type of file
	 static private String OBJ = ".obj"; //$NON-NLS-1$
	 protected BranchGroup bg = null;
	 protected TransformGroup tg = null;
	  
	 private boolean noTriangulate = true;
	 private boolean noStripify = false;
	 private double creaseAngle = 60.0;
	 protected Transform3D rX = new Transform3D();

	  public Java3dElementObject(ElementObject _element) {
	    super(_element);
	    //Transform Group
	    tg = new TransformGroup();
	    tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
	    tg.setCapability(Group.ALLOW_CHILDREN_READ);
	    tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	    tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    rX.rotX(ROTATION);
	    tg.setTransform(rX);
	    if(((ElementObject)element).getObjectFile()!=null) element.addChange(Element.CHANGE_SHAPE);
	  }


	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if ((_change & Element.CHANGE_SHAPE) != 0 ) {
			String file = ((ElementObject)element).getObjectFile().toLowerCase();
			if (file.substring(file.length()-5).contains(OBJ)) {
				  Resource resource = ResourceLoader.getResource(((ElementObject)element).getObjectFile());
			      int flags = ObjectFile.RESIZE;
			      if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
			      if (!noStripify) flags |= ObjectFile.STRIPIFY;
			      ObjectFile f = new ObjectFile(flags, (float)(creaseAngle * Math.PI / 180.0));
			      Scene s = null;
			      try {  
			        s = f.load(resource.getURL());
			        Hashtable<?,?> namedObjects = s.getNamedObjects();
			        Enumeration<?> e =  namedObjects.keys();
			        while (e.hasMoreElements()) {
			          String name = (String) e.nextElement();
			          Shape3D shape = (Shape3D) namedObjects.get(name);
			          shape.setAppearance(getAppearance());
			        }
			        if (bg!=null) tg.removeChild(bg);
			        bg = new BranchGroup();
			        bg.setCapability(BranchGroup.ALLOW_DETACH);
			        bg.addChild(s.getSceneGroup());
			        tg.addChild(bg);
			        addNode(tg);
			      } catch (Exception exc) { System.out.println("Incorrect OBJ file:"+((ElementObject)element).getObjectFile());}//exc.printStackTrace();}
			}
		}
	}
	
	@Override
	public boolean isPrimitive() {return false;}

}

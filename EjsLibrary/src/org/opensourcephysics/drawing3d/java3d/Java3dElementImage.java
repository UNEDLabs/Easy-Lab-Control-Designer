package org.opensourcephysics.drawing3d.java3d;

import java.awt.Color;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementImage;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: Java3dElementImage</p>
 * <p>Description: An 3D image using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */ 
public class Java3dElementImage extends Java3dElement {

	private TexCoord2f texCoord[] = {new TexCoord2f(0.0f, 0.0f), new TexCoord2f(0.0f, 1.0f),
										new TexCoord2f(1.0f, 0.0f),new TexCoord2f(1.0f, 1.0f)};
	private QuadArray quad = null;
    private BranchGroup bg = null; 
    private Transform3D t3d = null;
    private TransformGroup tg = null;
    private double dz = 0.0, dy = 0.0;
    private double scale = 1.0f;

    public Java3dElementImage (ElementImage _element){
      this(_element,false);
    }

	public Java3dElementImage (ElementImage _element, boolean _changeOrientation){
		super(_element);
		element.getStyle().setDrawingLines(true);
		element.getStyle().setFillColor(Color.black);
		getAppearance().getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		
		quad = new QuadArray(4, QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2 | QuadArray.NORMALS);
       	quad.setCapability(QuadArray.ALLOW_COORDINATE_WRITE);
       	quad.setCoordinate(0, new Point3f(0.0f,-0.5f,-0.5f));
       	quad.setCoordinate(1, new Point3f(0.0f,0.5f,-0.5f));
       	quad.setCoordinate(2, new Point3f(0.0f,0.5f,0.5f));
       	quad.setCoordinate(3, new Point3f(0.0f,-0.5f,0.5f));
       	
       	if (_changeOrientation) {
       	  quad.setTextureCoordinate(0, 3, texCoord[0]);
       	  quad.setTextureCoordinate(0, 2, texCoord[2]);
       	  quad.setTextureCoordinate(0, 1, texCoord[3]);
       	  quad.setTextureCoordinate(0, 0, texCoord[1]);
       	}
       	else {
       	  quad.setTextureCoordinate(0, 0, texCoord[0]);
       	  quad.setTextureCoordinate(0, 1, texCoord[2]);
       	  quad.setTextureCoordinate(0, 2, texCoord[3]);
       	  quad.setTextureCoordinate(0, 3, texCoord[1]);
       	}

       	tg = new TransformGroup(); t3d = new Transform3D();
		t3d.rotY(((ElementImage)element).getRotationAngle());
		tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
		tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);;
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setTransform(t3d);
		addNode(tg);
		
		//Events to tranform from simple3d
		element.addChange(Element.CHANGE_SHAPE);
		styleChanged(Style.CHANGED_RELATIVE_POSITION);
	}
	
	@Override
	public boolean isPrimitive() {return true;}
	
	public void styleChanged(int _change) {
		if (_change==Style.CHANGED_RELATIVE_POSITION){
			double[] size = new double[3];
			size[0] = element.getSizeX();
			size[1] = element.getSizeY();
			size[2] = element.getSizeZ();
			
			switch (element.getStyle().getRelativePosition()) {
				default :
				case Style.CENTERED   : dz = 0; 		 dy = 0; 		  break;
				case Style.NORTH      : dz = -size[2]/2;  dy = 0;          break;
				case Style.SOUTH      : dz = size[2]/2; dy = 0;  	      break;
				case Style.EAST       : dz = 0;  		 dy = -size[1]/2;  break;
				case Style.WEST       : dz = 0;          dy = size[1]/2; break;
				case Style.NORTH_EAST : dz = -size[2]/2;  dy = -size[1]/2; break;
				case Style.NORTH_WEST : dz = -size[2]/2;  dy = size[1]/2;  break;
				case Style.SOUTH_EAST : dz = size[2]/2; dy = -size[1]/2; break;
				case Style.SOUTH_WEST : dz = size[2]/2; dy = size[1]/2;  break;
			}
			element.addChange(Element.CHANGE_PROJECTION);
		}
		else super.styleChanged(_change);
	}
	
	/*private boolean isPowerOf(int number, int base) {
	  while (number>=base) {
	    int divided = number/base;
	    if (number != (divided*base)) return false;
	    number = divided;
	  }
	  return number==1;
	}*/
	
	public void processChanges(int _change, int _cummulativeChange) {
	    super.processChanges(_change,_cummulativeChange);
	    if ((_change & Element.CHANGE_SHAPE) != 0) {
	    	
	    	if(((ElementImage)element).getImageFile()!=null) element.getStyle().setTexture(((ElementImage)element).getImageFile(), null, 0, false);
	    	else element.getStyle().setTexture(((ElementImage)element).getImage(), null, 0, false);
	    	Shape3D shape = new Shape3D(quad);
	    	shape.setAppearance(getAppearance());
	    	bg = new BranchGroup();
	    	bg.setCapability(BranchGroup.ALLOW_DETACH);
	    	bg.addChild(shape);
	    	tg.addChild(bg);
	    	//addNode(tg);
	    }
   
	    if ((_change & Element.CHANGE_PROJECTION) != 0) {
	    	t3d.setIdentity();
	    	t3d.rotY(((ElementImage)element).getRotationAngle());
	    	t3d.setTranslation(new Vector3d(0, dy, dz));
	    	t3d.setScale(scale);
            tg.setTransform(t3d);
           // addNode(tg);
	    }
	    if ((_change & Element.CHANGE_SIZE) != 0){
	    	if(((ElementImage)element).isTrueSize()) scale = Math.max(((ElementImage)element).getImage().getWidth(null)/150,((ElementImage)element).getImage().getHeight(null)/150);
	    	else scale = 1.0f;
	    	t3d.setIdentity();
	    	t3d.setTranslation(new Vector3d(0, dy, dz));
	    	t3d.setScale(scale);
            tg.setTransform(t3d);
           // addNode(tg);
	    }
	}
	
}

package org.opensourcephysics.drawing3d.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.Resolution;

/**
 * <p>Title: Java3dAbstractTile</p>
 * <p>Description: A tile using Java 3D.</p>
 * A tile is the basic abstract superclass for all Elements which 
 * consist of a sequence of 3D colored planar polygons (tiles). 
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */ 
public abstract class Java3dAbstractTile extends Java3dElement {
  // Subclasses must add the Element.CHANGE_SHAPE change when a recomputation
  // of vertex is required, such as when adding or removing vertex
	static protected final int RECOMPUTE_VERTEX = Element.CHANGE_SHAPE | Element.CHANGE_RESOLUTION;

	private int numberOfTiles = -1;
	private double[][][] standardTile = null;
	private Vector3f[] standardNormals = null;
	private QuadArray qa;
	private Shape3D shape;
	protected BranchGroup bg;
	
	private TexCoord2f texCoord[] = {new TexCoord2f(0.0f, 0.0f), new TexCoord2f(0.0f, 1.0f),
		                             new TexCoord2f(1.0f, 1.0f),new TexCoord2f(1.0f, 0.0f)};
			
	public Java3dAbstractTile(Element _element) {
		super(_element);

		//Construct shape with proper Appearance attachment
		shape = new Shape3D();
		shape.setAppearance(getAppearance());
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		prepareVertex();
		bg.addChild(shape);
		addNode(bg);
	}

  /**
   * Allocates space for and computes the coordinates of a standard tile of the given class
   * @param int _nx the resolution in the X dimension
   * @param int _ny the resolution in the Y dimension
   * @param int _nz the resolution in the Z dimension
   */
  abstract protected double[][][] createStandardTile(int _nx, int _ny, int _nz);

  /**
   * Allocates space for and computes the standard normals
   * @param int _nx the resolution in the X dimension
   * @param int _ny the resolution in the Y dimension
   * @param int _nz the resolution in the Z dimension
   */
  abstract protected Vector3f[] createStandardNormals(double[][][] data, int _nx, int _ny, int _nz);

  // -------------------------------------
  // Implementation of ImplementingObject
  // -------------------------------------

  @Override
  public void processChanges(int _change, int _cummulativeChange) {
    super.processChanges(_change, _cummulativeChange);
    if ((_change & RECOMPUTE_VERTEX)!=0) prepareVertex();
  }

  // -------------------------------------
  // Implementation of Java3dElement
  // -------------------------------------
  
  @Override
  public boolean isPrimitive() { return true; }
  
  // -------------------------------------
  // Private or protected methods
  // -------------------------------------
  
  protected void prepareVertex() {
    int nx = 1, ny = 1, nz = 1;
    Resolution res = element.getStyle().getResolution();
    if (res!=null) {
      switch (res.getType()) {
        case Resolution.DIVISIONS :
          nx = Math.max(res.getN1(), 1);
          ny = Math.max(res.getN2(), 1);
          nz = Math.max(res.getN3(), 1);
          break;
        case Resolution.MAX_LENGTH :
          nx = Math.max((int) Math.round(0.49+Math.abs(element.getSizeX())/res.getMaxLength()), 1);
          ny = Math.max((int) Math.round(0.49+Math.abs(element.getSizeY())/res.getMaxLength()), 1);
          nz = Math.max((int) Math.round(0.49+Math.abs(element.getSizeZ())/res.getMaxLength()), 1);
          break;
      }
    }
    
    //reallocate arrays
    standardTile = createStandardTile(nx, ny, nz);
    if(standardTile == null) return;
    numberOfTiles = standardTile.length; 
    if(numberOfTiles == 0) return;
    standardNormals = createStandardNormals(standardTile,nx,ny,nz);

    int sides = standardTile[0].length;
    qa = new QuadArray(numberOfTiles * sides * 2, GeometryArray.COORDINATES | 
    		             GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.ALLOW_TEXCOORD_WRITE); 
    qa.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    qa.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
    qa.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
    qa.setCapability(GeometryArray.ALLOW_NORMAL_WRITE);
    
    if (sides==4) {
      for (int i = 0, j = 0; j < numberOfTiles; j++) {
        qa.setCoordinate(i,standardTile[j][0]);   
        qa.setTextureCoordinate(0,i,texCoord[0]); 
        qa.setNormal(i++,standardNormals[j]);      
        
        qa.setCoordinate(i,standardTile[j][1]);
        qa.setTextureCoordinate(0,i,texCoord[1]); 
        qa.setNormal(i++,standardNormals[j]); 
        
        qa.setCoordinate(i,standardTile[j][2]);   
        qa.setTextureCoordinate(0,i,texCoord[2]); 
        qa.setNormal(i++,standardNormals[j]);     
     
        qa.setCoordinate(i,standardTile[j][3]);
        qa.setTextureCoordinate(0,i,texCoord[3]); 
        qa.setNormal(i++,standardNormals[j]);
        
        qa.setCoordinate(i,standardTile[j][3]);
        qa.setTextureCoordinate(0,i,texCoord[3]); 
        qa.setNormal(i++,opposite(standardNormals[j]));    
        
        qa.setCoordinate(i,standardTile[j][2]);
        qa.setTextureCoordinate(0,i,texCoord[2]); 
        qa.setNormal(i++,opposite(standardNormals[j]));
        
        qa.setCoordinate(i,standardTile[j][1]);
        qa.setTextureCoordinate(0,i,texCoord[1]); 
        qa.setNormal(i++,opposite(standardNormals[j]));     
        
        qa.setCoordinate(i,standardTile[j][0]);
        qa.setTextureCoordinate(0,i,texCoord[0]);
        qa.setNormal(i++,opposite(standardNormals[j]));
      }
    }
    else {
      for (int i = 0, j = 0; j < numberOfTiles; j++) {
        for (int k=0; k<sides; k++) {
          qa.setCoordinate(i,standardTile[j][k]); 
          qa.setTextureCoordinate(0,i,texCoord[k]); 
          qa.setNormal(i++,standardNormals[j]);
        }
        for (int k=sides-1; k>=sides; k--) {
           qa.setCoordinate(i,standardTile[j][k]); 
           qa.setTextureCoordinate(0,i,texCoord[k]); 
           qa.setNormal(i++,opposite(standardNormals[j]));
        }
      }
    }
    shape.setGeometry(qa);
  }
  
  static Vector3f opposite (Vector3f data){
	  Vector3f r = new Vector3f();
	  r.x = -data.x; r.y = -data.y; r.z = -data.z;
	  return r;
  }
  
}

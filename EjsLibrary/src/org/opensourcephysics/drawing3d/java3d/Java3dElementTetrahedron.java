package org.opensourcephysics.drawing3d.java3d;



import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Quat4d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTetrahedron;

/**
 * <p>Title: Java3dElementBox</p>
 * <p>Description: A tetrahedron using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */

public class Java3dElementTetrahedron extends Java3dElement {
	
	//Corner variables
	private double[][][] standardTetra = null;
	static private final float SQRT3 = (float) Math.sqrt(3.0);
	  
	//Java 3D variables
	private QuadArray tetra;
	private Shape3D shape;
    private Vector3f[] standardNormals = null;
    private TexCoord2f texCoord[] = null;
    private TransformGroup tg;
    private BranchGroup bg;
       
    
	public Java3dElementTetrahedron(ElementTetrahedron _element){
		super(_element);
		setData();
	}

	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if ((_change & Element.CHANGE_SHAPE) != 0 ) setData();
	}
   
	private void setData(){
		
		//Configuration variables
		int nvertex, nfaces;
		
		//Height
		double height = ((ElementTetrahedron)element).getTruncationHeight()/element.getSizeZ();
	    if(!Double.isNaN(height))  height = Math.min(height, 1.0);
	    
	    //reallocate arrays
	    standardTetra = createTile();
	    if(standardTetra == null) return;
	    nfaces = standardTetra.length; 
	    if(nfaces == 0) return;
	    nvertex = standardTetra.length * standardTetra[0].length;
	    standardNormals = createNormals(standardTetra);
	    
	    //Vertex
	    tetra = new QuadArray(nvertex*2, GeometryArray.COORDINATES | GeometryArray.NORMALS |
				     			          GeometryArray.TEXTURE_COORDINATE_2);
	    
	    for (int i = 0, j = 0; j < nfaces; j++) {
	    	tetra.setCoordinate(i,standardTetra[j][0]);
	    	tetra.setNormal(i++,standardNormals[j]);      
	
	    	tetra.setCoordinate(i,standardTetra[j][1]);
	    	tetra.setNormal(i++,standardNormals[j]); 
	        
	    	tetra.setCoordinate(i,standardTetra[j][2]);
	    	tetra.setNormal(i++,standardNormals[j]);     
	     
	    	tetra.setCoordinate(i,standardTetra[j][3]);
	    	tetra.setNormal(i++,standardNormals[j]);
	        
	    	tetra.setCoordinate(i,standardTetra[j][3]);
	    	tetra.setNormal(i++,Java3dAbstractTile.opposite(standardNormals[j]));    
	        
	    	tetra.setCoordinate(i,standardTetra[j][2]);
	    	tetra.setNormal(i++,Java3dAbstractTile.opposite(standardNormals[j]));
	        
	    	tetra.setCoordinate(i,standardTetra[j][1]);
	    	tetra.setNormal(i++,Java3dAbstractTile.opposite(standardNormals[j]));     
	        
	    	tetra.setCoordinate(i,standardTetra[j][0]);
	    	tetra.setNormal(i++,Java3dAbstractTile.opposite(standardNormals[j]));
	   }
	    
	   if(!Double.isNaN(height) && height<1){
	    	int[] serie = {1,4,5,0}; //0,3,4, 0,4,1, 0,3,4, 0,3,1, 0,4,3;
	    	texCoord = new TexCoord2f[6];
	    	texCoord[0] = new TexCoord2f(0.0f, 0.0f);
	        texCoord[1] = new TexCoord2f(1.0f, 0.0f);
	        
	        texCoord[2] = new TexCoord2f(0.5f, SQRT3 / 2.0f);
	        texCoord[3] = texCoord[2];
	        texCoord[4] = new TexCoord2f(0.5f *(1.0f+(float)height),SQRT3 / 2.0f);
	        texCoord[5] = new TexCoord2f(0.5f *(1.0f-(float)height), SQRT3 / 2.0f);
	        
	    	for (int i =0, j = 0; j < nfaces; j++){
	    		if(i<24){
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[0]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[1]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[2]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[3]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[3]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[2]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[1]]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[serie[0]]);
	    		}
	    		else{
	    			tetra.setTextureCoordinate(0, i++, texCoord[0]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[1]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[2]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[3]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[3]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[2]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[1]);
	    			tetra.setTextureCoordinate(0, i++, texCoord[0]);
	    		}
	    	}
	    }
	    else{
	    	texCoord = new TexCoord2f[4];
	        texCoord[0] = new TexCoord2f(0.0f, 0.0f);
	        texCoord[1] = new TexCoord2f(1.0f, 0.0f);
	        texCoord[2] = new TexCoord2f(0.5f, SQRT3 / 2.0f);
	        texCoord[3] = texCoord[2];
	        for (int i = 0, j = 0; j < nfaces; j++) { 
	        	tetra.setTextureCoordinate(0, i++, texCoord[0]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[1]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[2]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[3]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[3]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[2]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[1]);
	        	tetra.setTextureCoordinate(0, i++, texCoord[0]);
	        }
	    }
	 	shape = new Shape3D();
		shape.setGeometry(tetra);
		shape.setAppearance(getAppearance());
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(shape);
		Quat4d quat = new Quat4d(element.getPanel().getCamera().getQuatMapping());
		if(quat.z!=0.0 && quat.w!=0.0){//ZXY and ZYX mappings
			quat.y = -quat.z;
			quat.z = 0.0;
		}
		Transform3D tq = new Transform3D();
		tq.set(quat);
		tg = new TransformGroup();
		tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(Group.ALLOW_CHILDREN_WRITE);
		tg.setTransform(tq);
		tg.addChild(bg);
		addNode(tg);
	}
	
	@Override
	public boolean isPrimitive() {return true;}


	protected Vector3f[] createNormals(double[][][] data) {
		int nTotal = data.length;
		Vector3f[] normals = new Vector3f[nTotal];
		for(int i=0; i<nTotal; i++){
			Vector3f u1 = new Vector3f();
			Vector3f u2 = new Vector3f();
			if(!Double.isNaN(((ElementTetrahedron)element).getTruncationHeight())){ 
				u1.sub(new Vector3f((float)data[i][2][0], (float)data[i][2][1], (float)data[i][2][2]),
					    new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]));
				u2.sub(new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]),
						new Vector3f((float)data[i][1][0], (float)data[i][1][1], (float)data[i][1][2]));
			}
			else{
				u1.sub(new Vector3f((float)data[i][2][0], (float)data[i][2][1], (float)data[i][2][2]),
						new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]));
				u2.sub(new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]),
					new Vector3f((float)data[i][1][0], (float)data[i][1][1], (float)data[i][1][2]));
			}
			normals[i] = new Vector3f();
			normals[i].cross(u1, u2);
			normals[i].normalize();
		}
		return normals;
	}

	
	protected double[][][] createTile() {
		double height = ((ElementTetrahedron)element).getTruncationHeight()/element.getSizeZ();
	    if(!Double.isNaN(height))  height = Math.min(height, 1.0);
		standardTetra = ElementTetrahedron.createStandardTetrahedron(((ElementTetrahedron)element).isClosedTop(), ((ElementTetrahedron)element).isClosedBottom(), height);
		return standardTetra; 
	}

}


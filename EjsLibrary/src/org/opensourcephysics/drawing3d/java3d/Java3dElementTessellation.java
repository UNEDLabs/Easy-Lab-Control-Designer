package org.opensourcephysics.drawing3d.java3d;

import javax.vecmath.Vector3f;

import org.opensourcephysics.drawing3d.ElementTessellation;

/**
 * <p>Title: Java3dElementTessellation</p>
 * <p>Description: A group of tiles using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementTessellation extends Java3dAbstractTile {

	public Java3dElementTessellation(ElementTessellation _element){super(_element);}
	
	
	@Override
	protected Vector3f[] createStandardNormals(double[][][] data, int _nx,int _ny, int _nz) {
		int nTotal = data.length;
		Vector3f[] normals = new Vector3f[nTotal];
		for(int i=0; i<nTotal; i++){
			Vector3f u1 = new Vector3f();
			u1.sub(new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]),
					new Vector3f((float)data[i][1][0], (float)data[i][1][1], (float)data[i][1][2]));
			Vector3f u2 = new Vector3f();
			u2.sub(new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]),
					new Vector3f((float)data[i][3][0], (float)data[i][3][1], (float)data[i][3][2]));
			normals[i] = new Vector3f();
			normals[i].cross(u1, u2);
			normals[i].normalize();
		}
		return normals;
	}

	@Override
	protected double[][][] createStandardTile(int _nx, int _ny, int _nz) {
		return ((ElementTessellation) element).getTiles();
	}

}

package org.opensourcephysics.drawing3d.java3d;


import javax.vecmath.Vector3f;
import org.opensourcephysics.drawing3d.ElementBox;

/**
 * <p>Title: Java3dElementBox</p>
 * <p>Description: A box using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementBox extends Java3dAbstractTile {

  public Java3dElementBox(ElementBox _element) { super(_element);}

  // -------------------------------------
  // Private or protected methods
  // -------------------------------------

  @Override
  protected double[][][] createStandardTile(int _nx, int _ny, int _nz) {
    ElementBox box = (ElementBox) element;
    return ElementBox.createStandardBox(_nx, _ny, _nz, box.isClosedTop(), box.isClosedBottom(),box.getSizeZReduction());
  }

  protected Vector3f[] createStandardNormals(double[][][] data, int _nx, int _ny, int _nz) {
    ElementBox box = (ElementBox) element;
	boolean bottom = box.isClosedBottom(), top = box.isClosedTop();
    int nTotal = 2*_nx*_nz + 2*_ny*_nz;
	if (bottom) nTotal += _nx*_ny;
	if (top)    nTotal += _nx*_ny;
	Vector3f[] normals = new Vector3f[nTotal];
	
    int tile = 0;
		for (int i=0; i<_nx; i++) { // x-y sides
			for (int j=0; j<_ny; j++) {
				if(bottom) normals[tile++] = new Vector3f(0, 0, 1);//new Vector3f(0, 0, -1);
				if(top) normals[tile++] = new Vector3f(0, 0, 1);//new Vector3f(0, 0, 1);
			}
		}
		for (int i=0; i<_nx; i++) { // x-z sides
			for (int k=0; k<_nz; k++) {
				normals[tile++] = new Vector3f(0, -1, 0);//new Vector3f(-1, 0, 0);
				normals[tile++] = new Vector3f(0, -1, 0);//new Vector3f(1, 0, 0);
			}
		}
		for (int k=0; k<_nz; k++) { // y-z sides
			for (int j=0; j<_ny; j++) {
				normals[tile++] = new Vector3f(-1, 0, 0);//new Vector3f(0, -1, 0);
				normals[tile++] = new Vector3f(-1, 0, 0);//new Vector3f(0, 1, 0);
			}
		}
		return normals;
	}

}



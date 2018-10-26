package org.opensourcephysics.display;

import org.colos.freefem.PDEData;
import org.colos.freefem.PDEMesh;

public class MeshTools {

  /**
   * Passes data to a displayMesh
   * @param displayMesh
   * @return true if successfull, false otherwise
   */
  static public boolean show(PDEData data, org.opensourcephysics.display.Mesh displayMesh) {
    if (data==null) {
      displayMesh.setPoints(null);
      return true;
    }
    switch (data.getType()) {
      case MESH_2D : // 2D mesh
      case MESH_3D : // 3D mesh
        show(data.getProblemMesh(),displayMesh); 
        return true;
      case SCALAR_2D_FIELD : // scalar (real or complex) 2D solution
      case SCALAR_3D_FIELD : // scalar (real or complex) 3D solution
      case VECTOR_2D_FIELD : // vector (real or complex) 2D solution
      case VECTOR_3D_FIELD : // vector (real or complex) 3D solution
        show(data.getSolutionMesh(),displayMesh);
        displayMesh.setFieldAtCells(data.getSolutionValues());
        return true;
      default : // Any other case 
        return false;
    }
  }

  /**
   * Passes the information of this mesh to a Tile object
   * @param displayMesh
   */
  static public void show(PDEMesh mesh, org.opensourcephysics.display.Mesh displayMesh) {
    displayMesh.setPoints(mesh.getPoints());
    displayMesh.setCells(mesh.getCells());
    displayMesh.setBoundary(mesh.getBoundaryElements());
    displayMesh.setBoundaryLabels(mesh.getBoundaryLabels());
  }
  
}

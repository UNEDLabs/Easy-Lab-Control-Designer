package org.colos.freefem;

import java.util.HashMap;
import java.util.Map;

import org.colos.ejs.library.server.DataMapExportable;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

/**
 * PDEData encapsulates the information about the solution of a Partial Differential Equation
 * as solved by a FreeFem++ script. 
 * It consists of a PDEMesh plus the information of the solution of the problem in
 * either this mesh (the so called 'problem' mesh) or in a refinement of this mesh (called 'solution' mesh).
 */
public class PDEData implements DataMapExportable {
  public enum DataType { MESH_2D, MESH_3D, SCALAR_2D_FIELD, SCALAR_3D_FIELD, VECTOR_2D_FIELD, VECTOR_3D_FIELD };

	private DataType mType; 
	private PDEMesh mProblemMesh;
  private double[][] mValues;    // value in each point in the finalMesh points order  
  private double[][] mSubpoints; // points to refine the mProblemMesh into a finalMesh (P2 case)
  private int[][] mSubelements;   // relation between the subpoints to define the finalMesh cells

  private PDEMesh mSolutionMesh = null;
  private double[][][] mSolutionValues=null;    

  /**
   * Creates a new PDEData with the information obtained from running a FreeFem++ script
   * @param type The type of the data as returned by FreeFem++ (0 = 2D-mesh, 1/2 = scalar/vector 2D-solution, 5 = 3D-mesh, 6/7 = scalar/vector 3D-solution)
   * @param mesh The original PDEMesh used to state the problem
   * @param values The values of the solution as provided by FreeFem++
   * @param subpoints a double[][] array of coordinates to compute the refined, solution mesh
   * @param subelements an int[][] array with the indexes to compute the refined, solution mesh
   */
  public PDEData (long type, PDEMesh mesh, double[][] values, double[][] subpoints, int[][] subelements) {
    switch((int) type) {
      default : 
      case 0 : mType = DataType.MESH_2D; break;
      case 1 : mType = DataType.SCALAR_2D_FIELD; break;
      case 2 : mType = DataType.VECTOR_2D_FIELD; break;
      case 5 : mType = DataType.MESH_3D; break;
      case 6 : mType = DataType.SCALAR_3D_FIELD; break;
      case 7 : mType = DataType.VECTOR_3D_FIELD; break;
    }
    mProblemMesh = mesh;   
    mValues = values; // values from FreeFem are like {{0},{10},{0.5},{},{},{}}, is necessary to adapt them to the cells
    mSubpoints = subpoints;
    mSubelements = subelements;
    if (subelements==null) mSolutionMesh = mProblemMesh;
  }

  // -------------------------------
  // Getter methods
  // -------------------------------

  /**
	 * Returns the type of the data.
	 * @return
	 */
	public DataType getType() { return mType; }
	
	/**
	 * Returns the mesh used to create the data
	 * @return
	 */
	public PDEMesh getProblemMesh() { return mProblemMesh; }
  
	/**
	 * Returns the possibly refined mesh used to provide the solution 
	 * @return
	 */
	public PDEMesh getSolutionMesh() {
	  if (mSolutionMesh==null) computeSolutionMesh();
	  return mSolutionMesh;
	}
	
	/**
	 * Returns the double[nCells][nPointsInCell][dimension] array with the solution of the PDE in the solution mesh.
	 * The dimension can be one of :
   * <ul>
   *   <li>1 : real scalar field</li>
   *   <li>2 : complex scalar field</li>
   *   <li>3 : real 2D (the third coordinate is always 0) or 3D vector field </li>
   *   <li>4 : complex 2D vector field { r1,c1, r2,c2 }</li>
   *   <li>6 : complex 3D vector: { r1,c1, r2,c2, r3,c3 }</li>
   * </ul>
	 * @return
	 */
	public double[][][] getSolutionValues() {
	   if (mSolutionMesh==null) computeSolutionMesh();
	   return mSolutionValues;
	}

  // -----------------------------------
  // Private methods
  //-----------------------------------
  
  static private int hasOriginalIndex(int dim, double[] coordinates) {
    switch (dim) {
      default :
      case 2 : 
        if (coordinates[0]==0) {
          if (coordinates[1]==0) return 0; // it is p0
          if (coordinates[1]==1) return 2; // it is p2
        }
        else if (coordinates[0]==1 && coordinates[1]==0) return 1; // it is p1
        break;
      case 3 : 
        if (coordinates[0]==0) {
          if (coordinates[1]==0) {
            if (coordinates[2]==0) return 0; // it is p0
            if (coordinates[2]==1) return 3; // it is p3
          }
          else if (coordinates[1]==1 && coordinates[2]==0) return 2; // it is p2
        }
        else if (coordinates[0]==1 && coordinates[1]==0 && coordinates[2]==0) return 1; // it is p1
        break;
    }
    return -1;
  }
  
  private void computeSolutionMesh() {
    double[][] problemPoints = mProblemMesh.getPoints();
    int[][] problemCells = mProblemMesh.getCells();
    int nProblemCells = problemCells.length;
    int dim = (mType==DataType.SCALAR_2D_FIELD || mType==DataType.VECTOR_2D_FIELD) ? 2 : 3; // type can only be 1, 2, 6, or 7

    int nSolutionPoints = problemPoints.length + nProblemCells*(mSubpoints.length-dim-1); // each solution cell has mSubpoints.length points, but (dim+1) are original (problem) points
    int nSolutionCells = nProblemCells*mSubelements.length;
    double[][] solutionPoints = new double[nSolutionPoints][];
    int [][] solutionCells = new int[nSolutionCells][]; // each problem cell subdivides into mSubelements.length subcells
    mSolutionValues = new double[solutionCells.length][][];

    // The first (original) points are just copied
    System.arraycopy(problemPoints, 0,  solutionPoints, 0, problemPoints.length);
    // Now, compute the rest of the points and build the solution cells
    
    // Classify the subpoints
    int[] refIndexes = new int[dim+1];
    java.util.Set<Integer> newPointsList = new java.util.HashSet<Integer>();
    for (int p=0; p<mSubpoints.length; p++) { // for each subpoint (new point in the subcell) 
      double[] coordinates = mSubpoints[p];
      int originalIndex = hasOriginalIndex(dim,coordinates);
      if (originalIndex>=0) refIndexes[originalIndex] = p;
      else newPointsList.add(p);
    }
    
    int[] newIndexes = new int[mSubpoints.length]; // The indexes of the points in the new subcell
    int newPointIndex = problemPoints.length; // next point to add
    int newCellIndex = 0; // new cell to add

    for (int cellIndex = 0; cellIndex<nProblemCells; cellIndex++) { // for each problem cell
      int[] problemCell = problemCells[cellIndex];
      // Compute the subpoints
      for (int p=0; p<mSubpoints.length; p++) { // for each subpoint (new point in the subcell)
        double[] coordinates = mSubpoints[p];
        if (newPointsList.contains(p)) { // It's a new point
          double[] point =  solutionPoints[newPointIndex] = new double[coordinates.length];
          double[] origin = problemPoints[problemCell[0]];
          System.arraycopy(origin,0,point,0,dim);
          for (int i=0; i<coordinates.length; i++) { // for each vector in the coordinate system
            double[] endPoint = problemPoints[problemCell[i+1]];
            for (int j=0; j<dim; j++) point[j] += coordinates[i]*(endPoint[j]-origin[j]);
          }
          newIndexes[p] = newPointIndex;
          newPointIndex++;
        }
        else { // It is a point of the original mesh
          for (int k=0; k<refIndexes.length; k++) {
            if (refIndexes[k]==p) {
              newIndexes[p] = problemCell[k];
              break;
            }
          }
        }
      } // end for each subpoint
      // Build the subcell
      int offset = cellIndex*mSubpoints.length;
      for (int i=0; i<mSubelements.length; i++) {
        int[] subCell = mSubelements[i];
        int[] newCell = solutionCells[newCellIndex] = new int[subCell.length];
        double[][] newCellValues = mSolutionValues[newCellIndex] = new double[subCell.length][];
        for (int j=0; j<subCell.length; j++) {
          int point = subCell[j];
          newCell[j] = newIndexes[point];
          newCellValues[j] = mValues[offset+point];
        }
        newCellIndex++;
      }
    } // end for each problem cell
    
    mSolutionMesh = new PDEMesh(solutionPoints, solutionCells, mProblemMesh.getBoundaryElements(), mProblemMesh.getBoundaryLabels());
  } 
  
  
  // -----------------------------
  // JSON utilities
  // -----------------------------
  
  
  public Map<String,Object> toDataMap () {
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("type", mType.ordinal());
    dataMap.put("problem_mesh", mProblemMesh.toDataMap());
    dataMap.put("solution_mesh", mSolutionMesh.toDataMap());
    dataMap.put("solution_values", mSolutionValues);
    return dataMap;
  }


    
}//end class Plot

	
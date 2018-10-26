package org.colos.freefem.utils;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.colos.freefem.PDEData;
import org.colos.freefem.PDEMesh;

//import org.colos.freefem.PDEData;
//import org.colos.freefem.PDEMesh;

/**
 * Visudata is a technical object that reads data from a FreeFem script and builds a plot output 
 *
 */
public class Visudata {
  static final String FFFILEMAGIC = "ffglutdata3";

  static private final int PLOTSTREAM_DT_NEWPLOT = -5;
  static private final int PLOTSTREAM_DT_MESHES = -1;
  static private final int PLOTSTREAM_DT_MESHES3 = -10;
  static private final int PLOTSTREAM_DT_PLOTS = -2;
  static private final int PLOTSTREAM_DT_ENDARG = 99999;

  //MESH'S TAGS
  static private final String FF_2DMESH_BEGIN = "Mesh2::GSave v0";
  static private final String FF_3DMESH_BEGIN = "Mesh3::GSave v0";
  static private final String FF_MESH_END = "end";

  //PLOT'S TAGS
  static private final int PLOTSTREAM_DT_ENDPLOT = -3;	

  /**
   * Read input from the pipe
   * @param dis
   * @param plotOutput
   * @param difEndianness
   * @return true if successful, false otherwise
   * @throws IOException
   */
  static boolean readBlock(DataInputStream dis, PlotOutput plotOutput, boolean difEndianness) throws IOException {
    if (Readpipe.readLong(dis, difEndianness) != PLOTSTREAM_DT_NEWPLOT) return false;
    long code = Readpipe.readLong(dis, difEndianness);
    while (code != PLOTSTREAM_DT_ENDARG) {
      switch ((int) code) {
        case 0: Readpipe.checkAndReadDouble(dis, difEndianness); break; // System.out.println("Reading arrowsize"); break;
        case 1: plotOutput.setText(Readpipe.checkAndReadString(dis, difEndianness)); break;
        case 2: Readpipe.checkAndReadString(dis, difEndianness); break; // System.out.println("Reading psfile"); break;
        case 3: plotOutput.setWantsToPause(Readpipe.checkAndReadBoolean(dis, difEndianness)); break; // System.out.println("Reading pause"); break;
        case 4: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading fill"); break; 
        case 5: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading value"); break;
        case 6: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading clean"); break; 
        case 7: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading aspectratio"); break;
        case 8: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading boundingbox"); break;
        case 9: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading nbiso"); break;
        case 10: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading nbarrowcolors"); break;
        case 11: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading isovalues"); break;
        case 12: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading arrowcolorvalues"); break;
        case 13: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading backandwhite"); break;
        case 14: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading isGrey"); break;
        case 15: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading colors"); break;
        case 16: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading drawborder"); break;
        case 17: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading plotdim"); break;
        case 18: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading add"); break;
        case 19: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading keepPV"); break;
        case 20:/* do nothing */ break; // System.out.println("Reading :-/"); break;
        case 21: Readpipe.checkAndReadDouble(dis, difEndianness); break; // System.out.println("Reading zscale"); break;
        case 22: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading white"); break;
        case 23: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading opaque"); break;
        case 24: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading borderasmesh"); break;
        case 25: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading showmeshes"); break;
        case 26: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading colosscheme"); break;
        case 27: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading arrowshapes"); break;
        case 28: Readpipe.checkAndReadDouble(dis, difEndianness); break; // System.out.println("Reading arrowsize2"); break;
        case 29: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading complexdisplay"); break;
        case 30: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading labelcolors"); break;
        case 31: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading showaxes"); break;
        case 32: Readpipe.checkAndReadBoolean(dis, difEndianness); break; // System.out.println("Reading docutplane"); break;
        case 33: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading cameraposition"); break;
        case 34: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading camera focal point"); break;
        case 35: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading cameraviewup"); break;
        case 36: Readpipe.checkAndReadDouble(dis, difEndianness); break; // System.out.println("Reading cameraviewangle"); break;
        case 37: Readpipe.checkAndReadArray(dis, difEndianness); break; // System.out.println("Reading cameraclippingRange"); break;
        case 38: Readpipe.checkAndReadArray(dis, difEndianness);break; // System.out.println("Reading cutplaneorigin"); break;
        case 39: Readpipe.checkAndReadArray(dis, difEndianness);break; // System.out.println("Reading cutplanenormal"); break;
        case 40: Readpipe.checkAndReadLong(dis, difEndianness); break; // System.out.println("Reading windowindex"); break;
        default: break; // System.out.println("Reading unknown command"); break;
      }
      code = Readpipe.readLong(dis, difEndianness);
    } // end of while

    // Read 2D meshes
    code = Readpipe.readLong(dis, difEndianness);
    if (code != PLOTSTREAM_DT_MESHES) {
      System.err.println("Visudata error: Plot stream dt meshes parameter missing");
      return false;
    }
    
    long nbmeshes = Readpipe.readLong(dis, difEndianness);
    for (int j = 0; j < nbmeshes; j++) {
      if (!readMesh(plotOutput, 2, dis, difEndianness)) { 
        System.err.println("CVisudata error: Error reading mesh #"+j); 
        return false; 
      }
    }

    // Read 3D meshes
    code = Readpipe.readLong(dis, difEndianness);
    if (code==PLOTSTREAM_DT_MESHES3) {
//      System.err.println("Visudata error: Plot stream dt meshes 3D parameter missing");
      nbmeshes = Readpipe.readLong(dis, difEndianness);
      for (int j = 0; j < nbmeshes; j++) {
        if (!readMesh(plotOutput,3, dis, difEndianness)) { 
          System.err.println("CVisudata error: Error reading mesh3D #"+j); 
          return false; 
        }
      }
      code = Readpipe.readLong(dis, difEndianness);
    }

    // PLOTS
    if (code != PLOTSTREAM_DT_PLOTS) {
      System.err.println("Visudata error: Plot stream dt plots parameter missing");
      return false;
    }
    
    long nbplots = Readpipe.readLong(dis, difEndianness);
    for (int i = 0; i < nbplots; i++) {
      PDEData newPlot = readPlot(dis, difEndianness, plotOutput);
      if (newPlot==null) {
        System.err.println("Visudata error: Plot read returned null");
        return false;
      }
      plotOutput.addData(newPlot);
    }

    // Read end plot
    code = Readpipe.readLong(dis, difEndianness);
    if (code != PLOTSTREAM_DT_ENDPLOT) {
      System.err.println("Visudata error: Plot stream dt end plots parameter missing");
      return false;
    }
    return true;
  }

  /**
   * Read a mesh
   * @param dim the dimension
   * @param dis
   * @param difEndianness
   * @return
   */
  static private boolean readMesh(PlotOutput plotOutput, int dim, DataInputStream dis, boolean difEndianness) throws IOException {
    long ffmeshref = Readpipe.readLong(dis, difEndianness);
    // Read header
    String info = Readpipe.readString(dis, difEndianness);
    if (dim == 2 && !info.contains(FF_2DMESH_BEGIN)) return false;
    if (dim == 3 && !info.contains(FF_3DMESH_BEGIN)) return false;

    int nPoints = Readpipe.readInt(dis, difEndianness);
    int nCells = Readpipe.readInt(dis, difEndianness);
    int nBoundaryElements = Readpipe.readInt(dis, difEndianness);

    // Read points
    double[][] points = new double[nPoints][dim];
    int[] labels = new int[nPoints];
    if (dim==3) {
      for (int j=0; j<nPoints; j++){
        double[] point = points[j];
        point[0] = Readpipe.readDouble(dis, difEndianness);
        point[1] = Readpipe.readDouble(dis, difEndianness);			
        point[2] = Readpipe.readDouble(dis, difEndianness);
        labels[j] = Readpipe.readInt(dis, difEndianness);
      }
    }
    else {
      for (int j=0; j<nPoints; j++){
        double[] point = points[j];
        point[0] = Readpipe.readDouble(dis, difEndianness);
        point[1] = Readpipe.readDouble(dis, difEndianness);     
        labels[j] = Readpipe.readInt(dis, difEndianness);
      }
    }

    // Read cells (triangles or tetrahedra)
    // each cell or element has dim+1 id's which refer to the points array
    int[][] cells = new int[nCells][dim+1];
    for (int i=0; i<nCells; i++) {
      int[] cell = cells[i];
      for (int p = 0; p<=dim; p++) cell[p] = Readpipe.readInt(dis, difEndianness);				
      Readpipe.readInt(dis, difEndianness); // label is ignored
    }

    // Read boundary elements
    //each boundarySegments has dim points to define it and a int label
    int [][] boundaryElements = new int[nBoundaryElements][dim];
    int [] boundaryLabels = new int[nBoundaryElements];
    for (int i=0; i<nBoundaryElements; i++) {
      int[] element = boundaryElements[i];
      for (int j=0; j<dim; j++) element[j] = Readpipe.readInt(dis, difEndianness);
      // label
      boundaryLabels[i] = Readpipe.readInt(dis, difEndianness);
    }

    // Read end
    info = Readpipe.readString(dis, difEndianness);
    if (dim == 2 && !info.contains(FF_MESH_END)) return false;
    plotOutput.addMesh(ffmeshref, new PDEMesh(points, cells, boundaryElements,boundaryLabels));
    return true;

  } //end read mesh

  /**
   * Read a plot
   * @param ref
   * @param dis
   * @param difEndianness
   * @param plotOutput
   * @return
   */
  static PDEData readPlot(DataInputStream dis, boolean difEndianness, PlotOutput plotOutput)  throws IOException {
    long type = Readpipe.readLong(dis, difEndianness);
    if (type == -1) { // empty plot
      Readpipe.readLong(dis, difEndianness); // dummy
      return null;
    }
    // filter out complex type
    boolean isComplex = false;
    if (type > 10) {
      type -= 10;
      isComplex = true;
    }

    switch( (int) type) { // different types of plot
      case 0 :  // 2D mesh
      case 5 :  // 3D mesh
        return new PDEData (type, plotOutput.getMesh(Readpipe.readLong(dis, difEndianness)),null,null,null);
      case 3 : // Curve plot 
        System.err.println("Visudata error : CurvePlot not supported");
        return null;
      case 4 : // BorderPlot
        System.err.println("Visudata error : BorderPlot not supported");
        return null;
      case 1 : // scalar 2D solution
      case 2 : // vector 2D solution
      case 6 : // scalar 3D solution
      case 7 : // vector 3D solution
        // different types
        int scalarSize = isComplex ? 2 : 1;
        int dimension = (type==1 || type==2) ? 2 : 3;
        int dataLength = (type==1 || type==6) ?  1 : ((type==2)? 2 : 3); // 2D points are also given with 3 coordinates (z=0)

        // Search corresponding mesh by reference
        PDEMesh mesh =  plotOutput.getMesh(Readpipe.readLong(dis, difEndianness)); 
        // Read internal mesh points coordinates,points = coordinates/dim 
        double[][] subpoints = Readpipe.readDoubleArray2D(dis,difEndianness,dimension);
        // Read internal elements defined as sets of references to the above points.
        int[][] subelements = Readpipe.readIntArray2D(dis, difEndianness,dimension);
        double[][] values = Readpipe.readValues(dis, difEndianness,scalarSize*dataLength);
        // check values length 
        if (values.length!=(mesh.getCells().length*subpoints.length)){
          System.err.println("Visudata error: Incorrect number of values read");
          return null;
        }     
        return new PDEData(type,mesh,values,subpoints,subelements);
      default : 
        System.err.println("Visudata error : Plot type : "+type+" not supported");
        return null;
    }

  } //end readPlot

}

package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;


import org.opensourcephysics.drawing3d.utils.VisualizationHints;
//import org.opensourcephysics.drawing3d.utils.transformations.Matrix3DTransformation;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementObject3DS;
import org.opensourcephysics.drawing3d.Group;
//import es.uhu.augmented_reality.ElementLocalAR;

public class Ejemplo3dStereoCompleto {

  protected static double[] point;
  protected static double[][] orientation;

  public static void main(String[] args) {

    //final ElementLocalAR arSystem = new ElementLocalAR();
    final DrawingPanel3D mPanel3D;
    
    final Group markerGroup = new Group();
    //final Matrix3DTransformation Matrix3D = new Matrix3DTransformation();
    
    //arSystem.setPort("Integrated Webcam");
    //arSystem.setConfigurationFile("./data/macbookpro.dat");
    //arSystem.addMarker(ElementLocalAR.getPredefinedMarker("4x4_31.patt"), 80);
    //arSystem.setFPS(25);
    //arSystem.setDrawingPanel3D(mPanel3D);
    //
    
    System.setProperty("j3d.stereo", "PREFERRED");
    
    mPanel3D = new DrawingPanel3D(DrawingPanel3D.IMPLEMENTATION_JAVA3D); // Para que la implementacion sea Java3D
    
    mPanel3D.getVisualizationHints().setDecorationType(VisualizationHints.DECORATION_CUBE);
    
    //
    //
//    mPanel3D.setEyeDistance(0.0078);
      
    ElementBox cube = new ElementBox();
    //Matrix3D.setElement(markerGroup);
    //
    cube.setSizeXYZ(0.25, 0.25, 4.25);
    cube.setZ(0.5);
    //
    // 3DS object
    ElementObject3DS complex3DS = new ElementObject3DS();
//    complex3DS.setObjectFile("./data/caravan.3ds");
    complex3DS.setObjectFile("./data/mi turbina.3ds");
    //complex3DS.setObjectFile("./data/Playground.3ds");
    complex3DS.setVisible(true);
    complex3DS.setSizeXYZ(2, 2, 2);
    complex3DS.setZ(-0.1);
    
    //cube.setZ(10);
    //cube.setX(1);
    //cube.setY(1);
    markerGroup.addElement(complex3DS);
    //markerGroup.addElement(cube);
    mPanel3D.addElement(markerGroup); 
    JFrame mFrame = new JFrame("STEREO TEST");
    mFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
    mFrame.setUndecorated(true);
    mFrame.getContentPane().setPreferredSize(new Dimension(640, 480));
   
    mFrame.getContentPane().add(mPanel3D.getComponent(), BorderLayout.CENTER);
    mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    mFrame.pack();
    mFrame.setVisible(true);
    //arSystem.setEnabled(true);
    //arSystem.setChangeListener(new ChangeListener() {
//      public void stateChanged(ChangeEvent arg0) {
//        if (!arSystem.isMarkerDetected(0)) {
//          System.err.println("Marker not detected!");
//          return;
//        }
//        point = arSystem.getMarkerPosition(0,new double[3]);
//        orientation = arSystem.getMarkerOrientation(0,new double[4][4]);
//        double distance = arSystem.getDistanceFromCamera(0)/1000.0;
//        markerGroup.setXYZ(point[0], point[1], point[2]);
//        Matrix3D.setMatrix(orientation);
//        System.out.println("Marker is at a distance of "+distance);
//        System.out.println("Marker X coord.: "+ point[0]);
//      }
//    });
    
  }

}

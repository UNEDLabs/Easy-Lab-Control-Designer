package org.colos.roboticsLabs.robots;

import java.awt.Color;

//import org.colos.ejs.library.Model;
//import org.colos.ejs.library.utils.ModelElementsUtilities;
//import org.colos.roboticsLabs.robots.RobotScaraOmron;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementCylinder;
import org.opensourcephysics.drawing3d.Group;

import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;

public class RobotScaraOmronWithComponents extends RobotScaraOmron {
  
	protected boolean mBasement;
	
  public RobotScaraOmronWithComponents(boolean basement){
    super();
    mBasement = basement;
    if(mBasement) mHighBasement = 0.88;
    super.updateView();
  }
  
  @Override
  public double[] calculatePositionLab(double[] q){  
	 double[] p = forwardKinematics(q, null);
	 double[] positionLab = {factorSize * p[0] + getInitialPosition()[0],  factorSize * p[1] + getInitialPosition()[1], 
			 getInitialPosition()[2] + mHighBasement + (sHighAxisZ + p[2]/10.) , p[3]};
	 return positionLab;
  }
 
  @Override
  protected Group createViewGroup() {
    Group mainGroup = new Group();
    //mainGroup.setTransformation(Matrix3DTransformation.rotationZ(Math.PI));
    Group robotGroup = super.createViewGroup();
    if(mBasement){
      Group tableGroup = new Group();
      tableGroup.setName("basementGroup");
      mainGroup.addElement(tableGroup);
      
      ElementBox base = new ElementBox();
    // base.setSizeXYZ(0.6, 0.3, 0.03); //REAL

      base.setSizeXYZ(0.6*factorSize, 0.3*factorSize, 0.03);
      base.setXYZ(0.1, 0, mHighBasement-0.015);
      base.getStyle().setFillColor(Color.WHITE);
      tableGroup.addElement(base);
      
      ElementCylinder column1 = new ElementCylinder();
      column1.setXYZ(0, 0,  (mHighBasement-0.03)/2.);
    //  column1.setSizeXYZ(0.1, 0.1, mHighBasement-0.05);
      column1.setSizeXYZ(0.1, 0.1, mHighBasement-0.03);
      column1.getStyle().setFillColor(Color.BLACK);
      tableGroup.addElement(column1);
     
      //OrientedBoundingBoxBasement
      ElementBox OBBbasement = new ElementBox();
      OBBbasement.setXYZ(0, 0, mHighBasement/2.);
      OBBbasement.setSizeXYZ(0.6, 0.3, mHighBasement);
      //OBBbasement.setSizeXYZ(0.6*factorSize, 0.3*factorSize, mHighBasement);
      OBBbasement.getStyle().setFillColor(Color.red);
      OBBbasement.getStyle().setLineColor(Color.RED);
      OBBbasement.getStyle().setDrawingFill(false);
      OBBbasement.setVisible(false);
      mainGroup.addElement(OBBbasement);
     
      
      {
  	    OrientedBoundingBox obb00 = new OrientedBoundingBox();
  	    obb00.setExtent(new Vector3f((float)(OBBbasement.getSizeX()/2), (float)(OBBbasement.getSizeY()/2), (float)(OBBbasement.getSizeZ()/2)));
  	    obbTable.put(obb00, OBBbasement);
  	    obbList.add(0, obb00);
  	    Element[] triedro = new Element[3];
  	    for (int i=0; i<3; i++) {
  	    	triedro[i] = new ElementArrow();
  	    	triedro[i].getStyle().setLineColor(Color.RED);
  	    	triedro[i].getStyle().setLineWidth(4);

//  	    	mainGroup.addElement(triedro[i]);
  	    }
  	    triedro[0].getStyle().setLineColor(Color.RED);
  	    triedro[1].getStyle().setLineColor(Color.YELLOW);
  	    triedro[2].getStyle().setLineColor(Color.BLUE);
  	    obbTriedroTable.put(obb00, triedro);
  	    }
    
      robotGroup.setXYZ(0, 0, mHighBasement);    
    }
    else{
    robotGroup.setXYZ(0, 0, 0);  
    }
    mainGroup.addElement(robotGroup);
    return mainGroup;
  }
  
}

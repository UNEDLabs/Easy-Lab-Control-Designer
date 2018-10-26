package org.colos.roboticsLabs.robots;
/**
 * @author Almudena Ruiz
 */
import java.awt.Color;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementArrow;
import org.opensourcephysics.drawing3d.ElementBox;
import org.opensourcephysics.drawing3d.ElementCylinder;
import org.opensourcephysics.drawing3d.Group;
import org.opensourcephysics.numerics.Matrix3DTransformation;

import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;

public class RobotTX60LWithComponents extends RobotTX60L {
  
	static final private double sHighBasement = 0.7;
	static final private double sLengthOfTool = 0.22;
	//private Group beltGroup, toolGroup;
	private Group toolGroup;
	//private boolean mBasement, mTool, mBelt, showBox;
	protected boolean mBasement, mTool, showBox;
	private ElementBox box, gripper1, gripper2;

	public RobotTX60LWithComponents(boolean basement, boolean tool){	
	  super();
	  mBasement = basement;	
	  mTool = tool;
	  if(mTool) mLengthOfTool = sLengthOfTool;
	  //mBelt = belt;
	  for (int i = 0, n=getDOF()*4; i<n; i++) {
	      mDHParameters[i] = getDHParameters()[i];
	  }
	  updateView();
	}
	

	//---------------------------------------
	// Positioning
  // ---------------------------------------

	@Override
  protected void moveRealRobot() throws Exception {
    super.moveRealRobot();
    writeSocket (mOpenTool ? "6\n" : "7\n");
  }
	
	@Override
  public void openTool() throws Exception {
    super.openTool();
    if (mHasViewGroup) {  
      gripper1.setXYZ(0,-0.04, 0.14);
      gripper2.setXYZ(0, 0.04, 0.14);
    }
  }

  @Override
  public void closeTool() throws Exception {
    super.closeTool();
    if (mHasViewGroup) { 
      gripper1.setXYZ(0,-0.02, 0.14);
      gripper2.setXYZ(0, 0.02, 0.14);
    }
  }
  
  @Override  
  public double[] calculatePositionLab(double[] q){	 
	  double [] positionLab = super.calculatePositionLab(q);
	  if(mBasement) positionLab[2] += sHighBasement;
		 return  positionLab;
  }
  
  
  //---------------------------------------
  // Required for implementation
  // ---------------------------------------

  @Override
  protected Group createViewGroup() {
    Group mainGroup = new Group();
    Group robotGroup = super.createViewGroup();    
    //Adding a basement to the robot
    if (mBasement){
      ElementBox basement = new ElementBox();
      basement.setXYZ(0, 0, sHighBasement/2.);
      basement.setSizeXYZ(0.25, 0.25, sHighBasement);
      basement.getStyle().setFillColor(Color.BLACK);
      mainGroup.addElement(basement);
      robotGroup.setXYZ(0, 0, sHighBasement);   
      //basement.setVisible(false);
      
      //OrientedBoundingBoxBasement
      ElementBox OBBbasement = new ElementBox();
      OBBbasement.setXYZ(0, 0, sHighBasement/2.);
      OBBbasement.setSizeXYZ(0.25, 0.25, sHighBasement);
      OBBbasement.getStyle().setFillColor(Color.red);
      OBBbasement.getStyle().setDrawingFill(false);
      OBBbasement.setVisible(false);
      mainGroup.addElement(OBBbasement);
      
      {
  	    OrientedBoundingBox obb00 = new OrientedBoundingBox();
  	    obb00.setExtent(new Vector3f((float)(OBBbasement.getSizeX()/2), (float)(OBBbasement.getSizeY()/2), (float)(OBBbasement.getSizeZ()/2)));
  	    obbTable.put(obb00, OBBbasement);
  	    obbList.add(0,obb00);
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
    
      
    }
    mainGroup.addElement(robotGroup); 
    //Defining a tool 
    if (mTool){
      toolGroup = new Group();
      toolGroup.setName("toolGroup");
      toolGroup.setXYZ(0, 0, 0.039);
      super.getFlangeGroup().addElement(toolGroup);

      ElementCylinder tool = new ElementCylinder();
      tool.setXYZ(0, 0, 0);
      tool.setSizeXYZ(0.04, 0.04, 0.06);
      tool.getStyle().setFillColor(Color.LIGHT_GRAY);
      toolGroup.addElement(tool);

      ElementBox boxTool = new ElementBox();
      boxTool.setXYZ(0, 0, 0.06);
      boxTool.setSizeXYZ(0.08, 0.08, 0.06);
      boxTool.getStyle().setFillColor(Color.GRAY);
      toolGroup.addElement(boxTool);

      gripper1 = new ElementBox();
      gripper1.setSizeXYZ(0.08, 0.004, 0.10);
      gripper1.getStyle().setFillColor(Color.LIGHT_GRAY);
      toolGroup.addElement(gripper1);

      gripper2 = new ElementBox();
      gripper2.setSizeXYZ(0.08, 0.004, 0.10);
      gripper2.getStyle().setFillColor(Color.LIGHT_GRAY);
      toolGroup.addElement(gripper2);
      
      if (mOpenTool) {  
        gripper1.setXYZ(0,-0.04, 0.14);
        gripper2.setXYZ(0, 0.04, 0.14);
      }
      else { 
        gripper1.setXYZ(0,-0.036, 0.14); // antes -0.02
        gripper2.setXYZ(0, 0.036, 0.14); // antes 0.02
      }
      
      //OrientedBoundingBox6
      ElementBox OBB6 = new ElementBox();
      OBB6.setXYZ(0, 0, 0.075+0.031);
    //OBB6.setSizeXYZ(0.08, 0.08, 0.22);
      OBB6.setSizeXYZ(0.08, 0.08, 0.16);
      OBB6.getStyle().setFillColor(Color.red);
      OBB6.getStyle().setDrawingFill(false);
      OBB6.setVisible(false);
      toolGroup.addElement(OBB6);
      
      {
    	    OrientedBoundingBox obb6 = new OrientedBoundingBox();
    	    obb6.setExtent(new Vector3f((float)(OBB6.getSizeX()/2), (float)(OBB6.getSizeY()/2), (float)(OBB6.getSizeZ()/2)));
    	    obbTable.put(obb6, OBB6);
    	    obbList.add(obb6);
    	    Element[] triedro = new Element[3];
    	    for (int i=0; i<3; i++) {
    	    	triedro[i] = new ElementArrow();
    	    	triedro[i].getStyle().setLineColor(Color.RED);
    	    	triedro[i].getStyle().setLineWidth(4);

//    	    	mainGroup.addElement(triedro[i]);
    	    }
    	    triedro[0].getStyle().setLineColor(Color.RED);
    	    triedro[1].getStyle().setLineColor(Color.YELLOW);
    	    triedro[2].getStyle().setLineColor(Color.BLUE);
    	    obbTriedroTable.put(obb6, triedro);
    	    }
      
      // Box 
      /*
      box = new ElementBox();
      box.setXYZ(0, 0, 0.14 + 0.065);
      box.setSizeXYZ(0.05, 0.05, 0.12);
      box.getStyle().setFillColor(new Color(255, 0, 128));
      box.setVisible(showBox);
      toolGroup.addElement(box);
      */
      
    }
    /*
    // Definition of a belt
    if (mBelt){
      beltGroup = new Group();
      beltGroup.setName("beltGroup");
      beltGroup.setXYZ(-0.33, 0, 0);
      beltGroup.setTransformation(Matrix3DTransformation.rotationZ(PI2));
      mainGroup.addElement(beltGroup);

      ElementBox base = new ElementBox();
      base.setXYZ(0, 0, 0.79);
      base.setSizeXYZ(1.3, 0.24, 0.18);
      base.getStyle().setFillColor(Color.GRAY);
      beltGroup.addElement(base);

      ElementBox base2 = new ElementBox();
      base2.setXYZ(0, 0, 0.791);
      base2.setSizeXYZ(1.3, 0.2, 0.18);
      base2.getStyle().setFillColor(Color.WHITE);
      beltGroup.addElement(base2);

      ElementCylinder column1 = new ElementCylinder();
      column1.setXYZ(0.4, 0.08, 0.35);
      column1.setSizeXYZ(0.05, 0.05, 0.7);
      column1.getStyle().setFillColor(Color.DARK_GRAY);
      beltGroup.addElement(column1);

      ElementCylinder column2 = new ElementCylinder();
      column2.setXYZ(0.4, -0.08, 0.35);
      column2.setSizeXYZ(0.05, 0.05, 0.7);
      column2.getStyle().setFillColor(Color.DARK_GRAY);
      beltGroup.addElement(column2);

      ElementCylinder column3 = new ElementCylinder();
      column3.setXYZ(-0.4, 0.08, 0.35);
      column3.setSizeXYZ(0.05, 0.05, 0.7);
      column3.getStyle().setFillColor(Color.DARK_GRAY);
      beltGroup.addElement(column3);

      ElementCylinder column4 = new ElementCylinder();
      column4.setXYZ(-0.4, -0.08, 0.35);
      column4.setSizeXYZ(0.05, 0.05, 0.7);
      column4.getStyle().setFillColor(Color.DARK_GRAY);
      beltGroup.addElement(column4);
    }
    */
    return mainGroup;
  }

  @Override
	public void attachObject(Element object) {
		if (object == null) System.out.println("Error: It is necessary to add a element");
		else {	
			if(mHasViewGroup){ 
				if(mTool){
				object.setXYZ(0, 0, 0.14 + object.getSizeZ()/2);	
				//OBB	
				 ElementBox OBBbox = new ElementBox();
				 OBBbox.setXYZ(object.getX(), object.getY(), object.getZ());
				 OBBbox.setSizeXYZ(object.getSizeX(),object.getSizeY(),object.getSizeZ());
				 OBBbox.getStyle().setFillColor(object.getStyle().getFillColor());
				 toolGroup.addElement(OBBbox);
				 
				OrientedBoundingBox obbObject = new OrientedBoundingBox();	
		        obbObject.setExtent(new Vector3f((float)(OBBbox.getSizeX()/2), (float)(OBBbox.getSizeY()/2), (float)(OBBbox.getSizeZ()/2)));   
		        obbTable.put(obbObject, OBBbox);
		        obbList.add(obbObject);
		       
		        Element[] triedro = new Element[3];
		        for (int i=0; i<3; i++) {
		        	triedro[i] = new ElementArrow();
		        	triedro[i].getStyle().setLineColor(Color.RED);
		        	triedro[i].getStyle().setLineWidth(4);

//		        	mainGroup.addElement(triedro[i]);
		        }
		        triedro[0].getStyle().setLineColor(Color.RED);
		        triedro[1].getStyle().setLineColor(Color.YELLOW);
		        triedro[2].getStyle().setLineColor(Color.BLUE);
		        obbTriedroTable.put(obbObject, triedro);
		        }
			else super.attachObject(object);
			}
			
		    }
		}
	

  @Override
	public void detachObject(Element object){
		if(mHasViewGroup){
			if(mTool){
				toolGroup.removeElement(object);
				obbTable.remove(obbObject);
				obbList.remove(obbObject);
				obbTriedroTable.remove(obbObject);
				double[] pos = toolGroup.toSpaceFrame(new double[]{0,0,0});
				object.setXYZ(pos[0]-object.getSizeX(), pos[1], pos[2]);
			}
			else super.detachObject(object);
		}
	}

}

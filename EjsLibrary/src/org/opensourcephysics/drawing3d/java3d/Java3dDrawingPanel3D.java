/*
 * Created on January 29, 2005
 * Modified on October 10, 2008
 */

package org.opensourcephysics.drawing3d.java3d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
//import java.util.ArrayList;

import javax.media.j3d.*;
import javax.swing.JOptionPane;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.display3d.simple3d.utils.EllipsoidUtils;
import org.opensourcephysics.tools.ResourceLoader;

/**
*
* <p>Title: Java3dDrawingPanel3D</p>
* <p>Description: The Java3D implementation of a DrawingPanel3D.</p>
* @author Carlos Jara Bravo
* @author Francisco Esquembre
* @author Glenn Ford
* @version August 2009
*/
public class Java3dDrawingPanel3D implements ImplementingPanel{

  
	/**The Canvas3D created for communication with Java3D**/
	public RenderCanvas canvas;
	
	//UpdateModel
	protected boolean updateModel = false;
  
	//Panel3D element base
	private DrawingPanel3D panel3D;
	
	//Static variables
	private static double RBACK = 5.0;
	private static final int BACKGROUND = 3;
	private static final double defaultEyeDistance = 0.0078;
	
	//Java 3D Elements
	private SimpleUniverse universe;
	private TransformGroup rootTG, scaleTG, backTG; 
	private BranchGroup branchGroup;
	private Appearance appBack;
	private BoundingSphere bounds;

	//Text Boxes Messages
	protected Java3DTextPanel trMessageBox = new Java3DTextPanel(DrawingPanel3D.TOP_RIGHT); // text box in top right hand corner for message
	protected Java3DTextPanel tlMessageBox = new Java3DTextPanel(DrawingPanel3D.TOP_LEFT); // text box in top left hand corner for message
	protected Java3DTextPanel brMessageBox = new Java3DTextPanel(DrawingPanel3D.BOTTOM_RIGHT); // text box in lower right hand corner for message
	protected Java3DTextPanel blMessageBox = new Java3DTextPanel(DrawingPanel3D.BOTTOM_LEFT); // text box in lower left hand corner for mouse coordinates

	private static RenderingHints optionsImage = new RenderingHints(null);
  
  //Statics methods
  static{
    optionsImage.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    optionsImage.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    optionsImage.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
    optionsImage.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    optionsImage.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    optionsImage.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    optionsImage.put(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);
    optionsImage.put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }
  
	
	// -------------------------
	// Constructor
	// -------------------------
	/**
	 * Default constructor for a DrawingPanel3D.  It initializes necessary variables and
	 * sets the preferred size to 300x300 and preferred min/max to -1, 1, -1, 1, -1, 1
	 */
	public Java3dDrawingPanel3D(DrawingPanel3D _panel) {
		this.panel3D = _panel;

		//Graphics Configuration and Canvas/OffScreenCanvas creation
		GraphicsConfiguration config = getPreferredConfiguration();
		//GraphicsConfiguration config = getBestConfigurationOnSameDevice(getFrame());	
		canvas = new RenderCanvas(config);
		canvas.setSize(new Dimension(300, 300));
		canvas.setDoubleBufferEnable(true); 
		canvas.setBackground(panel3D.getVisualizationHints().getBackgroundColor());

    // *************************************** Andres *****************************************************
    // Sets the double buffer (if exists)
    if (System.getProperty("j3d.stereo")!=null) {
      if (canvas.getDoubleBufferAvailable()) canvas.setDoubleBufferEnable(true); 
      System.out.println("Doble buffer :" + canvas.getDoubleBufferAvailable());
      // Sets the default eye distance
      this.setEyeDistance(defaultEyeDistance);
    }
    
    //***************************************************************************************************** 
		
		//Creation of the BranchGroup
		branchGroup = new BranchGroup();
		branchGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
		branchGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_READ);
		branchGroup.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
		
		//Object bounds
		bounds = new BoundingSphere();
		
		//----LIGHTS----//
		bounds.setRadius(Double.MAX_VALUE);
		AmbientLight lightA = new AmbientLight();
		lightA.setCapability(javax.media.j3d.AmbientLight.ALLOW_COLOR_READ);
		lightA.setCapability(javax.media.j3d.AmbientLight.ALLOW_COLOR_WRITE);
		lightA.setCapability(javax.media.j3d.AmbientLight.ALLOW_STATE_READ);
		lightA.setCapability(javax.media.j3d.AmbientLight.ALLOW_STATE_WRITE);
		lightA.setInfluencingBounds(bounds);
		lightA.setColor(new Color3f(Color.white));
		branchGroup.addChild(lightA);

		
		DirectionalLight lightD = new DirectionalLight();
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_COLOR_READ);
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_COLOR_WRITE);
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_DIRECTION_READ);
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_DIRECTION_WRITE);
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_STATE_READ);
		lightD.setCapability(javax.media.j3d.DirectionalLight.ALLOW_STATE_WRITE);
		lightD.setInfluencingBounds(bounds);
		lightD.setColor(new Color3f(Color.white));
		lightD.setDirection(new Vector3f(0, 0,-1));
		branchGroup.addChild(lightD);
		//----LIGHTS----//
		
		//TranformGroup (Camera TG)
		rootTG = new TransformGroup();
		rootTG.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_READ);
		rootTG.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
		rootTG.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
		rootTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rootTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		rootTG.setCapability(BranchGroup.ALLOW_DETACH);
		rootTG.setBounds(bounds);
		branchGroup.addChild(rootTG);
		
		//Background
		Color3f bkgdColor = new Color3f(panel3D.getVisualizationHints().getBackgroundColor());
		Background backGround = new Background(bkgdColor);
		backGround.setCapability(Background.ALLOW_COLOR_WRITE);
		backGround.setCapability(Background.ALLOW_IMAGE_WRITE);
		backGround.setCapability(Background.ALLOW_IMAGE_SCALE_MODE_WRITE);
		backGround.setApplicationBounds(new BoundingSphere());
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_READ);
		bg.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_WRITE);
		bg.addChild(backGround);
		branchGroup.addChild(bg);

		//Moveable Background
		backTG = new TransformGroup();
		backTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		backTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		backTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		backTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		backTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		Transform3D scaleBack = new Transform3D();
		scaleBack.setScale(RBACK*panel3D.getMaximum3DSize());
		backTG.setTransform(scaleBack);
		rootTG.addChild(backTG);

		//TranformGroup Scale TG
		scaleTG = new TransformGroup();
		scaleTG.setCapability(javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND);
		scaleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		scaleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		scaleTG.setBounds(bounds);
		rootTG.addChild(scaleTG);
		
		//SimpleUniverse
		universe = new SimpleUniverse(canvas);
		universe.getViewer().getView().setLocalEyeLightingEnable(true);
		universe.getViewer().getView().setCompatibilityModeEnable(false);
		universe.getViewer().getView().setBackClipDistance(200);
		
		//Universe. Add rootTG BG
		universe.addBranchGraph(branchGroup);
	}

	protected TransformGroup getTransformGroup() { return rootTG; }
	
	// -------------------------
	// Java 3D technical stuff
	// -------------------------
	

//	private Frame getFrame(){	
//		java.awt.Container parent = this.panel3D.getComponent().getParent();
//		while (parent!=null) {
//			if (parent instanceof Window) return (Frame) parent;
//			parent = parent.getParent();
//		}
//		return null;
//	}	
//	
// private static GraphicsConfiguration getBestConfigurationOnSameDevice(Frame frame){
//     
//	 GraphicsConfiguration gc = frame.getGraphicsConfiguration();
//	 GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//	 GraphicsDevice[] gs = ge.getScreenDevices();
//	 GraphicsConfiguration good = null;
//
//	 GraphicsConfigTemplate3D gct = new GraphicsConfigTemplate3D();
//
//	 for(GraphicsDevice gd: gs){
//
//		 if(gd==gc.getDevice()){
//			 good = gct.getBestConfiguration(gd.getConfigurations());
//			 if(good!=null)
//				 break;
//		 }
//	 }
//  return good;
//}

	
	/**
	 * Find a good graphics configuration.
	 */
	private final static GraphicsConfiguration getPreferredConfiguration() {
	  GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
	  String stereo;

	  // Check if the user has set the Java 3D stereo option.
	  // Getting the system properties causes appletviewer to fail with a
	  stereo = (String) java.security.AccessController.doPrivileged(
	      new java.security.PrivilegedAction<Object>() {
	        public Object run() { return System.getProperty("j3d.stereo"); }
	      });

	  // update template based on properties.
	  if (stereo != null) {
	    if (stereo.equals("REQUIRED")) template.setStereo(GraphicsConfigTemplate.REQUIRED); //$NON-NLS-1$
	    else if (stereo.equals("PREFERRED")) template.setStereo(GraphicsConfigTemplate.PREFERRED); //$NON-NLS-1$
	  }
	  // Return the GraphicsConfiguration that best fits our needs.
	  return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration( template );
	}
	
	
	public double getScreenRatio() {
		return panel3D.isSquareAspect() ? (float) canvas.getWidth()/(float) canvas.getHeight() : 1.0f;
	}

  // ------------------------------------
  // Implementation of ImplementingPanel
  // ------------------------------------

  public Component getComponent() { return canvas; }

  public void forceRefresh() { update(); }
  
  public synchronized void update() {
    for (Element el : panel3D.getElements()) el.processChanges(Element.CHANGE_NONE);
    for (Element el : panel3D.getDecoration().getElementList())  el.processChanges(Element.CHANGE_NONE);
    cameraChanged(Camera.CHANGE_ANY);
    updateModel = true;
  }
  
 //******************************************* Andres ***********************************************
 // Sets the distance between eyes  ******************************************************************
 // **************************************************************************************************
 public void setEyeDistance(double d) {

   //Canvas3D canvas = universe.getCanvas();
   if (canvas == null)
           return;
  
   double lx = -d/2;
   Point3d left = new Point3d();
   canvas.getView().getPhysicalBody().getLeftEyePosition(left);
   left.x = lx;
  
   double rx = d/2;
   Point3d right= new Point3d();
   canvas.getView().getPhysicalBody().getRightEyePosition(right);
   right.x = rx;
  
   canvas.setLeftManualEyeInImagePlate(left);
   canvas.setRightManualEyeInImagePlate(right);
  
   canvas.getView().getPhysicalBody().setLeftEyePosition(left);
   canvas.getView().getPhysicalBody().setRightEyePosition(right);
  
}

  public void setFastRedraw(boolean fast) { } // Does nothing
  
	@SuppressWarnings("fallthrough")
	public void cameraChanged(int change) {
	  Camera camera = panel3D.getCamera();

	  switch (change) {
	    case Camera.CHANGE_SCREEN : 
	    {
	      double factor = 1.0;
	      double dif = 0.0;
	      int diferencia = 0;
		  if(getComponent().getWidth()>=getComponent().getHeight()){
			  dif = getComponent().getWidth() - getComponent().getHeight();
	    	  diferencia = (int) (dif/10);
	    	  if(diferencia<10) factor = 0.8 + 0.05*diferencia;
	    	  else factor = (-6e-5*Math.pow(diferencia,3)+0.0083*Math.pow(diferencia,2)-0.4011*diferencia + 8.59)*dif/getComponent().getWidth(); 
		  }
		  if(getComponent().getWidth()<getComponent().getHeight()){
			  dif = getComponent().getHeight()-getComponent().getWidth();
	    	  diferencia = (int) (dif/10);
	    	  if(diferencia<10) factor = 0.65 + 0.045*diferencia;
	    	  factor = 3e-08*Math.pow(dif,3) - 2e-05*Math.pow(dif,2)+ 0.0078*dif + 0.686;
		  }
		  double scale = 1.37*(getComponent().getWidth()*factor/getComponent().getHeight());
	      double fov = 2*Math.atan(scale*panel3D.getDiagonal()/(2.0*camera.getDistanceToScreen()));
	      universe.getViewer().getView().setFieldOfView(fov);
	    }
	    break;
	    case Camera.CHANGE_MAPPING:
	    {
	    	panel3D.getDecoration().reset();
	    	for (Element el : panel3D.getElements()) el.getImplementingObject().processChanges(Java3dElement.AFFECTS_TRANSFORM, Java3dElement.AFFECTS_TRANSFORM);
	    }
	    break;
	    case Camera.CHANGE_ANY: 
	    {
	      double factor = 1.0;
	      double dif = 0.0;
	      int diferencia = 0;
	      if(getComponent().getWidth()>=getComponent().getHeight()){
	    	  dif = getComponent().getWidth() - getComponent().getHeight();
	    	  diferencia = (int) (dif/10);
	    	  if(diferencia<10) factor = 0.8 + 0.05*diferencia;
	    	  else factor = (-6e-5*Math.pow(diferencia,3)+0.0083*Math.pow(diferencia,2)-0.4011*diferencia + 8.59)*dif/getComponent().getWidth(); 
	    	  //factor = 2.35*(dif/getComponent().getWidth()); 
	      }
	      if(getComponent().getWidth()<getComponent().getHeight()){
	    	  dif = getComponent().getHeight()-getComponent().getWidth();
	    	  diferencia = (int) (dif/10);
	    	  if(diferencia<10) factor = 0.65 + 0.045*diferencia;
	    	  factor = 3e-08*Math.pow(dif,3) - 2e-05*Math.pow(dif,2)+ 0.0078*dif + 0.686;
	      }
	      double scale = 1.37*(getComponent().getWidth()*factor/getComponent().getHeight());
	      double fov = 2*Math.atan(scale*panel3D.getDiagonal()/(2.0*camera.getDistanceToScreen()));
	      universe.getViewer().getView().setFieldOfView(fov);
	    }
	    // do not break
	    case Camera.CHANGE_MODE :
	      switch(panel3D.getCamera().getProjectionMode()){
	        case Camera.MODE_PLANAR_XY:
	          universe.getViewer().getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
	          break;
	        case Camera.MODE_PLANAR_XZ:
	          universe.getViewer().getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
	          break;
	        case Camera.MODE_PLANAR_YZ:
	          universe.getViewer().getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
	          break;
	        case Camera.MODE_PERSPECTIVE:
	        case Camera.MODE_PERSPECTIVE_ON:
	          universe.getViewer().getView().setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
	          break;
	        case Camera.MODE_NO_PERSPECTIVE:
	        case Camera.MODE_PERSPECTIVE_OFF:
	          break;
	        default:
	      } 	
	  }
	  for (ElementText text : panel3D.getTextElements()) ((Java3dElementText)text.getImplementingObject()).updateText(camera.getDistanceToScreen());
	  
	  Transform3D currXform = new Transform3D();
	  Vector3d position = getPerspectivePosition(camera);
	  /*if(position.x==0.0 && position.y==0.0 && position.z==0.0 && 
			  camera.getFocusX()==0.0 && camera.getFocusY()==0 && camera.getFocusZ()==0) return;*/
	try{
	  if (camera.getProjectionMode()==Camera.MODE_PLANAR_XY)
	    currXform.lookAt(
	        new Point3d(position.x, position.y, position.z),
	        new Point3d(camera.getFocusX(), camera.getFocusY(), camera.getFocusZ()),
	        new Vector3d(0, 1, 0));

	  else
	    currXform.lookAt(
	        new Point3d(position.x, position.y, position.z),
            new Point3d(camera.getFocusX(), camera.getFocusY(), camera.getFocusZ()),
	        new Vector3d(0, 0, 1)); //so that our coordinate system is in the
	  	    //same direction as the simple3d one usually 0, 1, 0 is used
	    rootTG.setTransform(currXform);
	  }catch(javax.media.j3d.BadTransformException e){}
	}

	 private Vector3d getPerspectivePosition(Camera camera) {
	   if (camera.is3dMode()) return new Vector3d(camera.getX(),camera.getY(),camera.getZ());  
	    Vector3d position = new Vector3d(camera.map(panel3D.getCenter()));
	    switch (camera.getProjectionMode()) {
	      case Camera.MODE_PLANAR_XY: position.z += camera.getDistanceToFocus(); break;
	      case Camera.MODE_PLANAR_XZ: position.y -= camera.getDistanceToFocus(); break;
	      case Camera.MODE_PLANAR_YZ: position.x += camera.getDistanceToFocus(); break;
	    }
	    return position;
	 }

	 public void setMessage(String msg) {
	    brMessageBox.setText(msg); // the default message box
	  }

	  public void setMessage(String msg, int location) {
	    switch(location) {
	      case DrawingPanel3D.BOTTOM_LEFT : blMessageBox.setText(msg); break;
	      default :
	      case DrawingPanel3D.BOTTOM_RIGHT : brMessageBox.setText(msg); break;
	      case DrawingPanel3D.TOP_RIGHT : trMessageBox.setText(msg); break;
	      case DrawingPanel3D.TOP_LEFT : tlMessageBox.setText(msg); break;
	    }
	  }


  // ------------------------------------
  // Implementation of Renderable
  // ------------------------------------
	  
	public BufferedImage render() {
     this.update();
   // BufferedImage offscreenImage = null;
    //offscreenImage = render(null);
    //return offscreenImage;
    //if(updateModel) {
      /*org.opensourcephysics.tools.VideoTool vidCap = panel3D.getVideoTool();
      BufferedImage offscreenImage = null;
      if (vidCap!=null && vidCap.isRecording() ){ // buffered image should exists so use it.
        offscreenImage = render(null);
        if (offscreenImage!=null) vidCap.addFrame(offscreenImage);
      }
      //updateModel = false;
      return offscreenImage;*/
    //}
    //return null;
    return null;
	}

	/**
	 * @return Returns the BufferedImage holding the last frame drawn to the Canvas3D
	 * @param image This parameter is ignored in the Java3D implementation
	 */
	public BufferedImage render(BufferedImage image) {
    this.update();
    //this.update();
		//if (image==null){ return offScreenCanvas.doRender(null, canvas.getWidth(), canvas.getHeight()); }
		//return offScreenCanvas.doRender(image, image.getWidth(null), image.getHeight(null));
    return null;
	}


  public void hintChanged(int hintThatChanged){}
  
  
  // ----------------------------------
  // Implementation MouseMotionListener
  // ----------------------------------
  public void mouseDragged(MouseEvent evt) {/*setFocusLight();*/}
  
  public void visualizationChanged(int _change){
    switch(_change){
      case VisualizationHints.HINT_BACKGROUND_IMAGE:
        if (panel3D.getVisualizationHints().getBackgroundImage()!=null) setImage(panel3D.getVisualizationHints().getBackgroundImage(),panel3D.getVisualizationHints().getBackgroundTile(),null);
        else setImage(panel3D.getVisualizationHints().getBackgroundImageFilename(),panel3D.getVisualizationHints().getBackgroundTile());
        break;
      case VisualizationHints.HINT_BACKGROUND_MOVEABLE:
        setBackgroundMoveable(panel3D.getVisualizationHints().getBackgroundMoveable());
        break;
      case VisualizationHints.HINT_COLORS:
        if (panel3D.getVisualizationHints().getBackgroundImage()!=null) setImage(panel3D.getVisualizationHints().getBackgroundImage(), panel3D.getVisualizationHints().getBackgroundTile(),null);
        else setImage(panel3D.getVisualizationHints().getBackgroundImageFilename(), panel3D.getVisualizationHints().getBackgroundTile());
        break;
      case VisualizationHints.HINT_DEFAULT_ILLUMINATION:
        this.setLightEnabled(panel3D.getVisualizationHints().getDefaultIllumination());
        break;
      case VisualizationHints.HINT_BACKGROUND_SCALE:
        this.setScaleBackground(panel3D.getVisualizationHints().getScaleBackground());
        break;
    }
  }
  
 
  // ------------------------------
  // Background Features
  // ------------------------------
  
  private void setImage(String _name, Dimension _tile){
    if (_name==null) setImage(null,_tile,null);
    else setImage(ResourceLoader.getBufferedImage(_name),_tile, ResourceLoader.getResource(_name).getURL());
  }
  
  private void setImage(Image _image, Dimension _tile, java.net.URL _url){
    if (_image==null) { 
      this.branchGroup.removeChild(BACKGROUND);
      if(backTG.numChildren()>0) backTG.removeChild(0);
      setBackgroundMoveable(false);
      Color3f bkgdColor = new Color3f(panel3D.getVisualizationHints().getBackgroundColor());
      Background backGround = new Background(bkgdColor);
      backGround.setCapability(Background.ALLOW_COLOR_WRITE);
      backGround.setCapability(Background.ALLOW_IMAGE_WRITE);
      backGround.setCapability(Background.ALLOW_IMAGE_SCALE_MODE_WRITE);
      backGround.setApplicationBounds(new BoundingSphere());
      BranchGroup bg = new BranchGroup();
      bg.setCapability(BranchGroup.ALLOW_DETACH);
      bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      bg.addChild(backGround);
      branchGroup.addChild(bg);
      canvas.setBackground(panel3D.getVisualizationHints().getBackgroundColor());
      return;
    }
    if (! (_image instanceof BufferedImage)) {
      JOptionPane.showMessageDialog(getComponent(), "Image for background must be a BufferedImaged","DrawingPanel3D error",JOptionPane.ERROR_MESSAGE);
      return;
    }

    //Image Transformation (Scale Factor)
    int width=0,height=0;
    BufferedImage bufImg = (BufferedImage) _image; // ResourceLoader.getBufferedImage(_image);
    width = (_tile==null)? canvas.getWidth() : _tile.width;
    height = (_tile==null)? canvas.getHeight() : _tile.height;
    AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance((double)width/bufImg.getWidth(),(double)height/bufImg.getHeight()),optionsImage);
    ImageComponent2D image2D = new ImageComponent2D(ImageComponent.FORMAT_RGBA, op.filter(bufImg,null));

    //Background Image
    BranchGroup bg = (BranchGroup) this.branchGroup.getChild(BACKGROUND);
    Background bgImage = (Background)bg.getChild(0);
    if(_tile!=null){
      bgImage.setImageScaleMode(Background.SCALE_REPEAT);
      if(backTG.numChildren()>0)  backTG.removeChild(0);
      if(backTG.numChildren()<=0) backTG.addChild(this.getShapeBackground(_tile));	
    }
    else{
//      System.err.println ("Setting image to with a size of "+width+" x "+height);
      bgImage.setImageScaleMode(Background.SCALE_FIT_ALL);
      if(backTG.numChildren()>0) backTG.removeChild(0);
      if(backTG.numChildren()<=0) backTG.addChild(this.getShapeBackground(null));		
    }
    if (_url!=null) {
      TextureLoader tex = new TextureLoader(_url, new String("RGBA"),TextureLoader.BY_REFERENCE | TextureLoader.Y_UP, null);
      appBack.setTexture(tex.getTexture());
    }
    
//    System.err.println ("Do Setting image to with a size of "+width+" x "+height);
    bgImage.setImage(image2D);
    setBackgroundMoveable(panel3D.getVisualizationHints().getBackgroundMoveable());
  }
    
  private void setBackgroundMoveable(boolean _state){
	 if(appBack!=null) appBack.getRenderingAttributes().setVisible(_state);
  }
  
  private void setScaleBackground(double _factor){
	  Transform3D scaleBack = new Transform3D();
	  scaleBack.setScale(RBACK*_factor);
	  backTG.setTransform(scaleBack);
  }
  
  private BranchGroup getShapeBackground(Dimension _tile){
		
		Shape3D shape = null;
		Sphere s = null;
		float radius = 0.5f;
		
		//BranchGroup
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		
		//TransformGroup
		TransformGroup tg = new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(BranchGroup.ALLOW_DETACH);
		Transform3D t = new Transform3D();
		
		appBack = new Appearance();
		appBack.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		appBack.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		appBack.setRenderingAttributes(ra);
		appBack.getRenderingAttributes().setVisible(panel3D.getVisualizationHints().getBackgroundMoveable());
		
		if(_tile!=null){
			shape = createTileBackground(_tile.width,_tile.height);
			shape.setAppearance(appBack);
			t.setScale(new Vector3d(1.0f,1.0f,1.0f));
			tg.setTransform(t);
			tg.addChild(shape);
			bg.addChild(tg);
		}
		else{
			s = new Sphere(radius, Primitive.ENABLE_APPEARANCE_MODIFY |
									  Primitive.ENABLE_GEOMETRY_PICKING |
				  				        Primitive.GENERATE_TEXTURE_COORDS |
				  				          Sphere.GENERATE_NORMALS_INWARD,45);
			
		
			t.rotX(-Math.PI/2);
			s.setAppearance(appBack);
			tg.setTransform(t);
			tg.addChild(s);
			bg.addChild(tg);
		}
		return bg;
  }
	
  private Shape3D createTileBackground(int w, int h){
		double[][][] tileSphere = null;
		int res = 0;
		if(w>=100 || h>=100) res=10;
		else{
			float temp =-0.18f*(w+h)/2 + 30.f; 
			res = (int)temp;
		}
		tileSphere = EllipsoidUtils.createStandardEllipsoid(res, res, res, 0, 360, -90, 90, true, true, true, true);
		int totalN = tileSphere.length;
		int tileSize = tileSphere[0].length;
		Point3d coords[] = new Point3d[totalN*tileSize];
		TexCoord2f texCoords[] = new TexCoord2f[totalN*tileSize]; 
		TexCoord2f texCoordTile[] = {new TexCoord2f(0.0f, 0.0f), new TexCoord2f(1.0f, 0.0f),
				 				 new TexCoord2f(1.0f, 1.0f), new TexCoord2f(0.0f, 1.0f)};
		QuadArray quad = new QuadArray(totalN*tileSize, QuadArray.COORDINATES | 
														 QuadArray.TEXTURE_COORDINATE_2);
		
		for(int n = 0; n<totalN; n++){
	 		for(int j=0; j<tileSize; j++){
	 			texCoords[n*tileSize+j] = new TexCoord2f(texCoordTile[j]); 
	 			if(j==0) coords[n*tileSize+j+3] = new Point3d(tileSphere[n][j][0],tileSphere[n][j][1],tileSphere[n][j][2]);
	 			else if(j==1) coords[n*tileSize+j+1] = new Point3d(tileSphere[n][j][0],tileSphere[n][j][1],tileSphere[n][j][2]);
	 			else if(j==2) coords[n*tileSize+j-1] = new Point3d(tileSphere[n][j][0],tileSphere[n][j][1],tileSphere[n][j][2]);
	 			else coords[n*tileSize+j-3] = new Point3d(tileSphere[n][j][0],tileSphere[n][j][1],tileSphere[n][j][2]);
	 		}
		}
		quad.setCoordinates(0, coords);
		quad.setTextureCoordinates(0, 0, texCoords);
		Shape3D shape = new Shape3D(quad,this.appBack);
		return shape;
  }
  // ------------------------------
  // End Background Features
  // ------------------------------
  
  
  
  //-------------------------------
  // Lightning Features
  // ------------------------------
  public void setLightEnabled(boolean _state){
	  Light light;
	  for(int i=0;i<2;i++){
		  light = (Light)this.branchGroup.getChild(i);
		  light.setEnable(_state);
	  }
  }
  //-------------------------------
  // End Lightning Features
  // ------------------------------
  public void SetUpdateModel(boolean _state){
    updateModel = _state;
  }
  
  public boolean GetUpdateModel(){
    return updateModel;
  }
	
	
  // ----------------------------------------------------
  // Private Classes (Java3DPanel, RenderCanvas) and Methods
  // ----------------------------------------------------
  @SuppressWarnings("serial")
  private class Java3DTextPanel extends org.opensourcephysics.display.TextPanel {
    private BufferedImage image;
    private int location;

    public Java3DTextPanel(int _location) {
      super();
      this.location = _location;
      int width = 200, height = 50; //set arbitrary default size to start with
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(J3DGraphics2D g2d) {
      switch (location) {
        case DrawingPanel3D.TOP_LEFT : g2d.drawAndFlushImage(image, 0, 0, null); break;
        case DrawingPanel3D.TOP_RIGHT : g2d.drawAndFlushImage(image, canvas.getWidth() - getWidth(), 0, null); break;
        case DrawingPanel3D.BOTTOM_RIGHT : g2d.drawAndFlushImage(image, canvas.getWidth() - getWidth(), canvas.getHeight() - getHeight(), null); break;
        default :
        case DrawingPanel3D.BOTTOM_LEFT : g2d.drawAndFlushImage(image, 0, canvas.getHeight() - getHeight(), null); break;
      }
    }

    public void setText(String _text) {
      super.setText(_text);
      int width = getWidth(), height = getHeight();
      if (width==0 || height==0) return;
      if (image.getWidth()!=width || image.getHeight()!=height) {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }
      this.paint(image.getGraphics());
    }
  }

  @SuppressWarnings("serial")
  private class RenderCanvas extends Canvas3D /*implements Renderable*/ { 

    RenderCanvas(GraphicsConfiguration config){
      super(config);
    }
    
    //Rendering 2D Graphics of the canvas
    public void postRender() {
      J3DGraphics2D g2d = canvas.getGraphics2D();
      tlMessageBox.render(g2d);
      trMessageBox.render(g2d);
      brMessageBox.render(g2d);
      blMessageBox.render(g2d);
      //Java3dDrawingPanel3D.this.SetUpdateModel(false);
    }
    
    public void preRender(){
        try {
          Thread.sleep(75);
        } catch (Exception e) {e.printStackTrace();}
    }

    //Implementation of Renderable Interface
   /* public BufferedImage render(){
      return  Java3dDrawingPanel3D.this.render();
    }
    
    public BufferedImage render(BufferedImage image){
      return Java3dDrawingPanel3D.this.render(image);
    }*/
  }



}

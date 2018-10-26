/*
 * Created on April 6, 2010
 * New element for the Java 3D implementation
 */

package org.opensourcephysics.drawing3d.java3d;


import java.awt.Color;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.utils.Style;

/**
* <p>Title: Java3dElementLight</p>
* <p>Description: A 3D light in Java3D</p>
* <p>Copyright: Open Source Physics project</p>
* @author Carlos Jara Bravo
* @author Francisco Esquembre
* @version April 2010
*/
public class Java3dElementLight extends Java3dElementArrow {
  
  protected ElementLight lightElement;
  private Light light3D;
  private BoundingSphere bounds;
  private BranchGroup bg;
        
  public Java3dElementLight(ElementLight _element) {
    super (_element);
    lightElement = _element;
    bounds = new BoundingSphere();
    bounds.setRadius(Double.MAX_VALUE);
    createLight();
  }

  
  private void createLight(){
	 switch(lightElement.getType()){
    	case ElementLight.TYPE_AMBIENT:
    		light3D = new AmbientLight();//Ambient light
    		light3D.setCapability(javax.media.j3d.AmbientLight.ALLOW_COLOR_READ);
    		light3D.setCapability(javax.media.j3d.AmbientLight.ALLOW_COLOR_WRITE);
    		light3D.setCapability(javax.media.j3d.AmbientLight.ALLOW_STATE_READ);
    		light3D.setCapability(javax.media.j3d.AmbientLight.ALLOW_STATE_WRITE);
    		light3D.setInfluencingBounds(bounds);
    		light3D.setColor(new Color3f(((Color)lightElement.getStyle().getFillColor()).getRed(),
    				                     ((Color)lightElement.getStyle().getFillColor()).getGreen(),
    				                     ((Color)lightElement.getStyle().getFillColor()).getBlue()));
    		light3D.setEnable(lightElement.isOn());
    		break;
    	case ElementLight.TYPE_DIRECTIONAL:
    		light3D = new DirectionalLight();//Directional light
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_COLOR_READ);
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_COLOR_WRITE);
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_DIRECTION_READ);
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_DIRECTION_WRITE);
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_STATE_READ);
    		light3D.setCapability(javax.media.j3d.DirectionalLight.ALLOW_STATE_WRITE);
    		light3D.setInfluencingBounds(bounds);
    		light3D.setColor(new Color3f(((Color)lightElement.getStyle().getFillColor()).getRed(),
    									 ((Color)lightElement.getStyle().getFillColor()).getGreen(),
    									 ((Color)lightElement.getStyle().getFillColor()).getBlue()));
    		((DirectionalLight) light3D).setDirection(new Vector3f((float)lightElement.getSizeX(), 
    																  (float)lightElement.getSizeY(),
    																     (float)lightElement.getSizeZ()));
    		light3D.setEnable(lightElement.isOn());
    		break;
    	case ElementLight.TYPE_POINT:
    		light3D = new PointLight();//Point light
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_COLOR_READ);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_COLOR_WRITE);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_POSITION_READ);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_POSITION_WRITE);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_ATTENUATION_READ);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_ATTENUATION_WRITE);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_STATE_READ);
    		light3D.setCapability(javax.media.j3d.PointLight.ALLOW_STATE_WRITE);
    		light3D.setInfluencingBounds(bounds);
    		light3D.setColor(new Color3f(((Color)lightElement.getStyle().getFillColor()).getRed(),
                                         ((Color)lightElement.getStyle().getFillColor()).getGreen(),
                                         ((Color)lightElement.getStyle().getFillColor()).getBlue()));
    		((PointLight) light3D).setPosition((float)lightElement.getX(),
    											(float)lightElement.getY(),
    											  (float)lightElement.getZ());
    		((PointLight) light3D).setAttenuation((float)lightElement.getAttenuationLight()[0], 
    												(float)lightElement.getAttenuationLight()[1], 
    												 (float)lightElement.getAttenuationLight()[2]);
    		light3D.setEnable(lightElement.isOn());
    		break;
    	case ElementLight.TYPE_SPOT:
    		light3D = new SpotLight();///Spot light
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_COLOR_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_COLOR_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_POSITION_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_POSITION_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_DIRECTION_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_DIRECTION_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_ATTENUATION_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_ATTENUATION_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_CONCENTRATION_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_CONCENTRATION_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_SPREAD_ANGLE_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_SPREAD_ANGLE_WRITE);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_STATE_READ);
    		light3D.setCapability(javax.media.j3d.SpotLight.ALLOW_STATE_WRITE);
    		light3D.setInfluencingBounds(bounds);
    		light3D.setColor(new Color3f(((Color)lightElement.getStyle().getFillColor()).getRed(),
    									 ((Color)lightElement.getStyle().getFillColor()).getGreen(),
    									 ((Color)lightElement.getStyle().getFillColor()).getBlue()));
    		((SpotLight) light3D).setPosition((float)lightElement.getX(),
					 							(float)lightElement.getY(),
					 							 (float)lightElement.getZ());
    		((SpotLight) light3D).setDirection(new Vector3f((float)lightElement.getSizeX(), 
															 (float)lightElement.getSizeY(),
															   (float)lightElement.getSizeZ()));
    		((SpotLight) light3D).setAttenuation((float)lightElement.getAttenuationLight()[0], 
												  (float)lightElement.getAttenuationLight()[1], 
										            (float)lightElement.getAttenuationLight()[2]);
    		((SpotLight) light3D).setConcentration((float)lightElement.getConcentrationLight());
    		((SpotLight) light3D).setSpreadAngle((float)(lightElement.getAngleLight()*Math.PI/180));	
    		light3D.setEnable(lightElement.isOn());
    		break;
     }
     bg = new BranchGroup();
     bg.setCapability(BranchGroup.ALLOW_DETACH);
     bg.addChild(light3D);
     getBranchGroup().addChild(bg);

     /*BranchGroup bg2 = new BranchGroup();
     bg2.setCapability(BranchGroup.ALLOW_DETACH);
     bg2.addChild(light3D);
     addNode(bg2);*/
  }
  // ----------------------------------------
  // Implementation of the ImplementingObject
  // ----------------------------------------

  public void processChanges(int _change, int _cummulativeChange) {
	  super.processChanges(_change, _cummulativeChange);
	  
	  //LIGHT EVENTS
	  if(((_change & ElementLight.CHANGE_LIGHT_ON)!=0)){
		 light3D.setEnable(lightElement.isOn());
	  }
	  if(((_change & ElementLight.CHANGE_LIGHT_TYPE)!=0)) {
		  getBranchGroup().removeChild(2);
		  createLight();
	  }
	  if (((_change & ElementLight.CHANGE_LIGHT_AMBIENT_FACTOR) != 0)){
		  if(light3D instanceof AmbientLight){
			  for(Element el: lightElement.getPanel().getElements())
				  ((Java3dElement)el.getImplementingObject()).element.getStyle().setAmbientFactor(lightElement.getAmbientFactor());		  
		  }
	  }
	  if (((_change & ElementLight.CHANGE_LIGHT_ATTENUATION) != 0)){
		  if(light3D instanceof PointLight)
			  ((PointLight) light3D).setAttenuation((float)lightElement.getAttenuationLight()[0], 
					  								                   (float)lightElement.getAttenuationLight()[1], 
					  								                     (float)lightElement.getAttenuationLight()[2]);
		  if (light3D instanceof SpotLight)
			  ((SpotLight) light3D).setAttenuation((float)lightElement.getAttenuationLight()[0], 
						 							                    (float)lightElement.getAttenuationLight()[1], 
						 							                      (float)lightElement.getAttenuationLight()[2]);
	  }
	  if(((_change & ElementLight.CHANGE_LIGHT_CONCENTRATION)!=0)){
		  if(light3D instanceof SpotLight) 
			  ((SpotLight) light3D).setConcentration((float)lightElement.getConcentrationLight());
	  }
	  if(((_change & ElementLight.CHANGE_LIGHT_ANGLE)!=0)){
		  if(light3D instanceof SpotLight) 
			  ((SpotLight) light3D).setSpreadAngle((float)(lightElement.getAngleLight()*Math.PI/180));	
	  }
	  
	  //ELEMENT EVENTS
	  if(((_change & Element.CHANGE_POSITION)!=0)){
		  if(light3D instanceof PointLight){
			  element.setXYZ((float)lightElement.getX(),
								(float)lightElement.getY(),
						          (float)lightElement.getZ());
			  ((PointLight) light3D).setPosition((float)lightElement.getX(),
					  							   (float)lightElement.getY(), 
					  							     (float)lightElement.getZ());
		  }
		  if (light3D instanceof SpotLight){
			  element.setXYZ((float)lightElement.getX(),
								(float)lightElement.getY(),
								 (float)lightElement.getZ());
		    ((SpotLight) light3D).setPosition((float)lightElement.getX(),
		    								   (float)lightElement.getY(), 
		    								   	(float)lightElement.getZ());
		  }
	  }
	  if(((_change & Element.CHANGE_SIZE)!=0)){
		  if(light3D instanceof DirectionalLight){
			  ((DirectionalLight) light3D).setDirection(new Vector3f((float)lightElement.getSizeX(), 
																	    (float)lightElement.getSizeY(),
																	       (float)lightElement.getSizeZ()));
		  }
		  
		  if(light3D instanceof SpotLight){
			  ((SpotLight) light3D).setDirection(new Vector3f((float)lightElement.getSizeX(), 
						 									    (float)lightElement.getSizeY(),
						 									       (float)lightElement.getSizeZ()));
		  }
	  }
  }

  public void styleChanged(int _change) {
	  super.styleChanged(_change);
	  switch(_change) {
      	case Style.CHANGED_FILL_COLOR: 
      		if(lightElement.getStyle().getFillColor()!=null)
      		   light3D.setColor(new Color3f(((Color)lightElement.getStyle().getFillColor()).getRed(),
      				   						((Color)lightElement.getStyle().getFillColor()).getGreen(),
      				   						((Color)lightElement.getStyle().getFillColor()).getBlue()));
      		else light3D.setColor(new Color3f(Color.white));
      		element.getStyle().setFillColor(lightElement.getStyle().getFillColor());
      	break;
    }
  }
}

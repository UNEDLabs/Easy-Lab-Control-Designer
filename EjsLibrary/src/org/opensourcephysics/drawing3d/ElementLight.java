/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;


import org.opensourcephysics.drawing3d.utils.ImplementingObject;

/**
 * <p>Title: ElementLight</p>
 * <p>Description: A 3D Light in the scene</p>
 * A light is basically an arrow that adds illumniation to a scene.
 * The light direction is that of the arrow, which can be made visible or hidden.
 * The light is turned on and off using independently of the arrow's visibility.
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version April 2010
 */
public class ElementLight extends ElementArrow {
   static final public int TYPE_AMBIENT = 0;
   static final public int TYPE_DIRECTIONAL = 1;
   static final public int TYPE_POINT = 2;
   static final public int TYPE_SPOT = 3;

	//Possible changes applied to the element light
	static final public int CHANGE_LIGHT_NONE = 0;
	static final public int CHANGE_LIGHT_ON = 1;
	static final public int CHANGE_LIGHT_TYPE = 2;
	static final public int CHANGE_LIGHT_AMBIENT_FACTOR = 4;  
	static final public int CHANGE_LIGHT_ATTENUATION = 8;
	static final public int CHANGE_LIGHT_CONCENTRATION = 16;
	static final public int CHANGE_LIGHT_ANGLE = 32;
	
	//Configuration variables
	private int type = 0;
	private boolean lightIsOn = true;
	private double attenuationX = 0.0, attenuationY = 0.0, attenuationZ = 0.0;
	private double ambientFactor = 0.5, concentration = 60.0, angle = 45.0;
	
  {
    setXYZ (1,1,1);
    setSizeXYZ (-0.5,-0.5,-0.5);
    getStyle().setDrawingLines(true);
    setVisible(false); // hides the arrow that displays the light direction
  }
  
	@Override
	protected ImplementingObject createImplementingObject(int _implementation) {
		switch(_implementation) {
			default :
			case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
				return new org.opensourcephysics.drawing3d.simple3d.SimpleElementLight(this);
			case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
				return new org.opensourcephysics.drawing3d.java3d.Java3dElementLight(this);
		}
 	}

	// -------------------------------------
	// Configuration
	// -------------------------------------
	
	/**
	 * Turns the light on and off.
	 * Different to setVisible(), which shows/hides the arrow that visualizes teh light direction.
	 * @param _on whether the light is on (true) or off (false)
	 */
	public void setOn (boolean _on) { 
		if (this.lightIsOn==_on) return;
		this.lightIsOn = _on;
		addChange(ElementLight.CHANGE_LIGHT_ON);
	}
	
	/**
	 * Whether the light is on (true) or off (false)
	 * @return boolean
	 */
	public boolean isOn() { return this.lightIsOn; }
	
	/**
	 * Sets the type of light.
	 * Options are:
	 * <ul>
	 *   <li>TYPE_AMBIENT</li>
   *   <li>TYPE_DIRECTIONAL</li>
   *   <li>TYPE_POINT</li>
   *   <li>TYPE_SPOT</li>
   * </ul>
	 * @param _type One of the constants above.
	 */
	public void setType(int _type){
		if (this.type==_type) return;
		this.type = _type;
		addChange(ElementLight.CHANGE_LIGHT_TYPE);
	}
	
	/**
	 * Returns the type of light.
	 * @return int
	 * @see #setType(int)
	 */
	public int getType(){return this.type;}
	
	/**
	 * Sets the attenuation of the light. 
	 * Equivalent to setAttenuationLightXYZ(_att[0],_att[1],_att[2]);
	 * @param _att a double[] array
	 */
	public void setAttenuationLight(double[] _att) { 
	  setAttenuationLightXYZ(_att[0],_att[1],_att[2]); 
	}
	
	/**
	 * Sets the attenuation of the light
	 * @param _attX
	 * @param _attY
	 * @param _attZ
	 */
	public void setAttenuationLightXYZ(double _attX, double _attY, double _attZ) {
		 this.attenuationX = _attX;
		 this.attenuationY = _attY;
		 this.attenuationZ = _attZ;
		 addChange(ElementLight.CHANGE_LIGHT_ATTENUATION);
	}
  
	/**
	 * Returns the attenuation light factors
	 * @return double[]
	 */
	final public double[] getAttenuationLight() { return new double[] { this.attenuationX, this.attenuationY, this.attenuationZ}; }
  
	/**
	 * Sets the ambient factor of the light
	 * @param factor
	 */
	public void setAmbientFactor(double factor){
		if (this.ambientFactor==factor) return;
		this.ambientFactor = factor;
		addChange(ElementLight.CHANGE_LIGHT_AMBIENT_FACTOR);
	}

	/**
	 * Returns the ambient factor of the light
	 * @return double the ambient light factor
	 */
	public double getAmbientFactor() { return this.ambientFactor;}
	
	/**
	 * Sets the concentration of the light
	 * @param conc
	 */
	public void setConcentrationLight(double conc){
		if (this.concentration==conc) return;
		this.concentration = conc;
		addChange(ElementLight.CHANGE_LIGHT_CONCENTRATION);
	}

	/**
	 * Returns the concentration of the light
	 * @return double
	 */
	public double getConcentrationLight() { return this.concentration;}
	
	/**
	 * Sets the angle of the light
	 * @param angle
	 */
	public void setAngleLight(double angle){
		if (this.angle==angle) return;
		this.angle = angle;
		addChange(ElementLight.CHANGE_LIGHT_ANGLE);
	}

	/**
	 * Return the angle of the light
	 * @return double the angle of the ligth 
	 */
	public double getAngleLight() { return this.angle;}
	
}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

package org.colos.ejs.model_elements.roboticsLabs.components.Belt;

import org.colos.ejs.library.Model;

import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.roboticsLabs.components.Belt;

/**
 * @author Almudena Ruiz
 * @version Febrero 2015
 */
public class BeltWithObjectAdapter extends Belt {
	
	/**
	 * Standard constructor to be called by the simulation
	 * @param model the model of the simulation
	 * @param length the length of the element
	 * @param width the width of the element
	 * @param high the high of the element
	 */
		
	public BeltWithObjectAdapter(Model model, String length, String width, String high, boolean encoder){
		super(ModelElementsUtilities.getDoubleValue(model, length.trim()),
				ModelElementsUtilities.getDoubleValue(model, width.trim()), 
				ModelElementsUtilities.getDoubleValue(model, high.trim()), encoder);
	}
}

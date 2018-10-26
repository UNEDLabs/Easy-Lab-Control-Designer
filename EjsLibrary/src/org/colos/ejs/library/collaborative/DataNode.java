/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.awt.Color;
import java.awt.Font;


/**
 * Data of a tree node
 */

public class DataNode{
	
	    protected Font font;
	    protected Color color;
	    protected String string;

	    /**
	    * Constructor of a data node
	 	*/
	    public DataNode(Font newFont, Color newColor, String newString) {
	    	font = newFont;
	    	color = newColor;
	    	string = newString;
	    }
	    
	    
	    //Setters and Getters
	    public void setFont(Font newFont) {font = newFont;}

	    public Font getFont() {return font;}

	    public void setColor(Color newColor) {color = newColor;}

	    public Color getColor() {return color;}

	    public void setString(String newString) {string = newString;}

	    public String string() {return string;}
		
	    public String toString() {return string;}
}


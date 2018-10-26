/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.awt.Color;
import java.awt.Font;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * DefaultMutableTreeNode to manage the node of the tree
 */

public class MyTreeNode extends DefaultMutableTreeNode {
	
	//Serial version ID
	private static final long serialVersionUID = 1L;
	
	String[] _args;
	boolean load;
	static Font font = new Font("Serif", Font.PLAIN,10); 
	
	
	public MyTreeNode(Object _object,String[] args) 
	{
		super(_object);
		_args = args;
		if(_args!=null)
			loadChildren();
		
	}
	
	
	protected void loadChildren() {
		MyTreeNode newNode;
		DataNode data;
		int counter;
		
		for(counter = 0; counter < _args.length;counter++)
		{
			data = new DataNode(font, Color.blue, _args[counter]);
		    newNode = new MyTreeNode(data,null);
		    insert(newNode, counter);
		}
		load = true;
	}

}
